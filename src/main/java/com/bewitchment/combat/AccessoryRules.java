package com.bewitchment.combat;

/** Shared damage predicates keep equipment gating independent of damage alternatives. */
public final class AccessoryRules {
    public static boolean validDamage(float damage) { return Float.isFinite(damage) && damage > 0; }
    /** Each eligible defense pays separately, in the established Hellish/Nazar/Horseshoe order. */
    public static float reduce(float damage, boolean fire, boolean explosion, boolean demon, boolean magic, boolean coldIron,
            java.util.function.Predicate<com.bewitchment.item.LegacyAccessoryItem.Kind> equipped,
            java.util.function.IntPredicate pay) {
        if (!validDamage(damage)) return damage;
        if ((fire || explosion || demon) && equipped.test(com.bewitchment.item.LegacyAccessoryItem.Kind.HELLISH) && pay.test(20)) damage *= .85F;
        if (magic && equipped.test(com.bewitchment.item.LegacyAccessoryItem.Kind.NAZAR) && pay.test(20)) damage *= .9F;
        if (coldIron && equipped.test(com.bewitchment.item.LegacyAccessoryItem.Kind.HORSESHOE) && pay.test(20)) damage *= .9F;
        return damage;
    }
    public static boolean hellish(boolean equipped, boolean fire, boolean explosion, boolean demon) {
        return equipped && (fire || explosion || demon);
    }
    public static boolean vampiric(float damage, double roll) {
        return validDamage(damage) && roll >= 0 && roll < .175;
    }
    private AccessoryRules() {}
}
