package com.bewitchment.machine;

import com.bewitchment.block.WitchesCauldronBlock;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

/** Crafting and implemented potion brews; water brewing and lava storage; teleportation remains a separate port. */
public final class CauldronTile extends TileEntity implements ITickableTileEntity {
    private static final net.minecraft.tags.ITag.INamedTag<Block> HEAT_SOURCES = BlockTags.createOptional(new ResourceLocation("bewitchment", "cauldron_heat_sources"));
    private final List<ItemStack> ingredients = new ArrayList<>();
    private int heat, elapsed;
    private boolean failed, brewing;
    private final BrewBatch batch=new BrewBatch();
    private BlockPos altarPos;
    private long nextSearch;
    public CauldronTile() { super(ModMachines.CAULDRON.get()); }
    public static void init() { BrewIngredients.init(); } // Register the heat-source tag before the first data-pack reload.
    public int getHeat() { return heat; }
    public int getIngredientCount() { return ingredients.size(); }
    public boolean isBrewing(){return brewing;}
    public int bottlesRemaining(){return batch.remaining();}
    public boolean isFailed() { return failed; }
    private boolean filled() { return getBlockState().getValue(WitchesCauldronBlock.FILLED); }
    public boolean isLava() { return filled() && getBlockState().getValue(WitchesCauldronBlock.LAVA); }
    public boolean fill() { return fill(false); }
    public boolean fill(boolean lava) {
        if (filled() || !ingredients.isEmpty()) return false;
        failed = false; brewing=false; batch.clear(); heat = 0; elapsed = 0;
        level.setBlock(worldPosition, getBlockState().setValue(WitchesCauldronBlock.FILLED, true).setValue(WitchesCauldronBlock.LAVA, lava), 3); setChanged(); return true;
    }
    public boolean drainClean() {
        if (!filled() || !ingredients.isEmpty() || failed) return false;
        empty(); return true;
    }
    private void empty() {
        ingredients.clear(); batch.clear(); brewing=false; heat = 0; elapsed = 0; failed = false;
        level.setBlock(worldPosition, getBlockState().setValue(WitchesCauldronBlock.FILLED, false).setValue(WitchesCauldronBlock.LAVA, false), 3); setChanged();
    }
    private boolean heated() {
        BlockState below = level.getBlockState(worldPosition.below());
        return below.is(HEAT_SOURCES)
                && (!below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT));
    }
    private boolean drawPower(int amount) {
        TileEntity source = altarPos != null && level.hasChunkAt(altarPos) && AltarTile.inRange(altarPos, worldPosition) ? level.getBlockEntity(altarPos) : null;
        if (!(source instanceof AltarTile) || !((AltarTile) source).intact()) {
            altarPos = null;
            if (level.getGameTime() < nextSearch) return false;
            nextSearch = level.getGameTime() + 100;
            source = AltarTile.nearest((ServerWorld) level, worldPosition);
            if (source != null) altarPos = source.getBlockPos().immutable();
        }
        return source instanceof AltarTile && ((AltarTile) source).tryDrainMagic(amount);
    }
    @Override public void tick() {
        if (level == null || level.isClientSide || !filled()) return;
        // Legacy contact uses the whole block, before this tick's heat update.
        for(net.minecraft.entity.LivingEntity living:level.getEntitiesOfClass(net.minecraft.entity.LivingEntity.class,new AxisAlignedBB(worldPosition)))
            CauldronContact.apply(isLava(),heat,living);
        if (isLava()) {
            if(level.random.nextInt(100)==0){
                level.playSound(null,worldPosition,SoundEvents.LAVA_POP,SoundCategory.BLOCKS,.2F+level.random.nextFloat()*.2F,.9F+level.random.nextFloat()*.15F);
                ((ServerWorld)level).sendParticles(ParticleTypes.LAVA,worldPosition.getX()+.5,worldPosition.getY()+.6,worldPosition.getZ()+.5,1,0,0,0,0);
            }
            if(level.random.nextInt(200)==0)level.playSound(null,worldPosition,SoundEvents.LAVA_AMBIENT,SoundCategory.BLOCKS,.2F+level.random.nextFloat()*.2F,.9F+level.random.nextFloat()*.15F);
            return;
        }
        if (level.getGameTime() % 20 == 0) {
            heat = heated() ? Math.min(5, heat + 1) : Math.max(0, heat - 1);
            if (!ingredients.isEmpty() && !failed && ((!brewing && ++elapsed >= 20) || !drawPower(16 * ingredients.size()))) failed = true;
            if (heat >= 5) ((ServerWorld) level).sendParticles(failed ? ParticleTypes.SMOKE : ParticleTypes.BUBBLE_POP,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.62, worldPosition.getZ() + 0.5, 3, 0.2, 0.02, 0.2, 0);
            setChanged();
        }
        if (heat < 5 || level.getGameTime() % 5 != 0) return;
        AxisAlignedBB zone = new AxisAlignedBB(worldPosition.getX() + 0.13, worldPosition.getY(), worldPosition.getZ() + 0.13,
                worldPosition.getX() + 0.87, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.87);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, zone)) {
            if (!entity.isAlive() || entity.getItem().isEmpty()) continue;
            ItemStack stack = entity.getItem();
            if (stack.getItem() == ModItems.WOOD_ASH.get()) { consume(entity); empty(); return; }
            if (failed || batch.started() || ingredients.size() >= 10) continue;
            if (brewing && !BrewIngredients.supported(stack)) continue;
            if (ingredients.isEmpty() && BrewIngredients.id(stack).equals("bewitchment:mandrake_root")) brewing=true;
            boolean powered = drawPower(16 * ingredients.size());
            ItemStack input = stack.copy(); input.setCount(1);
            ingredients.add(input); consume(entity); setChanged();
            if (!powered) { failed = true; return; }
            if(brewing){
                ItemStack returned=BrewIngredients.returned(input);
                if(!returned.isEmpty())Block.popResource(level,worldPosition.above(),returned);
                return;
            }
            Inventory inventory = new Inventory(ingredients.toArray(new ItemStack[0]));
            CauldronRecipe recipe = level.getRecipeManager().getRecipeFor(CauldronRecipe.TYPE, inventory, level).orElse(null);
            if (recipe != null) {
                List<ItemStack> outputs = recipe.getOutputs();
                empty(); // Clear state before publishing outputs; none can be reabsorbed into an empty vessel.
                for (ItemStack output : outputs) {
                    ItemEntity result = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, output);
                    result.setDeltaMovement(0, 0.15, 0); result.setDefaultPickUpDelay(); level.addFreshEntity(result);
                }
                level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundCategory.BLOCKS, 1, 1);
            }
            return;
        }
    }
    private boolean bottling;
    public ItemStack bottle(net.minecraft.entity.player.ServerPlayerEntity player){
        if(bottling || level==null || level.isClientSide || !filled() || isLava() || !brewing || failed || heat<5)return ItemStack.EMPTY;
        bottling=true;
        try{
            if(!batch.started()){
                // A data-pack reload may remove a tag-based ingredient while the batch is waiting.
                if(ingredients.stream().anyMatch(item->!BrewIngredients.supported(item))){failed=true;setChanged();return ItemStack.EMPTY;}
                CompoundNBT before=save(new CompoundNBT());BlockState state=getBlockState();
                java.util.List<ItemStack> snapshot=new java.util.ArrayList<>();for(ItemStack item:ingredients)snapshot.add(item.copy());
                BrewBonuses bonus=BrewBonuses.forPlayer(player);
                BrewHooks.Prepared prepared=BrewHooks.prepare(this,player,snapshot,bonus.alchemist,bonus.leonards);
                // Listeners may replace the tile or change its contents. Preserve their changes, never overwrite them.
                if(prepared==null || isRemoved() || level.getBlockEntity(worldPosition)!=this || !state.equals(getBlockState())
                        || !before.equals(save(new CompoundNBT())))return ItemStack.EMPTY;
                batch.start(prepared.potion,prepared.bottles);
            }
            ItemStack result=batch.take();if(batch.remaining()==0)empty();else setChanged();return result;
        }finally{bottling=false;}
    }
    private static void consume(ItemEntity entity) {
        ItemStack rest = entity.getItem().copy(); rest.shrink(1);
        if (rest.isEmpty()) entity.remove(); else entity.setItem(rest);
    }
    public void dropContents() {
        if(!brewing)for (ItemStack stack : ingredients) Block.popResource(level, worldPosition, stack);
        ingredients.clear(); setChanged();
    }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); ListNBT list = new ListNBT();
        for (ItemStack stack : ingredients) list.add(stack.save(new CompoundNBT()));
        tag.put("Ingredients", list); tag.putInt("Heat", heat); tag.putInt("Elapsed", elapsed); tag.putBoolean("Failed", failed); tag.putBoolean("Brewing",brewing);tag.put("BrewBatch",batch.save());return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); ingredients.clear();
        ListNBT list = tag.getList("Ingredients", 10);
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i)); if (!stack.isEmpty()) { stack.setCount(1); ingredients.add(stack); }
        }
        brewing=tag.getBoolean("Brewing");boolean valid=batch.load(tag.getCompound("BrewBatch"));
        if(!brewing)batch.clear();
        heat = Math.max(0, Math.min(5, tag.getInt("Heat"))); elapsed = Math.max(0, Math.min(20, tag.getInt("Elapsed"))); failed = tag.getBoolean("Failed") || !valid || (brewing && (ingredients.isEmpty() || !BrewIngredients.id(ingredients.get(0)).equals("bewitchment:mandrake_root") || ingredients.stream().anyMatch(item->!BrewIngredients.supported(item))));
    }
}
