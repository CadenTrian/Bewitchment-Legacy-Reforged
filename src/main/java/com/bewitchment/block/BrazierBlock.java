package com.bewitchment.block;

import com.bewitchment.machine.BrazierTile;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.*;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

public final class BrazierBlock extends ContainerBlock {
    public static final BooleanProperty HANGING=BooleanProperty.create("hanging"),LIT=BooleanProperty.create("lit");
    public BrazierBlock(){super(Properties.of(Material.METAL).strength(5,30).sound(SoundType.STONE).noOcclusion());registerDefaultState(stateDefinition.any().setValue(HANGING,false).setValue(LIT,false));}
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){builder.add(HANGING,LIT);}
    @Override public BlockState getStateForPlacement(BlockItemUseContext context){return defaultBlockState().setValue(HANGING,context.getClickedFace()==Direction.DOWN);}
    @Override public VoxelShape getShape(BlockState state,IBlockReader world,BlockPos pos,ISelectionContext context){return Block.box(3,0,3,13,16,13);}
    @Override public BlockRenderType getRenderShape(BlockState state){return BlockRenderType.MODEL;}
    @Override public TileEntity newBlockEntity(IBlockReader world){return new BrazierTile();}
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
        if(player.isSpectator() || !player.abilities.mayBuild || !world.mayInteract(player,pos) || !player.mayUseItemAt(pos,hit.getDirection(),player.getItemInHand(hand)))return ActionResultType.FAIL;
        if(!world.isClientSide && player instanceof ServerPlayerEntity && world.getBlockEntity(pos) instanceof BrazierTile)((BrazierTile)world.getBlockEntity(pos)).use((ServerPlayerEntity)player,hand);
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state,World world,BlockPos pos,BlockState next,boolean moving){if(!state.is(next.getBlock())){if(world.getBlockEntity(pos) instanceof BrazierTile)((BrazierTile)world.getBlockEntity(pos)).dropContents();super.onRemove(state,world,pos,next,moving);}}
    @Override public void animateTick(BlockState state,World world,BlockPos pos,java.util.Random random){if(state.getValue(LIT) && random.nextDouble()<.33)for(int i=0;i<8;i++)world.addParticle(net.minecraft.particles.ParticleTypes.CLOUD,pos.getX()+.5+random.nextGaussian()/4,pos.getY()+1.2,pos.getZ()+.5+random.nextGaussian()/4,0,.01,0);}
}
