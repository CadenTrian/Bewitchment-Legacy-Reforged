package com.bewitchment.machine;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

/** Own one item per slot, including repeated ingredients in distinct slots. */
public final class RitualInventory extends ItemStackHandler {
    private final Runnable changed;
    public RitualInventory(Runnable changed) { super(10); this.changed = changed; }
    @Override public int getSlotLimit(int slot) { return 1; }
    public net.minecraft.util.NonNullList<ItemStack> takeAll() {
        net.minecraft.util.NonNullList<ItemStack> taken = net.minecraft.util.NonNullList.create();
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = extractItem(i, 1, false);
            if (!stack.isEmpty()) taken.add(stack);
        }
        return taken;
    }
    @Override protected void onContentsChanged(int slot) { changed.run(); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return super.insertItem(slot, stack.copy(), simulate); }
    @Override public void deserializeNBT(CompoundNBT tag) {
        CompoundNBT fixed = tag.copy(); fixed.putInt("Size", 10); super.deserializeNBT(fixed);
        for (int i = 0; i < getSlots(); i++) if (!getStackInSlot(i).isEmpty()) getStackInSlot(i).setCount(1);
    }
}
