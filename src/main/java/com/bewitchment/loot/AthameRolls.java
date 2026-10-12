package com.bewitchment.loot;

import java.util.Random;

/** Legacy independent uniform base and Looting rolls; skulls use a separate 20% chance. */
public final class AthameRolls {
    public static int material(Random random, int maximum, int looting) {
        return random.nextInt(maximum + 1) + random.nextInt(Math.max(0, looting) + 1);
    }
    public static int head(Random random, int looting) {
        return random.nextFloat() <= 0.8F ? 0 : 1 + (int) (random.nextFloat() * Math.max(0, looting)) / 2;
    }
    private AthameRolls() {}
}
