package com.bewitchment.combat;

import com.bewitchment.machine.AnimalShowerRules;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.math.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;

/** Bounded loaded-column search and final elevated collision check. */
public final class AnimalShowerFortunes {
    public static void spawn(PlayerEntity player, boolean catsAndDogs) {
        if (!(player.level instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return;
        ServerWorld world = (ServerWorld)player.level;
        if (catsAndDogs && world.dimensionType().hasSkyLight() && !world.dimensionType().hasCeiling()) {
            world.setWeatherParameters(0, 2000, true, true);
            ((IServerWorldInfo)world.getLevelData()).setThunderTime(3000);
        }
        int count = AnimalShowerRules.count(world.random);
        for (int i = 0; i < count; i++) {
            EntityType<? extends MobEntity> type = catsAndDogs
                    ? (world.random.nextBoolean() ? EntityType.WOLF : EntityType.OCELOT)
                    : (world.random.nextBoolean() ? EntityType.PIG : EntityType.COW);
            MobEntity entity = type.create(world);
            if (entity == null) continue;
            boolean placed = false;
            for (int attempt = 0; attempt < 16; attempt++) {
                BlockPos pos = new BlockPos(player.getX(), MathHelper.clamp(player.getY(), 1, world.getMaxBuildHeight()-2), player.getZ())
                        .offset(AnimalShowerRules.offset(world.random), 0, AnimalShowerRules.offset(world.random));
                if (!world.hasChunkAt(pos)) continue;
                while (pos.getY() > 0 && !world.getBlockState(pos.below()).getMaterial().blocksMotion()) pos = pos.below();
                if (!FortuneEncounterSpace.fits(world, entity, pos)) continue;
                entity.moveTo(entity.getX(), entity.getY()+AnimalShowerRules.height(world.random), entity.getZ(), world.random.nextInt(360), 0);
                if (airFits(world, entity)) { placed = true; break; }
            }
            if (!placed) continue;
            entity.finalizeSpawn(world, world.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.EVENT, null, null);
            if (!airFits(world, entity)) continue;
            if (catsAndDogs) {
                entity.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 9));
                entity.addEffect(new EffectInstance(ModEffects.WEDNESDAY.get(), 60));
            } else entity.addEffect(new EffectInstance(Effects.HARM, Integer.MAX_VALUE, 9));
            world.addFreshEntity(entity);
        }
    }
    private static boolean airFits(ServerWorld world, MobEntity entity) {
        AxisAlignedBB box = entity.getBoundingBox();
        return box.minY >= 0 && box.maxY <= world.getMaxBuildHeight()
                && box.minX >= world.getWorldBorder().getMinX() && box.maxX <= world.getWorldBorder().getMaxX()
                && box.minZ >= world.getWorldBorder().getMinZ() && box.maxZ <= world.getWorldBorder().getMaxZ()
                && world.hasChunksAt(new BlockPos(box.minX-1, box.minY-1, box.minZ-1), new BlockPos(box.maxX+1, box.maxY+1, box.maxZ+1))
                && world.noCollision(entity) && !world.containsAnyLiquid(box);
    }
    private AnimalShowerFortunes() {}
}
