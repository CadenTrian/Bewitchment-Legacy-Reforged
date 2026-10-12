package com.bewitchment.machine;

import com.bewitchment.block.DistilleryBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.RangedWrapper;
import javax.annotation.Nullable;
import java.util.*;

public final class DistilleryTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private int burnTime, progress, poweredTicks;
    private BlockPos altarPos;
    private long nextAltarSearch;
    private ResourceLocation activeRecipe;
    public final ItemStackHandler inventory = new ItemStackHandler(13) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 ? stack.getItem() == Items.BLAZE_POWDER : slot < 7; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final LazyOptional<IItemHandler> fuel = LazyOptional.of(() -> new RangedWrapper(inventory, 0, 1));
    private final LazyOptional<IItemHandler> inputs = LazyOptional.of(() -> new RangedWrapper(inventory, 1, 7));
    private final LazyOptional<IItemHandler> outputs = LazyOptional.of(() -> new RangedWrapper(inventory, 7, 13));
    public final IIntArray data = new IIntArray() {
        public int get(int i) { return i == 0 ? burnTime : i == 1 ? progress : poweredTicks; }
        public void set(int i, int value) { if(i == 0) burnTime = value; else if(i == 1) progress = value; else poweredTicks = value; }
        public int getCount() { return 3; }
    };
    public DistilleryTile() { super(ModMachines.DISTILLERY.get()); }
    public void linkAltar(BlockPos pos) { altarPos = pos.immutable(); poweredTicks = 0; setChanged(); }
    private boolean drawPower() {
        TileEntity source = altarPos != null && AltarTile.inRange(altarPos, worldPosition) && level.hasChunkAt(altarPos) ? level.getBlockEntity(altarPos) : null;
        if (!(source instanceof AltarTile) || !((AltarTile) source).intact()) {
            altarPos = null;
            if (level.getGameTime() < nextAltarSearch) return false;
            nextAltarSearch = level.getGameTime() + 100;
            source = AltarTile.nearest((net.minecraft.world.server.ServerWorld) level, worldPosition);
            if (source != null) { altarPos = source.getBlockPos().immutable(); setChanged(); }
        }
        return source instanceof MagicPowerSource && ((MagicPowerSource) source).tryDrainMagic(20);
    }
    /** Plan all outputs together; a partially fitting batch never consumes ingredients. */
    private List<ItemStack> planOutputs(DistilleryRecipe recipe) {
        List<ItemStack> planned = new ArrayList<>();
        for (int i = 7; i < 13; i++) planned.add(inventory.getStackInSlot(i).copy());
        for (ItemStack result : recipe.getOutputs()) {
            for (ItemStack slot : planned) {
                if (ItemStack.isSame(slot, result) && ItemStack.tagMatches(slot, result)) {
                    int moved = Math.min(result.getCount(), slot.getMaxStackSize() - slot.getCount());
                    slot.grow(moved); result.shrink(moved);
                    if (result.isEmpty()) break;
                }
            }
            if (!result.isEmpty()) {
                for (int i = 0; i < planned.size(); i++) if (planned.get(i).isEmpty()) {
                    planned.set(i, result.copy()); result.setCount(0); break;
                }
            }
            if (!result.isEmpty()) return null;
        }
        return planned;
    }
    @Override public void tick() {
        if (level == null || level.isClientSide) return;
        if (burnTime > 0) { burnTime--; setChanged(); }
        if (poweredTicks > 0) { poweredTicks--; setChanged(); }
        ItemStack[] stacks = new ItemStack[6]; for (int i = 0; i < 6; i++) stacks[i] = inventory.getStackInSlot(i + 1);
        DistilleryRecipe recipe = level.getRecipeManager().getRecipeFor(DistilleryRecipe.TYPE, new Inventory(stacks), level).orElse(null);
        ResourceLocation next = recipe == null ? null : recipe.getId();
        if (!Objects.equals(next, activeRecipe)) { progress = 0; activeRecipe = next; setChanged(); }
        List<ItemStack> planned = recipe == null ? null : planOutputs(recipe);
        boolean working = false;
        if (planned != null && (burnTime > 0 || inventory.getStackInSlot(0).getItem() == Items.BLAZE_POWDER)) {
            if (poweredTicks == 0 && drawPower()) poweredTicks = 20;
            if (poweredTicks > 0) {
                if (burnTime == 0) { inventory.extractItem(0, 1, false); burnTime = 1200; }
                working = true;
                if (++progress >= 200) {
                    for (int i = 0; i < 6; i++) inventory.setStackInSlot(i + 7, planned.get(i));
                    for (int i = 1; i < 7; i++) if (!inventory.getStackInSlot(i).isEmpty()) inventory.extractItem(i, 1, false);
                    progress = 0;
                    level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundCategory.BLOCKS, 1, 1);
                }
                setChanged();
            }
        }
        if (!working && progress > 0) { progress = planned == null ? 0 : Math.max(0, progress - 2); setChanged(); }
        if (getBlockState().getValue(DistilleryBlock.IN_USE) != working)
            level.setBlock(worldPosition, getBlockState().setValue(DistilleryBlock.IN_USE, working), 3);
    }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); tag.put("Inventory", inventory.serializeNBT()); tag.putInt("BurnTime", burnTime); tag.putInt("Progress", progress);
        tag.putInt("PoweredTicks", poweredTicks); if (altarPos != null) tag.putLong("AltarPos", altarPos.asLong());
        if (activeRecipe != null) tag.putString("Recipe", activeRecipe.toString()); return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); inventory.deserializeNBT(tag.getCompound("Inventory"));
        burnTime = Math.max(0, tag.getInt("BurnTime")); progress = Math.max(0, Math.min(199, tag.getInt("Progress")));
        poweredTicks = Math.max(0, Math.min(20, tag.getInt("PoweredTicks")));
        altarPos = tag.contains("AltarPos") ? BlockPos.of(tag.getLong("AltarPos")) : null;
        activeRecipe = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (side == Direction.DOWN ? outputs : side == Direction.UP ? inputs : fuel).cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); fuel.invalidate(); inputs.invalidate(); outputs.invalidate(); }
    @Override public ITextComponent getDisplayName() { return new TranslationTextComponent("block.bewitchment.distillery"); }
    @Override public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) { return new DistilleryMenu(id, inv, this); }
}
