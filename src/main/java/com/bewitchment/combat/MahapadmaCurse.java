package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.Predicate;

public final class MahapadmaCurse {
    public static boolean consume(CurseData data,boolean alive,boolean spectator){return alive && !spectator && data.remove(CurseData.Kind.MAHAPADMA);}
    public static List<BlockPos> candidates(BlockPos center,Predicate<BlockPos> allowed){
        List<BlockPos> result=new ArrayList<>();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-1,-2,-1),center.offset(1,2,1)))if(allowed.test(pos))result.add(pos.immutable());
        return result;
    }
    private static boolean allowed(ServerWorld world,PlayerEntity player,BlockPos pos){
        return pos.getY()>=0 && pos.getY()<world.getMaxBuildHeight() && world.hasChunkAt(pos)
            && world.getBlockState(pos).getMaterial().isReplaceable() && player.abilities.mayBuild
            && world.mayInteract(player,pos) && player.mayUseItemAt(pos,Direction.DOWN,ItemStack.EMPTY);
    }
    public static void apply(PlayerEntity player){
        if(!(player.level instanceof ServerWorld) || !player.isAlive() || player.isSpectator())return;
        ServerWorld world=(ServerWorld)player.level;
        for(BlockPos pos:candidates(player.blockPosition(),p->allowed(world,player,p)))if(allowed(world,player,pos))
            world.setBlockAndUpdate(pos,(player.getRandom().nextBoolean()?Blocks.PACKED_ICE:Blocks.ICE).defaultBlockState());
        player.hurt(DamageSource.MAGIC,(float)Integer.MAX_VALUE);
    }
    private MahapadmaCurse(){}
}