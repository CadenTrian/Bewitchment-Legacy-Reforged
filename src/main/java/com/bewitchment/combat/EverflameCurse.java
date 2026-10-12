package com.bewitchment.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.*;

public final class EverflameCurse {
    public static final int RADIUS=2;
    public static boolean ignitionRoll(double roll){return roll>=0 && roll<.1;}
    public static boolean eligible(boolean loaded,boolean air,boolean flammable,boolean mayBuild,boolean mayInteract){return loaded && air && flammable && mayBuild && mayInteract;}
    public static List<BlockPos> candidates(BlockPos center,DoubleSupplier random,Predicate<BlockPos> eligible){
        List<BlockPos> result=new ArrayList<>();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-RADIUS,-RADIUS,-RADIUS),center.offset(RADIUS,RADIUS,RADIUS)))
            if(ignitionRoll(random.getAsDouble()) && eligible.test(pos))result.add(pos.immutable());
        return result;
    }
    private static boolean allowed(ServerWorld world,PlayerEntity player,BlockPos pos){
        if(!world.hasChunkAt(pos) || pos.getY()<=0 || pos.getY()>=world.getMaxBuildHeight())return false;
        return eligible(true,world.isEmptyBlock(pos),world.getBlockState(pos.below()).isFlammable(world,pos.below(),Direction.UP),
            player.abilities.mayBuild,world.mayInteract(player,pos) && player.mayUseItemAt(pos,Direction.DOWN,ItemStack.EMPTY));
    }
    public static void apply(PlayerEntity player){
        if(!(player.level instanceof ServerWorld) || !player.isAlive() || player.isSpectator())return;
        ServerWorld world=(ServerWorld)player.level;
        for(BlockPos pos:candidates(player.blockPosition(),()->player.getRandom().nextDouble(),p->allowed(world,player,p)))
            if(allowed(world,player,pos))world.setBlockAndUpdate(pos,Blocks.FIRE.defaultBlockState());
    }
    private EverflameCurse(){}
}
