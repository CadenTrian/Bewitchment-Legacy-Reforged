package com.bewitchment.item;

import com.bewitchment.entity.FeuerwurmRules;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class CaduceusCheck {
    private static int checks;
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        CaduceusItem staff=new CaduceusItem();ItemStack stack=new ItemStack(staff);
        check(staff.getMaxDamage()==128 && staff.getMaxStackSize()==1,"Original durability and stack limit");
        check(!staff.isEnchantable(stack) && !staff.canApplyAtEnchantingTable(stack,Enchantments.UNBREAKING),"Table enchantment denied");
        check(!staff.isBookEnchantable(stack,new ItemStack(Items.ENCHANTED_BOOK)),"Book enchantment denied");
        check(!staff.isValidRepairItem(stack,new ItemStack(Items.STICK)) && !staff.isRepairable(stack),"Repair denied");
        for(int roll=0;roll<3;roll++)check(CaduceusRules.count(roll)==roll+1,"One to three summon choices");
        for(int invalid:new int[]{-1,3,Integer.MAX_VALUE}){boolean rejected=false;try{CaduceusRules.count(invalid);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"Invalid summon roll rejected");}
        List<Integer> inserted=new ArrayList<>();int[] created={0};
        check(CaduceusRules.spawn(2,()->++created[0],n->{inserted.add(n);return n!=2;})==2 && inserted.equals(Arrays.asList(1,2,3)),"Partial insertion retains successes without rerolling");
        created[0]=0;check(CaduceusRules.spawn(2,()->{created[0]++;return null;},n->{throw new AssertionError("Null insertion");})==0 && created[0]==3,"Null creations bounded and skipped");
        check(CaduceusRules.spawn(2,()->1,n->false)==0,"Entirely rejected wave returns zero");
        check(CaduceusRules.spawn(0,()->1,n->true)==1,"Single successful candidate");
        check(CaduceusRules.rescueHand(true,true)==Hand.MAIN_HAND,"Main hand wins when both hold staffs");
        check(CaduceusRules.rescueHand(false,true)==Hand.OFF_HAND,"Offhand rescue supported");
        check(CaduceusRules.rescueHand(true,false)==Hand.MAIN_HAND && CaduceusRules.rescueHand(false,false)==null,"No staff means no rescue");
        check(CaduceusRules.rescueHealth(20)==8 && CaduceusRules.rescueHealth(4)==4,"Health restored within actual maximum");
        check(CaduceusRules.rescueHealth(0)==0 && CaduceusRules.rescueHealth(-1)==0,"No negative restored health");
        check(CaduceusRules.LIFETIME==20*20 && CaduceusRules.COOLDOWN==2*20,"Original lifetime and cooldown");
        check(CaduceusRules.RESCUE_COST==16 && CaduceusRules.DURABILITY/CaduceusRules.RESCUE_COST==8,"Eight rescue charges before prior use");
        AxisAlignedBB body=new AxisAlignedBB(-.725,65,-.425,.725,65.85,.425);
        check(CaduceusRules.fits(body,-1,1,-1,1),"Full body fits open bounds");
        check(!CaduceusRules.fits(body,-.5,1,-1,1),"Center inside border cannot excuse protruding body");
        check(!CaduceusRules.fits(body.move(0,191,0),-1,1,-1,1),"Body above build height rejected");
        check(!CaduceusRules.fits(body.move(0,-66,0),-1,1,-1,1),"Body below world rejected");
        check(CaduceusRules.fits(new AxisAlignedBB(0,255,0,1,256,1),0,1,0,1),"Exact upper height and border accepted");
        for(int skin=0;skin<9;skin++)check(FeuerwurmRules.skin(skin)==skin,"All spectral summon skins valid");
        UUID owner=UUID.randomUUID();check(owner.equals(FeuerwurmRules.summoner(owner.toString())) && FeuerwurmRules.summoner("broken")==null,"Saved owner identity and malformed recovery");
        System.out.println("PASS: "+checks+" Caduceus item, summon settlement, rescue selection and placement checks.");
    }
}
