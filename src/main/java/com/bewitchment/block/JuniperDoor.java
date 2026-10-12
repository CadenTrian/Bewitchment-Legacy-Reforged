package com.bewitchment.block;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class JuniperDoor extends DoorBlock {
    public JuniperDoor(){super(Properties.copy(Blocks.OAK_DOOR).strength(3,Integer.MAX_VALUE));}
    @Override public BlockState getStateForPlacement(BlockItemUseContext context){BlockState state=super.getStateForPlacement(context);return state==null?null:state.setValue(OPEN,false).setValue(POWERED,false);}
    @Override public void setPlacedBy(World world,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){super.setPlacedBy(world,pos,state,placer,stack);JuniperLocks.placed(world,pos,placer);}
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){if(world.isClientSide)return ActionResultType.SUCCESS;return JuniperLocks.access(player,world,pos,true)?super.use(state,world,pos,player,hand,hit):ActionResultType.CONSUME;}
    @Override public float getDestroyProgress(BlockState state,PlayerEntity player,IBlockReader world,BlockPos pos){return JuniperLocks.access(player,world,pos,false)?super.getDestroyProgress(state,player,world,pos):0;}
    @Override public void neighborChanged(BlockState state,World world,BlockPos pos,Block block,BlockPos from,boolean moving){} // Vanilla updateShape still enforces halves and support.
    @Override public void setOpen(World world,BlockState state,BlockPos pos,boolean open){} // No AI bypass.
    @Override public PushReaction getPistonPushReaction(BlockState state){return PushReaction.BLOCK;}
}
