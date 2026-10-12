package com.bewitchment.combat;
import com.bewitchment.Bewitchment;
import com.bewitchment.potion.CombatBrewRules;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class CombatBrewEvents {
    @SubscribeEvent public static void attack(LivingAttackEvent event){
        LivingEntity target=event.getEntityLiving();
        if(event.isCanceled()||target.level.isClientSide||!(event.getSource().getDirectEntity() instanceof ProjectileEntity))return;
        EffectInstance effect=target.getEffect(ModEffects.DEFLECTION.get());if(effect==null)return;
        Entity projectile=event.getSource().getDirectEntity();
        projectile.setDeltaMovement(CombatBrewRules.deflect(projectile.getDeltaMovement(),effect.getAmplifier()));
        projectile.hasImpulse=true;projectile.hurtMarked=true;event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent event){
        LivingEntity target=event.getEntityLiving();
        if(event.isCanceled()||!(target.level instanceof ServerWorld)||!CombatBrewRules.trigger(event.getAmount(),event.getSource().isMagic(),event.getSource().isProjectile()))return;
        EffectInstance effect=target.getEffect(ModEffects.VOLATILITY.get());if(effect==null)return;
        ServerWorld world=(ServerWorld)target.level;float amount=CombatBrewRules.blast(effect.getAmplifier());
        CombatBrewRules.detonate(()->{
            List<LivingEntity> victims=world.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(3),LivingEntity::isAlive);
            // Clear the whole snapshot before publishing damage, preventing order-dependent chains.
            target.removeEffect(ModEffects.VOLATILITY.get());
            for(LivingEntity victim:victims)if(victim!=target)victim.removeEffect(ModEffects.VOLATILITY.get());
            world.playSound(null,target.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundCategory.NEUTRAL,1,1);
            world.sendParticles(ParticleTypes.EXPLOSION_EMITTER,target.getX(),target.getY(),target.getZ(),1,0,0,0,0);
            for(LivingEntity victim:victims)victim.hurt(DamageSource.explosion(target),amount);
        });
    }
    private CombatBrewEvents(){}
}
