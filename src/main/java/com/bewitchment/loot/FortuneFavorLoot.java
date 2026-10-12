package com.bewitchment.loot;

import com.bewitchment.combat.AccessoryEffects;
import com.bewitchment.item.LegacyAccessoryItem.Kind;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tags.*;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.*;
import java.util.*;

public final class FortuneFavorLoot extends LootModifier {
    public static final ITag.INamedTag<Block> ORES = BlockTags.createOptional(new ResourceLocation("bewitchment", "fortunes_favor_ores"));
    public FortuneFavorLoot(ILootCondition[] conditions) { super(conditions); }
    @Override protected List<ItemStack> doApply(List<ItemStack> loot, LootContext context) {
        Entity miner = context.getParamOrNull(LootParameters.THIS_ENTITY);
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootParameters.TOOL);
        if (!(miner instanceof PlayerEntity) || state == null || !state.is(ORES) || tool == null
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0
                || !context.getQueriedLootTableId().equals(state.getBlock().getLootTable())
                || !AccessoryEffects.wearing((PlayerEntity)miner, Kind.FORTUNE)) return loot;
        return bonus(loot, context.getRandom());
    }
    static List<ItemStack> bonus(List<ItemStack> loot, Random random) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (stack.isEmpty()) continue;
            int remaining = stack.getCount() + random.nextInt(2);
            while (remaining > 0) {
                ItemStack part = stack.copy(); int count = Math.min(remaining, part.getMaxStackSize());
                part.setCount(count); result.add(part); remaining -= count;
            }
        }
        return result;
    }
    public static final class Serializer extends GlobalLootModifierSerializer<FortuneFavorLoot> {
        @Override public FortuneFavorLoot read(ResourceLocation id, JsonObject json, ILootCondition[] conditions) { return new FortuneFavorLoot(conditions); }
        @Override public JsonObject write(FortuneFavorLoot modifier) { return makeConditions(modifier.conditions); }
    }
}
