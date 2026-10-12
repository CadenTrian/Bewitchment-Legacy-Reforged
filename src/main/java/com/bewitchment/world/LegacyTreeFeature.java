package com.bewitchment.world;

import com.bewitchment.registry.*;
import net.minecraft.block.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.IPlantable;
import java.util.*;

/** Legacy silhouettes, planned before placement so blocked trunks cannot damage builds. */
public final class LegacyTreeFeature extends Feature<NoFeatureConfig> {
    private final boolean cypress;
    public LegacyTreeFeature(boolean cypress) { super(NoFeatureConfig.CODEC); this.cypress = cypress; }
    @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos origin, NoFeatureConfig config) {
        WoodFamily family = cypress ? ModTreeBlocks.CYPRESS : ModTreeBlocks.JUNIPER;
        BlockPos soil = origin.below();
        if (!world.getBlockState(soil).canSustainPlant(world, soil, Direction.UP, (IPlantable) family.sapling.get())) return false;
        int height = cypress ? 5 + random.nextInt(9) : 2 + random.nextInt(3);
        Set<BlockPos> logs = new LinkedHashSet<>(), leaves = new LinkedHashSet<>();
        for (int y = 0; y < height; y++) logs.add(origin.above(y));
        if (cypress) {
            for (int y = -height + 2; y < 2; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                boolean cross = y <= -1, core = y > -1, full = y >= -height + 3 && y <= -height / 2;
                if ((core && z == 0 && x == 0) || full || (cross && (z == 0 || x == 0)))
                    leaves.add(origin.above(height).offset(x, y, z));
            }
        } else {
            Direction branch = Direction.from2DDataValue(random.nextInt(4));
            BlockPos fork = origin.above(height).relative(branch);
            List<BlockPos> tips = new ArrayList<>();
            tips.add(fork); tips.add(fork.relative(branch.getOpposite(), 2));
            for (int i = 0; i < height / 2; i++) tips.add(fork.above().relative(branch, i + 1));
            logs.addAll(tips);
            for (BlockPos tip : tips) for (Direction face : Direction.values()) {
                leaves.add(tip.relative(face));
                for (Direction other : Direction.values())
                    if (other != Direction.DOWN && random.nextDouble() < 0.8) leaves.add(tip.relative(face).relative(other));
            }
        }
        leaves.removeAll(logs);
        for (BlockPos pos : logs) if (!available(world, pos) || !replaceable(world.getBlockState(pos))) return false;
        // A player-grown tree must not load an adjacent chunk to finish its canopy.
        for (BlockPos pos : leaves) if (!available(world, pos)) return false;
        BlockState ground = world.getBlockState(soil);
        ground.getBlock().onPlantGrow(ground, world, soil, origin);
        for (BlockPos pos : logs) world.setBlock(pos, family.log.get().defaultBlockState(), 3);
        for (BlockPos pos : leaves) if (replaceable(world.getBlockState(pos))) {
            int distance = 7;
            for (BlockPos log : logs) distance = Math.min(distance, Math.abs(pos.getX() - log.getX()) + Math.abs(pos.getY() - log.getY()) + Math.abs(pos.getZ() - log.getZ()));
            if (distance < 7) world.setBlock(pos, family.leaves.get().defaultBlockState().setValue(LeavesBlock.DISTANCE, distance), 3);
        }
        return true;
    }
    private static boolean available(ISeedReader world, BlockPos pos) { return pos.getY() >= 1 && pos.getY() < 256 && world.hasChunkAt(pos); }
    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS)
                || (state.getMaterial().isReplaceable() && state.getFluidState().isEmpty());
    }
}
