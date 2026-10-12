package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;

/** Legacy cup upgrade and filled-goblet redstone source. */
public final class GobletBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 6, 10);
    private final boolean filled;
    public GobletBlock(boolean filled) {
        super(Properties.copy(Blocks.IRON_BLOCK).strength(0.3F, 1).noOcclusion());
        this.filled = filled;
    }
    public boolean isFilled() { return filled; }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return Block.canSupportCenter(world, pos.below(), Direction.UP); }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, IWorld world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighbor, world, pos, neighborPos);
    }
    @Override public boolean isSignalSource(BlockState state) { return filled; }
    @Override public int getSignal(BlockState state, IBlockReader world, BlockPos pos, Direction side) { return filled ? 8 : 0; }
    @Override public void onPlace(BlockState state, World world, BlockPos pos, BlockState previous, boolean moving) {
        super.onPlace(state, world, pos, previous, moving);
        if (filled && !previous.is(this)) world.updateNeighborsAt(pos, this);
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        super.onRemove(state, world, pos, replacement, moving);
        if (filled && !replacement.is(this)) world.updateNeighborsAt(pos, this);
    }
}
