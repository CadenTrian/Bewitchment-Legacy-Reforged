package com.bewitchment.entity;
public final class CleaverRules {
    public static int skin(int value){return value==1?1:0;}
    public static boolean enrages(int tick,float health,float max){return tick>=0 && tick%200<=5 && max>0 && health/max<.35;}
    public static boolean cripples(int roll){return roll>=0 && roll<3;}
    public static float damage(double attribute){return (float)(int)attribute;}
    private CleaverRules(){}
}
