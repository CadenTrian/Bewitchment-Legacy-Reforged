package com.bewitchment.entity;
import net.minecraft.entity.ai.attributes.*;
public final class LeonardRules {
    public static AttributeModifierMap.MutableAttribute attributes(AttributeModifierMap.MutableAttribute b){return b.add(Attributes.MAX_HEALTH,500).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,.5).add(Attributes.ARMOR,13.616).add(Attributes.ARMOR_TOUGHNESS,6.16).add(Attributes.KNOCKBACK_RESISTANCE,.9);}
    public static boolean pulse(int tick,int period){return tick>=0 && period>0 && tick%period<=5;}
    public static int timer(int value){return Math.max(0,Math.min(24000,value));}
    public static boolean stew(boolean pledged,boolean mainHand,boolean bowl,int levels,int timer){return pledged && mainHand && bowl && levels>=2 && timer==0;}
    private LeonardRules(){}
}