package com.bewitchment.entity;

import com.bewitchment.client.LegacyDemonModel;
import com.bewitchment.trade.DemonMerchantState;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class DemonCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean near(float a,float b){return Math.abs(a-b)<.0001;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int skin=0;skin<4;skin++)check(DemonRules.skin(skin)==skin,"Four valid skins");
        check(DemonRules.skin(-1)==0 && DemonRules.skin(4)==0 && DemonRules.skin(Integer.MAX_VALUE)==0,"Malformed skins repaired");
        int shots=0;for(int tick=0;tick<160;tick++)if(DemonRules.fires(tick,9))shots++;check(shots==12,"Six fireball ticks per eighty ticks");
        check(DemonRules.fires(5,4.01) && !DemonRules.fires(6,9) && DemonRules.fires(80,9),"Fireball cadence boundaries");
        for(double distance:new double[]{0,4,-1,Double.NaN,Double.POSITIVE_INFINITY})check(!DemonRules.fires(0,distance),"Invalid or melee distance excludes fireballs");
        check(!DemonRules.fires(-1,9) && DemonRules.spread(16)==1,"Spread and invalid tick");
        for(int bits=0;bits<8;bits++)for(int roll=0;roll<4;roll++){
            boolean fire=(bits&1)!=0,hellish=(bits&2)!=0,robes=(bits&4)!=0;
            check(DemonRules.prey(fire,hellish,robes,roll)==(!robes && (hellish?roll==0:!fire)),"Legacy fire immunity and accessory targeting");
        }
        boolean all=true;for(int bits=0;bits<256;bits++)all &= DemonRules.customer((bits&1)!=0,(bits&2)!=0,(bits&4)!=0,(bits&8)!=0,(bits&16)!=0,(bits&32)!=0,(bits&64)!=0,(bits&128)!=0,16)==(bits==43);
        check(all,"All customer eligibility combinations");
        for(double distance:new double[]{16.01,-1,Double.NaN,Double.POSITIVE_INFINITY})check(!DemonRules.customer(true,true,false,true,false,true,false,false,distance),"Out-of-range or invalid customer distance");
        check(DemonRules.customer(true,true,false,true,false,true,false,false,0),"Nearby customer allowed");
        AttributeModifierMap attributes=DemonRules.attributes(AttributeModifierMap.builder()).build();
        check(attributes.getBaseValue(Attributes.MAX_HEALTH)==195 && attributes.getBaseValue(Attributes.ATTACK_DAMAGE)==16,"Legacy health and melee damage");
        check(attributes.getBaseValue(Attributes.ARMOR)==6.66 && attributes.getBaseValue(Attributes.ARMOR_TOUGHNESS)==3.33,"Legacy armor");
        check(attributes.getBaseValue(Attributes.MOVEMENT_SPEED)==.8 && attributes.getBaseValue(Attributes.KNOCKBACK_RESISTANCE)==.7,"Legacy speed and knockback resistance");
        check(IMerchant.class.isAssignableFrom(DemonEntity.class),"Entity implements the vanilla merchant interface");
        LegacyDemonModel model=new LegacyDemonModel();
        check(model.bipedBody!=null && model.lWingMembrane!=null && model.rWingMembrane!=null && model.tailTip02!=null,"Original geometry includes body, membranes and tail");
        model.animate(0,0,0,0,0,90,30,0);check(near(model.bipedHead.yRot,(float)Math.PI/2) && near(model.bipedHead.xRot,(float)Math.PI/6),"Head yaw and pitch");
        check(near(model.bipedLeftLeg.xRot,-.26F) && near(model.bipedRightLeg.xRot,-.26F),"Resting leg bend");
        check(near(model.lWing01.zRot,.025F) && near(model.rWing01.zRot,-.025F),"Opposed wing idle motion");
        model.animate(0,10,0,0,0,0,0,0);check(near(model.bipedLeftArm.xRot,-.5F) && near(model.bipedRightArm.xRot,-.5F),"Attack pose");
        model.animate(0,0,0,0,0,0,0,0);check(near(model.bipedLeftArm.xRot,0) && near(model.bipedRightArm.xRot,0),"Attack pose resets between rendered entities");
        model.animate(12,0,2,1,10,0,0,.5F);check(!near(model.bipedLeftLeg.xRot,-.26F) && !near(model.tail01.yRot,0),"Walking animates legs and tail");
        DemonMerchantState state=new DemonMerchantState("noble");state.open(new Random(0),false,(entry,r,d)->new MerchantOffer(new ItemStack(Items.GOLD_INGOT,15),new ItemStack(Items.APPLE,2),7,0,0));
        MerchantOffer offer=state.offers().get(0);ItemStack insufficient=new ItemStack(Items.GOLD_INGOT,14);check(!offer.take(insufficient,ItemStack.EMPTY) && insufficient.getCount()==14,"Vanilla offer rejects insufficient payment without consumption");
        ItemStack payment=new ItemStack(Items.GOLD_INGOT,17);check(offer.take(payment,ItemStack.EMPTY) && payment.getCount()==2,"Vanilla offer consumes exact payment");
        check(state.completeTrade(offer,UUID.randomUUID(),new Random(0)).accepted && offer.getUses()==1,"Adapter callback counts successful purchase once");
        check(new MerchantOffer(offer.createTag()).getUses()==1,"Purchase usage persists");
        System.out.println("PASS: "+checks+" demon entity, model and merchant integration checks");
    }
}