package com.bewitchment.world;

import com.bewitchment.Bewitchment;
import com.bewitchment.config.WorldGenerationConfig;
import com.bewitchment.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.ArrayList;
import java.util.List;

public final class ModOreGeneration {
    private static final List<ConfiguredFeature<?, ?>> ORES = new ArrayList<>();

    public static void register() {
        ORES.clear();
        add("silver", ModBlocks.SILVER_ORE.get());
        add("amethyst", ModBlocks.AMETHYST_ORE.get());
        add("garnet", ModBlocks.GARNET_ORE.get());
        add("opal", ModBlocks.OPAL_ORE.get());
        add("salt", ModBlocks.SALT_ORE.get());
    }

    private static void add(String name, Block block) {
        WorldGenerationConfig.OreSettings settings = WorldGenerationConfig.get().ores.get(name);
        // Always retain registered feature IDs, even for disabled/invalid settings, for saved-world compatibility.
        int size = Math.max(1, settings.size), count = Math.max(1, settings.attempts);
        int min = settings.minY, span = Math.max(1, settings.maxY - min);
        ConfiguredFeature<?, ?> feature = Feature.ORE
                .configured(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, block.defaultBlockState(), size))
                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(min, 0, span)))
                .squared().count(count);
        feature = GenerationGate.wrap(feature, settings.enabled() ? 1 : 0);
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Bewitchment.MOD_ID, name + "_ore"), feature);
        ORES.add(feature);
    }

    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!WorldGenerationConfig.get().allowsBiome(event.getName())) return;
        if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND
                || event.getCategory() == Biome.Category.NONE) return;
        for (ConfiguredFeature<?, ?> feature : ORES) {
            event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
        }
    }

    private ModOreGeneration() {}
}

