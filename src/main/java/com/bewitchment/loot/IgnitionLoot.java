package com.bewitchment.loot;

import com.bewitchment.combat.PlayerCurses;
import com.bewitchment.machine.CurseData;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.loot.*;
import java.util.*;

/** Legacy replaces the entire harvest with one furnace recipe result for the block itself. */
public final class IgnitionLoot extends LootModifier {
    public IgnitionLoot(ILootCondition[] conditions){super(conditions);}
    public static boolean blockTable(BlockState state,ResourceLocation queried){return state!=null && queried!=null && queried.equals(state.getBlock().getLootTable());}
    public static ItemStack result(BlockState state,List<FurnaceRecipe> recipes,World world){
        if(state==null || state.getBlock().asItem()==Items.AIR)return ItemStack.EMPTY;
        Inventory input=new Inventory(new ItemStack(state.getBlock()));
        return recipes.stream().filter(recipe->recipe.matches(input,world)).sorted(Comparator.comparing(recipe->recipe.getId().toString())).findFirst().map(recipe->recipe.assemble(input)).orElse(ItemStack.EMPTY);
    }
    public static List<ItemStack> replace(List<ItemStack> loot,ItemStack output){if(output.isEmpty())return loot;List<ItemStack> result=new ArrayList<>();result.add(output.copy());return result;}
    @Override protected List<ItemStack> doApply(List<ItemStack> loot,LootContext context){
        Entity miner=context.getParamOrNull(LootParameters.THIS_ENTITY);BlockState state=context.getParamOrNull(LootParameters.BLOCK_STATE);
        if(!(miner instanceof PlayerEntity) || !miner.isAlive() || miner.isSpectator() || context.getParamOrNull(LootParameters.TOOL)==null || !blockTable(state,context.getQueriedLootTableId()))return loot;
        if(PlayerCurses.read((PlayerEntity)miner).remaining(CurseData.Kind.IGNITION)<=0)return loot;
        return replace(loot,result(state,context.getLevel().getRecipeManager().getAllRecipesFor(IRecipeType.SMELTING),context.getLevel()));
    }
    public static final class Serializer extends GlobalLootModifierSerializer<IgnitionLoot> {
        @Override public IgnitionLoot read(ResourceLocation id,JsonObject json,ILootCondition[] conditions){return new IgnitionLoot(conditions);}
        @Override public JsonObject write(IgnitionLoot modifier){return makeConditions(modifier.conditions);}
    }
}
