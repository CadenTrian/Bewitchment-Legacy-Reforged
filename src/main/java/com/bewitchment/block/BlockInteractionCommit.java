package com.bewitchment.block;

import net.minecraft.util.ActionResultType;
import java.util.function.BooleanSupplier;

/** Server mutation must succeed before inventory payment or rewards. */
public final class BlockInteractionCommit {
    public static ActionResultType apply(boolean eligible, boolean client, BooleanSupplier change, Runnable complete) {
        if (!eligible) return ActionResultType.PASS;
        if (client) return ActionResultType.SUCCESS;
        if (!change.getAsBoolean()) return ActionResultType.FAIL;
        complete.run();
        return ActionResultType.CONSUME;
    }
    private BlockInteractionCommit() {}
}