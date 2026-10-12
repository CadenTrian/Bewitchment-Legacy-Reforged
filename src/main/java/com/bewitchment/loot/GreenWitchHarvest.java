package com.bewitchment.loot;

import com.bewitchment.registry.ModClothing;
import com.google.gson.JsonObject;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.*;
import java.util.*;

public final class GreenWitchHarvest extends LootModifier {
    public GreenWitchHarvest(ILootCondition[] conditions) { super(conditions); }
    @Override protected List<ItemStack> doApply(List<ItemStack> loot, LootContext context) {
        Entity harvester = context.getParamOrNull(LootParameters.THIS_ENTITY);
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        if (!(harvester instanceof PlayerEntity) || state == null || !(state.getBlock() instanceof CropsBlock)
                || !((CropsBlock)state.getBlock()).isMaxAge(state) || !ModClothing.hasSet((PlayerEntity)harvester, "green_witch")
                || !context.getQueriedLootTableId().equals(state.getBlock().getLootTable())) return loot;
        return bonus(loot, context.getRandom());
    }
    static List<ItemStack> bonus(List<ItemStack> loot, Random random) {
        if (!random.nextBoolean()) return loot;
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (stack.isEmpty()) continue;
            int remaining = stack.getCount() + random.nextInt(3);
            while (remaining > 0) {
                ItemStack part = stack.copy(); int count = Math.min(remaining, part.getMaxStackSize());
                part.setCount(count); result.add(part); remaining -= count;
            }
        }
        return result;
    }
    public static final class Serializer extends GlobalLootModifierSerializer<GreenWitchHarvest> {
        @Override public GreenWitchHarvest read(ResourceLocation id, JsonObject json, ILootCondition[] conditions) { return new GreenWitchHarvest(conditions); }
        @Override public JsonObject write(GreenWitchHarvest modifier) { return makeConditions(modifier.conditions); }
    }
}
