package com.bewitchment.loot;

import com.bewitchment.registry.*;
import com.google.gson.JsonObject;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.*;
import net.minecraftforge.common.loot.*;
import java.util.*;

/** Preserve vanilla seed-roll frequency and Fortune count, then apply legacy seed weights. */
public final class PlantDrops extends LootModifier {
    private final boolean grass;
    public PlantDrops(ILootCondition[] conditions, boolean grass) { super(conditions); this.grass = grass; }
    @Override protected List<ItemStack> doApply(List<ItemStack> loot, LootContext context) {
        if (!grass) { loot.add(new ItemStack(ModItems.OAK_APPLE_GALL.get())); return loot; }
        List<Item> seeds = new ArrayList<>();
        ModHerbs.SEEDS.values().forEach(seed -> seeds.add(seed.get()));
        seeds.add(ModItems.GARLIC_SEEDS.get());
        for (int i = 0; i < loot.size(); i++) if (loot.get(i).getItem() == Items.WHEAT_SEEDS) {
            int roll = context.getRandom().nextInt(10 + 3 * seeds.size());
            if (roll >= 10) loot.set(i, new ItemStack(seeds.get((roll - 10) / 3), loot.get(i).getCount()));
        }
        return loot;
    }
    public static final class Serializer extends GlobalLootModifierSerializer<PlantDrops> {
        @Override public PlantDrops read(ResourceLocation id, JsonObject json, ILootCondition[] conditions) {
            String mode = JSONUtils.getAsString(json, "mode");
            if (!mode.equals("grass_seeds") && !mode.equals("oak_gall")) throw new IllegalArgumentException("Unknown plant drop mode: " + mode);
            return new PlantDrops(conditions, mode.equals("grass_seeds"));
        }
        @Override public JsonObject write(PlantDrops modifier) {
            JsonObject json = makeConditions(modifier.conditions); json.addProperty("mode", modifier.grass ? "grass_seeds" : "oak_gall"); return json;
        }
    }
}
