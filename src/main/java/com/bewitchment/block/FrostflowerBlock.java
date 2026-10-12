package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import java.util.Random;

public final class FrostflowerBlock extends BlueInkCapBlock {
    public FrostflowerBlock() { super(Properties.copy(Blocks.DANDELION).sound(SoundType.SNOW)); }
    @Override protected boolean mayPlaceOn(BlockState state, IBlockReader world, BlockPos pos) {
        return super.mayPlaceOn(state, world, pos) || state.getMaterial() == Material.TOP_SNOW || state.getMaterial() == Material.SNOW;
    }
    @Override public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextBoolean()) world.addParticle(ParticleTypes.ITEM_SNOWBALL, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
    }
}
