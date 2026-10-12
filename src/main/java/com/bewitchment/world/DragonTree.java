package com.bewitchment.world;

import com.bewitchment.block.DragonsBloodLog;
import com.bewitchment.registry.*;
import net.minecraft.block.*;
import net.minecraft.block.trees.Tree;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.*;

/** Legacy spreading crown, with all required logs checked before changing the world. */
public final class DragonTree extends Feature<NoFeatureConfig> {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<NoFeatureConfig>> FEATURE = FEATURES.register("dragons_blood", DragonTree::new);
    private static ConfiguredFeature<?, ?> tree;
    public DragonTree() { super(NoFeatureConfig.CODEC); }
    public static void register() {
        tree = FEATURE.get().configured(NoFeatureConfig.INSTANCE);
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "dragons_blood"), tree);
        ComposterBlock.COMPOSTABLES.put(ModTreeBlocks.DRAGONS_BLOOD.sapling.get().asItem(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModTreeBlocks.DRAGONS_BLOOD.leaves.get().asItem(), 0.3F);
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModTreeBlocks.DRAGONS_BLOOD.sapling.getId(), ModTreeBlocks.POTTED_DRAGONS_BLOOD);
    }
    @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos origin, NoFeatureConfig config) {
        WoodFamily family = ModTreeBlocks.DRAGONS_BLOOD;
        BlockPos soil = origin.below();
        if (!world.getBlockState(soil).canSustainPlant(world, soil, Direction.UP, (IPlantable) family.sapling.get())) return false;
        int height = 3 + random.nextInt(2), branches = 3 + random.nextInt(2);
        Set<BlockPos> logs = new LinkedHashSet<>(), leaves = new LinkedHashSet<>();
        for (int y = 0; y < height; y++) logs.add(origin.above(y));
        if (random.nextDouble() < 0.7) for (Direction face : Direction.Plane.HORIZONTAL) logs.add(origin.relative(face));
        BlockPos top = origin.above(height);
        for (int y = 0; y < branches; y++) {
            logs.add(top.above(y));
            for (Direction face : Direction.Plane.HORIZONTAL) logs.add(top.relative(face, y + 1).above(y));
            for (int x : new int[]{-1, 1}) for (int z : new int[]{-1, 1}) logs.add(top.offset(x * y, y, z * y));
        }
        for (int y = 1; y <= branches; y++)
            for (int x = -y; x <= y; x++) for (int z = -y; z <= y; z++) leaves.add(top.offset(x, y, z));
        for (int x = 1 - branches; x < branches; x++) for (int z = 1 - branches; z < branches; z++) leaves.add(top.offset(x, branches + 1, z));
        leaves.removeAll(logs);
        for (BlockPos pos : logs) if (!available(world, pos) || !replaceable(world.getBlockState(pos))) return false;
        for (BlockPos pos : leaves) if (!available(world, pos)) return false;
        BlockState ground = world.getBlockState(soil);
        ground.getBlock().onPlantGrow(ground, world, soil, origin);
        for (BlockPos pos : logs) world.setBlock(pos, family.log.get().defaultBlockState().setValue(DragonsBloodLog.NATURAL, true), 3);
        for (BlockPos pos : leaves) if (replaceable(world.getBlockState(pos))) {
            int distance = 7;
            for (BlockPos log : logs) distance = Math.min(distance, Math.abs(pos.getX()-log.getX()) + Math.abs(pos.getY()-log.getY()) + Math.abs(pos.getZ()-log.getZ()));
            if (distance < 7) world.setBlock(pos, family.leaves.get().defaultBlockState().setValue(LeavesBlock.DISTANCE, distance), 3);
        }
        return true;
    }
    private static boolean available(ISeedReader world, BlockPos pos) { return pos.getY() >= 1 && pos.getY() < 256 && world.hasChunkAt(pos); }
    private static boolean replaceable(BlockState state) { return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS) || (state.getMaterial().isReplaceable() && state.getFluidState().isEmpty()); }
    public static final class Grower extends Tree {
        @Override protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean flowers) { return null; }
        @Override public boolean growTree(ServerWorld world, ChunkGenerator generator, BlockPos pos, BlockState state, Random random) { return tree != null && tree.place(world, generator, random, pos); }
    }
}
