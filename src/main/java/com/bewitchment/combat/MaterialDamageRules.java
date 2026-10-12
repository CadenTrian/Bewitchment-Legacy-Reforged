package com.bewitchment.combat;

import java.util.function.IntConsumer;

/** Legacy Weakness and MaterialHandler arithmetic shared by melee and arrows. */
public final class MaterialDamageRules {
    public static float weakness(boolean weak, boolean player) { return weak ? player ? 2.25F : 1.5F : 1F; }
    public static float arrow(boolean weak, boolean player) { return weak ? weakness(true, player) : 0F; }
    public static float melee(float damage, boolean targetWeak, boolean targetPlayer, boolean tool,
                              boolean attackerWeak, int armorPieces, IntConsumer retaliate) {
        if (targetWeak && tool) return damage * weakness(true, targetPlayer);
        if (attackerWeak && armorPieces > 0) {
            retaliate.accept(armorPieces);
            return damage * (1F - .06F * armorPieces);
        }
        return damage;
    }
    private MaterialDamageRules() {}
}