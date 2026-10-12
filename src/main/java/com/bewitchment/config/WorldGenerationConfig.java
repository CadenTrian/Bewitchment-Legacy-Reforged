package com.bewitchment.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import java.util.*;

/** Common config is loaded before common setup; generation uses one immutable startup snapshot. */
public final class WorldGenerationConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS, BIOME_BLACKLIST;
    public static final Map<String, Ore> ORES = new LinkedHashMap<>();
    public static final Map<String, ForgeConfigSpec.DoubleValue> CHANCES = new LinkedHashMap<>();
    public static final ForgeConfigSpec.IntValue COQUINA_DEPTH;
    private static volatile Settings active;
    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Natural generation only; changes require a full game/server restart and affect new chunks.",
                "Sapling growth, existing blocks, recipes and creative items are unaffected.").push("worldgen");
        ENABLED = b.worldRestart().define("enabled", true);
        DIMENSIONS = b.comment("Dimension IDs allowed to generate ores, wild trees and coquina. Empty disables all.",
                "Overworld-only matches the legacy dimension whitelist.").worldRestart()
                .defineListAllowEmpty(Arrays.asList("dimensions"), () -> Collections.singletonList("minecraft:overworld"), WorldGenerationConfig::validId);
        BIOME_BLACKLIST = b.comment("Biome IDs excluded from all natural generation. Empty excludes none.").worldRestart()
                .defineListAllowEmpty(Arrays.asList("biomeBlacklist"), () -> Collections.<String>emptyList(), WorldGenerationConfig::validId);
        b.push("ores");
        ORES.put("silver", new Ore(b, "silver", 7, 12, 10, 128));
        ORES.put("amethyst", new Ore(b, "amethyst", 6, 2, 10, 42));
        ORES.put("garnet", new Ore(b, "garnet", 6, 2, 12, 42));
        ORES.put("opal", new Ore(b, "opal", 6, 2, 16, 42));
        ORES.put("salt", new Ore(b, "salt", 5, 10, 10, 120));
        b.pop().push("trees");
        for (String name : Arrays.asList("elder", "juniper", "cypress"))
            CHANCES.put(name, b.comment("Probability of one attempt per eligible chunk; 0 disables wild generation.")
                    .worldRestart().defineInRange(name + "Chance", 0.01, 0, 1));
        b.pop().push("coquina");
        CHANCES.put("coquina", b.comment("Probability of a deposit attempt in beach/ocean chunks; 0 disables.")
                .worldRestart().defineInRange("chance", 1.0 / 6.0, 0, 1));
        COQUINA_DEPTH = b.comment("Maximum downward random-walk steps; only sand can be replaced.")
                .worldRestart().defineInRange("maxDepthSteps", 8, 1, 32);
        b.pop().push("spanishMoss");
        CHANCES.put("spanish_moss", b.comment("Probability of one moss scan per swamp chunk; 0 disables natural moss. Does not disable growth.")
                .worldRestart().defineInRange("chance", 1.0, 0, 1));
        b.pop().pop();
        SPEC = b.build();
    }
    private static boolean validId(Object value) {
        return value instanceof String && ((String) value).contains(":") && ResourceLocation.tryParse((String) value) != null;
    }
    public static final class Ore {
        public final ForgeConfigSpec.IntValue size, attempts, minY, maxY;
        Ore(ForgeConfigSpec.Builder b, String name, int size, int attempts, int min, int max) {
            b.push(name);
            this.size = b.comment("Maximum vein size. 0 disables. Capped at the 1.16 ore codec limit.")
                    .worldRestart().defineInRange("veinSize", size, 0, 64);
            this.attempts = b.comment("Attempts per chunk; 0 disables.").worldRestart().defineInRange("attempts", attempts, 0, 127);
            minY = b.comment("Inclusive starting Y; veins may extend beyond starting heights.")
                    .worldRestart().defineInRange("minY", min, 0, 255);
            maxY = b.comment("Exclusive upper starting Y. Must exceed minY; otherwise this ore is disabled with a warning.")
                    .worldRestart().defineInRange("maxYExclusive", max, 1, 256);
            b.pop();
        }
    }
    public static final class OreSettings {
        public final int size, attempts, minY, maxY;
        OreSettings(Ore ore) { size = ore.size.get(); attempts = ore.attempts.get(); minY = ore.minY.get(); maxY = ore.maxY.get(); }
        public boolean enabled() { return size > 0 && attempts > 0 && maxY > minY; }
    }
    public static final class Settings {
        public final boolean enabled;
        public final Set<String> dimensions, biomeBlacklist;
        public final Map<String, OreSettings> ores;
        public final Map<String, Double> chances;
        public final int coquinaDepth;
        private Settings() {
            enabled = ENABLED.get();
            dimensions = Collections.unmodifiableSet(new HashSet<>(DIMENSIONS.get()));
            biomeBlacklist = Collections.unmodifiableSet(new HashSet<>(BIOME_BLACKLIST.get()));
            Map<String, OreSettings> oreCopy = new LinkedHashMap<>();
            ORES.forEach((name, ore) -> {
                OreSettings settings = new OreSettings(ore);
                if (settings.maxY <= settings.minY)
                    LogManager.getLogger().warn("Bewitchment: {} generation disabled: maxYExclusive must exceed minY", name);
                oreCopy.put(name, settings);
            });
            ores = Collections.unmodifiableMap(oreCopy);
            Map<String, Double> chanceCopy = new HashMap<>();
            CHANCES.forEach((name, value) -> chanceCopy.put(name, value.get()));
            chances = Collections.unmodifiableMap(chanceCopy);
            coquinaDepth = COQUINA_DEPTH.get();
        }
        public boolean allowsDimension(String id) { return enabled && dimensions.contains(id); }
        public boolean allowsBiome(ResourceLocation id) { return enabled && (id == null || !biomeBlacklist.contains(id.toString())); }
    }
    public static void capture() { active = new Settings(); }
    public static Settings get() {
        if (active == null) throw new IllegalStateException("World generation settings have not been captured during setup");
        return active;
    }
    private WorldGenerationConfig() {}
}
