package com.bewitchment.machine;

import net.minecraft.util.registry.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import java.util.*;

public final class SpinningWheelCheck {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        ItemStack sixtyThree = new ItemStack(Items.STRING, 63);
        ItemStack two = new ItemStack(Items.STRING, 2);
        List<ItemStack> plan = SpinningWheelTile.planOutputs(Arrays.asList(sixtyThree, ItemStack.EMPTY), Collections.singletonList(two));
        check(plan != null && plan.get(0).getCount() == 64 && plan.get(1).getCount() == 1, "Split a result across a partial stack and empty slot");
        check(sixtyThree.getCount() == 63 && two.getCount() == 2, "Planning must not mutate live inventory or recipe outputs");
        plan = SpinningWheelTile.planOutputs(Arrays.asList(sixtyThree, ItemStack.EMPTY), Arrays.asList(two, new ItemStack(Items.GLASS_BOTTLE)));
        check(plan == null && sixtyThree.getCount() == 63 && two.getCount() == 2, "Reject entire batch when its container cannot fit");
        ItemStack named = new ItemStack(Items.STRING); named.getOrCreateTag().putString("Custom", "keep");
        plan = SpinningWheelTile.planOutputs(Arrays.asList(named, ItemStack.EMPTY), Collections.singletonList(two));
        check(plan != null && plan.get(0).getCount() == 1 && plan.get(1).getCount() == 2, "Do not merge different NBT");
        plan = SpinningWheelTile.planOutputs(Arrays.asList(new ItemStack(Items.STRING, 64), new ItemStack(Items.STONE, 64)), Collections.singletonList(two));
        check(plan == null, "Full output stalls safely");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(Items.STRING)); ingredients.add(Ingredient.of(Items.STRING)); ingredients.add(Ingredient.of(Items.GOLD_NUGGET));
        SpinningWheelRecipe recipe = new SpinningWheelRecipe(new ResourceLocation("bewitchment:test"), ingredients, Collections.singletonList(two));
        check(recipe.matches(new Inventory(new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.STRING), ItemStack.EMPTY, new ItemStack(Items.STRING)), null), "Inputs match unordered with empty slots");
        check(!recipe.matches(new Inventory(new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.STRING, 2)), null), "Repeated ingredients need separate slots");
        check(!recipe.matches(new Inventory(new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.STRING), new ItemStack(Items.STRING), new ItemStack(Items.DIRT)), null), "Extra ingredients reject recipe");
        List<ItemStack> outputs = recipe.getOutputs(); outputs.get(0).shrink(1);
        check(recipe.getOutputs().get(0).getCount() == 2, "Callers cannot consume recipe outputs");
        System.out.println("PASS: " + checks + " spinning-wheel checks");
    }
}
