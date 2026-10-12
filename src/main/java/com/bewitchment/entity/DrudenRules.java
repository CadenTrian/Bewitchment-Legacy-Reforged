package com.bewitchment.entity;

public final class DrudenRules {
    private DrudenRules(){}
    public static int waterHealing(int tick,boolean water,boolean wet){return tick>=0 && tick%20==0?(water?3:0)+(wet?3:0):0;}
    public static boolean recovery(int tick,boolean injured){return injured && tick>=0 && tick%200<=5;}
    public static float axeDamage(float amount,boolean directLivingAxe){return directLivingAxe?amount*3:amount;}
    public static boolean prey(boolean player,boolean explicit,boolean fireImmune,boolean demon,boolean undead,double distance,boolean protectedClothing){
        return !protectedClothing && ((player && distance<2) || explicit || (!fireImmune && !demon && undead));
    }
}
