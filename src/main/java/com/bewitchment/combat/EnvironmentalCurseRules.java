package com.bewitchment.combat;

import com.bewitchment.machine.CurseData.Kind;

public final class EnvironmentalCurseRules {
    public static boolean proc(Kind kind,double roll){return roll>=0 && roll<kind.chance;}
    public static boolean sunlight(boolean skylight,int light,int darkening){return skylight && light-darkening==15;}
    public static boolean conflagration(int ticks){return ticks%20==0;}
    public static float wetDamage(float damage,boolean wet,boolean cursed){
        return wet && cursed && AccessoryRules.validDamage(damage)?(float)Math.min(Float.MAX_VALUE,(double)damage*1.5):damage;
    }
    private EnvironmentalCurseRules(){}
}
