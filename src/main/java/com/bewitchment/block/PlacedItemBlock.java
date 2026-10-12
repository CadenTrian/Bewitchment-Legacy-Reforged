package com.bewitchment.block;

import com.bewitchment.machine.PlacedItemTile;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

public final class PlacedItemBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public PlacedItemBlock() {
        super(Properties.copy(Blocks.STONE).strength(0).noCollission().noOcclusion().noDrops());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new PlacedItemTile(); }
    @Override public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.ENTITYBLOCK_ANIMATED; }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return Block.box(4, 0, 4, 12, 1, 12); }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP); }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos other) {
        return face == Direction.DOWN && !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbor, world, pos, other);
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.getItemInHand(hand).isEmpty()) return ActionResultType.PASS;
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof PlacedItemTile) {
            ItemStack stack = ((PlacedItemTile) world.getBlockEntity(pos)).takeItem();
            world.removeBlock(pos, false);
            if (!player.inventory.add(stack)) player.drop(stack, false);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !world.isClientSide && world.getBlockEntity(pos) instanceof PlacedItemTile)
            Block.popResource(world, pos, ((PlacedItemTile) world.getBlockEntity(pos)).takeItem());
        super.onRemove(state, world, pos, replacement, moving);
    }
    @Override public ItemStack getCloneItemStack(IBlockReader world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof PlacedItemTile ? ((PlacedItemTile) world.getBlockEntity(pos)).getItem() : ItemStack.EMPTY;
    }
}
