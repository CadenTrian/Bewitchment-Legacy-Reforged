package com.bewitchment.block;

import com.bewitchment.registry.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;

/** Legacy hellfire is persistent, but never spreads or burns surrounding blocks. */
public final class HellfireBlock extends Block {
    public HellfireBlock() {
        super(Properties.of(Material.FIRE).noCollission().noOcclusion().strength(0).sound(SoundType.WOOL).lightLevel(state -> 15).noDrops());
    }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isSolidRender(world, pos.below());
    }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos other) {
        return canSurvive(state, world, pos) ? state : Blocks.AIR.defaultBlockState();
    }
    @Override public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity) {
            entity.hurt(DamageSource.IN_FIRE, 2);
            entity.setSecondsOnFire(5);
            ((LivingEntity)entity).addEffect(new EffectInstance(ModEffects.HELLFIRE.get(), 200));
        }
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return BottledFireCollection.collect(world, pos, player, hand, hit, ModItems.BOTTLED_HELLFIRE);
    }

    @Override public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextBoolean()) world.addParticle(ParticleTypes.FLAME, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
    }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
}
