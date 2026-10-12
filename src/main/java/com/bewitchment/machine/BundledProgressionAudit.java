package com.bewitchment.machine;

import com.google.gson.*;
import com.bewitchment.combat.HostileFortunes;
import com.bewitchment.combat.PetFortunes;
import net.minecraft.advancements.Advancement;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Parse restored progression with registered items, entities and vanilla serializers. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "bewitchment")
public final class BundledProgressionAudit {
    private static boolean chestAuditChecked;
    /** The Forge gameplay bus is shut down during common setup; probe only after it starts. */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void auditChestHooks(net.minecraftforge.event.TickEvent event) {
        if (chestAuditChecked) return;
        chestAuditChecked = true;
        // Other mods may intentionally replace/cancel pools; keep this development audit opt-in.
        if (Boolean.getBoolean("bewitchment.auditChestLoot"))
            verifyChestHooks(LootSerializers.createLootTableSerializer().create(), new LootTableManager(new LootPredicateManager()));
    }
    public static void verify() {
        String[] names={"argonaut","brazier","cold_iron","consumed_stew","controlled_burn","das_tier_in_mir",
                "diabolus","distillery","eye_of_newt","fda","gaze","loom","mined_amethyst","mined_coquina",
                "mined_garnet","mined_opal","mined_salt","mined_silver","obtained_box","oven","sigil_table",
                "voodoo","wore_alchemist_set","wore_besmirched_set","wore_green_witch_set","broom","purification_in_progress"};
        Set<String> all=new HashSet<>(Arrays.asList(names)); all.add("root"); all.add("crafted_altar");
        LootPredicateManager predicates=new LootPredicateManager();
        for(String name:all) {
            JsonObject json=read("advancements/"+name+".json");
            // Advancement conditions contain nested item predicates; the native parser validates those.
            RecipeItemReferences.validate(json.getAsJsonObject("display").getAsJsonObject("icon"),
                    id->ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id)));
            if(json.has("parent") && !all.contains(new ResourceLocation(json.get("parent").getAsString()).getPath()))
                throw new IllegalStateException("Missing advancement parent: "+name);
            Advancement.Builder.fromJson(json,new ConditionArrayParser(new ResourceLocation("bewitchment",name),predicates));
        }
        Gson gson=LootSerializers.createLootTableSerializer().create();
        for(String name:new String[]{"materials","nether_materials"}) {
            JsonObject json=read("loot_tables/chests/"+name+".json");
            for(JsonElement pool:json.getAsJsonArray("pools")) for(JsonElement entry:pool.getAsJsonObject().getAsJsonArray("entries")) {
                String item=entry.getAsJsonObject().get("name").getAsString();
                if(!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item)))throw new IllegalStateException("Unknown fortune loot: "+item);
            }
            // Forge's serializer requires a loot-table context; custom=true avoids firing load events during this audit.
            net.minecraftforge.common.ForgeHooks.loadLootTable(gson,new ResourceLocation("bewitchment","chests/"+name),
                    json,true,new LootTableManager(predicates));
        }
        Set<Object> pets=new HashSet<>();
        for(int i=0;i<PetFortunes.POOL_SIZE;i++)if(!pets.add(PetFortunes.type(i)))throw new IllegalStateException("Duplicate pet pool entry");
        for(FortuneData.Kind kind:FortuneData.Kind.values())if(HostileFortuneEncounter.supported(kind) && HostileFortunes.type(kind)==null)
            throw new IllegalStateException("Missing hostile fortune entity: "+kind);
        LogManager.getLogger().info("Verified 29 progression advancements, two fortune loot tables and registered fortune encounter pools");
    }
    /** Exercise the transformed Forge bus during startup, using disposable empty chest tables. */
    private static void verifyChestHooks(Gson gson, LootTableManager manager) {
        JsonObject empty = new JsonParser().parse("{\"type\":\"minecraft:chest\",\"pools\":[]}").getAsJsonObject();
        for (ResourceLocation id : com.bewitchment.loot.LegacyChestLoot.CHESTS) {
            LootTable loaded = net.minecraftforge.common.ForgeHooks.loadLootTable(gson, id, empty, false, manager);
            if (loaded == null || !loaded.isFrozen()
                    || loaded.getPool(com.bewitchment.loot.LegacyChestLoot.MATERIALS_POOL) == null
                    || (loaded.getPool(com.bewitchment.loot.LegacyChestLoot.NETHER_POOL) != null) != id.equals(LootTables.NETHER_BRIDGE))
                throw new IllegalStateException("Missing legacy chest loot event hook: " + id);
        }
        LootTable custom = net.minecraftforge.common.ForgeHooks.loadLootTable(gson, LootTables.NETHER_BRIDGE, empty, true, manager);
        if (custom.getPool(com.bewitchment.loot.LegacyChestLoot.MATERIALS_POOL) != null
                || custom.getPool(com.bewitchment.loot.LegacyChestLoot.NETHER_POOL) != null)
            throw new IllegalStateException("Custom loot table unexpectedly modified");
        net.minecraftforge.event.LootTableLoadEvent canceled = new net.minecraftforge.event.LootTableLoadEvent(
                LootTables.SIMPLE_DUNGEON, LootTable.lootTable().build(), manager);
        canceled.setCanceled(true);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(canceled);
        if (canceled.getTable().getPool(com.bewitchment.loot.LegacyChestLoot.MATERIALS_POOL) != null)
            throw new IllegalStateException("Canceled loot event unexpectedly modified");
        LogManager.getLogger().info("Verified 12 legacy chest loot hooks, fortress extra pool, custom-table bypass and canceled-event handling");
    }
    private static JsonObject read(String path) {
        String resource="data/bewitchment/"+path;
        try(InputStream stream=BundledProgressionAudit.class.getClassLoader().getResourceAsStream(resource)) {
            if(stream==null)throw new FileNotFoundException(resource);
            return new JsonParser().parse(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        } catch(IOException error) {throw new IllegalStateException("Cannot read progression resource: "+resource,error);}
    }
    private BundledProgressionAudit() {}
}
