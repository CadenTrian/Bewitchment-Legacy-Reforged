package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Legacy demon sacrifice activation, restricted to loaded glyphs and normal casting checks. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class DemonConjurationTrigger {
    static boolean nearby(double distanceSquared){return distanceSquared>=0 && distanceSquared<100;}
    static Iterable<BlockPos> positions(BlockPos center){return BlockPos.betweenClosed(center.offset(-2,-2,-2),center.offset(2,2,2));}
    static boolean eligible(Class<?> type){
        return VillagerEntity.class.isAssignableFrom(type) || WitchEntity.class.isAssignableFrom(type)
            || SpellcastingIllagerEntity.class.isAssignableFrom(type) || VindicatorEntity.class.isAssignableFrom(type)
            || PlayerEntity.class.isAssignableFrom(type);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event){
        if(event.isCanceled() || !eligible(event.getEntityLiving().getClass()) || !(event.getEntityLiving().level instanceof ServerWorld))return;
        LivingEntity victim=event.getEntityLiving();ServerWorld world=(ServerWorld)victim.level;
        PlayerEntity player=world.players().stream().filter(p->p.isAlive() && !p.isSpectator() && nearby(p.distanceToSqr(victim)))
                .min(java.util.Comparator.comparingDouble(p->p.distanceToSqr(victim))).orElse(null);
        if(player==null)return;
        BlockPos center=victim.blockPosition();
        for(BlockPos pos:positions(center)){
            if(!world.hasChunkAt(pos))continue;
            TileEntity tile=world.getBlockEntity(pos);
            if(tile instanceof GlyphTile)((GlyphTile)tile).onSacrifice(player,RitualEffect.CONJURE_DEMON);
        }
    }
    private DemonConjurationTrigger(){}
}
