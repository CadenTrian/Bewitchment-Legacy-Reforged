package com.bewitchment.machine;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;

/** Single-creature conjurations using already registered types. */
public final class RitualConjuration {
    static boolean fits(AxisAlignedBB box, double minX, double maxX, double minZ, double maxZ) {
        return box.minY >= 0 && box.maxY <= 256 && box.minX >= minX && box.maxX <= maxX && box.minZ >= minZ && box.maxZ <= maxZ;
    }
    static boolean demonVariant(java.util.Random random) { return random.nextBoolean(); }
    static boolean blessed(float roll) { return roll >= 0 && roll < .1F; }
    static EffectInstance blessing() { return new EffectInstance(Effects.REGENERATION, 6000, 2, false, false); }
    public static MobEntity prepare(RitualEffect effect, ServerWorld world, BlockPos pos) {
        if (!effect.conjuration() || world.getDifficulty() == Difficulty.PEACEFUL) return null;
        MobEntity entity = effect == RitualEffect.CONJURE_WITCH ? EntityType.WITCH.create(world)
                : effect == RitualEffect.CONJURE_BAPHOMET ? com.bewitchment.registry.ModCreatures.BAPHOMET.get().create(world)
                : effect == RitualEffect.CONJURE_LEONARD ? com.bewitchment.registry.ModCreatures.LEONARD.get().create(world)
                : effect == RitualEffect.CONJURE_DEMON ? (demonVariant(world.random) ? com.bewitchment.registry.ModCreatures.DEMON.get() : com.bewitchment.registry.ModCreatures.DEMONESS.get()).create(world)
                : effect == RitualEffect.CONJURE_IMP ? com.bewitchment.registry.ModCreatures.IMP.get().create(world) : EntityType.WITHER.create(world);
        if (entity == null) return null;
        entity.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        AxisAlignedBB box = entity.getBoundingBox();
        if (!fits(box, world.getWorldBorder().getMinX(), world.getWorldBorder().getMaxX(), world.getWorldBorder().getMinZ(), world.getWorldBorder().getMaxZ())
                || !world.hasChunksAt(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))
                || !world.noCollision(entity) || world.containsAnyLiquid(box)) return null;
        return entity;
    }
    public static boolean spawn(RitualEffect effect, ServerWorld world, BlockPos pos, MobEntity entity) {
        entity.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, world.random.nextInt(360), 0);
        entity.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), SpawnReason.MOB_SUMMONED, null, null);
        if (entity instanceof WitherEntity) ((WitherEntity)entity).makeInvulnerable();
        else if (effect == RitualEffect.CONJURE_WITCH && blessed(world.random.nextFloat())) entity.addEffect(blessing());
        if (!world.addFreshEntity(entity)) return false;
        for (ServerPlayerEntity player : world.getEntitiesOfClass(ServerPlayerEntity.class, entity.getBoundingBox().inflate(50)))
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
        return true;
    }
    public static void particles(RitualEffect effect, ServerWorld world, BlockPos pos) {
        world.sendParticles(effect == RitualEffect.CONJURE_WITCH ? ParticleTypes.ENCHANT : ParticleTypes.FLAME,
                pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, effect == RitualEffect.CONJURE_WITCH ? 15 : 25, .5, .5, .5, .1);
    }
    private RitualConjuration() {}
}
