package com.bewitchment.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RitualConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WEDNESDAY;
    static {
        ForgeConfigSpec.Builder builder=new ForgeConfigSpec.Builder();
        WEDNESDAY=builder.comment("Enable the optional legacy Wednesday toad ritual. Uses the server's real-world weekday.").define("wednesday",false);
        SPEC=builder.build();
    }
    private RitualConfig(){}
}
