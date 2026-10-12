package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.potion.DefensiveBrewEffects;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class DefensiveBrewEvents {
    // Magic Weakness runs at HIGH; resistance divides the resulting damage at NORMAL.
    @SubscribeEvent public static void reduce(LivingDamageEvent event){
        LivingEntity target=event.getEntityLiving();
        if(event.isCanceled()||target.level.isClientSide||!Float.isFinite(event.getAmount())||event.getAmount()<=0)return;
        EffectInstance effect=target.getEffect(event.getSource().isMagic()?ModEffects.MAGIC_RESISTANCE.get():ModEffects.GRACE.get());
        if(effect!=null&&(event.getSource().isMagic()||event.getSource()==DamageSource.FALL))
            event.setAmount(DefensiveBrewEffects.reduced(event.getAmount(),effect.getAmplifier()));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void shell(LivingDamageEvent event){
        LivingEntity target=event.getEntityLiving();
        if(event.isCanceled()||target.level.isClientSide)return;
        EffectInstance effect=target.getEffect(ModEffects.SHELL_ARMOR.get());
        if(effect!=null&&DefensiveBrewEffects.reflect(event.getAmount(),event.getSource().getEntity() instanceof LivingEntity,
                event.getSource().getEntity()==target,"thorns".equals(event.getSource().getMsgId())))
            event.getSource().getEntity().hurt(DamageSource.thorns(target),DefensiveBrewEffects.reflected(effect.getAmplifier()));
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event){
        LivingEntity target=event.getEntityLiving();if(target.level.isClientSide)return;
        EffectInstance effect=target.getEffect(ModEffects.SINKING.get());
        if(effect!=null&&DefensiveBrewEffects.sink(effect.getAmplifier(),target.getRandom().nextInt(3))){
            Vector3d motion=target.getDeltaMovement();target.setDeltaMovement(motion.x,0,motion.z);target.hasImpulse=true;
            if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new SEntityVelocityPacket(target));
        }
    }
    private DefensiveBrewEvents(){}
}
