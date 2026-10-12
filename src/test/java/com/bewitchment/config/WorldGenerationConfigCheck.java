package com.bewitchment.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.util.ResourceLocation;
import java.util.*;

/** Standalone regression checks using the real Forge config spec, with no additional test dependency. */
public final class WorldGenerationConfigCheck {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        CommentedConfig data = CommentedConfig.inMemory();
        WorldGenerationConfig.SPEC.correct(data);
        WorldGenerationConfig.SPEC.setConfig(data);
        WorldGenerationConfig.capture();
        WorldGenerationConfig.Settings defaults = WorldGenerationConfig.get();
        check(defaults.allowsDimension("minecraft:overworld"), "Overworld default");
        check(!defaults.allowsDimension("minecraft:the_nether"), "Nether excluded");
        check(!defaults.allowsDimension("example:overworld_copy"), "Shared biomes do not allow another dimension");
        int[][] expected = {{7,12,10,128},{6,2,10,42},{6,2,12,42},{6,2,16,42},{5,10,10,120}};
        int index = 0;
        for (WorldGenerationConfig.OreSettings ore : defaults.ores.values()) {
            int[] values = expected[index++];
            check(ore.size == values[0] && ore.attempts == values[1] && ore.minY == values[2] && ore.maxY == values[3], "Legacy ore defaults");
            check(ore.enabled(), "Default ore enabled");
        }
        WorldGenerationConfig.Ore silver = WorldGenerationConfig.ORES.get("silver");
        silver.attempts.set(0);
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().ores.get("silver").enabled(), "Zero attempts disables");
        check(defaults.ores.get("silver").enabled(), "Snapshot is independent of config edits");
        silver.attempts.set(12); silver.size.set(0);
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().ores.get("silver").enabled(), "Zero size disables");
        silver.size.set(7); silver.minY.set(128); silver.maxY.set(128);
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().ores.get("silver").enabled(), "Empty range disables");
        silver.maxY.set(10);
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().ores.get("silver").enabled(), "Reversed range disables");
        silver.minY.set(255); silver.maxY.set(256);
        WorldGenerationConfig.capture();
        check(WorldGenerationConfig.get().ores.get("silver").enabled(), "Single top-layer range valid");
        WorldGenerationConfig.DIMENSIONS.set(Collections.emptyList());
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().allowsDimension("minecraft:overworld"), "Empty whitelist disables");
        WorldGenerationConfig.DIMENSIONS.set(Arrays.asList("minecraft:overworld", "example:forest"));
        WorldGenerationConfig.BIOME_BLACKLIST.set(Collections.singletonList("minecraft:plains"));
        WorldGenerationConfig.capture();
        check(WorldGenerationConfig.get().allowsDimension("example:forest"), "Custom dimension permitted explicitly");
        check(!WorldGenerationConfig.get().allowsBiome(new ResourceLocation("minecraft:plains")), "Blacklisted biome excluded");
        check(WorldGenerationConfig.get().allowsBiome(new ResourceLocation("minecraft:forest")), "Other biome permitted");
        for (String name : Arrays.asList("elder", "juniper", "cypress", "coquina", "spanish_moss")) {
            WorldGenerationConfig.CHANCES.get(name).set(0.0);
            WorldGenerationConfig.capture();
            check(WorldGenerationConfig.get().chances.get(name) == 0.0, name + " can be disabled");
            WorldGenerationConfig.CHANCES.get(name).set(1.0);
            WorldGenerationConfig.capture();
            check(WorldGenerationConfig.get().chances.get(name) == 1.0, name + " can attempt every chunk");
        }
        WorldGenerationConfig.ENABLED.set(false);
        WorldGenerationConfig.capture();
        check(!WorldGenerationConfig.get().allowsDimension("example:forest"), "Master switch disables dimension");
        check(!WorldGenerationConfig.get().allowsBiome(new ResourceLocation("minecraft:forest")), "Master switch disables biome injection");
        data.set("worldgen.ores.silver.veinSize", 1000);
        data.set("worldgen.ores.silver.minY", -1);
        data.set("worldgen.trees.elderChance", 2.0);
        data.set("worldgen.dimensions", Arrays.asList("BAD ID", "minecraft:overworld"));
        WorldGenerationConfig.SPEC.correct(data);
        check(((Number) data.get("worldgen.ores.silver.veinSize")).intValue() <= 64, "Oversized vein corrected");
        check(((Number) data.get("worldgen.ores.silver.minY")).intValue() >= 0, "Negative height corrected");
        check(((Number) data.get("worldgen.trees.elderChance")).doubleValue() <= 1, "Invalid probability corrected");
        check(!((List<?>) data.get("worldgen.dimensions")).contains("BAD ID"), "Malformed dimension corrected");
        System.out.println("PASS: " + checks + " world-generation config checks");
    }
}
