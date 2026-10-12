package com.bewitchment.block;

import com.bewitchment.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import com.bewitchment.combat.MaterialEffects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

/** A non-conductive salt line; legacy weakness targets are supplied by an entity tag. */
public final class SaltBarrierBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final ITag.INamedTag<EntityType<?>> BLOCKED = EntityTypeTags.createOptional(new net.minecraft.util.ResourceLocation("bewitchment", "salt_barrier_blocked"));
    public static final BooleanProperty NORTH_UP = BooleanProperty.create("north_up");
    public static final BooleanProperty EAST_UP = BooleanProperty.create("east_up");
    public static final BooleanProperty SOUTH_UP = BooleanProperty.create("south_up");
    public static final BooleanProperty WEST_UP = BooleanProperty.create("west_up");
    public static BooleanProperty connection(Direction direction, boolean up) {
        switch (direction) {
            case NORTH: return up ? NORTH_UP : NORTH;
            case EAST: return up ? EAST_UP : EAST;
            case SOUTH: return up ? SOUTH_UP : SOUTH;
            case WEST: return up ? WEST_UP : WEST;
            default: throw new IllegalArgumentException("Horizontal direction required");
        }
    }
    public static boolean blocks(boolean explicit, boolean living, boolean silver, boolean werewolf, boolean cold, boolean slime) {
        return explicit || slime || living && (silver && !werewolf || cold);
    }
    public static boolean blocks(Entity entity) {
        return entity != null && blocks(BLOCKED.contains(entity.getType()), entity instanceof LivingEntity,
                MaterialEffects.SILVER_WEAK.contains(entity.getType()), PurifyingEarthBlock.WEREWOLVES.contains(entity.getType()),
                MaterialEffects.COLD_IRON_WEAK.contains(entity.getType()), entity instanceof SlimeEntity);
    }
    private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape WALL = Block.box(0, -256, 0, 16, 256, 16);

    public SaltBarrierBlock() {
        super(Properties.of(Material.DECORATION).strength(0).sound(SoundType.SAND).noOcclusion().dynamicShape());
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false).setValue(NORTH_UP, false).setValue(EAST_UP, false).setValue(SOUTH_UP, false).setValue(WEST_UP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, NORTH_UP, EAST_UP, SOUTH_UP, WEST_UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        IWorldReader world = context.getLevel();
        return connections(defaultBlockState(), world, pos);
    }

    /** Retain the original boolean connections in saved states; added UP flags default false. */
    public BlockState connections(BlockState state, IBlockReader world, BlockPos pos) {
        boolean headroom = !world.getBlockState(pos.above()).isRedstoneConductor(world, pos.above());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            BlockState neighbor = world.getBlockState(side);
            boolean rise = headroom && supports(world, side) && world.getBlockState(side.above()).is(this);
            boolean up = rise && neighbor.isFaceSturdy(world, side, direction.getOpposite());
            boolean connected = rise || neighbor.is(this) || !neighbor.isRedstoneConductor(world, side) && world.getBlockState(side.below()).is(this);
            state = state.setValue(connection(direction, false), connected).setValue(connection(direction, true), up);
        }
        return state;
    }

    private boolean supports(IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos).isFaceSturdy(world, pos, Direction.UP) || world.getBlockState(pos).is(Blocks.HOPPER);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos below = pos.below();
        return supports(world, below);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, world, pos)) return Blocks.AIR.defaultBlockState();
        return connections(state, world, pos);
    }

    @Override public void updateIndirectNeighbourShapes(BlockState state, IWorld world, BlockPos pos, int flags, int depth) {
        if (depth <= 0) return;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int dy : new int[]{-1, 1}) {
                BlockPos target = pos.relative(direction).offset(0, dy, 0);
                BlockState old = world.getBlockState(target);
                if (old.is(this)) {
                    BlockState next = old.canSurvive(world, target) ? connections(old, world, target) : Blocks.AIR.defaultBlockState();
                    if (next != old) Block.updateOrDestroy(old, next, world, target, flags, depth - 1);
                }
            }
        }
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState result = state;
        for (Direction direction : Direction.Plane.HORIZONTAL) for (boolean up : new boolean[]{false, true})
            result = result.setValue(connection(rotation.rotate(direction), up), state.getValue(connection(direction, up)));
        return result;
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState result = state;
        for (Direction direction : Direction.Plane.HORIZONTAL) for (boolean up : new boolean[]{false, true})
            result = result.setValue(connection(mirror.mirror(direction), up), state.getValue(connection(direction, up)));
        return result;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Entity entity = context instanceof EntitySelectionContext ? ((EntitySelectionContext) context).getEntity() : null;
        return blocks(entity) ? WALL : VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
        return PathNodeType.BLOCKED;
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader world, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.SALT.get());
    }
}
