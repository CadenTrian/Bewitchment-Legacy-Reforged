package com.bewitchment.machine;

import com.bewitchment.potion.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class DefensiveBrewsCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void attribute(Effect effect,Attribute attribute,double base,double change,String uuid){
        AttributeModifier modifier=effect.getAttributeModifiers().get(attribute);
        check(effect.getAttributeModifiers().size()==1&&modifier.getId().equals(UUID.fromString(uuid))&&modifier.getOperation()==AttributeModifier.Operation.ADDITION,"Original attribute identity");
        AttributeModifierManager manager=new AttributeModifierManager(AttributeModifierMap.builder().add(attribute,base).build());
        for(int amp:new int[]{0,1,2}){
            effect.addAttributeModifiers(null,manager,amp);
            check(manager.getValue(attribute)==Math.max(0,base+change*(amp+1)),"Amplifier replaces rather than stacks modifier");
            effect.removeAttributeModifiers(null,manager,amp);
            check(manager.getValue(attribute)==base,"Removal restores original value");
        }
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        attribute(DefensiveBrewEffects.bulk(),Attributes.ARMOR,10,2,"e02d546b-c672-4ae6-a241-38be9bb20bb5");
        attribute(DefensiveBrewEffects.paperSkin(),Attributes.ARMOR_TOUGHNESS,5,-1,"a2cdb3f3-8277-45a0-8a3c-2dcc53a59b61");
        for(int amp:new int[]{0,1,2,255,Integer.MAX_VALUE}){
            check(DefensiveBrewEffects.reduced(24,amp)>0&&DefensiveBrewEffects.reduced(24,amp)<=12,"Finite decreasing damage");
            check(Float.isFinite(DefensiveBrewEffects.reflected(amp))&&DefensiveBrewEffects.reflected(amp)>0,"Reflection arithmetic cannot wrap");
            check(Float.isFinite(DefensiveBrewEffects.holyDamage(amp))&&DefensiveBrewEffects.holyDamage(amp)>0,"Holy damage arithmetic cannot wrap");
        }
        check(DefensiveBrewEffects.reduced(24,0)==12&&DefensiveBrewEffects.reduced(24,1)==8&&DefensiveBrewEffects.reduced(24,2)==6,"Legacy damage divisors");
        check(DefensiveBrewEffects.reduced(MagicWeaknessEffect.damage(9,0,true),0)==6,"Weakness before resistance");
        check(DefensiveBrewEffects.reflected(0)==3&&DefensiveBrewEffects.reflected(2)==9,"Shell damage per level");
        check(DefensiveBrewEffects.holyDamage(0)==8&&DefensiveBrewEffects.holyDamage(2)==24,"Holy damage per level");
        for(int flags=0;flags<8;flags++)for(float damage:new float[]{0,-1,1,Float.NaN,Float.POSITIVE_INFINITY}){
            boolean living=(flags&1)!=0,self=(flags&2)!=0,thorns=(flags&4)!=0;
            check(DefensiveBrewEffects.reflect(damage,living,self,thorns)==(flags==1&&damage==1),"Only positive attributed non-self non-thorns damage reflects");
        }
        for(int amp=-1;amp<5;amp++){
            int blocked=0;for(int roll=0;roll<3;roll++)if(DefensiveBrewEffects.sink(amp,roll))blocked++;
            check(blocked==Math.min(3,Math.max(0,amp+1)),"Exact one-third/two-thirds/all jump outcomes");
        }
        check(!DefensiveBrewEffects.sink(255,-1)&&!DefensiveBrewEffects.sink(255,3),"Invalid random draws rejected");
        for(int flags=0;flags<8;flags++)check(DefensiveBrewEffects.holyTarget((flags&1)!=0,(flags&2)!=0,(flags&4)!=0)==(flags!=0),"All holy susceptibility combinations");
        InstantBrewEffect holy=new InstantBrewEffect(true),reveal=new InstantBrewEffect(false);
        check(holy.isInstantenous()&&reveal.isInstantenous(),"Splash and cloud instant dispatch");
        check(holy.isDurationEffectTick(1,0)&&reveal.isDurationEffectTick(1,0)&&!holy.isDurationEffectTick(0,0),"Drinking tick dispatch and zero-duration boundary");
        check(holy.getCategory()==EffectType.BENEFICIAL&&holy.getColor()==0x7f7fff&&reveal.getCategory()==EffectType.HARMFUL&&reveal.getColor()==0x00c4ff,"Original instant metadata");
        check(DefensiveBrewEffects.bulk().getColor()==0x660000&&DefensiveBrewEffects.paperSkin().getColor()==0xE3DAC9,"Original attribute colors");
        check(DefensiveBrewEffects.magicResistance().isBeneficial()&&DefensiveBrewEffects.grace().isBeneficial()&&DefensiveBrewEffects.shellArmor().isBeneficial()&&!DefensiveBrewEffects.sinking().isBeneficial(),"Original effect categories");
        System.out.println("PASS: "+checks+" defensive brewing formulas, reflection guards, jump outcomes, attributes and instant dispatch checks");
    }
}
