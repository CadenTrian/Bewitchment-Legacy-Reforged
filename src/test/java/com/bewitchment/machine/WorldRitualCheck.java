package com.bewitchment.machine;

import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.common.crafting.*;

public final class WorldRitualCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void rejects(Runnable task, String message) {
        boolean rejected = false;
        try { task.run(); } catch (IllegalArgumentException | JsonParseException e) { rejected = true; }
        check(rejected, message);
    }
    private static final class Environment implements RitualEffect.Environment {
        long time = 19000; int writes, rainTicks, roll = 1234, rainStarts, rainStops;
        public long dayTime() { return time; }
        public void setDayTime(long value) { time = value; writes++; }
        public int rainRoll() { return roll; }
        public void startRain(int ticks) { rainTicks = ticks; rainStarts++; }
        public void stopRain() { rainStops++; }
        public void startStorm() { rainStarts++; }
        public void stopStorm() { rainStops++; }
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        CraftingHelper.register(new ResourceLocation("minecraft", "item"), VanillaIngredientSerializer.INSTANCE);
        for (RitualEffect effect : RitualEffect.values()) {
            check(RitualEffect.parse(effect.id()) == effect, "Stable effect names round trip");
            check((effect.failure(false, true) == null) == (effect == RitualEffect.LESSER_CURSE_CLEANSING || effect == RitualEffect.GREATER_CURSE_CLEANSING || effect == RitualEffect.ITEM || effect == RitualEffect.PERCEPTION || effect.instant() || effect == RitualEffect.HUNGRY_FLAMES || effect == RitualEffect.FRENZIED_GROWTH || (effect.conjuration() && effect != RitualEffect.CONJURE_IMP && effect != RitualEffect.CONJURE_DEMON && effect != RitualEffect.CONJURE_LEONARD && effect != RitualEffect.CONJURE_BAPHOMET) || effect == RitualEffect.TELEPORT || (effect == RitualEffect.LESSER_HELLMOUTH || effect == RitualEffect.HELLMOUTH || effect == RitualEffect.GREATER_HELLMOUTH) || effect == RitualEffect.BIOME_SHIFT || effect == RitualEffect.CALL_OF_THE_WILD), "Only local effects accept other dimensions");
        }
        check("no_day".equals(RitualEffect.SOLAR_GLORY.failure(true, true)), "Solar refuses daytime before paying");
        check(RitualEffect.SOLAR_GLORY.failure(true, false) == null, "Solar accepts night");
        check("no_night".equals(RitualEffect.HIGH_MOON.failure(true, false)), "Moon refuses night before paying");
        check(RitualEffect.HIGH_MOON.failure(true, true) == null, "Moon accepts day");
        check(RitualEffect.DELUGE.failure(true, true) == null && RitualEffect.DELUGE.failure(true, false) == null, "Deluge has no time restriction");
        check(RitualEffect.DROUGHT.failure(true, true) == null && RitualEffect.DROUGHT.failure(true, false) == null, "Drought has no time restriction");
        rejects(() -> RitualEffect.parse("sunrise"), "Reject unknown effect");
        rejects(() -> RitualEffect.parse("SOLAR_GLORY"), "Reject noncanonical effect name");
        for (int phase : new int[]{6000, 17600}) {
            boolean valid = true;
            for (long time = -24000; time < 48000; time++) {
                long next = RitualEffect.nextTime(time, phase);
                valid &= next >= time && next - time < 24000 && Math.floorMod(next, 24000L) == phase;
            }
            check(valid, "Time advances to target phase over negative, current and next days");
            check(RitualEffect.nextTime(phase, phase) == phase, "Already at phase does not skip a day");
        }
        check(RitualEffect.nextTime(19000, 6000) == 30000, "Solar reaches next noon");
        check(RitualEffect.nextTime(6000, 17600) == 17600, "Moon retains legacy 17600 target");
        check(RitualEffect.nextTime(Long.MAX_VALUE, 6000) == Long.MAX_VALUE, "No overflow rewinds huge world times");
        rejects(() -> RitualEffect.nextTime(0, 24000), "Reject invalid phase");
        check(RitualEffect.rainDuration(0) == 3600 && RitualEffect.rainDuration(8399) == 11999, "Rain range is three to just under ten minutes");
        rejects(() -> RitualEffect.rainDuration(-1), "Reject negative rain roll");
        rejects(() -> RitualEffect.rainDuration(8400), "Reject out of range rain roll");
        Environment env = new Environment(); RitualEffect.ITEM.complete(env);
        check(env.writes == 0 && env.rainStarts == 0 && env.rainStops == 0, "Item completion does not mutate world");
        RitualEffect.SOLAR_GLORY.complete(env);
        check(env.time == 30000 && env.writes == 1 && env.rainStarts == 0, "Solar changes only day time");
        RitualEffect.HIGH_MOON.complete(env);
        check(env.time == 41600 && env.writes == 2, "Moon preserves day count");
        RitualEffect.DELUGE.complete(env);
        check(env.rainTicks == 4834 && env.rainStarts == 1 && env.writes == 2, "Deluge changes weather without advancing time");
        RitualEffect.DROUGHT.complete(env);
        check(env.rainStops == 1 && env.rainStarts == 1 && env.writes == 2, "Drought only stops rain");

        ResourceLocation id = new ResourceLocation("bewitchment", "test_world_ritual");
        RitualRecipe.Serializer serializer = new RitualRecipe.Serializer();
        JsonObject base = new JsonParser().parse("{\"ingredients\":[{\"item\":\"minecraft:gold_ingot\"}],\"seconds\":5,\"starting_power\":250,\"running_power\":20}").getAsJsonObject();
        rejects(() -> serializer.fromJson(id, base), "Item recipes still require an output");
        for (RitualEffect effect : RitualEffect.values()) {
            JsonObject json = new JsonParser().parse(base.toString()).getAsJsonObject(); json.addProperty("effect", effect.id());
            if (effect.instant()) continue; // Instant recipes have their own drawing/network checks.
            if (effect.continuous()) json.addProperty("seconds", -1);
            if (effect == RitualEffect.ITEM || effect == RitualEffect.DELUGE || effect == RitualEffect.DROUGHT)
                json.add("result", new JsonParser().parse("{\"item\":\"minecraft:bucket\",\"nbt\":{\"marker\":7}}"));
            RitualRecipe recipe = serializer.fromJson(id, json);
            check(recipe.effect == effect && recipe.seconds == (effect.continuous() ? -1 : 5) && recipe.startingPower == 250 && recipe.runningPower == 20, "JSON preserves effect and costs");
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                serializer.toNetwork(buffer, recipe); RitualRecipe copy = serializer.fromNetwork(id, buffer);
                check(copy.effect == effect && copy.circleType(0) == 1 && copy.circleType(1) == -1 && !buffer.isReadable(), "Network preserves effect and consumes packet");
                check(copy.getResultItem().isEmpty() == recipe.getResultItem().isEmpty(), "Outputless and container-return recipes survive networking");
                if (!copy.getResultItem().isEmpty()) check(copy.getResultItem().getTag().getInt("marker") == 7, "Returned container NBT survives networking");
            } finally { buffer.release(); }
        }
        JsonObject old = new JsonParser().parse(base.toString()).getAsJsonObject(); old.add("result", new JsonParser().parse("{\"item\":\"minecraft:book\"}"));
        check(serializer.fromJson(id, old).effect == RitualEffect.ITEM, "Old datapacks default to item completion");
        JsonObject bad = new JsonParser().parse(base.toString()).getAsJsonObject(); bad.addProperty("effect", "unknown");
        rejects(() -> serializer.fromJson(id, bad), "JSON rejects unknown effect");
        bad.addProperty("effect", "solar_glory"); bad.addProperty("seconds", 0);
        rejects(() -> serializer.fromJson(id, bad), "World effects cannot bypass paid duration");
        bad.addProperty("seconds", -1);
        rejects(() -> serializer.fromJson(id, bad), "Finite effects cannot use continuous duration");
        CompoundNBT saved = new CompoundNBT(); saved.putString("Ritual", "bewitchment:ritual/deluge");
        check(GlyphTile.readRitualId(saved).toString().equals("bewitchment:ritual/deluge"), "World ritual resumes through existing recipe ID persistence");
        RitualClock clock = new RitualClock(); Environment pending = new Environment(); int[] paid = {0};
        for (int tick = 0; tick < 99; tick++) {
            RitualClock.Step step = clock.advance(5, 20, amount -> { paid[0] += amount; return true; });
            if (step == RitualClock.Step.FINISHED) RitualEffect.DELUGE.complete(pending);
        }
        check(pending.rainStarts == 0 && paid[0] == 80, "No early weather effect");
        check(clock.advance(5, 20, amount -> false) == RitualClock.Step.NO_POWER && pending.rainStarts == 0, "Failed final payment cannot finish");
        if (clock.advance(5, 20, amount -> { paid[0] += amount; return true; }) == RitualClock.Step.FINISHED) RitualEffect.DELUGE.complete(pending);
        check(pending.rainStarts == 1 && paid[0] == 100, "Final paid tick completes effect once");
        System.out.println("PASS: " + checks + " world ritual effects, preconditions, time arithmetic, recipe and payment checks.");
    }
}
