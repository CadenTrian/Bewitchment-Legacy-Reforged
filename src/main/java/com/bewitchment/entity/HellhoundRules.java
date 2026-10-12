package com.bewitchment.entity;
public final class HellhoundRules {
    public static boolean prey(boolean special,boolean fireImmune,boolean demon,boolean undead,boolean protectedByClothes){
        return !protectedByClothes && (special || (!fireImmune && !demon && !undead));
    }
    public static int nextAttackTimer(int timer){return timer<=0?0:Math.min(5,timer)-1;}
    public static float jaw(int timer){return timer>0?.55F:0;}
    private HellhoundRules(){}
}
