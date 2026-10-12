package com.bewitchment.combat;

import com.bewitchment.item.BarkArmor;
import net.minecraft.item.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import java.util.Random;

public final class AdvancedAccessoryCheck {
    private static int checks;
    private static void check(boolean condition, String description) { checks++; if (!condition) throw new AssertionError(description); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        // Exhaust every equipment/damage-source combination: fire/explosions must not bypass equipment.
        for (int mask = 0; mask < 16; mask++) {
            boolean equipped = (mask & 8) != 0, fire = (mask & 4) != 0, explosion = (mask & 2) != 0, demon = (mask & 1) != 0;
            check(AccessoryRules.hellish(equipped, fire, explosion, demon) == (equipped && (mask & 7) != 0), "Hellish equipment/source combination " + mask);
        }
        check(!AccessoryRules.vampiric(0, 0) && !AccessoryRules.vampiric(-1, 0), "Vampiric cannot activate on no damage");
        check(!AccessoryRules.vampiric(Float.NaN, 0) && !AccessoryRules.vampiric(Float.POSITIVE_INFINITY, 0), "Reject malformed final damage");
        check(AccessoryRules.vampiric(1, 0) && AccessoryRules.vampiric(1, Math.nextDown(.175)), "Rolls below 17.5 percent activate");
        check(!AccessoryRules.vampiric(1, .175) && !AccessoryRules.vampiric(1, 1) && !AccessoryRules.vampiric(1, -1), "Roll threshold is exclusive and nonnegative");
        ItemStack belt = new ItemStack(Items.LEATHER);
        check(BarkArmor.get(belt) == 0 && !belt.hasTag(), "Reading bark never creates client-side NBT");
        belt.getOrCreateTag().putString("OtherMod", "retained");
        BarkArmor.set(belt, 8); check(BarkArmor.get(belt) == 4, "Bark caps at four");
        check(!BarkArmor.canGrow(belt, true, 0), "Full bark cannot trigger growth/power spending");
        BarkArmor.set(belt, -3); check(BarkArmor.get(belt) == 0, "Negative bark clamps to zero");
        check(!BarkArmor.canGrow(belt, false, 0), "No growth away from grass");
        check(BarkArmor.canGrow(belt, true, 0) && BarkArmor.canGrow(belt, true, Math.nextDown(.0008)), "Growth chance retains legacy boundary");
        check(!BarkArmor.canGrow(belt, true, .0008) && !BarkArmor.canGrow(belt, true, -1), "Growth threshold is exclusive and nonnegative");
        BarkArmor.Hit hit = BarkArmor.hit(belt, 10, false, false, true);
        check(!hit.blocked && !hit.spent && hit.damage == 10, "Empty bark does not protect");
        BarkArmor.set(belt, 4);
        hit = BarkArmor.hit(belt, 2, false, false, true);
        check(!hit.blocked && BarkArmor.get(belt) == 4, "Two-damage hits do not consume bark");
        hit = BarkArmor.hit(belt, Math.nextUp(2F), false, false, true);
        check(hit.blocked && hit.spent && hit.damage == 0 && BarkArmor.get(belt) == 3, "Above-threshold physical hit consumes exactly one layer");
        hit = BarkArmor.hit(belt, 10, false, true, true);
        check(!hit.blocked && !hit.spent && hit.damage == 10 && BarkArmor.get(belt) == 3, "Attacker-sourced magic bypasses bark");
        hit = BarkArmor.hit(belt, 10, false, false, false);
        check(!hit.blocked && !hit.spent && hit.damage == 10, "Environmental nonfire damage bypasses bark");
        hit = BarkArmor.hit(belt, 10, true, false, true);
        check(!hit.blocked && hit.spent && hit.damage == 12.5F && BarkArmor.get(belt) == 2, "Attacker fire burns one layer without physical cancellation or double spending");
        hit = BarkArmor.hit(belt, 4, true, true, true);
        check(!hit.blocked && hit.damage == 5 && BarkArmor.get(belt) == 1, "Magical fire retains fire vulnerability");
        hit = BarkArmor.hit(belt, 1, true, false, false);
        check(hit.damage == 1.25F && BarkArmor.get(belt) == 0, "Small environmental fire still burns one layer");
        hit = BarkArmor.hit(belt, 10, true, false, true);
        check(hit.damage == 10 && !hit.spent && BarkArmor.get(belt) == 0, "Fire cannot consume below zero or amplify without bark");
        BarkArmor.set(belt, 2);
        check(!BarkArmor.hit(belt, 0, true, false, true).spent && !BarkArmor.hit(belt, -2, true, false, true).spent && BarkArmor.get(belt) == 2, "Canceled/nonpositive damage consumes no bark");
        check(!BarkArmor.hit(belt, Float.NaN, true, false, true).spent && BarkArmor.get(belt) == 2, "Malformed damage consumes no bark");
        ItemStack reloaded = belt.copy();
        check(BarkArmor.get(reloaded) == 2, "Bark survives NBT copy/save");
        BarkArmor.clear(belt);
        check(BarkArmor.get(belt) == 0 && !belt.getTag().contains("bark") && belt.getTag().getString("OtherMod").equals("retained"), "Removal clears bark and preserves unrelated NBT");
        check(BarkArmor.get(reloaded) == 2, "Clearing one stack cannot mutate another stack");
        reloaded.getTag().putInt("bark", Integer.MAX_VALUE);
        check(BarkArmor.get(reloaded) == 4, "Malformed saved bark remains bounded");
        Vector3d before = new Vector3d(1, 2, 3);
        Vector3d after = FearMovement.jitter(before, new Random() { private int call; @Override public double nextGaussian() { return call++ == 0 ? 2 : -4; } });
        check(after.x == 1.5 && after.y == 2 && after.z == 2, "Fear applies independent Gaussian quarter-scale impulses on X/Z only");
        check(before.x == 1 && before.y == 2 && before.z == 3, "Fear does not mutate original motion");
        check(FearMovement.jitter(before, new Random(18)).equals(FearMovement.jitter(before, new Random(18))), "Fear jitter is reproducible for a seeded random source");
        System.out.println("PASS: " + checks + " advanced accessory, bark and Fear checks.");
    }
}
