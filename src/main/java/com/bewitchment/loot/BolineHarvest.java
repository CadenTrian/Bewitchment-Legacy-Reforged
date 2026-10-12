package com.bewitchment.loot;

import com.bewitchment.registry.ModItems;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.*;
import java.util.*;

/** Evaluates explicitly tagged block tables with a shears tool; leaves destruction and protection checks to vanilla. */
public final class BolineHarvest extends LootModifier {
    private static final ITag.INamedTag<net.minecraft.block.Block> BLOCKS = BlockTags.createOptional(new ResourceLocation("bewitchment", "boline_shearable"));
    public BolineHarvest(ILootCondition[] conditions) { super(conditions); }
    @Override protected List<ItemStack> doApply(List<ItemStack> generated, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootParameters.TOOL);
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        if (tool == null || tool.getItem() != ModItems.BOLINE.get() || state == null || !state.is(BLOCKS)
                || !state.getBlock().getLootTable().equals(context.getQueriedLootTableId())) return generated;
        ItemStack shears = new ItemStack(Items.SHEARS);
        if (tool.hasTag()) shears.setTag(tool.getTag().copy());
        LootContext replacement = new LootContext.Builder(context).withParameter(LootParameters.TOOL, shears).create(LootParameterSets.BLOCK);
        List<ItemStack> drops = new ArrayList<>();
        // Raw evaluation avoids recursively running global modifiers. This modifier is ordered before our plant modifiers.
        context.getLevel().getServer().getLootTables().get(state.getBlock().getLootTable()).getRandomItemsRaw(replacement, drops::add);
        return drops;
    }
    public static final class Serializer extends GlobalLootModifierSerializer<BolineHarvest> {
        @Override public BolineHarvest read(ResourceLocation id, JsonObject json, ILootCondition[] conditions) { return new BolineHarvest(conditions); }
        @Override public JsonObject write(BolineHarvest modifier) { return makeConditions(modifier.conditions); }
    }
}
