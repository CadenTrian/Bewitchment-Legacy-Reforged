package com.bewitchment.block;

import com.bewitchment.item.*;
import com.bewitchment.machine.PlacedSigilTile;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.registries.ForgeRegistries;

public final class PlacedSigilBlock extends Block {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final IntegerProperty VARIATION=IntegerProperty.create("variation",0,9);
    public PlacedSigilBlock(){super(Properties.of(Material.DECORATION).strength(.8F).sound(SoundType.WOOL).noCollission().noOcclusion().noDrops().lightLevel(state->4));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP).setValue(VARIATION,0));}
    public static int variation(BlockPos pos){return (int)(Math.abs((long)pos.getX()+pos.getY()+2L*pos.getZ())%10);}
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){builder.add(FACING,VARIATION);}
    @Override public BlockState getStateForPlacement(BlockItemUseContext context){return defaultBlockState().setValue(FACING,context.getClickedFace()).setValue(VARIATION,variation(context.getClickedPos()));}
    @Override public void onPlace(BlockState state,World world,BlockPos pos,BlockState old,boolean moving){if(!world.isClientSide && state.getValue(VARIATION)!=variation(pos))world.setBlock(pos,state.setValue(VARIATION,variation(pos)),2);}
    @Override public boolean hasTileEntity(BlockState state){return true;}
    @Override public TileEntity createTileEntity(BlockState state,IBlockReader world){return new PlacedSigilTile();}
    @Override public boolean canSurvive(BlockState state,IWorldReader world,BlockPos pos){Direction face=state.getValue(FACING);BlockPos support=pos.relative(face.getOpposite());return world.getBlockState(support).isFaceSturdy(world,support,face);}
    @Override public BlockState updateShape(BlockState state,Direction face,BlockState neighbor,IWorld world,BlockPos pos,BlockPos from){return face==state.getValue(FACING).getOpposite() && !canSurvive(state,world,pos)?Blocks.AIR.defaultBlockState():state;}
    @Override public PushReaction getPistonPushReaction(BlockState state){return PushReaction.DESTROY;}
    @Override public net.minecraft.pathfinding.PathNodeType getAiPathNodeType(BlockState state,IBlockReader world,BlockPos pos,net.minecraft.entity.MobEntity entity){return net.minecraft.pathfinding.PathNodeType.OPEN;}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public VoxelShape getShape(BlockState state,IBlockReader world,BlockPos pos,ISelectionContext context){
        switch(state.getValue(FACING)){
            case DOWN:return box(0,15.84,0,16,16,16);
            case EAST:return box(0,0,0,.16,16,16);
            case WEST:return box(15.84,0,0,16,16,16);
            case NORTH:return box(0,0,15.84,16,16,16);
            case SOUTH:return box(0,0,0,16,16,.16);
            default:return box(0,0,0,16,.16,16);
        }
    }
    @Override public void setPlacedBy(World world,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        if(!world.isClientSide && placer!=null && stack.getItem() instanceof ChestSigilItem && world.getBlockEntity(pos) instanceof PlacedSigilTile)
            ((PlacedSigilTile)world.getBlockEntity(pos)).install(((ChestSigilItem)stack.getItem()).kind,placer.getUUID());
    }
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
        ItemStack held=player.getItemInHand(hand);
        if(!(held.getItem() instanceof TaglockItem) || !TaglockItem.bound(held))return ActionResultType.PASS;
        if(!world.isClientSide && world.getBlockEntity(pos) instanceof PlacedSigilTile){
            PlacedSigilTile tile=(PlacedSigilTile)world.getBlockEntity(pos);boolean changed=tile.data.targets.toggle(held.getTag().getString("boundId"));
            if(changed)tile.setChanged();player.displayClientMessage(new TranslationTextComponent(changed?"message.bewitchment.chest_sigil_toggled":"message.bewitchment.chest_sigil_invalid"),true);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public ItemStack getPickBlock(BlockState state,RayTraceResult hit,IBlockReader world,BlockPos pos,PlayerEntity player){
        TileEntity tile=world.getBlockEntity(pos);if(!(tile instanceof PlacedSigilTile) || ((PlacedSigilTile)tile).kind()==null)return ItemStack.EMPTY;
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(((PlacedSigilTile)tile).kind().id())));
    }
    @Override public void animateTick(BlockState state,World world,BlockPos pos,java.util.Random random){
        Direction face=state.getValue(FACING);double x=pos.getX()+.5-face.getStepX()*.49,y=pos.getY()+.5-face.getStepY()*.49,z=pos.getZ()+.5-face.getStepZ()*.49;
        world.addParticle(RedstoneParticleData.REDSTONE,x,y,z,0,0,0);
    }
}
