package com.bewitchment.block;

import com.bewitchment.machine.SigilTableTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;

public final class SigilTableBlock extends ContainerBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public SigilTableBlock(){super(Properties.copy(Blocks.STONE).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockItemUseContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public BlockRenderType getRenderShape(BlockState state){return BlockRenderType.MODEL;}
    @Override public TileEntity newBlockEntity(IBlockReader world){return new SigilTableTile();}
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
        if(!world.isClientSide && !player.isSpectator() && world.getBlockEntity(pos) instanceof SigilTableTile)NetworkHooks.openGui((ServerPlayerEntity)player,(SigilTableTile)world.getBlockEntity(pos));
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state,World world,BlockPos pos,BlockState replacement,boolean moving){
        if(!state.is(replacement.getBlock())){
            TileEntity tile=world.getBlockEntity(pos);if(!world.isClientSide && tile instanceof SigilTableTile)InventoryHelper.dropContents(world,pos,((SigilTableTile)tile).matrix);
            super.onRemove(state,world,pos,replacement,moving);
        }
    }
}
