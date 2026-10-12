package com.bewitchment.machine;

import com.google.gson.JsonParseException;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Bootstrap;

public final class FlamesGrowthCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void rejects(Runnable task, String message) {
        boolean rejected = false;
        try { task.run(); } catch (IllegalArgumentException | JsonParseException e) { rejected = true; }
        check(rejected, message);
    }
    private static RitualRecipe recipe(RitualEffect effect, int duration, ItemStack output) {
        NonNullList<Ingredient> inputs = NonNullList.create(); inputs.add(Ingredient.of(Items.COAL));
        return new RitualRecipe(new ResourceLocation("bewitchment", "timed_active_test"), inputs, output, duration, 333, 25, new int[]{2,-1,-1}, effect);
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        ItemStack input = new ItemStack(Items.IRON_ORE, 5); input.getOrCreateTag().putInt("marker", 42);
        ItemStack cooked = new ItemStack(Items.IRON_INGOT); cooked.getOrCreateTag().putString("recipe", "kept");
        HungryFlames.Outcome success = HungryFlames.plan(input, cooked, .699999);
        check(success.remaining.getCount() == 4 && success.product.getItem() == Items.IRON_INGOT && success.experience == 0, "Seventy-percent branch consumes one and smelts");
        check(success.remaining.getTag().getInt("marker") == 42 && success.product.getTag().getString("recipe").equals("kept"), "Preserve input and recipe result NBT");
        check(input.getCount() == 5 && cooked.getCount() == 1, "Planning never mutates source stacks");
        success.remaining.getTag().putInt("marker", 0); success.product.shrink(1);
        check(input.getTag().getInt("marker") == 42 && cooked.getCount() == 1, "Outcome owns independent copies");
        for (double roll : new double[]{.7, .8, .999999}) {
            HungryFlames.Outcome xp = HungryFlames.plan(input, cooked, roll);
            check(xp.remaining.getCount() == 4 && xp.product.isEmpty() && xp.experience == 2, "Failure consumes one and awards exactly two XP");
        }
        check(HungryFlames.plan(new ItemStack(Items.IRON_ORE), cooked, 0).remaining.isEmpty(), "Last input is consumed completely");
        check(HungryFlames.plan(input, ItemStack.EMPTY, 0) == null, "Unsmeltable input is unchanged");
        check(HungryFlames.plan(ItemStack.EMPTY, cooked, 0) == null, "Empty input ignored");
        check(HungryFlames.plan(new ItemStack(Items.LAVA_BUCKET), cooked, 0) == null, "Container items are excluded");
        check(HungryFlames.plan(input, new ItemStack(Items.IRON_INGOT, 65), 0) == null, "Reject invalid oversized recipe result");
        rejects(() -> HungryFlames.plan(input, cooked, Double.NaN), "Reject nonfinite random roll");
        rejects(() -> HungryFlames.plan(input, cooked, 1), "Reject out of range random roll");
        check(HungryFlames.due(0) && HungryFlames.due(40) && HungryFlames.due(80) && !HungryFlames.due(39), "Pulse every forty elapsed ticks");
        AxisAlignedBB box = HungryFlames.bounds(new BlockPos(10,64,-10));
        check(box.minX == 7 && box.maxX == 14 && box.minY == 61 && box.maxY == 68 && box.minZ == -13 && box.maxZ == -6, "Exact seven-block legacy item area");
        RitualRecipe flames = recipe(RitualEffect.HUNGRY_FLAMES, 180, ItemStack.EMPTY);
        check(flames.effect.consumesOnStart() && !flames.effect.continuous(), "Timed active effect is distinct from indefinite effects");
        rejects(() -> recipe(RitualEffect.HUNGRY_FLAMES, -1, ItemStack.EMPTY), "Flames cannot run indefinitely");
        rejects(() -> recipe(RitualEffect.HUNGRY_FLAMES, 180, cooked), "Flames cannot create a completion item");
        RitualInventory offerings = new RitualInventory(() -> {}); offerings.insertItem(0, new ItemStack(Items.COAL), false);
        RitualConsumption consumption = new RitualConsumption(); consumption.start(flames, offerings);
        check(consumption.consumed() && offerings.getStackInSlot(0).isEmpty(), "Timed active start consumes offerings");
        CompoundNBT tag = new CompoundNBT(); consumption.save(tag, true);
        RitualConsumption restored = new RitualConsumption(); restored.load(tag, true);
        check(restored.accepts(flames), "Timed consumption survives reload");
        check(!restored.accepts(recipe(RitualEffect.FRENZIED_GROWTH, 5, ItemStack.EMPTY)), "Changed effect cannot reuse consumed offerings");
        RitualClock clock = new RitualClock(); int[] power = {0}; boolean early = false;
        for (int tick = 0; tick < 3599; tick++) early |= clock.advance(180,25, amount -> { power[0] += amount; return true; }) == RitualClock.Step.FINISHED;
        check(!early && power[0] == 4475, "Flames lasts full three minutes");
        check(clock.advance(180,25, amount -> false) == RitualClock.Step.NO_POWER && clock.ticks() == 3599, "Final effect tick requires payment");
        check(clock.advance(180,25, amount -> { power[0] += amount; return true; }) == RitualClock.Step.FINISHED && power[0] == 4500, "Completes after 180 running payments");
        check(FrenziedGrowth.target(Blocks.OAK_SAPLING) == FrenziedGrowth.Target.ELDER, "Oak to Elder");
        check(FrenziedGrowth.target(Blocks.BIRCH_SAPLING) == FrenziedGrowth.Target.ELDER, "Birch to Elder");
        check(FrenziedGrowth.target(Blocks.SPRUCE_SAPLING) == FrenziedGrowth.Target.CYPRESS, "Spruce to Cypress");
        check(FrenziedGrowth.target(Blocks.JUNGLE_SAPLING) == FrenziedGrowth.Target.CYPRESS, "Jungle to Cypress");
        check(FrenziedGrowth.target(Blocks.ACACIA_SAPLING) == FrenziedGrowth.Target.JUNIPER, "Acacia to Juniper");
        check(FrenziedGrowth.target(Blocks.DARK_OAK_SAPLING) == FrenziedGrowth.Target.NONE, "Dark Oak is excluded");
        check(FrenziedGrowth.target(Blocks.OAK_LOG) == FrenziedGrowth.Target.NONE && FrenziedGrowth.target(Blocks.POTTED_OAK_SAPLING) == FrenziedGrowth.Target.NONE, "Trees and potted saplings are excluded");
        BlockPos center = new BlockPos(-30, 64, 70); int count = 0; boolean inBounds = true, lower = false, upper = false;
        for (BlockPos pos : FrenziedGrowth.positions(center)) {
            count++; inBounds &= Math.abs(pos.getX()-center.getX()) <= 16 && Math.abs(pos.getY()-center.getY()) <= 16 && Math.abs(pos.getZ()-center.getZ()) <= 16;
            lower |= pos.equals(center.offset(-16,-16,-16)); upper |= pos.equals(center.offset(16,16,16));
        }
        check(count == 35937 && inBounds && lower && upper, "Legacy inclusive 33-cube scan at negative coordinates");
        check(RitualPower.startingCost(333,true) == 283 && RitualPower.startingCost(550,true) == 467, "Startup discounts retain legacy rounding");
        System.out.println("PASS: " + checks + " Hungry Flames and Frenzied Growth checks.");
    }
}
