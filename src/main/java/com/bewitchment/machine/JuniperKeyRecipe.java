package com.bewitchment.machine;

import com.bewitchment.item.JuniperKeyItem;
import com.bewitchment.registry.ModItems;
import com.bewitchment.registry.ModTreeBlocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.*;

public final class JuniperKeyRecipe extends SpecialRecipe {
    private final boolean duplicate;
    public JuniperKeyRecipe(ResourceLocation id,boolean duplicate){super(id);this.duplicate=duplicate;}
    public static boolean accepts(List<ItemStack> stacks,Item planks,boolean duplicate){
        int keys=0,wood=0;
        for(ItemStack stack:stacks){if(stack.isEmpty())continue;if(stack.getItem() instanceof JuniperKeyItem)keys++;else if(duplicate && stack.getItem()==planks)wood++;else return false;}
        return duplicate?keys==1 && wood==1:keys>1;
    }
    private List<ItemStack> inputs(CraftingInventory inv){List<ItemStack> result=new ArrayList<>();for(int i=0;i<inv.getContainerSize();i++)result.add(inv.getItem(i));return result;}
    @Override public boolean matches(CraftingInventory inv,World world){return canCraftInDimensions(inv.getWidth(),inv.getHeight()) && accepts(inputs(inv),duplicate?ModTreeBlocks.JUNIPER.planks.get().asItem():Items.AIR,duplicate);}
    @Override public ItemStack assemble(CraftingInventory inv){
        if(!matches(inv,null))return ItemStack.EMPTY;
        if(!duplicate)return JuniperKeyItem.combine(ModItems.JUNIPER_KEY_RING.get(),inputs(inv));
        for(ItemStack stack:inputs(inv))if(stack.getItem() instanceof JuniperKeyItem){ItemStack result=stack.copy();result.setCount(1);return result;}
        return ItemStack.EMPTY;
    }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv){
        NonNullList<ItemStack> result=NonNullList.withSize(inv.getContainerSize(),ItemStack.EMPTY);
        if(duplicate)for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).getItem() instanceof JuniperKeyItem){result.set(i,inv.getItem(i).copy());result.get(i).setCount(1);}
        return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height){return (long)width*height>=(duplicate?9:2);}
    @Override public IRecipeSerializer<?> getSerializer(){return duplicate?ModMachines.DUPLICATE_KEY_RECIPE.get():ModMachines.KEY_RING_RECIPE.get();}
}
