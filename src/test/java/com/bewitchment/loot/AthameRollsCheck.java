package com.bewitchment.loot;

import java.util.Random;

public final class AthameRollsCheck {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        Random low = new Random() {
            @Override public int nextInt(int bound) { return 0; }
            @Override public float nextFloat() { return 0; }
        };
        Random high = new Random() {
            @Override public int nextInt(int bound) { return bound - 1; }
            @Override public float nextFloat() { return Math.nextDown(1.0F); }
        };
        for (int base : new int[]{1, 2, 3, 4, 6, 8}) {
            check(AthameRolls.material(low, base, 3) == 0, "No guaranteed extra drop");
            check(AthameRolls.material(high, base, 0) == base, "Base upper bound is inclusive");
            check(AthameRolls.material(high, base, 3) == base + 3, "Looting upper bound is inclusive");
            check(AthameRolls.material(high, base, -1) == base, "Negative Looting treated as zero");
        }
        check(AthameRolls.head(low, 3) == 0, "Head chance can fail");
        check(AthameRolls.head(high, 0) == 1, "One head without Looting");
        check(AthameRolls.head(high, 2) == 1, "Legacy integer rounding at Looting II");
        check(AthameRolls.head(high, 3) == 2, "Two heads possible at Looting III");
        check(AthameRolls.head(high, -1) == 1, "Negative Looting does not remove a successful head");
        Random random = new Random(20261004);
        int heads = 0, total = 0;
        for (int i = 0; i < 100000; i++) {
            int count = AthameRolls.head(random, 0);
            if (count < 0 || count > 1) throw new AssertionError("Invalid head stack");
            heads += count;
            count = AthameRolls.material(random, 8, 3);
            if (count < 0 || count > 11) throw new AssertionError("Invalid material stack");
            total += count;
        }
        check(heads > 19000 && heads < 21000, "Skull chance approximately 20 percent");
        check(total > 540000 && total < 560000, "Independent uniform rolls average 5.5");
        System.out.println("PASS: " + checks + " athame loot checks plus 100000 seeded sample pairs");
    }
}
