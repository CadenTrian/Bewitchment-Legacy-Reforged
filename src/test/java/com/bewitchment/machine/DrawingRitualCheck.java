package com.bewitchment.machine;

import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.common.crafting.*;
import java.util.*;

public final class DrawingRitualCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void rejects(Runnable task, String message) {
        boolean rejected = false;
        try { task.run(); } catch (IllegalArgumentException | JsonParseException e) { rejected = true; }
        check(rejected, message);
    }
    private static final class Ring implements RitualDrawing.Access {
        final Map<BlockPos, Integer> states = new LinkedHashMap<>();
        final Set<BlockPos> denied = new HashSet<>();
        int writes, failAt = -1;
        public int type(BlockPos pos) { return states.getOrDefault(pos, -1); }
        public boolean allowed(BlockPos pos) { return !denied.contains(pos); }
        public boolean setType(BlockPos pos, int type) {
            if (++writes == failAt) return false;
            states.put(pos, type); return true;
        }
    }
    private static RitualRecipe recipe(RitualEffect effect, int seconds, int running, int[] rings, ItemStack output) {
        NonNullList<Ingredient> inputs = NonNullList.create(); inputs.add(Ingredient.of(Items.CLAY_BALL));
        return new RitualRecipe(new ResourceLocation("bewitchment", "drawing_test"), inputs, output, seconds, 150, running, rings, effect);
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        CraftingHelper.register(new ResourceLocation("minecraft", "item"), VanillaIngredientSerializer.INSTANCE);
        BlockPos center = new BlockPos(-19, 70, -33);
        for (int size = 0; size < 3; size++) {
            int count = new int[]{16, 28, 40}[size];
            Ring world = new Ring(); world.states.put(center, 0);
            for (int ring = 0; ring < 3; ring++) for (BlockPos pos : RitualCircle.positions(ring, center)) world.states.put(pos, 1);
            Map<BlockPos, Integer> original = new LinkedHashMap<>(world.states);
            List<BlockPos> positions = RitualCircle.positions(size, center);
            check(positions.size() == count && new HashSet<>(positions).size() == count && !positions.contains(center), "Exact distinct target glyphs exclude focal storage");
            RitualDrawing.Plan plan = RitualDrawing.plan(size, center, 2, world);
            check(plan != null && plan.size() == count && world.writes == 0, "Preflight is read-only");
            check(plan.apply() && world.writes == count, "Draw changes exactly the target ring");
            check(world.states.entrySet().stream().allMatch(e -> e.getValue() == (positions.contains(e.getKey()) ? 2 : original.get(e.getKey()))), "Other rings and center preserved");
            world.writes = 0;
            check(RitualDrawing.plan(size, center, 2, world).apply() && world.writes == 0, "Already matching chalk is a valid no-op repaint");
            for (BlockPos missing : positions) {
                int previous = world.states.remove(missing);
                check(RitualDrawing.plan(size, center, 3, world) == null && world.writes == 0, "Every existing target glyph is required");
                world.states.put(missing, previous);
            }
            BlockPos first = positions.get(0);
            world.denied.add(first);
            check(RitualDrawing.plan(size, center, 1, world) == null && world.writes == 0, "Support/permission denial rejects before mutation");
            world.denied.clear();
            RitualDrawing.Plan changed = RitualDrawing.plan(size, center, 1, world);
            world.states.put(first, 3);
            check(!changed.apply() && world.writes == 0, "Changed glyph invalidates prepared draw");
            world.states.put(first, 2);
            RitualDrawing.Plan denied = RitualDrawing.plan(size, center, 1, world); world.denied.add(first);
            check(!denied.apply() && world.writes == 0, "Changed permission invalidates prepared draw");
            world.denied.clear();
            Map<BlockPos, Integer> beforeFailure = new LinkedHashMap<>(world.states);
            world.failAt = 3;
            check(!RitualDrawing.plan(size, center, 1, world).apply() && world.states.equals(beforeFailure), "Failed placement rolls earlier glyph changes back");
            world.states.put(first, 0);
            check(RitualDrawing.plan(size, center, 1, world) == null, "Focal storage cannot be repainted");
            check(RitualDrawing.enoughChalk(1, 128 - count, 128, count, false), "Exact remaining durability accepted");
            check(!RitualDrawing.enoughChalk(1, 129 - count, 128, count, false), "One missing use rejected");
        }
        for (int type = 1; type <= 3; type++) {
            check(RitualDrawing.enoughChalk(type, 0, 128, 40, false), "Each non-focal chalk supported");
            check(RitualDrawing.enoughChalk(type, 127, 128, 40, true), "Creative bypasses durability cost");
        }
        check(!RitualDrawing.enoughChalk(0, 0, 128, 16, true), "Creative cannot draw focal rings");
        check(!RitualDrawing.enoughChalk(4, 0, 128, 16, false), "Any is a requirement, not drawable chalk");
        check(!RitualDrawing.enoughChalk(1, -1, 128, 16, false), "Malformed negative damage rejected");
        check(!RitualDrawing.enoughChalk(1, Integer.MAX_VALUE, 128, 16, false), "Overdamaged chalk cannot overflow check");
        rejects(() -> RitualCircle.positions(3, center), "Unknown circle size rejected");

        RitualRecipe.Serializer serializer = new RitualRecipe.Serializer();
        ResourceLocation id = new ResourceLocation("bewitchment", "drawing_test");
        RitualEffect[] effects = {RitualEffect.DRAW_SMALL, RitualEffect.DRAW_MEDIUM, RitualEffect.DRAW_LARGE};
        for (int size = 0; size < effects.length; size++) {
            RitualEffect effect = effects[size];
            JsonObject json = new JsonParser().parse("{\"ingredients\":[{\"item\":\"minecraft:clay_ball\"}],\"seconds\":0,\"starting_power\":150,\"running_power\":0}").getAsJsonObject();
            json.addProperty("effect", effect.id()); JsonArray circles = new JsonArray();
            for (int ring = 0; ring <= size; ring++) circles.add(new JsonPrimitive("any"));
            json.add("circles", circles); RitualRecipe drawing = serializer.fromJson(id, json);
            check(drawing.effect.drawingRing() == size && drawing.seconds == 0 && drawing.effect.failure(false, false) == null, "Instant effect has target size and no dimension restriction");
            check(!drawing.acceptsRun(""), "Instant recipe cannot resume as a timed saved ritual");
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                serializer.toNetwork(buffer, drawing); RitualRecipe copy = serializer.fromNetwork(id, buffer);
                check(copy.effect == effect && copy.seconds == 0 && copy.runningPower == 0 && copy.getResultItem().isEmpty() && copy.circleType(size) == 4 && !buffer.isReadable(), "Instant recipe round trip preserves zero duration and ANY circles");
            } finally { buffer.release(); }
        }
        rejects(() -> recipe(RitualEffect.DRAW_SMALL, 1, 0, new int[]{4,-1,-1}, ItemStack.EMPTY), "Drawing cannot run for a finite second");
        rejects(() -> recipe(RitualEffect.DRAW_SMALL, -1, 0, new int[]{4,-1,-1}, ItemStack.EMPTY), "Drawing cannot run continuously");
        rejects(() -> recipe(RitualEffect.DRAW_SMALL, 0, 1, new int[]{4,-1,-1}, ItemStack.EMPTY), "Instant drawing cannot have running power");
        rejects(() -> recipe(RitualEffect.DRAW_SMALL, 0, 0, new int[]{4,-1,-1}, new ItemStack(Items.BOOK)), "Drawing cannot create an item");
        rejects(() -> recipe(RitualEffect.DRAW_MEDIUM, 0, 0, new int[]{4,-1,-1}, ItemStack.EMPTY), "Drawing requires its target ring");
        rejects(() -> recipe(RitualEffect.DRAW_LARGE, 0, 0, new int[]{4,4,-1}, ItemStack.EMPTY), "Large drawing requires all rings");
        check(RitualPower.startingCost(150,true)==127 && RitualPower.startingCost(300,true)==255 && RitualPower.startingCost(450,true)==382, "Legacy Hecate drawing discounts");
        System.out.println("PASS: " + checks + " drawing preflight, rollback, durability, recipe and synchronization checks.");
    }
}
