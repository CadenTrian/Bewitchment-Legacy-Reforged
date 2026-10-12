package com.bewitchment.machine;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

/** Legacy fluid contact, called only for a filled cauldron on the server. */
public final class CauldronContact {
    interface Target {
        void extinguish();
        void hurt(DamageSource source,float amount);
        void ignite(int seconds);
    }
    static void apply(boolean lava,int heat,Target target){
        if(lava){target.hurt(DamageSource.LAVA,4);target.ignite(15);}
        else{target.extinguish();if(heat>=5)target.hurt(DamageSource.HOT_FLOOR,1);}
    }
    public static void apply(boolean lava,int heat,LivingEntity living){
        if(living.level.isClientSide)return;
        apply(lava,heat,new Target(){
            public void extinguish(){living.clearFire();}
            public void hurt(DamageSource source,float amount){living.hurt(source,amount);}
            public void ignite(int seconds){living.setSecondsOnFire(seconds);}
        });
    }
    private CauldronContact(){}
}
