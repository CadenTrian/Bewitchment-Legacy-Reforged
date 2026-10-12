package com.bewitchment.potion;
import net.minecraft.potion.*;
import net.minecraft.util.math.vector.Vector3d;

public final class CombatBrewRules {
    private static final ThreadLocal<Boolean> DETONATING=ThreadLocal.withInitial(()->false);
    public static Effect deflection(){return new Effect(EffectType.BENEFICIAL,0xc000ff) {};}
    public static Effect volatility(){return new Effect(EffectType.HARMFUL,0xff6000) {};}
    public static boolean trigger(float damage,boolean magic,boolean projectile){return Float.isFinite(damage)&&damage>0&&(magic||!projectile);}
    public static float blast(int amplifier){return 8F*UtilityBrewEffect.level(amplifier);}
    public static Vector3d deflect(Vector3d motion,int amplifier){return motion.scale(4D*Math.max(0,Math.min(255,amplifier)));}
    public static boolean owner(boolean player,boolean alive,boolean spectator,boolean sameWorld){return player&&alive&&!spectator&&sameWorld;}
    public static boolean friendship(boolean tameable,String className){return tameable&&!className.contains("Dragon");}
    /** All removal/damage callbacks share this guard, including cancelled effect removal. */
    public static boolean detonate(Runnable action){
        if(DETONATING.get())return false;
        DETONATING.set(true);
        try{action.run();return true;}finally{DETONATING.remove();}
    }
    private CombatBrewRules(){}
}
