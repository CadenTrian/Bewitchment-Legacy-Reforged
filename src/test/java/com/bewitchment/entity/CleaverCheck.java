package com.bewitchment.entity;
import com.bewitchment.client.LegacyCleaverModel;
import com.bewitchment.item.CleaverSwordItem;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.util.HandSide;
import net.minecraft.util.registry.Bootstrap;

public final class CleaverCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean close(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        check(CleaverRules.skin(0)==0 && CleaverRules.skin(1)==1,"Two skins");
        check(CleaverRules.skin(-1)==0 && CleaverRules.skin(2)==0 && CleaverRules.skin(Integer.MAX_VALUE)==0,"Invalid saved skin repaired");
        check(!CleaverRules.enrages(0,14.01F,40) && CleaverRules.enrages(0,14,40) && CleaverRules.enrages(0,13.99F,40),"Legacy float division compared to double threshold includes fourteen of forty health");
        int pulses=0;for(int tick=0;tick<400;tick++)if(CleaverRules.enrages(tick,10,40))pulses++;
        check(pulses==12,"Six enrage ticks per two hundred ticks");
        check(CleaverRules.enrages(205,1,40) && !CleaverRules.enrages(206,1,40),"Enrage interval boundary");
        check(!CleaverRules.enrages(-1,1,40) && !CleaverRules.enrages(0,1,0),"Invalid enrage inputs rejected");
        int cripples=0;for(int roll=0;roll<100;roll++)if(CleaverRules.cripples(roll))cripples++;
        check(cripples==3 && !CleaverRules.cripples(-1),"Three percent slowing strike");
        check(CleaverRules.damage(4)==4 && CleaverRules.damage(10.9)==10,"Integer-truncated total attribute damage");
        check(BafometyrRules.prey(false,true,true,true,false,false) && !BafometyrRules.prey(false,true,true,true,false,true),"Rival demon targeting and Besmirched protection");
        check(CambionRules.canPickUp(Items.IRON_SWORD) && CambionRules.canPickUp(Items.SHIELD) && CambionRules.canPickUp(Items.GOLDEN_HELMET) && !CambionRules.canPickUp(Items.IRON_AXE),"Pickup type filter");
        CleaverSwordItem blade=new CleaverSwordItem();ItemStack stack=new ItemStack(blade);
        check(blade.getMaxDamage()==333 && blade.getMaxStackSize()==1,"Blade durability and stack limit");
        check(blade.getDamage()==6 && CleaverSwordItem.TIER.getAttackDamageBonus()==3,"Six weapon bonus plus one player base damage");
        check(CleaverSwordItem.TIER.getSpeed()==16 && CleaverSwordItem.TIER.getLevel()==2,"Hellish tool tier");
        check(!blade.isEnchantable(stack) && !blade.isValidRepairItem(stack,new ItemStack(Items.IRON_INGOT)),"No enchanting-table enchantment or material repair");
        check(CleaverSwordItem.TIER.getRepairIngredient()==net.minecraft.item.crafting.Ingredient.EMPTY,"No repair ingredient");
        int corrosion=0;for(int roll=0;roll<100;roll++)if(CleaverSwordItem.corrodes(false,true,roll))corrosion++;
        check(corrosion==5,"Five percent blade proc");
        check(!CleaverSwordItem.corrodes(true,true,0) && CleaverSwordItem.corrodes(true,false,4),"PvP exclusion does not exclude a mob attacking a player");
        check(!CleaverSwordItem.corrodes(false,false,5) && !CleaverSwordItem.corrodes(false,false,-1),"Proc boundaries");
        Effect effect=ModEffects.corrosion();AttributeModifier modifier=effect.getAttributeModifiers().get(Attributes.ARMOR);
        check(modifier.getAmount()==-2 && modifier.getOperation()==AttributeModifier.Operation.ADDITION,"Corrosion subtracts armor");
        check(effect.getAttributeModifierValue(1,modifier)==-4,"Corrosion II subtracts four armor");
        check(modifier.getId().toString().equals("e8506ffe-e2b4-4f19-8669-becb8e3eb666"),"Original armor modifier identity");
        LegacyCleaverModel model=new LegacyCleaverModel(1);
        model.pose(0,0,0,90,30,false,0,HandSide.RIGHT,0);
        check(close(model.head.yRot,(float)Math.PI/2) && close(model.head.xRot,(float)Math.PI/6),"Head tracking");
        check(close(model.body.y,-5.7F) && model.lowerJaw.xRot==0,"Original torso and closed jaw");
        model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,Integer.MAX_VALUE-1);
        check(model.lowerJaw.xRot==.75F,"Enrage jaw pose");
        model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,20);
        check(model.lowerJaw.xRot==.75F,"Attack jaw pose");
        model.riding=true;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,0);
        check(close(model.rightLeg.xRot,-1.4137167F) && model.rightLeg.yRot>0,"Riding pose");
        model.riding=false;model.attackTime=.5F;model.pose(0,0,0,0,0,false,0,HandSide.LEFT,0);
        check(model.leftArm.xRot<model.rightArm.xRot,"Left attack uses real arm");
        model.attackTime=0;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,0);
        check(model.lowerJaw.xRot==0 && model.body.yRot==0 && model.rightArm.x==-5 && model.leftArm.x==5,"Next creature clears jaw/body/pivots");
        check(model.rightLeg.yRot==0 && model.leftLeg.yRot==0 && close(model.leftLeg.zRot,-.104719755F) && close(model.rightLeg.zRot,.104719755F),"Original leg angles restored after riding");
        check(close(model.rightLeg.xRot,-.26F) && close(model.leftLeg.xRot,-.26F),"Original digitigrade idle stance");
        System.out.println("PASS: "+checks+" Cleaver combat, blade, Corrosion and original model checks.");
    }
}
