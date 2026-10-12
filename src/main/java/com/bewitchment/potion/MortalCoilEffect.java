package com.bewitchment.potion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
/** Original lethal magic hit on the final effect tick, through normal damage handling. */
public final class MortalCoilEffect extends Effect {
    public MortalCoilEffect(){super(EffectType.HARMFUL,0x4B3621);}
    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration==1;}
    @Override public void applyEffectTick(LivingEntity entity,int amplifier){if(!entity.level.isClientSide)entity.hurt(DamageSource.MAGIC,Integer.MAX_VALUE);}
}