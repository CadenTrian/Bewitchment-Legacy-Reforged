package com.bewitchment.block;

import com.bewitchment.machine.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraftforge.common.ToolType;

public final class PoppetShelfBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public PoppetShelfBlock() {
        super(Properties.copy(Blocks.OAK_PLANKS).harvestTool(ToolType.AXE).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockItemUseContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new PoppetShelfTile(); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selection) {
        switch (state.getValue(FACING)) {
            case SOUTH: return Block.box(0, 0, 0, 16, 16, 4);
            case WEST: return Block.box(12, 0, 0, 16, 16, 16);
            case EAST: return Block.box(0, 0, 0, 4, 16, 16);
            default: return Block.box(0, 0, 12, 16, 16, 16);
        }
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (hand != Hand.MAIN_HAND) return ActionResultType.PASS;
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof PoppetShelfTile) {
            int slot = ShelfLayout.slot(state.getValue(FACING), hit.getLocation().x - pos.getX(), hit.getLocation().y - pos.getY(), hit.getLocation().z - pos.getZ());
            ((PoppetShelfTile)world.getBlockEntity(pos)).interact(player, slot);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            TileEntity tile = world.getBlockEntity(pos);
            if (!world.isClientSide && tile instanceof PoppetShelfTile) {
                ShelfInventory inventory = ((PoppetShelfTile)tile).inventory;
                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inventory.extractItem(slot, 64, false));
            }
            PoppetShelfNetwork.remove(world, pos);
            super.onRemove(state, world, pos, replacement, moving);
        }
    }
    @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 20; }
    @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 5; }
}
