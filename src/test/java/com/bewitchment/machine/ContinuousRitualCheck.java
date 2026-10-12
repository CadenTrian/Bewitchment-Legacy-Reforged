package com.bewitchment.machine;

import com.google.gson.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Bootstrap;

public final class ContinuousRitualCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void rejects(Runnable task, String message) {
        boolean rejected = false;
        try { task.run(); } catch (IllegalArgumentException | JsonParseException e) { rejected = true; }
        check(rejected, message);
    }
    private static RitualRecipe recipe(RitualEffect effect, int seconds, ItemStack output) {
        NonNullList<Ingredient> inputs = NonNullList.create(); inputs.add(Ingredient.of(Items.SAND));
        return new RitualRecipe(new ResourceLocation("bewitchment", "continuous_test"), inputs, output,
                seconds, 600, 50, new int[]{1, 1, -1}, effect);
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        RitualRecipe sands = recipe(RitualEffect.SANDS_OF_TIME, -1, ItemStack.EMPTY);
        RitualRecipe perception = recipe(RitualEffect.PERCEPTION, -1, ItemStack.EMPTY);
        RitualRecipe finite = recipe(RitualEffect.ITEM, 2, new ItemStack(Items.BOOK));
        check(sands.seconds == -1 && sands.getResultItem().isEmpty(), "Continuous recipe has no finish or output");
        rejects(() -> recipe(RitualEffect.SANDS_OF_TIME, 5, ItemStack.EMPTY), "Continuous effect cannot acquire finite duration");
        rejects(() -> recipe(RitualEffect.PERCEPTION, -1, new ItemStack(Items.BOOK)), "Reject continuous item output");
        rejects(() -> recipe(RitualEffect.ITEM, -1, new ItemStack(Items.BOOK)), "Reject indefinite crafting");
        rejects(() -> recipe(RitualEffect.SANDS_OF_TIME, -2, ItemStack.EMPTY), "Reject other negative durations");
        check("overworld".equals(RitualEffect.SANDS_OF_TIME.failure(false, true)), "Sands confined to Overworld");
        check(RitualEffect.PERCEPTION.failure(false, false) == null, "Perception works in other dimensions");

        RitualClock clock = new RitualClock(); int[] paid = {0}; boolean bounded = true;
        for (int tick = 1; tick <= 100000; tick++) {
            RitualClock.Step step = clock.advanceContinuous(50, amount -> { paid[0] += amount; return true; });
            bounded &= clock.ticks() == tick % 20 && step == (tick % 20 == 0 ? RitualClock.Step.PAID : RitualClock.Step.WAITING);
        }
        check(bounded && paid[0] == 250000, "Long run remains bounded and pays every second without finishing");
        for (int phase = 0; phase < 20; phase++) {
            RitualClock saved = new RitualClock(); saved.load(phase);
            RitualClock loaded = new RitualClock(); loaded.load(saved.ticks()); int[] calls = {0};
            for (int i = phase; i < 20; i++) loaded.advanceContinuous(25, cost -> { calls[0]++; return true; });
            check(calls[0] == 1 && loaded.ticks() == 0, "Save/reload preserves payment boundary at phase " + phase);
        }
        clock.load(19);
        check(clock.advanceContinuous(50, amount -> false) == RitualClock.Step.NO_POWER && clock.ticks() == 19, "Failed drain neither advances nor finishes");
        check(clock.advanceContinuous(50, amount -> true) == RitualClock.Step.PAID && clock.ticks() == 0, "Next paid second wraps safely");
        clock.load(Integer.MAX_VALUE);
        check(clock.advanceContinuous(0, amount -> { throw new AssertionError("Free ritual must not drain"); }) == RitualClock.Step.PAID && clock.ticks() == 0, "Huge saved progress normalizes and free cost skips payer");
        rejects(() -> clock.advanceContinuous(-1, amount -> true), "Negative running cost rejected");
        clock.reset(); check(clock.ticks() == 0, "Stopping resets payment phase");

        RitualInventory inventory = new RitualInventory(() -> {});
        inventory.insertItem(0, new ItemStack(Items.SAND), false);
        RitualConsumption state = new RitualConsumption(); state.start(finite, inventory);
        check(!state.consumed() && !inventory.getStackInSlot(0).isEmpty() && state.accepts(finite), "Finite start retains offerings");
        check(!state.accepts(sands), "Old finite save cannot become continuous for free");
        state.start(sands, inventory);
        check(state.consumed() && inventory.getStackInSlot(0).isEmpty() && state.accepts(sands), "Continuous start consumes offerings and records effect");
        check(!state.accepts(finite) && !state.accepts(perception), "Datapack change cannot turn consumed run into another effect/output");
        CompoundNBT tag = new CompoundNBT(); state.save(tag, true);
        tag.put("Inventory", inventory.serializeNBT());
        RitualConsumption restored = new RitualConsumption(); restored.load(tag, true);
        RitualInventory restoredItems = new RitualInventory(() -> {}); restoredItems.deserializeNBT(tag.getCompound("Inventory"));
        check(restored.accepts(sands) && restored.consumed() && restoredItems.getStackInSlot(0).isEmpty(), "Active save retains consumption without recreating inputs");
        restored.reset();
        check(!restored.consumed() && restoredItems.extractItem(0, 1, false).isEmpty(), "Cancellation cannot refund consumed offerings");
        restored.save(tag, false); check(!tag.contains("ConsumedEffect"), "Idle save removes stale consumed marker");
        tag.putString("ConsumedEffect", "sands_of_time"); restored.load(tag, false);
        check(!restored.consumed() && restored.accepts(finite), "Inactive stale marker ignored");
        tag.putString("ConsumedEffect", "unknown"); restored.load(tag, true);
        check(!restored.accepts(sands) && !restored.accepts(finite), "Unknown saved marker cannot resume either lifecycle");
        restored.load(new CompoundNBT(), true); check(!restored.accepts(sands) && restored.accepts(finite), "Missing marker preserves old finite saves only");

        check(ContinuousRitualEffects.acceleratedTime(23998) == 24003, "Sands crosses midnight without resetting date");
        check(ContinuousRitualEffects.acceleratedTime(-5) == 0, "Negative command-set time advances correctly");
        check(ContinuousRitualEffects.acceleratedTime(Long.MAX_VALUE - 2) == Long.MAX_VALUE, "Sands does not overflow world time");
        long time = 0;
        for (int i = 0; i < 20; i++) time = ContinuousRitualEffects.acceleratedTime(time);
        check(time == 100, "Adds 100 daylight ticks per normal game second");
        boolean pulses = true;
        for (int i = 1; i <= 500; i++) pulses &= ContinuousRitualEffects.perceptionDue(i) == (i == 100 || i == 200 || i == 300 || i == 400 || i == 500);
        check(pulses && ContinuousRitualEffects.perceptionDue(0), "Perception follows elapsed world ticks every five seconds");
        AxisAlignedBB box = ContinuousRitualEffects.perceptionBounds(new BlockPos(-10, 64, 30));
        check(box.minX == -30 && box.maxX == 11 && box.minY == 44 && box.maxY == 85 && box.minZ == 10 && box.maxZ == 51, "Exact legacy block box expanded by twenty");
        EffectInstance reveal = ContinuousRitualEffects.revealed();
        check(reveal.getEffect() == Effects.GLOWING && reveal.getDuration() == 110 && reveal.getAmplifier() == 0 && !reveal.isVisible(), "Reveal uses 110 ticks of Glowing I without particles");
        check(RitualPower.startingCost(600, true) == 510 && RitualPower.startingCost(500, true) == 425, "Hecate continuous startup costs");
        System.out.println("PASS: " + checks + " continuous ritual payment, consumption, persistence and effect checks.");
    }
}
