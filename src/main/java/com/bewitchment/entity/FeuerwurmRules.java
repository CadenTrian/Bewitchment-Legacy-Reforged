package com.bewitchment.entity;
import java.util.UUID;
public final class FeuerwurmRules {
    public static int skin(int value){return value>=0 && value<9?value:0;}
    public static UUID summoner(String value){try{return UUID.fromString(value);}catch(IllegalArgumentException ex){return null;}}
    public static boolean lavaHeals(int tick,boolean wet,boolean lava){return tick>=0 && !wet && lava && tick%20==0;}
    public static boolean prey(boolean animal,boolean hellhound,boolean fireImmune,boolean demon,boolean undead,boolean protectedByClothes){
        return !protectedByClothes && (animal || hellhound || (!fireImmune && !demon && !undead));
    }
    private FeuerwurmRules(){}
}
