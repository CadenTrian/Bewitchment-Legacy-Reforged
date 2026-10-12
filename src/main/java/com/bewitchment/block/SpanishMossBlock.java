package com.bewitchment.block;

import com.bewitchment.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.Random;

public final class SpanishMossBlock extends VineBlock {
    private final boolean terminal;
    public SpanishMossBlock(boolean terminal) { super(Properties.copy(Blocks.VINE)); this.terminal = terminal; }
    private BlockState supported(BlockState state, IBlockReader world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        if (state.getValue(UP) && !isAcceptableNeighbour(world, pos.above(), Direction.UP)) state = state.setValue(UP, false);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty face = getPropertyForFace(direction);
            if (state.getValue(face) && !isAcceptableNeighbour(world, pos.relative(direction), direction)
                    && !(above.getBlock() instanceof SpanishMossBlock && above.getValue(face))) state = state.setValue(face, false);
        }
        return state;
    }
    private static boolean hasFaces(BlockState state) {
        for (BooleanProperty face : PROPERTY_BY_DIRECTION.values()) if (state.getValue(face)) return true;
        return false;
    }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return hasFaces(supported(state, world, pos)); }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState other, IWorld world, BlockPos pos, BlockPos neighbor) {
        BlockState result = supported(state, world, pos); return hasFaces(result) ? result : Blocks.AIR.defaultBlockState();
    }
    @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (terminal || !world.isAreaLoaded(pos, 4)) return;
        boolean emptyBelow = world.isEmptyBlock(pos.below());
        super.randomTick(state, world, pos, random);
        BlockState below = world.getBlockState(pos.below());
        if (emptyBelow && below.is(this) && random.nextInt(4) == 0) {
            BlockState end = ModBlocks.SPANISH_MOSS_END.get().defaultBlockState();
            for (BooleanProperty face : PROPERTY_BY_DIRECTION.values()) end = end.setValue(face, below.getValue(face));
            world.setBlock(pos.below(), end, 3);
        }
    }
    @Override public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) { return false; }
    @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 100; }
    @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 15; }
    @Override public ItemStack getCloneItemStack(IBlockReader world, BlockPos pos, BlockState state) { return new ItemStack(ModBlocks.SPANISH_MOSS.get()); }
}
