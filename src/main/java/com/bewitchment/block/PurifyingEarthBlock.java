package com.bewitchment.block;

import com.bewitchment.combat.MaterialEffects;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PurifyingEarthBlock extends Block {
    public static final ITag.INamedTag<EntityType<?>> WEREWOLVES = EntityTypeTags.createOptional(new ResourceLocation("bewitchment", "werewolves"));
    public PurifyingEarthBlock() { super(Properties.copy(Blocks.DIRT)); }
    public static boolean shouldIgnite(boolean silverWeak, boolean werewolf, boolean coldWeak, boolean burning) {
        return silverWeak && !werewolf || coldWeak && !burning;
    }
    @Override public void stepOn(World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity && shouldIgnite(MaterialEffects.SILVER_WEAK.contains(entity.getType()),
                WEREWOLVES.contains(entity.getType()), MaterialEffects.COLD_IRON_WEAK.contains(entity.getType()), entity.isOnFire())) entity.setSecondsOnFire(25);
        super.stepOn(world, pos, entity);
    }
    @Override public void animateTick(BlockState state, World world, BlockPos pos, java.util.Random random) {
        for (int i = 0; i < 16; i++) if (random.nextBoolean()) world.addParticle(
                new net.minecraft.particles.RedstoneParticleData(1, 1, 1, 1),
                pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
    }
}
