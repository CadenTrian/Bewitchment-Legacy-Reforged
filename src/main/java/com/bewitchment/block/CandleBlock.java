package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import java.util.Random;

public final class CandleBlock extends Block {
    private static final VoxelShape CANDLE_SHAPE = Block.box(6.08, 0, 6.08, 9.92, 8, 9.92);
    private static final VoxelShape CANDELABRA_SHAPE = Block.box(1, 0, 1, 15, 18, 15);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private final boolean candelabra;
    public CandleBlock(boolean candelabra) {
        super((candelabra ? Properties.copy(Blocks.IRON_BLOCK).harvestTool(net.minecraftforge.common.ToolType.PICKAXE).harvestLevel(1)
                : Properties.copy(Blocks.WHITE_WOOL)).strength(1, 1).noOcclusion().lightLevel(state -> state.getValue(LIT) ? 9 : 0));
        this.candelabra = candelabra;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }
    public boolean isCandelabra() { return candelabra; }
    public int altarBonus() { return (int) (64 * (candelabra ? 1.225 : 1.125)); }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(LIT); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return candelabra ? CANDELABRA_SHAPE : CANDLE_SHAPE;
    }
    @Override public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) { return Block.canSupportCenter(world, pos.below(), Direction.UP); }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos other) {
        return face == Direction.DOWN && !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbor, world, pos, other);
    }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
    @Override public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) { return candelabra ? 1.5F : 0.5F; }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        boolean lit = state.getValue(LIT);
        ItemStack stack = player.getItemInHand(hand);
        boolean allowed = !player.isSpectator() && player.abilities.mayBuild && world.mayInteract(player, pos)
                && player.mayUseItemAt(pos, hit.getDirection(), stack);
        return CandleInteraction.use(lit, stack.getItem() == Items.FLINT_AND_STEEL, allowed, world.isClientSide,
                () -> world.setBlock(pos, state.setValue(LIT, !lit), 3),
                () -> stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand)),
                () -> world.playSound(null, pos, lit ? SoundEvents.FIRE_EXTINGUISH : SoundEvents.FLINTANDSTEEL_USE,
                        SoundCategory.BLOCKS, CandleInteraction.volume(lit), CandleInteraction.pitch(lit, world.random)));
    }    @Override public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) return;
        world.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + (candelabra ? 1.175 : 0.7), pos.getZ() + 0.5, 0, 0, 0);
        if (candelabra) for (Direction direction : Direction.Plane.HORIZONTAL)
            world.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5 + direction.getStepX() * 0.375, pos.getY() + 1.075, pos.getZ() + 0.5 + direction.getStepZ() * 0.375, 0, 0, 0);
    }
}
