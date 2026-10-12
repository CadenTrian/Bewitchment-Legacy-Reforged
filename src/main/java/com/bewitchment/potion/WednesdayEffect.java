package com.bewitchment.potion;

import com.bewitchment.machine.WednesdayRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.server.ServerWorld;

/** Cosmetic explosion followed by legacy removal; no explosion or block damage. */
public final class WednesdayEffect extends Effect {
    public WednesdayEffect(){super(EffectType.HARMFUL,0x00ff00);}
    @Override public boolean isDurationEffectTick(int duration,int amplifier){return WednesdayRules.expires(duration);}
    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level.isClientSide)return;
        ServerWorld world=(ServerWorld)entity.level;
        world.sendParticles(ParticleTypes.EXPLOSION_EMITTER,entity.getX(),entity.getY(),entity.getZ(),1,0,0,0,0);
        world.playSound(null,entity.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundCategory.NEUTRAL,1,1);
        if(entity instanceof PlayerEntity)entity.hurt(DamageSource.OUT_OF_WORLD,Integer.MAX_VALUE);
        else entity.remove();
    }
}
