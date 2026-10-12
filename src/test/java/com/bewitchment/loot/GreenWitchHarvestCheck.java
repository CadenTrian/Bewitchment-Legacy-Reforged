package com.bewitchment.loot;

import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class GreenWitchHarvestCheck {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        Random high = new Random() {
            @Override public boolean nextBoolean() { return true; }
            @Override public int nextInt(int bound) { return bound - 1; }
        };
        Random miss = new Random() { @Override public boolean nextBoolean() { return false; } };
        ItemStack full = new ItemStack(Items.WHEAT, 64); full.getOrCreateTag().putString("Test", "retained");
        List<ItemStack> original = Arrays.asList(full, new ItemStack(Items.WHEAT_SEEDS));
        check(GreenWitchHarvest.bonus(original, miss) == original, "Failed chance leaves original drops intact");
        List<ItemStack> result = GreenWitchHarvest.bonus(original, high);
        check(result.size() == 3 && result.get(0).getCount() == 64 && result.get(1).getCount() == 2 && result.get(2).getCount() == 3, "Bonus splits full stacks and independently grows seeds");
        check(full.getCount() == 64 && original.get(1).getCount() == 1, "Do not mutate original drop objects");
        check(ItemStack.tagMatches(result.get(0), full) && ItemStack.tagMatches(result.get(1), full), "Split items preserve NBT");
        check(result.stream().allMatch(stack -> stack.getCount() <= stack.getMaxStackSize()), "Never produce oversized stacks");
        check(GreenWitchHarvest.bonus(Collections.emptyList(), high).isEmpty(), "No invented drops from empty loot");
        Random zero = new Random() {
            @Override public boolean nextBoolean() { return true; }
            @Override public int nextInt(int bound) { return 0; }
        };
        check(GreenWitchHarvest.bonus(Collections.singletonList(new ItemStack(Items.WHEAT)), zero).get(0).getCount() == 1, "Successful roll may still give zero extra items");
        System.out.println("PASS: " + checks + " green-witch harvest checks");
    }
}
