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

public final class IncenseRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<IncenseRecipe> TYPE=IRecipeType.register("bewitchment:incense");
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    public final IncenseData.Kind incense;
    public IncenseRecipe(ResourceLocation id,NonNullList<Ingredient> inputs,IncenseData.Kind incense){
        if(incense==null || inputs.isEmpty() || inputs.size()>8 || inputs.contains(Ingredient.EMPTY))throw new JsonSyntaxException("Invalid incense incense or ingredients");
        this.id=id;this.incense=incense;this.inputs=NonNullList.create();this.inputs.addAll(inputs);
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
    @Override public IRecipeSerializer<?> getSerializer(){return ModMachines.INCENSE_RECIPE.get();}
    @Override public IRecipeType<?> getType(){return TYPE;}
    @Override public boolean isSpecial(){return true;}
    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IncenseRecipe>{
        @Override public IncenseRecipe fromJson(ResourceLocation id,JsonObject json){NonNullList<Ingredient> inputs=NonNullList.create();for(JsonElement value:JSONUtils.getAsJsonArray(json,"ingredients"))inputs.add(Ingredient.fromJson(value));return new IncenseRecipe(id,inputs,IncenseData.Kind.fromId(JSONUtils.getAsString(json,"incense")));}
        @Override public IncenseRecipe fromNetwork(ResourceLocation id,PacketBuffer buffer){int count=buffer.readVarInt();if(count<1 || count>8)throw new IllegalArgumentException("Invalid incense size");NonNullList<Ingredient> inputs=NonNullList.create();for(int i=0;i<count;i++)inputs.add(Ingredient.fromNetwork(buffer));return new IncenseRecipe(id,inputs,IncenseData.Kind.fromId(buffer.readUtf(128)));}
        @Override public void toNetwork(PacketBuffer buffer,IncenseRecipe recipe){buffer.writeVarInt(recipe.inputs.size());for(Ingredient input:recipe.inputs)input.toNetwork(buffer);buffer.writeUtf(recipe.incense.id);}
    }
}
