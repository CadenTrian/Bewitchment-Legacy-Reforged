package com.bewitchment.entity;

public final class BafometyrRules {
    public static boolean fires(int tick,double distanceSquared) {return tick>=0 && tick%80<=5 && distanceSquared>4 && Double.isFinite(distanceSquared);}
    public static int flame(int tick) {return Math.floorMod(tick,8);}
    public static int nextAttackTimer(int timer) {return timer>0?timer-1:0;}
    public static double spread(double distanceSquared) {return Math.sqrt(Math.sqrt(Math.max(0,distanceSquared)))*.5;}
    public static boolean prey(boolean player,boolean rival,boolean fireImmune,boolean demon,boolean undead,boolean besmirched) {
        return !besmirched && (player || rival || (!fireImmune && !demon && !undead));
    }
    private BafometyrRules() {}
}
