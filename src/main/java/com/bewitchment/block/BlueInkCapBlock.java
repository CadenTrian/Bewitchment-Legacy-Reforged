package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import java.util.Random;

public class BlueInkCapBlock extends BushBlock {
    public BlueInkCapBlock() { super(Properties.copy(Blocks.RED_MUSHROOM).sound(SoundType.SLIME_BLOCK).randomTicks()); }
    protected BlueInkCapBlock(Properties properties) { super(properties.randomTicks()); }
    @Override protected boolean mayPlaceOn(BlockState state, IBlockReader world, BlockPos pos) { return state.getMaterial() == Material.DIRT || super.mayPlaceOn(state, world, pos); }
    @Override public net.minecraft.util.math.shapes.VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos,
            net.minecraft.util.math.shapes.ISelectionContext context) {
        return Block.box(3.2, 0, 3.2, 12.8, 9.6, 12.8);
    }
    @Override public void randomTick(BlockState state, ServerWorld world, BlockPos origin, Random random) {
        BlockPos target = PlantSpread.choose(origin, random,
                pos -> pos.getY() >= 0 && pos.getY() < world.getMaxBuildHeight() && world.hasChunkAt(pos) && world.getWorldBorder().isWithinBounds(pos),
                pos -> world.getBlockState(pos).is(this),
                pos -> world.isEmptyBlock(pos) && defaultBlockState().canSurvive(world, pos));
        if (target != null) world.setBlock(target, defaultBlockState(), 3);
    }
}