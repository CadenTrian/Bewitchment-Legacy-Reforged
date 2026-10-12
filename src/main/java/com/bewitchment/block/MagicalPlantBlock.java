package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import java.util.Random;

public final class MagicalPlantBlock extends BlueInkCapBlock {
    private final boolean torchwood;
    public MagicalPlantBlock(boolean torchwood) {
        super(Properties.copy(Blocks.GRASS).sound(torchwood ? SoundType.WOOD : SoundType.GRASS).lightLevel(state -> torchwood ? 10 : 0));
        this.torchwood = torchwood;
    }
    @Override protected boolean mayPlaceOn(BlockState state, IBlockReader world, BlockPos pos) {
        return super.mayPlaceOn(state, world, pos) || (torchwood && (state.getMaterial() == Material.STONE || state.getMaterial() == Material.SAND));
    }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return torchwood ? super.getShape(state, world, pos, context) : Block.box(1.6, 0, 1.6, 14.4, 14.4, 14.4); }
    @Override public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!torchwood && !world.isClientSide && entity instanceof LivingEntity) entity.setSecondsOnFire(10);
    }
    @Override public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!torchwood && random.nextBoolean()) world.addParticle(ParticleTypes.FLAME, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
    }
}
