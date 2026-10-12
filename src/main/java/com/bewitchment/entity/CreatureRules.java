package com.bewitchment.entity;

/** Shared safe variant and legacy behavior calculations, independent of rendering. */
public final class CreatureRules {
    public static int skin(int value){return value>=0 && value<4?value:0;}
    public static boolean lizardHeals(int tick){return tick>=0 && tick%200<=5;}
    public static boolean toadHeals(int tick,boolean wet){return tick>=0 && wet && tick%20==0;}
    public static float health(boolean tame){return tame?20:8;}
    public static float reducedDamage(float amount,boolean nonPlayerNonArrow){return nonPlayerNonArrow?(amount+1)/2:amount;}
    public static boolean tameRoll(int roll){return roll==0;}
    public static float hop(float limbSwing,float amount,boolean sitting){
        return sitting?0:(float)Math.abs(Math.sin(limbSwing*.8))*Math.min(1,Math.max(0,amount));
    }
    private CreatureRules(){}
}
