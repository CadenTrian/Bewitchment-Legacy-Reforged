package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.block.BrazierBlock;
import com.bewitchment.machine.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class IncenseWakeEffects {
    @SubscribeEvent public static void wake(PlayerWakeUpEvent event){
        PlayerEntity player=event.getPlayer();if(player.level.isClientSide || !player.isAlive() || player.isSpectator())return;
        BlockPos center=player.blockPosition();List<IncenseData.Kind> sources=new ArrayList<>();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-2,-2,-2),center.offset(2,2,2))){
            if(!player.level.hasChunkAt(pos))continue;
            if(!(player.level.getBlockState(pos).getBlock() instanceof BrazierBlock) || !player.level.getBlockState(pos).getValue(BrazierBlock.LIT))continue;
            TileEntity tile=player.level.getBlockEntity(pos);if(tile instanceof BrazierTile){IncenseData.Kind kind=((BrazierTile)tile).incenseKind();if(kind!=null)sources.add(kind);}
        }
        IncenseData.wakingEffects(sources).forEach(player::addEffect);
    }
    private IncenseWakeEffects(){}
}
