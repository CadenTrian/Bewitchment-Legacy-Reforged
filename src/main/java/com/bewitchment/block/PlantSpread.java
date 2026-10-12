package com.bewitchment.block;

import net.minecraft.util.math.BlockPos;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Original five-candidate mushroom walk, without reading unavailable world positions. */
public final class PlantSpread {
    @Nullable public static BlockPos choose(BlockPos origin, Random random, Predicate<BlockPos> available,
                                            Predicate<BlockPos> samePlant, Predicate<BlockPos> canGrow) {
        if (random.nextInt(25) != 0) return null;
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -1, -4), origin.offset(4, 1, 4))) {
            if (!available.test(pos)) return null;
            if (samePlant.test(pos) && ++count >= 5) return null;
        }
        BlockPos center = origin;
        BlockPos candidate = offset(center, random);
        for (int i = 0; i < 4; i++) {
            if (!available.test(candidate) || !available.test(candidate.below())) return null;
            if (canGrow.test(candidate)) center = candidate;
            candidate = offset(center, random);
        }
        return available.test(candidate) && available.test(candidate.below()) && canGrow.test(candidate) ? candidate : null;
    }
    private static BlockPos offset(BlockPos pos, Random random) {
        return pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
    }
    private PlantSpread() {}
}