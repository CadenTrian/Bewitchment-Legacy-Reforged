package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Legacy chicken-death activation, restricted to loaded glyphs and normal casting checks. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class ImpConjurationTrigger {
    static boolean nearby(double distanceSquared){return distanceSquared>=0 && distanceSquared<100;}
    static Iterable<BlockPos> positions(BlockPos center){return BlockPos.betweenClosed(center.offset(-2,-2,-2),center.offset(2,2,2));}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event){
        if(event.isCanceled() || !(event.getEntityLiving() instanceof ChickenEntity) || !(event.getEntityLiving().level instanceof ServerWorld))return;
        ChickenEntity chicken=(ChickenEntity)event.getEntityLiving();ServerWorld world=(ServerWorld)chicken.level;
        PlayerEntity player=world.players().stream().filter(p->p.isAlive() && !p.isSpectator() && nearby(p.distanceToSqr(chicken)))
                .min(java.util.Comparator.comparingDouble(p->p.distanceToSqr(chicken))).orElse(null);
        if(player==null)return;
        BlockPos center=chicken.blockPosition();
        for(BlockPos pos:positions(center)){
            if(!world.hasChunkAt(pos))continue;
            TileEntity tile=world.getBlockEntity(pos);
            if(tile instanceof GlyphTile)((GlyphTile)tile).onChickenDeath(player);
        }
    }
    private ImpConjurationTrigger(){}
}
