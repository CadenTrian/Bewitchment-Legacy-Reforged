package com.bewitchment.block;

import com.bewitchment.machine.AltarTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;

public final class WitchesAltarBlock extends Block {
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
    public WitchesAltarBlock() {
        this(Properties.copy(Blocks.STONE).strength(2, 6));
    }
    public WitchesAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, 0).setValue(COLOR, 0));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(TYPE, COLOR); }
    @Override public boolean hasTileEntity(BlockState state) { return state.getValue(TYPE) == 2; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new AltarTile(); }
    @Override public net.minecraft.block.material.PushReaction getPistonPushReaction(BlockState state) { return net.minecraft.block.material.PushReaction.BLOCK; }

    public static AltarTile core(World world, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 0, 2))) {
            if (!world.hasChunkAt(p)) continue;
            TileEntity tile = world.getBlockEntity(p);
            if (tile instanceof AltarTile && ((AltarTile) tile).contains(pos)) return (AltarTile) tile;
        }
        return null;
    }
    private boolean rectangle(World world, BlockPos origin, int width, int depth) {
        for (int x = -1; x <= width; x++) for (int z = -1; z <= depth; z++) {
            BlockPos p = origin.offset(x, 0, z);
            if (!world.hasChunkAt(p)) return false;
            BlockState s = world.getBlockState(p);
            boolean inside = x >= 0 && x < width && z >= 0 && z < depth;
            if (inside ? !s.is(this) || s.getValue(TYPE) != 0 : s.getBlock() instanceof WitchesAltarBlock) return false;
        }
        return true;
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        Block carpet = stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock() : Blocks.AIR;
        if (!stack.isEmpty() && !(carpet instanceof CarpetBlock)) return ActionResultType.PASS;
        if (world.isClientSide) return ActionResultType.SUCCESS;
        AltarTile tile = core(world, pos);
        if (tile != null) {
            if (carpet instanceof CarpetBlock && hit.getDirection() == Direction.UP && tile.recolor(stack)
                    && !player.abilities.instabuild) stack.shrink(1);
            if (stack.isEmpty()) player.displayClientMessage(new TranslationTextComponent("altar.bewitchment.power", tile.getPower(), tile.getCapacity(), tile.getRegeneration()), true);
            return ActionResultType.CONSUME;
        }
        if (carpet instanceof CarpetBlock && hit.getDirection() == Direction.UP) {
            for (int width : new int[]{3, 2}) {
                int depth = 5 - width;
                for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) {
                    BlockPos origin = pos.offset(-x, 0, -z);
                    if (!rectangle(world, origin, width, depth)) continue;
                    int color = ((CarpetBlock) carpet).getColor().getId() + 1;
                    BlockPos center = origin.offset(width / 2, 0, depth / 2);
                    for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1)))
                        world.setBlock(p, defaultBlockState().setValue(TYPE, p.equals(center) ? 2 : 1).setValue(COLOR, color), 3);
                    ((AltarTile) world.getBlockEntity(center)).initialize(origin, width, depth, stack);
                    if (!player.abilities.instabuild) stack.shrink(1);
                    return ActionResultType.CONSUME;
                }
            }
        }
        player.displayClientMessage(new TranslationTextComponent("altar.bewitchment.form"), true);
        return ActionResultType.CONSUME;
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !world.isClientSide) {
            AltarTile tile = core(world, pos);
            if (tile != null) tile.dismantle(pos);
        }
        super.onRemove(state, world, pos, replacement, moving);
    }
}
