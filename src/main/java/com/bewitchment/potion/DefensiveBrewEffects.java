package com.bewitchment.potion;

import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.*;

/** Legacy colors and attribute UUIDs; vanilla handles modifier scaling and removal. */
public final class DefensiveBrewEffects {
    public static Effect magicResistance(){return new Effect(EffectType.BENEFICIAL,0xc6c6c6) {};}
    public static Effect grace(){return new Effect(EffectType.BENEFICIAL,0xc6c6c6) {};}
    public static Effect shellArmor(){return new Effect(EffectType.BENEFICIAL,0x00c040) {};}
    public static Effect sinking(){return new Effect(EffectType.HARMFUL,0x4000c0) {};}
    public static Effect bulk(){return new Effect(EffectType.BENEFICIAL,0x660000) {}
            .addAttributeModifier(Attributes.ARMOR,"e02d546b-c672-4ae6-a241-38be9bb20bb5",2,AttributeModifier.Operation.ADDITION);}
    public static Effect paperSkin(){return new Effect(EffectType.HARMFUL,0xE3DAC9) {}
            .addAttributeModifier(Attributes.ARMOR_TOUGHNESS,"a2cdb3f3-8277-45a0-8a3c-2dcc53a59b61",-1,AttributeModifier.Operation.ADDITION);}
    public static float reduced(float damage,int amplifier){return damage/(Math.max(0,amplifier)+2F);}
    public static float reflected(int amplifier){return 3F*(Math.max(0,amplifier)+1F);}
    public static boolean reflect(float damage,boolean livingAttacker,boolean self,boolean thorns){return Float.isFinite(damage)&&damage>0&&livingAttacker&&!self&&!thorns;}
    public static boolean sink(int amplifier,int roll){return roll>=0&&roll<3&&roll<=amplifier;}
    public static boolean holyTarget(boolean undead,boolean fireImmune,boolean coldIronWeak){return undead||fireImmune||coldIronWeak;}
    public static float holyDamage(int amplifier){return 8F*(Math.max(0,amplifier)+1F);}
    private DefensiveBrewEffects(){}
}
