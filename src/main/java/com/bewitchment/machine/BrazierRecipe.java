package com.bewitchment.machine;

import com.google.gson.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistryEntry;
import java.util.*;

public final class BrazierRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<BrazierRecipe> TYPE=IRecipeType.register("bewitchment:brazier_curse");
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    public final CurseData.Kind curse;
    public BrazierRecipe(ResourceLocation id,NonNullList<Ingredient> inputs,CurseData.Kind curse){
        if(curse==null || inputs.isEmpty() || inputs.size()>9 || inputs.contains(Ingredient.EMPTY))throw new JsonSyntaxException("Invalid Brazier curse or ingredients");
        this.id=id;this.curse=curse;this.inputs=NonNullList.create();this.inputs.addAll(inputs);
    }
    @Override public boolean matches(Inventory inventory,World world){
        List<ItemStack> stacks=new ArrayList<>();for(int i=0;i<inventory.getContainerSize();i++)if(!inventory.getItem(i).isEmpty()){
            if(inventory.getItem(i).getCount()!=1)return false;stacks.add(inventory.getItem(i));
        }
        return stacks.size()==inputs.size() && RecipeMatcher.findMatches(stacks,inputs)!=null;
    }
    @Override public ItemStack assemble(Inventory inventory){return ItemStack.EMPTY;}
    @Override public ItemStack getResultItem(){return ItemStack.EMPTY;}
    @Override public NonNullList<Ingredient> getIngredients(){NonNullList<Ingredient> copy=NonNullList.create();copy.addAll(inputs);return copy;}
    @Override public boolean canCraftInDimensions(int width,int height){return width>0 && height>0 && (long)width*height>=inputs.size();}
    @Override public ResourceLocation getId(){return id;}
    @Override public IRecipeSerializer<?> getSerializer(){return ModMachines.BRAZIER_RECIPE.get();}
    @Override public IRecipeType<?> getType(){return TYPE;}
    @Override public boolean isSpecial(){return true;}
    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<BrazierRecipe>{
        @Override public BrazierRecipe fromJson(ResourceLocation id,JsonObject json){NonNullList<Ingredient> inputs=NonNullList.create();for(JsonElement value:JSONUtils.getAsJsonArray(json,"ingredients"))inputs.add(Ingredient.fromJson(value));return new BrazierRecipe(id,inputs,CurseData.Kind.fromId(JSONUtils.getAsString(json,"curse")));}
        @Override public BrazierRecipe fromNetwork(ResourceLocation id,PacketBuffer buffer){int count=buffer.readVarInt();if(count<1 || count>9)throw new IllegalArgumentException("Invalid Brazier size");NonNullList<Ingredient> inputs=NonNullList.create();for(int i=0;i<count;i++)inputs.add(Ingredient.fromNetwork(buffer));return new BrazierRecipe(id,inputs,CurseData.Kind.fromId(buffer.readUtf(128)));}
        @Override public void toNetwork(PacketBuffer buffer,BrazierRecipe recipe){buffer.writeVarInt(recipe.inputs.size());for(Ingredient input:recipe.inputs)input.toNetwork(buffer);buffer.writeUtf(recipe.curse.id);}
    }
}
