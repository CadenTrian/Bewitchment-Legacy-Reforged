package com.bewitchment.machine;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

/** The two vanilla creatures in the legacy Lesser Hellmouth pool. */
public final class LesserHellmouth {
    static String failure(boolean peaceful, boolean raining) {
        return peaceful ? "hellmouth_peaceful" : raining ? "no_rain" : null;
    }
    static String failure(ServerWorld world) { return failure(world.getDifficulty() == Difficulty.PEACEFUL, world.isRaining()); }
    static int count(Random random) {
        // Legacy rerolls its loop bound each iteration; it is not a uniform 2..5 roll.
        int count = 0;
        while (count < random.nextInt(4) + 2) count++;
        return count;
    }
    static EntityType<?> type(boolean pig) { return pig ? EntityType.ZOMBIFIED_PIGLIN : EntityType.MAGMA_CUBE; }
    static int offset(Random random) { return random.nextInt(12) - 6; }
    static boolean overlaps(AxisAlignedBB box, List<MobEntity> planned) {
        for (MobEntity other : planned) if (box.intersects(other.getBoundingBox())) return true;
        return false;
    }
    static boolean fits(ServerWorld world, MobEntity entity, List<MobEntity> planned) {
        AxisAlignedBB box = entity.getBoundingBox();
        return RitualConjuration.fits(box, world.getWorldBorder().getMinX(), world.getWorldBorder().getMaxX(),
                world.getWorldBorder().getMinZ(), world.getWorldBorder().getMaxZ())
                && world.hasChunksAt(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))
                && !overlaps(box, planned) && world.noCollision(entity) && !world.containsAnyLiquid(box);
    }
    static List<MobEntity> prepare(ServerWorld world, BlockPos center) {
        List<MobEntity> planned = new ArrayList<>();
        if (failure(world) != null) return planned;
        int count = count(world.random);
        for (int i = 0; i < count; i++) {
            MobEntity entity = (MobEntity)type(world.random.nextBoolean()).create(world);
            if (entity == null) continue;
            entity.moveTo(center.getX() + .5, center.getY(), center.getZ() + .5, 0, 0);
            // Initialize before testing the body: Magma Cube size is chosen here.
            ILivingEntityData group = entity instanceof ZombieEntity
                    ? new ZombieEntity.GroupData(ZombieEntity.getSpawnAsBabyOdds(world.random), false) : null;
            entity.finalizeSpawn(world, world.getCurrentDifficultyAt(center), SpawnReason.MOB_SUMMONED, group, null);
            for (int attempt = 0; attempt < 16; attempt++) {
                BlockPos landing = center.offset(offset(world.random), 0, offset(world.random));
                if (!world.hasChunkAt(landing)) continue;
                while (landing.getY() > 0 && !world.getBlockState(landing.below()).getMaterial().blocksMotion()) landing = landing.below();
                if (landing.getY() <= 0) continue;
                entity.moveTo(landing.getX() + .5, landing.getY(), landing.getZ() + .5, world.random.nextInt(360), 0);
                if (fits(world, entity, planned)) { planned.add(entity); break; }
            }
        }
        return planned;
    }
    static int spawn(ServerWorld world, List<MobEntity> planned) {
        int spawned = 0;
        for (MobEntity entity : planned) {
            // Event handlers from earlier insertions may have changed the landing area.
            if (!fits(world, entity, Collections.emptyList()) || !world.addFreshEntity(entity)) continue;
            spawned++;
            for (ServerPlayerEntity player : world.getEntitiesOfClass(ServerPlayerEntity.class, entity.getBoundingBox().inflate(50)))
                CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
        }
        return spawned;
    }
    static void particles(ServerWorld world, BlockPos pos) {
        world.sendParticles(ParticleTypes.FLAME, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 5, .5, .5, .5, .1);
    }
    private LesserHellmouth() {}
}
