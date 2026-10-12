package com.bewitchment.loot;

import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class FortuneFavorCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        Random one = new Random() { @Override public int nextInt(int bound) { return bound - 1; } };
        Random zero = new Random() { @Override public int nextInt(int bound) { return 0; } };
        ItemStack original = new ItemStack(Items.DIAMOND, 64); original.getOrCreateTag().putString("marker", "kept");
        List<ItemStack> result = FortuneFavorLoot.bonus(Arrays.asList(original), one);
        check(result.size() == 2 && result.get(0).getCount() == 64 && result.get(1).getCount() == 1, "Bonus splits full stacks without losing items");
        check(result.stream().allMatch(s -> s.getTag().getString("marker").equals("kept")), "Split bonus preserves NBT");
        check(original.getCount() == 64 && result.get(0) != original, "Original loot is not mutated or aliased");
        result.get(1).getTag().putString("marker", "changed");
        check(original.getTag().getString("marker").equals("kept") && result.get(0).getTag().getString("marker").equals("kept"), "Split tags are independent");
        result = FortuneFavorLoot.bonus(Arrays.asList(original), zero);
        check(result.size() == 1 && result.get(0).getCount() == 64, "Zero roll leaves quantity unchanged");
        result = FortuneFavorLoot.bonus(Arrays.asList(ItemStack.EMPTY, new ItemStack(Items.COAL, 2), new ItemStack(Items.LAPIS_LAZULI, 4)), one);
        check(result.size() == 2 && result.get(0).getCount() == 3 && result.get(1).getCount() == 5, "Roll applies once to each nonempty stack");
        check(FortuneFavorLoot.bonus(Collections.emptyList(), one).isEmpty(), "No drops remain no drops");
        Random seeded = new Random(618); int bonuses = 0;
        for (int i = 0; i < 10000; i++) bonuses += FortuneFavorLoot.bonus(Arrays.asList(new ItemStack(Items.COAL)), seeded).get(0).getCount() - 1;
        check(bonuses > 4800 && bonuses < 5200, "Seeded bonus distribution remains approximately 50 percent");
        System.out.println("PASS: " + checks + " Fortune's Favor drop checks.");
    }
}
