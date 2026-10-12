package com.bewitchment.combat;

import com.bewitchment.item.PoppetItem;
import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;

public final class ExtendedPoppetCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Item item:new Item[]{Items.IRON_PICKAXE,Items.DIAMOND_SWORD,Items.SHIELD,Items.BOW}){
            for(int damage:new int[]{0,1,item.getMaxDamage()-1,item.getMaxDamage(),item.getMaxDamage()+1}){
                ItemStack original=new ItemStack(item);original.setDamageValue(damage);
                original.getOrCreateTag().putString("custom","preserved");
                ItemStack rescued=ExtendedPoppetEffects.rescuedTool(original);
                check(rescued.getItem()==item&&rescued.getCount()==1,"One matching item restored");
                check(rescued.getDamageValue()==Math.min(damage,item.getMaxDamage()-1),"Retain pre-break damage, clamp broken snapshots");
                check(rescued.getTag().getString("custom").equals("preserved"),"Preserve metadata");
                rescued.getTag().putString("custom","changed");
                check(original.getTag().getString("custom").equals("preserved")&&original.getDamageValue()==damage,"Independent snapshot");
            }
        }
        for(ItemStack invalid:new ItemStack[]{ItemStack.EMPTY,new ItemStack(Items.STICK),new ItemStack(Items.IRON_PICKAXE,2),new ItemStack(new PoppetItem(PoppetItem.Kind.TOOL)),new ItemStack(new PoppetItem(PoppetItem.Kind.VAMPIRIC))})
            check(ExtendedPoppetEffects.rescuedTool(invalid).isEmpty(),"Reject invalid or recursive rescue");
        for(float amount:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY})
            check(!ExtendedPoppetEffects.transfer(amount,()->{throw new AssertionError("Invalid damage reached target");}),"Reject invalid damage");
        check(!ExtendedPoppetEffects.transfer(2,()->false),"Rejected damage does not transfer");
        int[] calls={0};
        check(ExtendedPoppetEffects.transfer(4,()->{calls[0]++;check(!ExtendedPoppetEffects.transfer(4,()->{throw new AssertionError("Recursive damage");}),"Reciprocal transfer blocked");return true;}),"Accepted damage transfers");
        check(calls[0]==1,"Exactly one target call");
        boolean threw=false;try{ExtendedPoppetEffects.transfer(3,()->{throw new IllegalStateException("test");});}catch(IllegalStateException expected){threw=true;}
        check(threw&&ExtendedPoppetEffects.transfer(3,()->true),"Exception releases reentry guard");
        check(ExtendedPoppetEffects.transfer(1,()->true),"Later independent hit allowed");
        System.out.println("PASS: "+checks+" tool snapshot, metadata, damage rejection and transfer reentry checks");
    }
}
