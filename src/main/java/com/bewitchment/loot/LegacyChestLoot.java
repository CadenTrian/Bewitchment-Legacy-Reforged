package com.bewitchment.loot;

import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** MiscHandler's two additive chest pools, using the original nested loot tables. */
@Mod.EventBusSubscriber(modid = "bewitchment")
public final class LegacyChestLoot {
    public static final ResourceLocation MATERIALS = new ResourceLocation("bewitchment", "chests/materials");
    public static final ResourceLocation NETHER_MATERIALS = new ResourceLocation("bewitchment", "chests/nether_materials");
    public static final String MATERIALS_POOL = "bewitchment_materials_pool";
    public static final String NETHER_POOL = "bewitchment_nether_materials_pool";
    public static final Set<ResourceLocation> CHESTS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            LootTables.SIMPLE_DUNGEON, LootTables.ABANDONED_MINESHAFT,
            LootTables.STRONGHOLD_CORRIDOR, LootTables.STRONGHOLD_CROSSING, LootTables.STRONGHOLD_LIBRARY,
            LootTables.NETHER_BRIDGE,
            // The pre-1.14 blacksmith chest became the weaponsmith chest; do not broaden to every village chest.
            LootTables.VILLAGE_WEAPONSMITH,
            LootTables.JUNGLE_TEMPLE, LootTables.DESERT_PYRAMID, LootTables.IGLOO_CHEST,
            LootTables.END_CITY_TREASURE, LootTables.WOODLAND_MANSION)));

    @SubscribeEvent
    public static void load(LootTableLoadEvent event) {
        if (!CHESTS.contains(event.getName())) return;
        LootTable table = event.getTable();
        // Legacy inserts the Nether pool first. Never replace existing vanilla or other-mod pools.
        if (LootTables.NETHER_BRIDGE.equals(event.getName())) add(table, NETHER_POOL, NETHER_MATERIALS);
        add(table, MATERIALS_POOL, MATERIALS);
    }

    private static void add(LootTable table, String poolName, ResourceLocation reference) {
        // A repeated load notification must not duplicate rewards; respect an existing named pool.
        if (table.getPool(poolName) != null) return;
        table.addPool(LootPool.lootPool().name(poolName).setRolls(new RandomValueRange(1))
                .bonusRolls(0, 1).add(TableLootEntry.lootTableReference(reference).setWeight(5).setQuality(0)).build());
    }

    private LegacyChestLoot() {}
}
