package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

public final class DecorativeCarpet extends Block {
    public DecorativeCarpet() { super(Properties.copy(Blocks.WHITE_CARPET)); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return Block.box(0, 0, 0, 16, 1, 16); }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return !world.isEmptyBlock(pos.below()); }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos other) { return !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbor, world, pos, other); }
    @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 60; }
    @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 30; }
}
