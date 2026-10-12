package com.bewitchment.machine;

import com.bewitchment.item.PoppetItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;
import java.util.UUID;

public final class ShelfInventory extends ItemStackHandler {
    private final Runnable changed;
    public ShelfInventory(Runnable changed) { super(9); this.changed = changed; }
    @Override protected void onContentsChanged(int slot) { changed.run(); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // Forge's handler can retain the supplied object on a complete insertion.
        // Own a copy so later caller mutations cannot silently alter stored contents.
        return super.insertItem(slot, stack.copy(), simulate);
    }
    @Override public void deserializeNBT(CompoundNBT tag) {
        // Keep nine slots even if a saved/command-provided handler claims another size.
        CompoundNBT fixed = tag.copy(); fixed.putInt("Size", 9); super.deserializeNBT(fixed);
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        }
    }
    public boolean spend(PoppetItem.Kind kind, UUID owner) {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack copy = getStackInSlot(slot).copy();
            if (PoppetItem.spend(copy, kind, owner)) {
                setStackInSlot(slot, copy);
                return true;
            }
        }
        return false;
    }
}
