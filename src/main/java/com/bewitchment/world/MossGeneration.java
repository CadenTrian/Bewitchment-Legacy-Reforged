package com.bewitchment.world;

import com.bewitchment.registry.ModBlocks;
import com.bewitchment.config.WorldGenerationConfig;
import net.minecraft.block.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.Random;

public final class MossGeneration {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<NoFeatureConfig>> MOSS = FEATURES.register("spanish_moss", () -> new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
        @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos origin, NoFeatureConfig config) {
            boolean placed = false;
            int x = (origin.getX() >> 4) * 16 + 8, z = (origin.getZ() >> 4) * 16 + 8;
            // Legacy scans a swamp chunk's center column at Y=64..127.
            for (int y = 64; y < 128; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!world.hasChunkAt(pos) || !world.isEmptyBlock(pos)) continue;
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    if (!VineBlock.isAcceptableNeighbour(world, pos.relative(face), face)) continue;
                    BlockState state = ModBlocks.SPANISH_MOSS.get().defaultBlockState().setValue(VineBlock.getPropertyForFace(face), true);
                    // Advance the current segment, fixing the legacy loop's repeated origin.down().
                    for (int length = 0; length < 5 && pos.getY() > 0 && world.hasChunkAt(pos) && world.isEmptyBlock(pos); length++, pos = pos.below()) {
                        world.setBlock(pos, state, 2); placed = true;
                    }
                    break;
                }
            }
            return placed;
        }
    });
    private static ConfiguredFeature<?, ?> configured;
    public static void register() {
        configured = GenerationGate.wrap(MOSS.get().configured(NoFeatureConfig.INSTANCE), WorldGenerationConfig.get().chances.get("spanish_moss"));
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "spanish_moss"), configured);
    }
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (configured != null && WorldGenerationConfig.get().allowsBiome(event.getName()) && event.getCategory() == Biome.Category.SWAMP)
            event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, configured);
    }
    private MossGeneration() {}
}
