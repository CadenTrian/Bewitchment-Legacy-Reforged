package com.bewitchment.machine;

import com.bewitchment.block.BrazierBlock;
import com.bewitchment.combat.CurseCasting;
import com.bewitchment.item.TaglockItem;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import java.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import javax.annotation.Nullable;

/** Manual single-item slots. Inputs are reserved across external casting callbacks. */
public final class BrazierTile extends TileEntity implements ITickableTileEntity {
    final Inventory contents=new Inventory(9);
    final IncenseData incense=new IncenseData();
    private boolean invalidIncense;
    private boolean casting;
    private int capabilityGeneration;
    private LazyOptional<IItemHandler> automation=createAutomation();
    private LazyOptional<IItemHandler> createAutomation(){
        final int generation=capabilityGeneration;
        return LazyOptional.of(()->new BrazierItemHandler(contents,()->generation==capabilityGeneration && !remove && level!=null && !level.isClientSide && !casting && !invalidIncense && !getBlockState().getValue(BrazierBlock.LIT)));
    }
    public BrazierTile(){this(ModMachines.BRAZIER.get());}
    BrazierTile(TileEntityType<?> type){super(type);contents.addListener(inv->setChanged());}
    public static UUID target(Inventory inventory){
        UUID target=null;int locks=0;
        for(int i=0;i<inventory.getContainerSize();i++){ItemStack stack=inventory.getItem(i);if(stack.getItem() instanceof TaglockItem){locks++;target=stack.hasTag()?ChestSigilData.parse(stack.getTag().getString("boundId")):null;}}
        return locks==1?target:null;
    }
    private void message(PlayerEntity player,String key){player.displayClientMessage(new TranslationTextComponent("brazier.bewitchment."+key),true);}
    private static void give(PlayerEntity player,ItemStack stack){if(!stack.isEmpty() && !player.inventory.add(stack))player.drop(stack,false);}
    public void dropContents(){if(level!=null && !level.isClientSide){InventoryHelper.dropContents(level,worldPosition,contents);contents.clearContent();}}
    public void use(ServerPlayerEntity player,Hand hand){
        if(level==null || level.isClientSide || casting)return;
        BlockState state=getBlockState();ItemStack held=player.getItemInHand(hand);
        if(state.getValue(BrazierBlock.LIT)){
            if(held.getItem() instanceof ShovelItem){stopBurning();}
            else message(player,"extinguish");return;
        }
        if(player.isShiftKeyDown() && held.isEmpty()){
            for(int i=8;i>=0;i--)if(!contents.getItem(i).isEmpty()){give(player,contents.removeItemNoUpdate(i));setChanged();return;}
            return;
        }
        if(held.getItem() instanceof FlintAndSteelItem){ignite(player);return;}
        if(held.isEmpty()){int count=0;for(int i=0;i<9;i++)if(!contents.getItem(i).isEmpty())count++;player.displayClientMessage(new TranslationTextComponent("brazier.bewitchment.contents",count),true);return;}
        for(int i=0;i<9;i++)if(contents.getItem(i).isEmpty()){contents.setItem(i,held.split(1));message(player,"inserted");return;}
        message(player,"full");
    }
    private void ignite(ServerPlayerEntity player){
        IncenseRecipe aroma=level.getRecipeManager().getAllRecipesFor(IncenseRecipe.TYPE).stream().filter(r->r.matches(contents,level)).sorted(Comparator.comparing(r->r.getId().toString())).findFirst().orElse(null);
        if(aroma!=null){
            NonNullList<ItemStack> offerings=NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)offerings.set(i,contents.removeItemNoUpdate(i));
            incense.start(aroma.incense);invalidIncense=false;
            level.setBlock(worldPosition,getBlockState().setValue(BrazierBlock.LIT,true),3);setChanged();
            for(ItemStack stack:offerings)if(stack.hasContainerItem())give(player,stack.getContainerItem());
            level.playSound(null,worldPosition,SoundEvents.BLAZE_SHOOT,SoundCategory.BLOCKS,1,1);message(player,"incense");return;
        }
        BrazierRecipe recipe=level.getRecipeManager().getAllRecipesFor(BrazierRecipe.TYPE).stream().filter(r->r.matches(contents,level)).sorted(Comparator.comparing(r->r.getId().toString())).findFirst().orElse(null);
        if(recipe==null){message(player,"recipe");return;}
        UUID id=target(contents);ServerPlayerEntity victim=id==null?null:player.getServer().getPlayerList().getPlayer(id);
        if(victim==null){message(player,"target");return;}
        NonNullList<ItemStack> reserved=NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)reserved.set(i,contents.removeItemNoUpdate(i));
        casting=true;boolean committed=false;
        try{
            CurseCasting.Outcome outcome=CurseCasting.cast(player,victim,recipe.curse,7);
            if(outcome==null){message(player,"canceled");return;}
            committed=true;
            for(ItemStack stack:reserved){if(stack.getItem() instanceof TaglockItem)give(player,new ItemStack(ModItems.TAGLOCK.get()));else if(stack.hasContainerItem())give(player,stack.getContainerItem());}
            if(level.getBlockEntity(worldPosition)==this)level.setBlock(worldPosition,getBlockState().setValue(BrazierBlock.LIT,true),3);
            level.playSound(null,worldPosition,SoundEvents.BLAZE_SHOOT,SoundCategory.BLOCKS,1,1);
            message(player,outcome.reflected?"reflected":outcome.added?"cast":"duplicate");
        }finally{
            if(!committed)for(int i=0;i<9;i++)if(!reserved.get(i).isEmpty()){
                if(level.getBlockEntity(worldPosition)==this)contents.setItem(i,reserved.get(i));else Block.popResource(level,worldPosition,reserved.get(i));
            }
            casting=false;setChanged();
        }
    }
    public IncenseData.Kind incenseKind(){return incense.kind();}
    private void stopBurning(){incense.clear();invalidIncense=false;level.setBlock(worldPosition,getBlockState().setValue(BrazierBlock.LIT,false),3);setChanged();}
    @Override public void tick(){
        if(level==null || level.isClientSide)return;
        if(invalidIncense){stopBurning();return;}
        if(incense.kind()==null)return;
        if(!getBlockState().getValue(BrazierBlock.LIT)){incense.clear();setChanged();return;}
        if(level.getGameTime()%20==0){if(!incense.second())stopBurning();else setChanged();}
    }
    @Override public CompoundNBT save(CompoundNBT tag){super.save(tag);NonNullList<ItemStack> stacks=NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)stacks.set(i,contents.getItem(i));ItemStackHelper.saveAllItems(tag,stacks);if(incense.kind()!=null)tag.put("Incense",incense.save());else tag.remove("Incense");return tag;}
    @Override public void load(BlockState state,CompoundNBT tag){super.load(state,tag);NonNullList<ItemStack> stacks=NonNullList.withSize(9,ItemStack.EMPTY);ItemStackHelper.loadAllItems(tag,stacks);for(int i=0;i<9;i++)contents.setItem(i,stacks.get(i));incense.load(tag.getCompound("Incense"));invalidIncense=tag.contains("Incense") && incense.kind()==null;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,@Nullable Direction side){
        if(!remove && capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)return automation.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();capabilityGeneration++;automation.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();capabilityGeneration++;automation.invalidate();automation=createAutomation();}
    // Inventory and burn state remain server-owned.
    @Override public CompoundNBT getUpdateTag(){return new CompoundNBT();}
}
