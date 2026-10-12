package com.bewitchment.machine;

import com.bewitchment.block.SpinningWheelBlock;
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

public final class SpinningWheelTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private int progress, poweredTicks;
    private BlockPos altarPos;
    private long nextSearch;
    private SpinningWheelRecipe activeRecipe;
    public final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot < 4; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private LazyOptional<IItemHandler> inputs = LazyOptional.of(() -> new RangedWrapper(inventory, 0, 4));
    private LazyOptional<IItemHandler> outputs = LazyOptional.of(() -> new RangedWrapper(inventory, 4, 6));
    public final IIntArray data = new IIntArray() {
        public int get(int i) { return i == 0 ? progress : poweredTicks; }
        public void set(int i, int value) { if (i == 0) progress = value; else poweredTicks = value; }
        public int getCount() { return 2; }
    };
    public SpinningWheelTile() { super(ModMachines.SPINNING_WHEEL.get()); }
    private boolean drawPower() {
        TileEntity source = altarPos != null && AltarTile.inRange(altarPos, worldPosition) && level.hasChunkAt(altarPos) ? level.getBlockEntity(altarPos) : null;
        if (!(source instanceof AltarTile) || !((AltarTile)source).intact()) {
            altarPos = null;
            if (level.getGameTime() < nextSearch) return false;
            nextSearch = level.getGameTime() + 100;
            source = AltarTile.nearest((net.minecraft.world.server.ServerWorld)level, worldPosition);
            if (source != null) altarPos = source.getBlockPos().immutable();
        }
        return source instanceof MagicPowerSource && ((MagicPowerSource)source).tryDrainMagic(20);
    }
    /** Simulate the whole batch before touching either real output slot. */
    private List<ItemStack> planOutputs(SpinningWheelRecipe recipe) {
        return planOutputs(Arrays.asList(inventory.getStackInSlot(4), inventory.getStackInSlot(5)), recipe.getOutputs());
    }
    static List<ItemStack> planOutputs(List<ItemStack> existing, List<ItemStack> results) {
        List<ItemStack> planned = new ArrayList<>();
        for (ItemStack stack : existing) planned.add(stack.copy());
        for (ItemStack output : results) {
            ItemStack result = output.copy();
            for (ItemStack slot : planned) {
                if (!slot.isEmpty() && ItemStack.isSame(slot, result) && ItemStack.tagMatches(slot, result)) {
                    int moved = Math.min(result.getCount(), slot.getMaxStackSize() - slot.getCount());
                    slot.grow(moved); result.shrink(moved);
                    if (result.isEmpty()) break;
                }
            }
            for (int i = 0; !result.isEmpty() && i < planned.size(); i++) if (planned.get(i).isEmpty()) {
                planned.set(i, result.copy()); result.setCount(0);
            }
            if (!result.isEmpty()) return null;
        }
        return planned;
    }
    @Override public void tick() {
        if (level == null || level.isClientSide) return;
        ItemStack[] stacks = new ItemStack[4]; for (int i = 0; i < 4; i++) stacks[i] = inventory.getStackInSlot(i);
        SpinningWheelRecipe recipe = level.getRecipeManager().getRecipeFor(SpinningWheelRecipe.TYPE, new Inventory(stacks), level).orElse(null);
        // Object identity also detects /reload replacing a recipe with the same ID.
        if (recipe != activeRecipe) { activeRecipe = recipe; progress = 0; setChanged(); }
        List<ItemStack> planned = recipe == null ? null : planOutputs(recipe);
        boolean working = false;
        if (planned != null) {
            if (poweredTicks == 0 && drawPower()) poweredTicks = 20;
            if (poweredTicks > 0) {
                poweredTicks--; working = true;
                if (++progress >= 200) {
                    for (int i = 0; i < 2; i++) inventory.setStackInSlot(i + 4, planned.get(i));
                    for (int i = 0; i < 4; i++) if (!inventory.getStackInSlot(i).isEmpty()) inventory.extractItem(i, 1, false);
                    progress = 0;
                    level.playSound(null, worldPosition, SoundEvents.WOOL_PLACE, SoundCategory.BLOCKS, 0.6F, 1);
                }
                setChanged();
            }
        } else if (progress != 0) { progress = 0; setChanged(); }
        if (getBlockState().getValue(SpinningWheelBlock.IN_USE) != working)
            level.setBlock(worldPosition, getBlockState().setValue(SpinningWheelBlock.IN_USE, working), 3);
    }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); tag.put("Inventory", inventory.serializeNBT());
        // Restart partial batches on load; paid ticks remain paid, and no items are consumed early.
        tag.putInt("PoweredTicks", poweredTicks);
        if (altarPos != null) tag.putLong("AltarPos", altarPos.asLong()); return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); inventory.deserializeNBT(tag.getCompound("Inventory"));
        progress = 0; activeRecipe = null; poweredTicks = Math.max(0, Math.min(20, tag.getInt("PoweredTicks")));
        altarPos = tag.contains("AltarPos") ? BlockPos.of(tag.getLong("AltarPos")) : null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (side == Direction.DOWN ? outputs : inputs).cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); inputs.invalidate(); outputs.invalidate(); }
    @Override public void reviveCaps() {
        super.reviveCaps(); inputs = LazyOptional.of(() -> new RangedWrapper(inventory, 0, 4)); outputs = LazyOptional.of(() -> new RangedWrapper(inventory, 4, 6));
    }
    @Override public ITextComponent getDisplayName() { return new TranslationTextComponent("block.bewitchment.spinning_wheel"); }
    @Override public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) { return new SpinningWheelMenu(id, inv, this); }
}
