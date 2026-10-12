package com.bewitchment.machine;

import java.util.Random;

public final class AnimalShowerRules {
    public static int count(Random random) {
        // Preserve the legacy rerolled loop bound, rather than a uniform count.
        int count = 0;
        while (count < random.nextInt(8) + 3) count++;
        return count;
    }
    public static int offset(Random random) { return random.nextInt(12) - 6; }
    public static int height(Random random) { return 24 + random.nextInt(32); }
    private AnimalShowerRules() {}
}
