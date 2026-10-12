package com.bewitchment.block;

import net.minecraft.util.ActionResultType;
import java.util.Random;
import java.util.function.BooleanSupplier;

/** Commit the light change before tool wear or feedback; clients only predict success. */
public final class CandleInteraction {
    public static ActionResultType use(boolean lit, boolean flint, boolean allowed, boolean client,
                                       BooleanSupplier change, Runnable wear, Runnable sound) {
        if (!allowed || !lit && !flint) return ActionResultType.PASS;
        if (client) return ActionResultType.SUCCESS;
        if (!change.getAsBoolean()) return ActionResultType.FAIL;
        if (!lit) wear.run();
        sound.run();
        return ActionResultType.CONSUME;
    }
    public static float volume(boolean extinguishing) { return extinguishing ? .5F : 1F; }
    public static float pitch(boolean extinguishing, Random random) {
        return extinguishing ? 2.6F + (random.nextFloat() - random.nextFloat()) * .8F : random.nextFloat() * .4F + .8F;
    }
    private CandleInteraction() {}
}