package com.bewitchment.loot;

import com.google.gson.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.LootTableLoadEvent;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;

public final class LegacyChestLootCheck {
    private static int checks;
    private static Gson GSON;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        Bootstrap.bootStrap();
        GSON = LootSerializers.createLootTableSerializer().create();
        String[] paths = {"simple_dungeon", "abandoned_mineshaft", "stronghold_corridor", "stronghold_crossing",
                "stronghold_library", "nether_bridge", "village/village_weaponsmith", "jungle_temple",
                "desert_pyramid", "igloo_chest", "end_city_treasure", "woodland_mansion"};
        Set<ResourceLocation> expected = new HashSet<>();
        for (String path : paths) expected.add(new ResourceLocation("minecraft", "chests/" + path));
        check(LegacyChestLoot.CHESTS.equals(expected), "Exact twelve legacy chest targets, with weaponsmith migration");
        try { LegacyChestLoot.CHESTS.clear(); throw new AssertionError("Mutable chest set"); }
        catch (UnsupportedOperationException correct) { checks++; }
        for (ResourceLocation id : expected) {
            LootTable table = baseTable();
            LootPool original = table.getPool("vanilla");
            LootTableLoadEvent event = new LootTableLoadEvent(id, table, null);
            LegacyChestLoot.load(event);
            boolean nether = id.equals(LootTables.NETHER_BRIDGE);
            check(event.getTable() == table && table.getPool("vanilla") == original, "Original table and pools retained");
            check(!event.isCanceled(), "Loot event not canceled");
            verifyPool(table.getPool(LegacyChestLoot.MATERIALS_POOL), LegacyChestLoot.MATERIALS);
            check((table.getPool(LegacyChestLoot.NETHER_POOL) != null) == nether, "Nether rewards only in fortress");
            if (nether) verifyPool(table.getPool(LegacyChestLoot.NETHER_POOL), LegacyChestLoot.NETHER_MATERIALS);
            JsonArray pools = GSON.toJsonTree(table).getAsJsonObject().getAsJsonArray("pools");
            check(pools.size() == (nether ? 3 : 2), "Only expected pools appended");
            if (nether) check(pools.get(1).getAsJsonObject().get("name").getAsString().equals(LegacyChestLoot.NETHER_POOL),
                    "Original Nether-first order");
            LootPool added = table.getPool(LegacyChestLoot.MATERIALS_POOL);
            LegacyChestLoot.load(event);
            check(table.getPool(LegacyChestLoot.MATERIALS_POOL) == added
                    && GSON.toJsonTree(table).getAsJsonObject().getAsJsonArray("pools").size() == pools.size(),
                    "Repeated notification does not multiply rewards");
            checkRewards(table, nether, 0, 1);
            checkRewards(table, nether, 2, 2);
            checkRewards(table, nether, -1, 0);
            LootTable reloaded = baseTable();
            LegacyChestLoot.load(new LootTableLoadEvent(id, reloaded, null));
            check(reloaded.getPool(LegacyChestLoot.MATERIALS_POOL) != added, "Reload builds fresh pools");
        }
        for (String id : new String[]{"minecraft:chests/village/village_toolsmith", "minecraft:chests/village/village_armorer",
                "minecraft:chests/village/village_plains_house", "minecraft:chests/jungle_temple_dispenser",
                "minecraft:chests/bastion_treasure", "minecraft:chests/shipwreck_treasure", "minecraft:chests/spawn_bonus_chest",
                "minecraft:entities/zombie", "other:chests/simple_dungeon", "bewitchment:chests/materials",
                "bewitchment:chests/nether_materials"}) {
            LootTable table = baseTable();
            String before = GSON.toJson(table);
            LegacyChestLoot.load(new LootTableLoadEvent(new ResourceLocation(id), table, null));
            check(before.equals(GSON.toJson(table)), "Unrelated loot and nested tables unchanged: " + id);
        }
        LootTable custom = LootTable.lootTable().withPool(LootPool.lootPool().name(LegacyChestLoot.MATERIALS_POOL)
                .setRolls(new RandomValueRange(7)).add(ItemLootEntry.lootTableItem(Items.STICK))).build();
        LootPool customPool = custom.getPool(LegacyChestLoot.MATERIALS_POOL);
        LegacyChestLoot.load(new LootTableLoadEvent(LootTables.SIMPLE_DUNGEON, custom, null));
        check(custom.getPool(LegacyChestLoot.MATERIALS_POOL) == customPool, "Existing named override is respected");
        System.out.println("Legacy chest loot checks passed: " + checks);
    }

    private static LootTable baseTable() {
        return LootTable.lootTable().withPool(LootPool.lootPool().name("vanilla").setRolls(new RandomValueRange(1))
                .add(ItemLootEntry.lootTableItem(Items.APPLE))).build();
    }

    private static void verifyPool(LootPool pool, ResourceLocation reference) {
        check(pool != null, "Pool exists");
        RandomValueRange rolls = (RandomValueRange) pool.getRolls();
        RandomValueRange bonus = (RandomValueRange) pool.getBonusRolls();
        check(rolls.getMin() == 1 && rolls.getMax() == 1, "One base roll");
        check(bonus.getMin() == 0 && bonus.getMax() == 1, "Legacy zero-to-one Luck bonus");
        JsonObject json = GSON.toJsonTree(pool).getAsJsonObject();
        JsonArray entries = json.getAsJsonArray("entries");
        check(entries.size() == 1, "Single nested-table entry");
        JsonObject entry = entries.get(0).getAsJsonObject();
        check(entry.get("type").getAsString().equals("minecraft:loot_table")
                && entry.get("name").getAsString().equals(reference.toString()), "Original nested table reference");
        check(entry.get("weight").getAsInt() == 5 && (!entry.has("quality") || entry.get("quality").getAsInt() == 0),
                "Legacy weight five and quality zero");
    }

    private static void checkRewards(LootTable table, boolean nether, float luck, int multiplier) throws Exception {
        List<ResourceLocation> calls = new ArrayList<>();
        Function<ResourceLocation, LootTable> resolver = id -> {
            calls.add(id);
            if (id.equals(LegacyChestLoot.MATERIALS)) return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(new RandomValueRange(2)).add(ItemLootEntry.lootTableItem(Items.DIAMOND))).build();
            if (id.equals(LegacyChestLoot.NETHER_MATERIALS)) return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(new RandomValueRange(1)).add(ItemLootEntry.lootTableItem(Items.GOLD_INGOT))).build();
            throw new AssertionError("Unexpected referenced table: " + id);
        };
        // Native loot evaluation without a running server; the constructor receives only lookup functions and RNG.
        Constructor<LootContext> constructor = LootContext.class.getDeclaredConstructor(Random.class, float.class,
                ServerWorld.class, Function.class, Function.class, Map.class, Map.class);
        constructor.setAccessible(true);
        Random random = new Random(42) { @Override public float nextFloat() { return .75F; } };
        LootContext context = constructor.newInstance(random, luck, null, resolver,
                (Function<ResourceLocation, ILootCondition>) id -> null, Collections.emptyMap(), Collections.emptyMap());
        List<ItemStack> drops = new ArrayList<>();
        table.getRandomItemsRaw(context, drops::add);
        check(count(drops, Items.APPLE) == 1, "Original loot generated once at any Luck");
        check(count(drops, Items.DIAMOND) == 2 * multiplier, "Shared table performs two selections per reference");
        check(count(drops, Items.GOLD_INGOT) == (nether ? multiplier : 0), "Nether extra selections");
        check(calls.size() == (nether ? 2 : 1) * multiplier, "No recursive or duplicate table execution");
        if (nether && multiplier > 0) check(calls.get(0).equals(LegacyChestLoot.NETHER_MATERIALS), "Nether pool evaluated first");
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(stack -> stack.getItem() == item).mapToInt(ItemStack::getCount).sum();
    }
}
