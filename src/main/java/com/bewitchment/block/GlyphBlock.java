package com.bewitchment.block;

import com.bewitchment.machine.GlyphTile;
import com.bewitchment.registry.ModRituals;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

public final class GlyphBlock extends Block {
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 3);
    public static final IntegerProperty LETTER = IntegerProperty.create("letter", 0, 5);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public GlyphBlock() {
        super(Properties.of(Material.DECORATION).strength(5, 100).noCollission().noOcclusion().noDrops());
        registerDefaultState(stateDefinition.any().setValue(TYPE, 1).setValue(LETTER, 0).setValue(FACING, Direction.NORTH));
    }
    public static int letter(BlockPos pos) { return (int)(Math.abs((long)pos.getX() + (long)pos.getZ() * 2) % 6); }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(TYPE, LETTER, FACING); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return Block.box(0, 0, 0, 16, .04, 16); }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP); }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos from) {
        return face == Direction.DOWN && !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : state;
    }
    @Override public boolean canBeReplaced(BlockState state, BlockItemUseContext context) { return state.getValue(TYPE) != 0; }
    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
    @Override public boolean hasTileEntity(BlockState state) { return state.getValue(TYPE) == 0; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new GlyphTile(); }
    @Override public ItemStack getPickBlock(BlockState state, RayTraceResult hit, IBlockReader world, BlockPos pos, PlayerEntity player) {
        int type = state.getValue(TYPE);
        return new ItemStack(type == 0 ? ModRituals.FOCAL_CHALK.get() : type == 2 ? ModRituals.FIERY_CHALK.get() : type == 3 ? ModRituals.PHASING_CHALK.get() : ModRituals.RITUAL_CHALK.get());
    }
    @Override public void animateTick(BlockState state, World world, BlockPos pos, java.util.Random random) {
        int type = state.getValue(TYPE);
        if (type >= 2) world.addParticle(type == 2 ? net.minecraft.particles.ParticleTypes.FLAME : net.minecraft.particles.ParticleTypes.PORTAL,
                pos.getX() + .5 + random.nextGaussian() / 3, pos.getY() + .05, pos.getZ() + .5 + random.nextGaussian() / 3, 0, 0, 0);
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (state.getValue(TYPE) != 0 || hand != Hand.MAIN_HAND || !player.abilities.mayBuild || !world.mayInteract(player, pos)) return ActionResultType.PASS;
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof GlyphTile) ((GlyphTile)world.getBlockEntity(pos)).interact(player);
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) || next.getValue(TYPE) != state.getValue(TYPE)) {
            if (!world.isClientSide && world.getBlockEntity(pos) instanceof GlyphTile) ((GlyphTile)world.getBlockEntity(pos)).cancel();
            world.removeBlockEntity(pos);
        }
        super.onRemove(state, world, pos, next, moving);
    }
}
