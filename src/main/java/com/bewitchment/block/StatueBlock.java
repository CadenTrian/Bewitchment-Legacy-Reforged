package com.bewitchment.block;

import com.bewitchment.machine.StatueTile;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import javax.annotation.Nullable;

/** Each occupied cell belongs to this statue; only the bottom cell renders it. */
public final class StatueBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
    public final String deity, material;
    public final boolean idol;
    public final int height;

    public StatueBlock(Properties properties, String deity, String material, boolean idol, int height) {
        super(properties.noOcclusion());
        if (height < 1 || height > 4) throw new IllegalArgumentException("Statue height must be between one and four");
        this.deity = deity; this.material = material; this.idol = idol; this.height = height;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0));
    }

    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING, PART); }

    @Nullable @Override public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos origin = context.getClickedPos();
        for (int i = 0; i < height; i++) {
            BlockPos pos = origin.above(i);
            if (World.isOutsideBuildHeight(pos) || !world.getWorldBorder().isWithinBounds(pos)
                    || !world.hasChunkAt(pos)
                    || !world.getBlockState(pos).canBeReplaced(context)
                    || !world.isUnobstructed(null, defaultBlockState().getCollisionShape(world, pos).move(pos.getX(), pos.getY(), pos.getZ()))
                    || (context.getPlayer() != null && (!world.mayInteract(context.getPlayer(), pos)
                    || !context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())))) return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        for (int i = 1; i < height; i++) world.setBlock(pos.above(i), state.setValue(PART, i), 3);
    }

    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (replacement.getBlock() != this && state.getValue(PART) < height) {
            BlockPos origin = pos.below(state.getValue(PART));
            // Removing any piece removes its siblings without harvesting them. The originally
            // broken cell follows vanilla loot, tool, creative-mode and explosion handling.
            for (int i = 0; i < height; i++) {
                BlockPos sibling = origin.above(i);
                if (sibling.equals(pos)) continue;
                BlockState other = world.getBlockState(sibling);
                if (isMatchingPart(state, other, i))
                    world.setBlock(sibling, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, world, pos, replacement, moving);
    }

    /** A neighboring statue or malformed part is never owned by this column. */
    public boolean isMatchingPart(BlockState origin, BlockState candidate, int part) {
        return part >= 0 && part < height && candidate.is(this)
                && candidate.getValue(PART) == part && candidate.getValue(FACING) == origin.getValue(FACING);
    }

    @Override public boolean hasTileEntity(BlockState state) { return state.getValue(PART) == 0; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new StatueTile(); }
    @Override public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) { return state.getValue(PART) == 0 ? (idol ? 1.5F : 3.5F) : 0; }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
}
