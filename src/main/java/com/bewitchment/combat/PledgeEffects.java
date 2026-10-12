package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.entity.*;
import com.bewitchment.registry.*;
import com.bewitchment.world.DemonPledges;
import com.bewitchment.world.DemonPledges.Patron;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.Comparator;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class PledgeEffects {
    public static Patron protector(Class<?> type){
        if(BafometyrEntity.class.isAssignableFrom(type) || FeuerwurmEntity.class.isAssignableFrom(type))return Patron.BAPHOMET;
        if(CambionEntity.class.isAssignableFrom(type))return Patron.LEONARD;
        return null;
    }
    public static boolean token(Patron patron,ResourceLocation item){
        return patron!=null && new ResourceLocation("bewitchment",patron==Patron.BAPHOMET?"pentacle":"green_candle").equals(item);
    }
    public static boolean dressed(PlayerEntity player){
        return ModClothing.hasSet(player,"alchemist") || ModClothing.hasSet(player,"besmirched")
            || ModClothing.hasSet(player,"green_witch") || ModClothing.hasSet(player,"witches");
    }
    @SubscribeEvent public static void target(LivingSetAttackTargetEvent event){
        if(!(event.getEntityLiving().level instanceof ServerWorld) || !(event.getEntityLiving() instanceof MobEntity) || !(event.getTarget() instanceof PlayerEntity))return;
        MobEntity mob=(MobEntity)event.getEntityLiving();
        Patron patron=mob instanceof Pledgeable?((Pledgeable)mob).patron():protector(mob.getClass());
        if(DemonPledges.get((ServerWorld)mob.level).has(event.getTarget().getUUID(),patron))mob.setTarget(null);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void hurt(LivingHurtEvent event){
        if(event.isCanceled() || !(event.getEntityLiving().level instanceof ServerWorld) || !(event.getEntityLiving() instanceof Pledgeable) || !(event.getSource().getEntity() instanceof PlayerEntity))return;
        PlayerEntity player=(PlayerEntity)event.getSource().getEntity();Pledgeable demon=(Pledgeable)event.getEntityLiving();
        if(DemonPledges.get((ServerWorld)player.level).remove(player.getUUID(),demon.patron()))demon.pledgeChanged(player,false);
    }
    public static boolean eat(PlayerEntity player){
        if(!(player.level instanceof ServerWorld) || !player.isAlive() || player.isSpectator() || !dressed(player))return false;
        ServerWorld world=(ServerWorld)player.level;
        // Stable nearest choice replaces the original unspecified entity-list order.
        MobEntity demon=world.getEntitiesOfClass(MobEntity.class,new AxisAlignedBB(player.getX()-8,player.getY()-5,player.getZ()-8,player.getX()+8,player.getY()+5,player.getZ()+8),
                mob->mob.isAlive() && mob instanceof Pledgeable).stream()
                .min(Comparator.comparingDouble((MobEntity mob)->mob.distanceToSqr(player)).thenComparing(mob->mob.getUUID().toString())).orElse(null);
        if(demon==null)return false;Pledgeable pledgeable=(Pledgeable)demon;
        if(!token(pledgeable.patron(),player.getOffhandItem().getItem().getRegistryName()))return false;
        boolean changed=DemonPledges.get(world).add(player.getUUID(),pledgeable.patron());
        pledgeable.pledgeChanged(player,true);if(demon.getTarget()==player)demon.setTarget(null);
        world.playSound(null,player.blockPosition(),SoundEvents.EVOKER_CAST_SPELL,SoundCategory.PLAYERS,2.5F,1);
        return changed;
    }
    private PledgeEffects(){}
}