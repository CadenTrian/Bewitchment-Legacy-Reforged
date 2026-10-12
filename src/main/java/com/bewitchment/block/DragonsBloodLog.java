package com.bewitchment.block;

import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.Random;

public final class DragonsBloodLog extends RotatedPillarBlock {
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
    public static final BooleanProperty SLASHED = BooleanProperty.create("slashed");
    public DragonsBloodLog() {
        super(Properties.copy(Blocks.OAK_LOG).randomTicks());
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(NATURAL, false).setValue(SLASHED, false));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(NATURAL, SLASHED);
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack tool = player.getItemInHand(hand);
        boolean eligible = !player.isSpectator() && player.abilities.mayBuild && world.mayInteract(player, pos)
                && player.mayUseItemAt(pos, hit.getDirection(), tool) && tool.getItem() == ModItems.BOLINE.get()
                && state.getValue(NATURAL) && !state.getValue(SLASHED);
        return BlockInteractionCommit.apply(eligible, world.isClientSide,
                () -> world.setBlock(pos, state.setValue(SLASHED, true), 3), () -> {
                    tool.hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(hand));
                    world.playSound(null, pos, SoundEvents.WOOD_HIT, SoundCategory.BLOCKS, 1, 1);
                });
    }    @Override public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.getValue(NATURAL) && state.getValue(SLASHED) && random.nextInt(100) <= 7) {
            BlockPos drop = pos.relative(Direction.from2DDataValue(random.nextInt(4)));
            if (world.hasChunkAt(drop)) Block.popResource(world, drop, new ItemStack(ModItems.DRAGONS_BLOOD_RESIN.get()));
        }
    }
    @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 5; }
    @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return 5; }
}
