package com.bewitchment.machine;

import net.minecraft.util.math.BlockPos;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Bounded search; preparation is read-only and insertion occurs once in the caller. */
public final class VillagerFortunePlacement {
    public static final int ATTEMPTS = 16;
    public static final int RADIUS = 16;
    public static final int CHILD_AGE = -2400;

    public static BlockPos candidate(BlockPos origin, double gaussianX, double gaussianZ) {
        if (!Double.isFinite(gaussianX) || !Double.isFinite(gaussianZ)) return null;
        return origin.offset(offset(gaussianX), 0, offset(gaussianZ));
    }

    private static int offset(double gaussian) {
        return (int) Math.floor(Math.max(-RADIUS, Math.min(RADIUS, gaussian * 4)));
    }

    public static boolean height(int y, int maxHeight) { return y > 0 && y < maxHeight - 1; }

    public static BlockPos find(Supplier<BlockPos> candidate, Predicate<BlockPos> fits) {
        for (int i = 0; i < ATTEMPTS; i++) {
            BlockPos pos = candidate.get();
            if (pos != null && fits.test(pos)) return pos.immutable();
        }
        return null;
    }

    private VillagerFortunePlacement() {}
}
