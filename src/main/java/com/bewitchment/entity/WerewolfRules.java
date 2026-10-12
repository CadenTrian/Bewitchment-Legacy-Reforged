package com.bewitchment.entity;

public final class WerewolfRules {
    private WerewolfRules(){}
    public static int skin(int value){return value>=0 && value<6?value:0;}
    public static int variant(boolean snowy,boolean coniferous,boolean swamp,boolean coin){return snowy?(coin?1:5):coniferous?(coin?3:2):swamp?(coin?3:0):(coin?4:0);}
    public static boolean canSpawn(boolean daytime,int moonPhase){return !daytime && moonPhase==0;}
    public static boolean vanishes(boolean daytime,boolean named){return daytime && !named;}
    public static boolean heals(int tick,boolean injured){return injured && tick>=0 && tick%200<=5;}
    /** Preserve the integer cast used by the original attack implementation. */
    public static float damage(double attribute){return (float)(int)attribute;}
    public static String texture(int variant){return new String[]{"brown","arctic","black","red","timber","grey"}[skin(variant)];}
}
