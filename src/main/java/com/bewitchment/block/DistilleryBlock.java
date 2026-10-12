package com.bewitchment.block;

import com.bewitchment.machine.DistilleryTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

public final class DistilleryBlock extends Block {
    public static final BooleanProperty IN_USE = BooleanProperty.create("in_use");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0,0,4,16,15,12);
    public DistilleryBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK).strength(5,6).harvestTool(ToolType.PICKAXE)
                .harvestLevel(0).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(IN_USE, false));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING, IN_USE); }
    @Override public BlockState getStateForPlacement(BlockItemUseContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new DistilleryTile(); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return state.getValue(FACING).getAxis() == Direction.Axis.X ? Block.box(4,0,0,12,15,16) : SHAPE; }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof DistilleryTile)
            NetworkHooks.openGui((ServerPlayerEntity) player, (DistilleryTile) world.getBlockEntity(pos));
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            TileEntity tile = world.getBlockEntity(pos);
            if (!world.isClientSide && tile instanceof DistilleryTile) {
                DistilleryTile oven = (DistilleryTile) tile;
                for (int i = 0; i < 13; i++) InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), oven.inventory.getStackInSlot(i));
            }
            super.onRemove(state, world, pos, replacement, moving);
        }
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
}

