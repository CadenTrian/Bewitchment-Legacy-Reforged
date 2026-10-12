package com.bewitchment.block;

import com.bewitchment.machine.JuniperChestTile;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

/** Independent, keyed chest; adjacent chests never merge. */
public final class JuniperChestBlock extends ContainerBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE=Block.box(1,0,1,15,14,15);
    public JuniperChestBlock(){super(Properties.copy(Blocks.CHEST).strength(2.5F,Integer.MAX_VALUE));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockItemUseContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public TileEntity newBlockEntity(IBlockReader world){return new JuniperChestTile();}
    @Override public BlockRenderType getRenderShape(BlockState state){return BlockRenderType.ENTITYBLOCK_ANIMATED;}
    @Override public VoxelShape getShape(BlockState state,IBlockReader world,BlockPos pos,ISelectionContext context){return SHAPE;}
    @Override public PushReaction getPistonPushReaction(BlockState state){return PushReaction.BLOCK;}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public void setPlacedBy(World world,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        super.setPlacedBy(world,pos,state,placer,stack);
        if(stack.hasCustomHoverName() && world.getBlockEntity(pos) instanceof JuniperChestTile)((JuniperChestTile)world.getBlockEntity(pos)).setCustomName(stack.getHoverName());
        JuniperLocks.placed(world,pos,placer);
    }
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
        if(player.isShiftKeyDown() && !player.getItemInHand(hand).isEmpty())return ActionResultType.PASS;
        if(!world.isClientSide && world.getBlockEntity(pos) instanceof JuniperChestTile)player.openMenu((JuniperChestTile)world.getBlockEntity(pos));
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public float getDestroyProgress(BlockState state,PlayerEntity player,IBlockReader world,BlockPos pos){return JuniperLocks.access(player,world,pos,false)?super.getDestroyProgress(state,player,world,pos):0;}
    @Override public void onRemove(BlockState state,World world,BlockPos pos,BlockState replacement,boolean moving){
        if(!state.is(replacement.getBlock())){
            TileEntity tile=world.getBlockEntity(pos);
            if(!world.isClientSide && tile instanceof JuniperChestTile)InventoryHelper.dropContents(world,pos,(JuniperChestTile)tile);
            super.onRemove(state,world,pos,replacement,moving);
        }
    }
}
