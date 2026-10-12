package com.bewitchment.item;

import com.bewitchment.machine.LeonardsWandRecipe;
import net.minecraft.item.*;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class LeonardsWandCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static CraftingInventory grid(int width,int height){return new CraftingInventory(new Container(null,-1){@Override public boolean stillValid(PlayerEntity player){return true;}},width,height);}
    public static void main(String[] args){
        Bootstrap.bootStrap();LeonardsWandItem item=new LeonardsWandItem();ItemStack wand=new ItemStack(item);
        check(item.getMaxDamage()==128 && item.getMaxStackSize()==1,"Original durability and stack limit");
        check(!item.isEnchantable(wand) && !item.isBookEnchantable(wand,new ItemStack(Items.ENCHANTED_BOOK)) && !item.canApplyAtEnchantingTable(wand,net.minecraft.enchantment.Enchantments.UNBREAKING),"Normal enchantment paths disabled");
        check(!item.isRepairable(wand) && !item.isValidRepairItem(wand,new ItemStack(Items.STICK)),"Repair blocked");
        check(LeonardsWandItem.charges(wand)==0 && !item.isFoil(wand),"Empty wand not charged");
        wand.setDamageValue(57);wand.getOrCreateTag().putString("marker","retain");
        wand.setHoverName(new net.minecraft.util.text.StringTextComponent("Named wand"));
        check(!item.isFoil(wand),"Damage and name alone do not enable firing/glint");
        ItemStack potion=PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION),Potions.LONG_POISON);
        PotionUtils.setCustomEffects(potion,Arrays.asList(new EffectInstance(Effects.MOVEMENT_SPEED,231,2,false,false)));
        ItemStack loaded=LeonardsWandItem.charge(wand,potion);
        check(LeonardsWandItem.charges(loaded)==8 && item.isFoil(loaded),"Splash potion provides eight shots");
        check(loaded.getDamageValue()==57 && loaded.getHoverName().getString().equals("Named wand") && loaded.getTag().getString("marker").equals("retain"),"Charging preserves damage/name/custom data");
        check(LeonardsWandItem.charges(wand)==0 && !wand.getTag().contains("CustomPotionEffects"),"Charging leaves input wand unchanged");
        List<EffectInstance> effects=PotionUtils.getMobEffects(loaded);
        check(effects.size()==2 && effects.get(0).getEffect()==Effects.POISON && effects.get(1).getEffect()==Effects.MOVEMENT_SPEED,"Base and custom effects transferred");
        check(effects.get(1).getDuration()==231 && effects.get(1).getAmplifier()==2 && !effects.get(1).isVisible(),"Custom effect properties preserved");
        ItemStack thrown=LeonardsWandItem.projectile(loaded);
        check(thrown.getItem()==Items.SPLASH_POTION && PotionUtils.getMobEffects(thrown).size()==2,"Vanilla splash projectile payload");
        check(thrown.getTag().getInt("CustomPotionColor")==PotionUtils.getColor(effects),"Color calculated from effects");
        check(!thrown.getTag().contains("marker") && !thrown.getTag().contains("uses") && !thrown.hasCustomHoverName(),"Wand metadata cannot leak into projectile");
        for(int left=7;left>=0;left--){LeonardsWandItem.spend(loaded);check(LeonardsWandItem.charges(loaded)==left,"Charge decrement "+left);}
        check(!loaded.getTag().contains("CustomPotionEffects") && !loaded.getTag().contains("uses") && !item.isFoil(loaded),"Last shot clears charge/effects/glint");
        check(loaded.getDamageValue()==57 && loaded.hasCustomHoverName() && loaded.getTag().getString("marker").equals("retain"),"Last shot cannot repair wand or erase metadata");
        LeonardsWandItem.spend(loaded);check(LeonardsWandItem.charges(loaded)==0 && loaded.getDamageValue()==57,"Empty spending cannot underflow or repair");
        loaded.getOrCreateTag().putInt("uses",Integer.MAX_VALUE);check(LeonardsWandItem.charges(loaded)==8,"Oversized count bounded");
        loaded.getTag().putInt("uses",-1);check(LeonardsWandItem.charges(loaded)==0,"Negative charge rejected");
        loaded.getTag().putString("uses","eight");check(LeonardsWandItem.charges(loaded)==0,"Wrong tag type safe");
        ItemStack water=PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION),Potions.WATER);
        ItemStack replaced=LeonardsWandItem.charge(LeonardsWandItem.charge(wand,potion),water);
        check(LeonardsWandItem.charges(replaced)==8 && PotionUtils.getMobEffects(replaced).isEmpty(),"Empty-effect splash replaces prior effects");
        LeonardsWandRecipe recipe=new LeonardsWandRecipe(new ResourceLocation("bewitchment:wand_test"));
        check(recipe.canCraftInDimensions(2,2) && recipe.canCraftInDimensions(3,3) && !recipe.canCraftInDimensions(1,4) && !recipe.canCraftInDimensions(4,1),"Original two-dimensional grid requirement");
        CraftingInventory grid=grid(2,2);grid.setItem(0,wand);grid.setItem(3,potion);
        check(recipe.matches(grid,null) && LeonardsWandItem.charges(recipe.assemble(grid))==8,"Shapeless charge craft");
        check(recipe.assemble(grid).getDamageValue()==57 && grid.getItem(0)==wand,"Recipe keeps wear and input stack");
        grid.setItem(1,new ItemStack(Items.STICK));check(!recipe.matches(grid,null) && recipe.assemble(grid).isEmpty(),"Extra ingredient rejected");grid.setItem(1,ItemStack.EMPTY);
        grid.setItem(2,new ItemStack(item));check(!recipe.matches(grid,null),"Second wand rejected");grid.setItem(2,ItemStack.EMPTY);
        grid.setItem(3,new ItemStack(Items.POTION));check(!recipe.matches(grid,null),"Drinkable potion rejected");
        grid.setItem(3,new ItemStack(Items.LINGERING_POTION));check(!recipe.matches(grid,null),"Lingering potion rejected");
        grid.setItem(3,ItemStack.EMPTY);check(!recipe.matches(grid,null),"Missing splash rejected");
        grid.setItem(3,potion);check(recipe.getRemainingItems(grid).stream().allMatch(ItemStack::isEmpty),"No invented bottle or wand remainder");
        check(recipe.isSpecial(),"Dynamic recipe not a static recipe-book output");
        System.out.println("PASS: "+checks+" Leonard wand charging, effects, metadata, recipe and restrictions checks.");
    }
}
