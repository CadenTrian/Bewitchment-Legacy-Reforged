package com.bewitchment.entity;
public final class ImpRules {
    public static int skin(int value){return value>=0 && value<7?value:0;}
    public static int nextAttackTimer(int timer){return timer<=0?0:Math.min(10,timer)-1;}
    public static boolean lavaHeals(int tick,boolean lava){return tick>=0 && lava && tick%20==0;}
    public static boolean prey(boolean player,boolean rival,double distanceSquared,boolean clothed){return !clothed && (player?distanceSquared<2:rival);}
    public static float arm(int ticks,float partial){
        float x=Math.max(0,Math.min(10,ticks))-Math.max(0,Math.min(1,partial));
        return -2+1.5F*(Math.abs(x%10-5)-2.5F)/2.5F;
    }
    private ImpRules(){}
}
