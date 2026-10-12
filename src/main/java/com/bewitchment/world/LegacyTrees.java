package com.bewitchment.world;

import com.bewitchment.registry.*;
import com.bewitchment.config.WorldGenerationConfig;
import net.minecraft.block.*;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.Random;

public final class LegacyTrees {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<NoFeatureConfig>> JUNIPER = FEATURES.register("juniper", () -> new LegacyTreeFeature(false));
    private static final RegistryObject<Feature<NoFeatureConfig>> CYPRESS = FEATURES.register("cypress", () -> new LegacyTreeFeature(true));
    private static ConfiguredFeature<?, ?> juniper, cypress, wildJuniper, wildCypress;
    public static void register() {
        juniper = JUNIPER.get().configured(NoFeatureConfig.INSTANCE);
        cypress = CYPRESS.get().configured(NoFeatureConfig.INSTANCE);
        wildJuniper = placed("juniper", juniper);
        wildCypress = placed("cypress", cypress);
        for (WoodFamily family : new WoodFamily[]{ModTreeBlocks.JUNIPER, ModTreeBlocks.CYPRESS}) {
            ComposterBlock.COMPOSTABLES.put(family.sapling.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(family.leaves.get().asItem(), 0.3F);
        }
        ComposterBlock.COMPOSTABLES.put(ModItems.JUNIPER_BERRIES.get(), 0.3F);
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        pot.addPlant(ModBlocks.ELDER_SAPLING.getId(), ModTreeBlocks.POTTED_ELDER);
        pot.addPlant(ModTreeBlocks.JUNIPER.sapling.getId(), ModTreeBlocks.POTTED_JUNIPER);
        pot.addPlant(ModTreeBlocks.CYPRESS.sapling.getId(), ModTreeBlocks.POTTED_CYPRESS);
    }
    private static ConfiguredFeature<?, ?> placed(String name, ConfiguredFeature<?, ?> tree) {
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", name), tree);
        ConfiguredFeature<?, ?> placed = GenerationGate.wrap(tree.decorated(Placement.HEIGHTMAP_WORLD_SURFACE.configured(IPlacementConfig.NONE)).squared(),
                WorldGenerationConfig.get().chances.get(name));
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", name + "_wild"), placed);
        return placed;
    }
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!WorldGenerationConfig.get().allowsBiome(event.getName())) return;
        if (event.getCategory() == Biome.Category.SAVANNA && wildJuniper != null)
            event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, wildJuniper);
        if (event.getCategory() == Biome.Category.TAIGA && wildCypress != null)
            event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, wildCypress);
    }
    public static final class Grower extends Tree {
        private final boolean tall;
        public Grower(boolean tall) { this.tall = tall; }
        @Override protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean flowers) { return null; }
        @Override public boolean growTree(ServerWorld world, ChunkGenerator generator, BlockPos pos, BlockState state, Random random) {
            ConfiguredFeature<?, ?> feature = tall ? cypress : juniper;
            return feature != null && feature.place(world, generator, random, pos);
        }
    }
    private LegacyTrees() {}
}
