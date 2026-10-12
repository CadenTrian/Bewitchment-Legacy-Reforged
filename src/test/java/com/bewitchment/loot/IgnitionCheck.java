package com.bewitchment.loot;

import com.bewitchment.machine.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class IgnitionCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static FurnaceRecipe recipe(String id,Item input,ItemStack result){return new FurnaceRecipe(new ResourceLocation("bewitchment",id),"",Ingredient.of(input),result,.7F,200);}
    public static void main(String[] args){
        Bootstrap.bootStrap();BlockState sand=Blocks.SAND.defaultBlockState();ItemStack glass=new ItemStack(Items.GLASS);glass.getOrCreateTag().putString("recipe","preserved");
        FurnaceRecipe smelt=recipe("sand",Items.SAND,glass);
        check(IgnitionLoot.result(sand,Collections.singletonList(smelt),null).getItem()==Items.GLASS,"Broken block is furnace input");
        check(IgnitionLoot.result(Blocks.GRAVEL.defaultBlockState(),Collections.singletonList(smelt),null).isEmpty(),"Unsmeltable block keeps ordinary drops");
        check(IgnitionLoot.result(null,Collections.singletonList(smelt),null).isEmpty(),"Missing block has no output");
        check(IgnitionLoot.result(Blocks.AIR.defaultBlockState(),Collections.singletonList(smelt),null).isEmpty(),"Block without item has no output");
        check(IgnitionLoot.result(sand,Collections.emptyList(),null).isEmpty(),"Removed recipe is immediately unavailable");
        ItemStack output=IgnitionLoot.result(sand,Collections.singletonList(smelt),null);output.getOrCreateTag().putString("recipe","changed");
        check(IgnitionLoot.result(sand,Collections.singletonList(smelt),null).getTag().getString("recipe").equals("preserved"),"Assembly does not mutate recipe NBT");
        FurnaceRecipe first=recipe("a",Items.SAND,new ItemStack(Items.DIAMOND,2)),last=recipe("z",Items.SAND,new ItemStack(Items.EMERALD));
        for(List<FurnaceRecipe> recipes:Arrays.asList(Arrays.asList(first,last),Arrays.asList(last,first)))check(IgnitionLoot.result(sand,recipes,null).getItem()==Items.DIAMOND,"Overlapping recipes sorted deterministically");
        check(IgnitionLoot.result(sand,Collections.singletonList(last),null).getItem()==Items.EMERALD,"Changed recipe collection used without stale cache");
        for(int count:new int[]{1,2,16,64}){
            ItemStack original=new ItemStack(Items.SAND,count);original.getOrCreateTag().putString("original","yes");List<ItemStack> drops=Arrays.asList(original,new ItemStack(Items.STICK,3));
            List<ItemStack> replaced=IgnitionLoot.replace(drops,glass);check(replaced.size()==1 && replaced.get(0).getItem()==Items.GLASS && replaced.get(0).getCount()==1,"Whole drop list replaced once regardless of original count "+count);
            check(drops.size()==2 && original.getCount()==count && original.getTag().getString("original").equals("yes"),"Incoming loot not mutated "+count);
            replaced.get(0).getTag().putString("recipe","changed");check(glass.getTag().getString("recipe").equals("preserved"),"Recipe output copied "+count);
            check(IgnitionLoot.replace(drops,ItemStack.EMPTY)==drops,"Unsmeltable result preserves exact list "+count);
        }
        List<ItemStack> custom=IgnitionLoot.replace(Collections.emptyList(),new ItemStack(Items.GLASS,3));check(custom.size()==1 && custom.get(0).getCount()==3,"Recipe output count preserved even from empty harvest");
        check(IgnitionLoot.blockTable(sand,Blocks.SAND.getLootTable()),"Root block table accepted");
        check(!IgnitionLoot.blockTable(sand,Blocks.GRAVEL.getLootTable()),"Nested or unrelated table rejected");
        check(!IgnitionLoot.blockTable(null,Blocks.SAND.getLootTable()) && !IgnitionLoot.blockTable(sand,null),"Missing context rejected");
        check(DonationContractData.ingredient(CurseData.Kind.IGNITION,0)==Items.BLAZE_ROD && DonationContractData.ingredient(CurseData.Kind.IGNITION,1)==Items.MAGMA_CREAM,"Legacy Ignition task ingredients");
        System.out.println("PASS: "+checks+" Ignition loot checks");
    }
}
