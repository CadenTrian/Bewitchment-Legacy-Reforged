package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.machine.CurseData.Kind;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Event-driven positive contract benefits; contract task items are a separate port. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class ContractEffects {
    public static float berserker(float amount,float health,float maximum){
        if(!AccessoryRules.validDamage(amount) || !Float.isFinite(health) || !Float.isFinite(maximum) || health<=0 || maximum<=0)return amount;
        return (float)Math.min(Float.MAX_VALUE,(double)amount/Math.min(1D,(double)health/maximum));
    }
    public static boolean salamandrineSource(DamageSource source){return source==DamageSource.IN_FIRE || source==DamageSource.ON_FIRE || source==DamageSource.LAVA;}
    public static float salamandrine(float amount,DamageSource source){return AccessoryRules.validDamage(amount) && salamandrineSource(source)?amount/3:amount;}
    public static EffectInstance regeneration(){return new EffectInstance(Effects.REGENERATION,20,9);}
    public static EffectInstance cropStrength(){return new EffectInstance(Effects.DAMAGE_BOOST,1200,2);}
    public static final float DEVOURING_HEAL=3;
    private static boolean active(PlayerEntity player,Kind kind){return !player.level.isClientSide && player.isAlive() && !player.isSpectator() && PlayerCurses.read(player).remaining(kind)>0;}
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void hurt(LivingHurtEvent event){
        if(event.isCanceled() || event.getEntityLiving().level.isClientSide || !AccessoryRules.validDamage(event.getAmount()))return;
        float amount=event.getAmount();
        if(event.getSource().getEntity() instanceof PlayerEntity){PlayerEntity attacker=(PlayerEntity)event.getSource().getEntity();if(active(attacker,Kind.BERSERKER))amount=berserker(amount,attacker.getHealth(),attacker.getMaxHealth());}
        if(event.getEntityLiving() instanceof PlayerEntity && active((PlayerEntity)event.getEntityLiving(),Kind.SALAMANDRINE_TOLERANCE))amount=salamandrine(amount,event.getSource());
        event.setAmount(amount);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void kill(LivingDeathEvent event){
        if(event.isCanceled() || event.getEntityLiving().level.isClientSide || !(event.getEntityLiving() instanceof AnimalEntity) || !(event.getSource().getEntity() instanceof PlayerEntity))return;
        PlayerEntity player=(PlayerEntity)event.getSource().getEntity();if(active(player,Kind.DEVOURING_RAGE)){player.heal(DEVOURING_HEAL);player.addEffect(regeneration());}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void harvest(BlockEvent.BreakEvent event){
        if(event.isCanceled() || !(event.getState().getBlock() instanceof CropsBlock))return;
        PlayerEntity player=event.getPlayer();if(player!=null && active(player,Kind.DESICCATION))player.addEffect(cropStrength());
    }
    private ContractEffects(){}
}
