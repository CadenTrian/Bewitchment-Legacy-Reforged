package com.bewitchment.machine;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class SigilCrafting {
    /** Return the target slot without modifying either inventory. */
    public static int destination(IInventory target,int slots,ItemStack result){
        for(int i=0;i<slots;i++){
            ItemStack stack=target.getItem(i);
            if(!stack.isEmpty() && ItemStack.isSame(stack,result) && ItemStack.tagMatches(stack,result)
                && stack.getCount()+result.getCount()<=Math.min(stack.getMaxStackSize(),target.getMaxStackSize()))return i;
        }
        for(int i=0;i<slots;i++)if(target.getItem(i).isEmpty())return i;
        return -1;
    }
    /** A server-thread transaction: validate, reserve space, consume all 25 inputs, deliver. */
    public static boolean craft(SigilRecipe recipe,Inventory matrix,IInventory target,int slots){
        if(recipe==null || !recipe.matches(matrix,null))return false;
        ItemStack result=recipe.assemble(matrix);int slot=destination(target,slots,result);if(slot<0)return false;
        for(int i=0;i<25;i++)matrix.removeItem(i,1);
        ItemStack existing=target.getItem(slot);if(existing.isEmpty())target.setItem(slot,result);else existing.grow(result.getCount());
        matrix.setChanged();target.setChanged();return true;
    }
    private SigilCrafting(){}
}
