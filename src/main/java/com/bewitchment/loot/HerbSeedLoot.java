package com.bewitchment.loot;

import com.bewitchment.Bewitchment;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;

/** Legacy allium/dead-bush seed replacement, selected by data-pack conditions. */
public final class HerbSeedLoot extends LootModifier {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, Bewitchment.MOD_ID);
    static {
        SERIALIZERS.register("ignition", IgnitionLoot.Serializer::new);
        SERIALIZERS.register("boline_harvest", BolineHarvest.Serializer::new);
        SERIALIZERS.register("green_witch_harvest", GreenWitchHarvest.Serializer::new);
        SERIALIZERS.register("fortunes_favor", FortuneFavorLoot.Serializer::new);
        SERIALIZERS.register("herb_seeds", Serializer::new);
        SERIALIZERS.register("plant_drops", PlantDrops.Serializer::new);
    }
    private final Item seed;
    public HerbSeedLoot(ILootCondition[] conditions, Item seed) { super(conditions); this.seed = seed; }
    @Override protected List<ItemStack> doApply(List<ItemStack> loot, LootContext context) {
        ItemStack tool = context.getParamOrNull(net.minecraft.loot.LootParameters.TOOL);
        if (tool != null && tool.getItem() instanceof net.minecraft.item.ShearsItem) return loot;
        loot.clear(); loot.add(new ItemStack(seed)); return loot;
    }
    private static final class Serializer extends GlobalLootModifierSerializer<HerbSeedLoot> {
        @Override public HerbSeedLoot read(ResourceLocation id, JsonObject json, ILootCondition[] conditions) {
            ResourceLocation itemId = new ResourceLocation(JSONUtils.getAsString(json, "seed"));
            if (!ForgeRegistries.ITEMS.containsKey(itemId)) throw new IllegalArgumentException("Unknown herb seed: " + itemId);
            return new HerbSeedLoot(conditions, ForgeRegistries.ITEMS.getValue(itemId));
        }
        @Override public JsonObject write(HerbSeedLoot modifier) {
            JsonObject json = makeConditions(modifier.conditions);
            json.addProperty("seed", modifier.seed.getRegistryName().toString()); return json;
        }
    }
}
