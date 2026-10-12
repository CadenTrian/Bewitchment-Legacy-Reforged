package com.bewitchment.machine;

public final class RitualPower {
    public static int startingCost(int cost, boolean visage) {
        if (cost < 0) throw new IllegalArgumentException("Negative ritual power");
        return visage ? (int)(cost * .85) : cost;
    }
    private RitualPower() {}
}
