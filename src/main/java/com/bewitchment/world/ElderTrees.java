package com.bewitchment.world;

import com.bewitchment.registry.ModBlocks;
import com.bewitchment.config.WorldGenerationConfig;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import java.util.Random;

public final class ElderTrees extends Tree {
    private static ConfiguredFeature<BaseTreeFeatureConfig, ?> tree;
    private static ConfiguredFeature<?, ?> forestTree;
    public static void register() {
        net.minecraft.block.ComposterBlock.COMPOSTABLES.put(ModBlocks.ELDER_SAPLING.get().asItem(), 0.3F);
        net.minecraft.block.ComposterBlock.COMPOSTABLES.put(ModBlocks.ELDER_LEAVES.get().asItem(), 0.3F);
        net.minecraft.block.ComposterBlock.COMPOSTABLES.put(com.bewitchment.registry.ModItems.ELDERBERRIES.get(), 0.3F);
        tree = Feature.TREE.configured(new BaseTreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(ModBlocks.ELDER_WOOD.get().defaultBlockState()),
                new SimpleBlockStateProvider(ModBlocks.ELDER_LEAVES.get().defaultBlockState()),
                new BlobFoliagePlacer(FeatureSpread.fixed(2), FeatureSpread.fixed(0), 3),
                new StraightTrunkPlacer(4, 1, 0), new TwoLayerFeature(1, 0, 1)).ignoreVines().build());
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "elder"), tree);
        forestTree = GenerationGate.wrap(tree.decorated(Placement.HEIGHTMAP_WORLD_SURFACE.configured(IPlacementConfig.NONE)).squared(),
                WorldGenerationConfig.get().chances.get("elder"));
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "elder_forest"), forestTree);
    }
    @Override protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean flowers) { return tree; }
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!WorldGenerationConfig.get().allowsBiome(event.getName())) return;
        if (event.getCategory() == Biome.Category.FOREST && forestTree != null)
            event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, forestTree);
    }
}
