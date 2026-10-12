package com.bewitchment.entity;
import com.bewitchment.client.LegacyDrudenModel;
import com.bewitchment.item.ThyrsusItem;
import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;

public final class DrudenCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int i=0;i<4;i++)check(CreatureRules.skin(i)==i,"Four valid skins");
        check(CreatureRules.skin(-1)==0 && CreatureRules.skin(4)==0,"Invalid skin fallback");
        check(DrudenRules.waterHealing(20,true,true)==6,"Water and wet healing stack");
        check(DrudenRules.waterHealing(20,false,true)==3,"Rain heals three");
        check(DrudenRules.waterHealing(20,false,false)==0,"Dry Druden gets no water heal");
        check(DrudenRules.waterHealing(19,true,true)==0,"Water cadence is one second");
        check(DrudenRules.waterHealing(-20,true,true)==0,"Negative ticks do not heal");
        int pulses=0;for(int tick=0;tick<400;tick++)if(DrudenRules.recovery(tick,true))pulses++;
        check(pulses==12,"Two six-tick recovery bursts in 400 ticks");
        check(DrudenRules.recovery(205,true) && !DrudenRules.recovery(206,true),"Recovery boundary");
        check(!DrudenRules.recovery(200,false),"Full health skips recovery");
        check(DrudenRules.axeDamage(4,true)==12,"Direct axe damage tripled");
        check(DrudenRules.axeDamage(4,false)==4,"Arrows and other damage unchanged");
        check(DrudenRules.prey(true,false,false,false,false,1.99,false),"Close player targeted");
        check(!DrudenRules.prey(true,false,false,false,false,2,false),"Strict squared-distance boundary");
        check(DrudenRules.prey(false,true,true,true,false,100,false),"Explicit species bypass generic restrictions");
        check(DrudenRules.prey(false,false,false,false,true,100,false),"Ordinary undead hunted");
        check(!DrudenRules.prey(false,false,false,false,false,0,false),"Ordinary living animals are not generic prey");
        check(!DrudenRules.prey(false,false,true,false,true,0,false),"Fire immune generic undead excluded");
        check(!DrudenRules.prey(false,false,false,true,true,0,false),"Demon generic undead excluded");
        check(!DrudenRules.prey(true,true,false,false,true,0,true),"Protective clothing overrides all proactive branches");
        LegacyDrudenModel model=new LegacyDrudenModel();
        check(model.head==model.bipedHead && model.body==model.bipedBody,"Actual head/body bound to native model hooks");
        check(model.leftArm==model.bipedLeftArm && model.rightArm==model.bipedRightArm,"Held items use original arms");
        check(model.leftLeg==model.bipedLeftLeg && model.rightLeg==model.bipedRightLeg,"Original legs bound");
        check(model.texWidth==128 && model.texHeight==64,"Original atlas size");
        check(model.bipedBody.y==-8.8F && model.lowerBody.y==6,"Original torso and hip pivots");
        check(model.bipedLeftLeg.y==8 && model.bipedRightLeg.y==8,"Legs remain local to lower body");
        ThyrsusItem staff=new ThyrsusItem();
        check(staff.getMaxDamage()==72 && staff.getMaxStackSize()==1,"Staff durability and stack limit");
        check(ThyrsusItem.TIER.getAttackDamageBonus()==3 && staff.getDamage()==6,"Staff sword bonus preserves seven total player damage");
        check(ThyrsusItem.TIER.getEnchantmentValue()==20 && ThyrsusItem.TIER.getLevel()==1,"Staff enchantability and tier");
        check(ThyrsusItem.TIER.getRepairIngredient().test(new ItemStack(Items.STICK)),"Stick repairs staff");
        check(!ThyrsusItem.TIER.getRepairIngredient().test(new ItemStack(Items.IRON_INGOT)),"Unrelated repair excluded");
        int nausea=0;for(int roll=0;roll<100;roll++)if(ThyrsusItem.nauseates(false,true,roll))nausea++;
        check(nausea==10,"Ten percent nausea chance");
        check(!ThyrsusItem.nauseates(true,true,0),"No extra nausea in player-versus-player melee");
        check(ThyrsusItem.nauseates(true,false,9),"Mob attacker passes player-target exclusion");
        check(!ThyrsusItem.nauseates(false,false,10) && !ThyrsusItem.nauseates(false,false,-1),"Nausea roll boundaries");
        System.out.println("PASS: "+checks+" Druden and Thyrsus checks.");
    }
}
