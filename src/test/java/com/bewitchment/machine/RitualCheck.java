package com.bewitchment.machine;

import com.bewitchment.block.GlyphBlock;
import com.bewitchment.item.ChalkItem;
import com.google.gson.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import io.netty.buffer.Unpooled;
import java.util.*;

public final class RitualCheck {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static void rejects(Runnable code, String message) { boolean failed = false; try { code.run(); } catch (IllegalArgumentException | JsonParseException e) { failed = true; } check(failed, message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        // Standalone checks do not run Forge's recipe-serializer registration event.
        net.minecraftforge.common.crafting.CraftingHelper.register(new ResourceLocation("minecraft", "item"),
                net.minecraftforge.common.crafting.VanillaIngredientSerializer.INSTANCE);
        BlockPos center = new BlockPos(-123, 70, 21); Set<BlockPos> required = new HashSet<>();
        check(RitualCircle.matches(center, p -> { required.add(p); return true; }) && required.size() == 16, "Legacy small circle has sixteen positions");
        check(!required.contains(center) && required.stream().allMatch(p -> p.getY() == center.getY()), "Ring is horizontal and excludes focal center");
        check(required.contains(center.offset(-3, 0, 0)) && required.contains(center.offset(0, 0, 3)) && required.contains(center.offset(2, 0, 2)), "Cardinals and corners match original circle");
        for (BlockPos missing : required) check(!RitualCircle.matches(center, p -> !p.equals(missing)), "Every circle glyph is required: " + missing);
        check(RitualCircle.matches(center, required::contains), "Exact translated circle passes");
        check(GlyphBlock.letter(new BlockPos(-1, 0, -2)) == 5 && GlyphBlock.letter(new BlockPos(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)) >= 0, "Glyph letters support negative/extreme positions");
        RitualInventory storage = new RitualInventory(() -> {});
        ItemStack source = new ItemStack(Items.PAPER, 3); source.getOrCreateTag().putString("owner", "source");
        check(storage.insertItem(0, source, true).getCount() == 2 && storage.getStackInSlot(0).isEmpty(), "Simulated insertion does not store items");
        check(storage.insertItem(0, source, false).getCount() == 2 && storage.getStackInSlot(0).getCount() == 1 && source.getCount() == 3, "Insert exactly one item without mutating caller");
        source.getTag().putString("owner", "changed");
        check(storage.getStackInSlot(0).getTag().getString("owner").equals("source"), "Storage owns independent NBT");
        CompoundNBT saved = storage.serializeNBT(); saved.putInt("Size", 100000);
        RitualInventory restored = new RitualInventory(() -> {}); restored.deserializeNBT(saved);
        check(restored.getSlots() == 10 && restored.getStackInSlot(0).getItem() == Items.PAPER && restored.getStackInSlot(0).getCount() == 1, "NBT restores fixed ten-slot storage");
        check(restored.extractItem(0, 64, false).getCount() == 1 && restored.extractItem(0, 64, false).isEmpty(), "Repeated cancellation/break extraction cannot duplicate items");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(Items.PAPER)); ingredients.add(Ingredient.of(Items.PAPER)); ingredients.add(Ingredient.of(Items.LEATHER));
        ItemStack output = new ItemStack(Items.BOOK); output.getOrCreateTag().putInt("amount", 0); output.getTag().putInt("maxAmount", 1000);
        ResourceLocation id = new ResourceLocation("bewitchment", "test_ritual");
        RitualRecipe recipe = new RitualRecipe(id, ingredients, output, 5, 150, 20);
        Inventory inventory = new Inventory(10); inventory.setItem(0, new ItemStack(Items.LEATHER)); inventory.setItem(4, new ItemStack(Items.PAPER)); inventory.setItem(8, new ItemStack(Items.PAPER));
        check(recipe.matches(inventory, null), "Order-independent repeated ingredients match");
        inventory.setItem(8, ItemStack.EMPTY); inventory.setItem(4, new ItemStack(Items.PAPER, 2));
        check(!recipe.matches(inventory, null), "Repeated ingredients require distinct stored offers");
        inventory.setItem(8, new ItemStack(Items.PAPER)); inventory.setItem(9, new ItemStack(Items.DIRT));
        check(!recipe.matches(inventory, null), "Extra ingredients cannot be consumed accidentally");
        output.getTag().putInt("maxAmount", 2); ingredients.clear();
        check(recipe.getResultItem().getTag().getInt("maxAmount") == 1000 && recipe.getIngredients().size() == 3, "Recipe owns its lists and output NBT");
        recipe.getResultItem().shrink(1);
        check(!recipe.assemble(inventory).isEmpty(), "Crafting/result consumers cannot empty shared output");
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            RitualRecipe.Serializer serializer = new RitualRecipe.Serializer(); serializer.toNetwork(buffer, recipe);
            RitualRecipe received = serializer.fromNetwork(id, buffer);
            check(received.seconds == 5 && received.startingPower == 150 && received.runningPower == 20 && received.getIngredients().size() == 3
                    && received.getResultItem().getTag().getInt("maxAmount") == 1000, "Recipe network round trip preserves costs, repeated inputs and NBT");
        } finally { buffer.release(); }
        rejects(() -> new RitualRecipe(id, NonNullList.create(), new ItemStack(Items.BOOK), 5, 150, 20), "Reject empty recipes");
        rejects(() -> new RitualRecipe(id, recipe.getIngredients(), output, 0, 150, 20), "Reject zero duration");
        rejects(() -> new RitualRecipe(id, recipe.getIngredients(), output, 5, -1, 20), "Reject negative power");
        rejects(() -> new RitualRecipe(id, recipe.getIngredients(), ItemStack.EMPTY, 5, 150, 20), "Reject empty outputs");
        RitualClock clock = new RitualClock(); int[] paid = {0}; RitualClock.Step last = null;
        for (int i = 0; i < 99; i++) { last = clock.advance(5, 20, cost -> { paid[0] += cost; return true; }); if (last == RitualClock.Step.FINISHED) throw new AssertionError("Finished early"); }
        check(clock.ticks() == 99 && paid[0] == 80, "No completion before fifth full paid second");
        check(clock.advance(5, 20, cost -> { paid[0] += cost; return true; }) == RitualClock.Step.FINISHED && paid[0] == 100, "Exactly five payments over 100 ticks");
        clock.load(39);
        check(clock.advance(5, 20, cost -> false) == RitualClock.Step.NO_POWER && clock.ticks() == 39, "Failed payment cannot advance progress");
        RitualClock resumed = new RitualClock(); resumed.load(clock.ticks());
        check(resumed.advance(5, 20, cost -> true) == RitualClock.Step.PAID && resumed.ticks() == 40, "Saved partial second resumes at next required payment");
        resumed.reset(); check(resumed.ticks() == 0, "New/cancelled rituals reset progress");
        resumed.load(-1); check(resumed.ticks() == 0, "Negative saved progress clamps");
        resumed.load(Integer.MAX_VALUE); check(resumed.ticks() == 71999, "Oversized saved progress clamps");
        resumed.load(19); check(resumed.advance(1, 0, cost -> { throw new AssertionError("Zero power must not drain"); }) == RitualClock.Step.FINISHED, "Zero-power datapack rituals do not call power source");
        ChalkItem chalk = new ChalkItem(1); ItemStack chalkStack = new ItemStack(chalk);
        check(chalkStack.getMaxDamage() == 128 && chalkStack.getMaxStackSize() == 1 && !chalk.isRepairable(chalkStack) && !chalk.isEnchantable(chalkStack), "Chalk retains 128 uses, stack limit and no repair/enchanting");
        CompoundNBT state = new CompoundNBT();
        check(GlyphTile.readRitualId(state) == null, "Idle glyph save must not become an active empty-name ritual");
        state.putString("Ritual", ""); check(GlyphTile.readRitualId(state) == null, "Explicit empty ID remains idle");
        state.putString("Ritual", "INVALID SPACE"); check(GlyphTile.readRitualId(state) == null, "Malformed saved ID is safely ignored");
        state.putString("Ritual", "bewitchment:ritual/grimoire_magia"); check(GlyphTile.readRitualId(state).toString().equals("bewitchment:ritual/grimoire_magia"), "Valid saved ritual ID can resume");
        Set<BlockPos> all = new HashSet<>(required);
        for (int ring = 1; ring <= 2; ring++) {
            final int index = ring; Set<BlockPos> positions = new HashSet<>();
            check(RitualCircle.matches(index, center, p -> { positions.add(p); return true; }) && positions.size() == (ring == 1 ? 28 : 40), "Outer ring legacy count");
            check(Collections.disjoint(all, positions) && !positions.contains(center), "Concentric rings do not overlap");
            for (BlockPos missing : positions) check(!RitualCircle.matches(index, center, p -> !p.equals(missing)), "Every outer glyph is mandatory");
            all.addAll(positions);
        }
        rejects(() -> RitualCircle.matches(3, center, p -> true), "Reject unknown ring size");
        for (int actual = 0; actual <= 4; actual++) {
            check(RitualCircle.accepts(4, actual) == (actual >= 1 && actual <= 3), "Any accepts only non-focal chalk");
            for (int wanted = 1; wanted <= 3; wanted++) check(RitualCircle.accepts(wanted, actual) == (wanted == actual), "Typed glyph matching");
        }
        int[] rings = {1, 2, 3};
        RitualRecipe typed = new RitualRecipe(id, recipe.getIngredients(), recipe.getResultItem(), 2, 200, 20, rings);
        rings[1] = 4; check(typed.circleType(1) == 2, "Recipe owns ring array");
        PacketBuffer typedBuffer = new PacketBuffer(Unpooled.buffer());
        try {
            RitualRecipe.Serializer serializer = new RitualRecipe.Serializer(); serializer.toNetwork(typedBuffer, typed);
            RitualRecipe received = serializer.fromNetwork(id, typedBuffer);
            check(received.circleType(0) == 1 && received.circleType(1) == 2 && received.circleType(2) == 3, "Typed circles survive network synchronization");
        } finally { typedBuffer.release(); }
        for (int[] invalid : new int[][]{{0, -1, -1}, {-1, -1, -1}, {1, -1, 3}, {1, 5, -1}, {1}})
            rejects(() -> new RitualRecipe(id, recipe.getIngredients(), recipe.getResultItem(), 2, 200, 20, invalid), "Reject invalid circle schema");
        JsonObject json = new JsonParser().parse("{\"ingredients\":[{\"item\":\"minecraft:dirt\"}],\"result\":{\"item\":\"minecraft:book\"},\"seconds\":2,\"starting_power\":200,\"running_power\":20}").getAsJsonObject();
        RitualRecipe.Serializer serializer = new RitualRecipe.Serializer();
        check(serializer.fromJson(id, json).circleType(1) == -1, "Old recipe default remains small normal");
        json.add("circles", new JsonParser().parse("[\"any\",\"nether\",\"ender\"]"));
        check(serializer.fromJson(id, json).circleType(0) == 4, "JSON accepts typed concentric circles");
        json.addProperty("circle", "small_normal"); rejects(() -> serializer.fromJson(id, json), "Reject ambiguous circle fields");
        json.remove("circle"); json.add("circles", new JsonParser().parse("[\"unknown\"]"));
        rejects(() -> serializer.fromJson(id, json), "Reject unknown chalk type");
        check(RitualPower.startingCost(150, true) == 127 && RitualPower.startingCost(200, true) == 170, "Visage floors legacy startup discount");
        check(RitualPower.startingCost(150, false) == 150 && RitualPower.startingCost(0, true) == 0, "Normal and free startup unchanged");
        check(RitualPower.startingCost(Integer.MAX_VALUE, true) > 0, "Large costs do not overflow");
        rejects(() -> RitualPower.startingCost(-1, true), "Negative startup rejected");
        // Explicit truth table: bit 0 silver, bit 1 werewolf, bit 2 cold iron, bit 3 already burning.
        Set<Integer> ignites = new HashSet<>(Arrays.asList(1, 4, 5, 6, 7, 9, 13));
        for (int bits = 0; bits < 16; bits++) check(com.bewitchment.block.PurifyingEarthBlock.shouldIgnite(
                (bits & 1) != 0, (bits & 2) != 0, (bits & 4) != 0, (bits & 8) != 0) == ignites.contains(bits), "Purifying Earth ignition truth table " + bits);
        System.out.println("PASS: " + checks + " ritual circle, inventory, recipe, timing and chalk checks.");
    }
}
