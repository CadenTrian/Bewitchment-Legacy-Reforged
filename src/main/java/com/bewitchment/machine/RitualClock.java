package com.bewitchment.machine;

import java.util.function.IntPredicate;

/** Persisted local ticks: every complete second must be paid before progress completes. */
public final class RitualClock {
    public enum Step { WAITING, PAID, FINISHED, NO_POWER }
    private int ticks;
    public int ticks() { return ticks; }
    public void load(int value) { ticks = Math.max(0, Math.min(71999, value)); }
    public void reset() { ticks = 0; }
    /** Continuous rituals persist only their partial payment second, so long runs never overflow. */
    public Step advanceContinuous(int cost, IntPredicate pay) {
        if (cost < 0) throw new IllegalArgumentException("Invalid ritual running cost");
        ticks %= 20;
        if (ticks == 19) {
            if (cost > 0 && !pay.test(cost)) return Step.NO_POWER;
            ticks = 0;
            return Step.PAID;
        }
        ticks++;
        return Step.WAITING;
    }
    public Step advance(int seconds, int cost, IntPredicate pay) {
        if (seconds < 1 || seconds > 3600 || cost < 0) throw new IllegalArgumentException("Invalid ritual clock parameters");
        if ((ticks + 1) % 20 != 0) { ticks++; return Step.WAITING; }
        if (cost > 0 && !pay.test(cost)) return Step.NO_POWER;
        ticks++;
        return ticks >= seconds * 20 ? Step.FINISHED : Step.PAID;
    }
}
