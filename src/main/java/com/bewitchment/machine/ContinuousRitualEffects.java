package com.bewitchment.machine;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** Legacy continuous effects, invoked only while the caster, rings and payment remain valid. */
public final class ContinuousRitualEffects {
    public static long acceleratedTime(long time) { return time > Long.MAX_VALUE - 5 ? Long.MAX_VALUE : time + 5; }
    public static boolean perceptionDue(long gameTime) { return Math.floorMod(gameTime, 100L) == 0; }
    public static AxisAlignedBB perceptionBounds(BlockPos center) { return new AxisAlignedBB(center).inflate(20); }
    static EffectInstance revealed() { return new EffectInstance(Effects.GLOWING, 110, 0, false, false); }
    static void reveal(LivingEntity entity) {
        entity.removeEffect(Effects.INVISIBILITY);
        entity.addEffect(revealed());
    }
    public static void tick(RitualEffect effect, ServerWorld world, BlockPos center) {
        if (effect == RitualEffect.SANDS_OF_TIME) {
            world.setDayTime(acceleratedTime(world.getDayTime()));
        } else if (effect == RitualEffect.PERCEPTION && perceptionDue(world.getGameTime())) {
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, perceptionBounds(center))) reveal(entity);
        }
    }
    private ContinuousRitualEffects() {}
}
