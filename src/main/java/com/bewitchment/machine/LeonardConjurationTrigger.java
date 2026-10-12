package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Legacy sheep-death activation, restricted to loaded glyphs and normal casting checks. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class LeonardConjurationTrigger {
    static boolean nearby(double distanceSquared){return distanceSquared>=0 && distanceSquared<100;}
    static Iterable<BlockPos> positions(BlockPos center){return BlockPos.betweenClosed(center.offset(-2,-2,-2),center.offset(2,2,2));}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event){
        if(event.isCanceled() || !(event.getEntityLiving() instanceof SheepEntity) || !(event.getEntityLiving().level instanceof ServerWorld))return;
        SheepEntity sheep=(SheepEntity)event.getEntityLiving();ServerWorld world=(ServerWorld)sheep.level;
        PlayerEntity player=world.players().stream().filter(p->p.isAlive() && !p.isSpectator() && nearby(p.distanceToSqr(sheep)))
                .min(java.util.Comparator.comparingDouble(p->p.distanceToSqr(sheep))).orElse(null);
        if(player==null)return;
        BlockPos center=sheep.blockPosition();
        for(BlockPos pos:positions(center)){
            if(!world.hasChunkAt(pos))continue;
            TileEntity tile=world.getBlockEntity(pos);
            if(tile instanceof GlyphTile){((GlyphTile)tile).onSacrifice(player,RitualEffect.CONJURE_LEONARD);((GlyphTile)tile).onSacrifice(player,RitualEffect.CONJURE_BAPHOMET);}
        }
    }
    private LeonardConjurationTrigger(){}
}
