package com.bewitchment.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ToolConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue SKELETON_KEY_USES;
    static {
        ForgeConfigSpec.Builder builder=new ForgeConfigSpec.Builder();
        SKELETON_KEY_USES=builder.comment("Maximum Skeleton Key durability (legacy default: 10).")
            .defineInRange("maxSkeletonKeyUses",10,1,32767);
        SPEC=builder.build();
    }
    private ToolConfig(){}
}
