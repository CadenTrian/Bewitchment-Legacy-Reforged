package com.bewitchment.world;

import com.bewitchment.config.WorldGenerationConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Checks the actual generation world, since a biome can be shared by several dimensions. */
public final class GenerationGate extends Feature<GenerationGate.Config> {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<Config>> GATE = FEATURES.register("generation_gate", GenerationGate::new);
    private GenerationGate() { super(Config.CODEC); }
    public static ConfiguredFeature<?, ?> wrap(ConfiguredFeature<?, ?> feature, double chance) {
        return GATE.get().configured(new Config(() -> feature, chance));
    }
    @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, Config config) {
        if (!WorldGenerationConfig.get().allowsDimension(world.getLevel().dimension().location().toString())) return false;
        if (config.chance <= 0 || (config.chance < 1 && random.nextFloat() >= config.chance)) return false;
        return config.feature.get().place(world, generator, random, pos);
    }
    public static final class Config implements IFeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ConfiguredFeature.CODEC.fieldOf("feature").forGetter((Config c) -> c.feature),
                Codec.doubleRange(0, 1).fieldOf("chance").forGetter((Config c) -> c.chance)
        ).apply(instance, Config::new));
        public final Supplier<ConfiguredFeature<?, ?>> feature;
        public final double chance;
        public Config(Supplier<ConfiguredFeature<?, ?>> feature, double chance) { this.feature = feature; this.chance = chance; }
        @Override public Stream<ConfiguredFeature<?, ?>> getFeatures() { return feature.get().getFeatures(); }
    }
}
