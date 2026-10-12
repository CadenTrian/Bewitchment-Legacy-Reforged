package com.bewitchment.machine;

import com.bewitchment.item.LeonardsWandItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public final class LeonardsWandRecipe extends SpecialRecipe {
    public LeonardsWandRecipe(ResourceLocation id){super(id);}
    @Override public boolean matches(CraftingInventory inventory,World world){
        if(!canCraftInDimensions(inventory.getWidth(),inventory.getHeight()))return false;
        int wands=0,potions=0;
        for(int i=0;i<inventory.getContainerSize();i++){
            ItemStack stack=inventory.getItem(i);if(stack.isEmpty())continue;
            if(stack.getItem() instanceof LeonardsWandItem)wands++;
            else if(stack.getItem()==Items.SPLASH_POTION)potions++;
            else return false;
        }
        return wands==1 && potions==1;
    }
    @Override public ItemStack assemble(CraftingInventory inventory){
        if(!matches(inventory,null))return ItemStack.EMPTY;
        ItemStack wand=ItemStack.EMPTY,potion=ItemStack.EMPTY;
        for(int i=0;i<inventory.getContainerSize();i++){ItemStack stack=inventory.getItem(i);if(stack.getItem() instanceof LeonardsWandItem)wand=stack;else if(stack.getItem()==Items.SPLASH_POTION)potion=stack;}
        return LeonardsWandItem.charge(wand,potion);
    }
    @Override public boolean canCraftInDimensions(int width,int height){return width>=2 && height>=2;}
    @Override public IRecipeSerializer<?> getSerializer(){return ModMachines.WAND_RECIPE.get();}
}
