package com.bewitchment.machine;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import java.util.function.BooleanSupplier;

/** Automation shares the manual inventory, but never merges offerings into stacks. */
public final class BrazierItemHandler implements IItemHandler {
    private final Inventory inventory;
    private final BooleanSupplier accessible;
    public BrazierItemHandler(Inventory inventory,BooleanSupplier accessible){this.inventory=inventory;this.accessible=accessible;}
    private void checkSlot(int slot){if(slot<0 || slot>=getSlots())throw new IndexOutOfBoundsException("Brazier slot: "+slot);}
    @Override public int getSlots(){return inventory.getContainerSize();}
    @Override public int getSlotLimit(int slot){checkSlot(slot);return 1;}
    @Override public boolean isItemValid(int slot,ItemStack stack){checkSlot(slot);return !stack.isEmpty() && accessible.getAsBoolean();}
    @Override public ItemStack getStackInSlot(int slot){checkSlot(slot);return accessible.getAsBoolean()?inventory.getItem(slot).copy():ItemStack.EMPTY;}
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
        checkSlot(slot);if(stack.isEmpty() || !accessible.getAsBoolean() || !inventory.getItem(slot).isEmpty())return stack;
        if(!simulate){ItemStack offered=stack.copy();offered.setCount(1);inventory.setItem(slot,offered);}
        ItemStack remainder=stack.copy();remainder.shrink(1);return remainder;
    }
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
        checkSlot(slot);if(amount<=0 || !accessible.getAsBoolean())return ItemStack.EMPTY;
        ItemStack current=inventory.getItem(slot);if(current.isEmpty())return ItemStack.EMPTY;
        int count=Math.min(amount,current.getCount());
        if(simulate){ItemStack result=current.copy();result.setCount(count);return result;}
        return inventory.removeItem(slot,count);
    }
}
