package com.bewitchment.config;

import com.bewitchment.world.LegacyStructureRules.Kind;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;

/** Independent from ore/tree generation, as in legacy ModConfig.worldGen.enableStructures. */
public final class StructureGenerationConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final Map<Kind, ForgeConfigSpec.DoubleValue> CHANCES;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLED = builder.comment("Legacy surface structures in new Overworld chunks. Restart after changes.")
                .worldRestart().define("enabled", true);
        Map<Kind, ForgeConfigSpec.DoubleValue> chances = new EnumMap<>(Kind.class);
        for (Kind kind : Kind.values()) chances.put(kind, builder.comment("Independent chance per eligible chunk; zero disables this family.")
                .worldRestart().defineInRange(kind.name().toLowerCase(Locale.ROOT) + "Chance", .001, 0, 1));
        CHANCES = Collections.unmodifiableMap(chances);
        SPEC = builder.build();
    }
    private StructureGenerationConfig() {}
}
