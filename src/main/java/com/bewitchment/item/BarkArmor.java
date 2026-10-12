package com.bewitchment.item;

import net.minecraft.item.ItemStack;

/** Bounded legacy bark charges. Fire has precedence over physical-hit protection. */
public final class BarkArmor {
    public static int get(ItemStack stack) { return stack.hasTag() ? Math.max(0, Math.min(4, stack.getTag().getInt("bark"))) : 0; }
    public static void set(ItemStack stack, int amount) { if (!stack.isEmpty()) stack.getOrCreateTag().putInt("bark", Math.max(0, Math.min(4, amount))); }
    public static void clear(ItemStack stack) { if (stack.hasTag()) stack.getTag().remove("bark"); }
    public static boolean canGrow(ItemStack stack, boolean grass, double roll) { return grass && get(stack) < 4 && roll >= 0 && roll < .0008; }
    public static final class Hit {
        public final float damage; public final boolean blocked, spent;
        Hit(float damage, boolean blocked, boolean spent) { this.damage = damage; this.blocked = blocked; this.spent = spent; }
    }
    public static Hit hit(ItemStack stack, float damage, boolean fire, boolean magic, boolean attacker) {
        if (damage <= 0 || !Float.isFinite(damage) || get(stack) == 0) return new Hit(damage, false, false);
        if (fire) { set(stack, get(stack) - 1); return new Hit(damage * 1.25F, false, true); }
        if (!magic && attacker && damage > 2) { set(stack, get(stack) - 1); return new Hit(0, true, true); }
        return new Hit(damage, false, false);
    }
    private BarkArmor() {}
}
