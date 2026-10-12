package com.bewitchment.combat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

public final class GreaterCurseEffects {
    public static final float LIGHTNING_DAMAGE=3;
    public static boolean lightning(boolean rainingAtPlayer,boolean wet){return rainingAtPlayer && wet;}
    public static EffectInstance[] pestilence(){return new EffectInstance[]{new EffectInstance(Effects.WITHER,600),new EffectInstance(Effects.POISON,600)};}
    public static void strike(PlayerEntity player){
        if(!(player.level instanceof ServerWorld) || !lightning(player.level.isRainingAt(player.blockPosition()),player.isInWaterOrRain()))return;
        ServerWorld world=(ServerWorld)player.level;
        LightningBoltEntity bolt=EntityType.LIGHTNING_BOLT.create(world);
        if(bolt!=null){bolt.moveTo(player.getX(),player.getY(),player.getZ());bolt.setVisualOnly(true);world.addFreshEntity(bolt);}
        // Legacy applies the direct hit independently of inserting the visual weather entity.
        player.hurt(DamageSource.LIGHTNING_BOLT,LIGHTNING_DAMAGE);
    }
    private GreaterCurseEffects(){}
}
