package com.bewitchment.world;

import com.bewitchment.registry.ModCoquina;
import com.bewitchment.config.WorldGenerationConfig;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.Random;

public final class CoquinaGeneration {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<NoFeatureConfig>> FEATURE = FEATURES.register("coquina", () -> new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
        @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos origin, NoFeatureConfig config) {
            BlockPos center = new BlockPos(origin.getX(), world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()) - 1, origin.getZ());
            boolean placed = false;
            // Bounded sandy deposits; never replace water, air, or structures.
            for (int depth = 0; depth < WorldGenerationConfig.get().coquinaDepth && center.getY() > 0 && world.hasChunkAt(center) && world.getBlockState(center).is(Blocks.SAND); depth++) {
                for (int blob = 0; blob < 3; blob++) {
                    int x = random.nextInt(2), y = random.nextInt(2), z = random.nextInt(2);
                    double radius = (x + y + z) / 3.0 + 0.5;
                    for (BlockPos p : BlockPos.betweenClosed(center.offset(-x, -y, -z), center.offset(x, y, z)))
                        if (world.hasChunkAt(p) && p.distSqr(center) <= radius * radius && world.getBlockState(p).is(Blocks.SAND)) {
                            world.setBlock(p, ModCoquina.RAW.get().defaultBlockState(), 2); placed = true;
                        }
                    center = center.offset(random.nextInt(2) - 1, -random.nextInt(2), random.nextInt(2) - 1);
                }
                center = center.below();
            }
            return placed;
        }
    });
    private static ConfiguredFeature<?, ?> deposit;
    public static void register() {
        deposit = GenerationGate.wrap(FEATURE.get().configured(NoFeatureConfig.INSTANCE).squared(), WorldGenerationConfig.get().chances.get("coquina"));
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "coquina"), deposit);
    }
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!WorldGenerationConfig.get().allowsBiome(event.getName())) return;
        if (deposit != null && (event.getCategory() == Biome.Category.BEACH || event.getCategory() == Biome.Category.OCEAN))
            event.getGeneration().addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, deposit);
    }
    private CoquinaGeneration() {}
}
