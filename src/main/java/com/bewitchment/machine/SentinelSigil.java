package com.bewitchment.machine;

import com.bewitchment.entity.GhostEntity;
import com.bewitchment.registry.ModCreatures;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import java.util.function.*;

/** Legacy hostile Ghost summons: one fresh entity per attempt, without chunk generation. */
public final class SentinelSigil {
    static int count(int roll){return Math.max(0,Math.min(2,roll))+1;}
    static <T> int spawn(int roll,Supplier<T> factory,Predicate<T> insert){
        int inserted=0;
        for(int i=0;i<count(roll);i++){
            T entity=factory.get();
            if(entity!=null && insert.test(entity))inserted++;
        }
        return inserted;
    }
    static boolean fits(AxisAlignedBB box,double minX,double maxX,double minZ,double maxZ){
        return Double.isFinite(box.minX) && Double.isFinite(box.maxX)
            && Double.isFinite(box.minY) && Double.isFinite(box.maxY)
            && Double.isFinite(box.minZ) && Double.isFinite(box.maxZ)
            && box.minY>=0 && box.maxY<=256 && box.minX>=minX && box.maxX<=maxX
            && box.minZ>=minZ && box.maxZ<=maxZ;
    }
    public static void apply(LivingEntity target){
        if(!(target instanceof PlayerEntity) || !(target.level instanceof ServerWorld)
            || !target.isAlive() || target.isSpectator())return;
        ServerWorld world=(ServerWorld)target.level;
        if(world.getDifficulty()==Difficulty.PEACEFUL)return;
        spawn(target.getRandom().nextInt(3),()->{
            GhostEntity ghost=ModCreatures.GHOST.get().create(world);
            if(ghost!=null)ghost.moveTo(target.getX()+target.getRandom().nextGaussian()*5,
                target.getY()+3,target.getZ()+target.getRandom().nextGaussian()*5,0,0);
            return ghost;
        },ghost->{
            AxisAlignedBB box=ghost.getBoundingBox();
            if(!fits(box,world.getWorldBorder().getMinX(),world.getWorldBorder().getMaxX(),
                world.getWorldBorder().getMinZ(),world.getWorldBorder().getMaxZ())
                || !world.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ)))return false;
            // Ghosts phase through terrain; no solid-block or fluid rejection is needed.
            ghost.finalizeSpawn(world,world.getCurrentDifficultyAt(ghost.blockPosition()),SpawnReason.MOB_SUMMONED,null,null);
            ghost.setTarget(target);
            return world.addFreshEntity(ghost);
        });
    }
    private SentinelSigil(){}
}
