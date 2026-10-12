package com.bewitchment.machine;

import com.google.gson.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

/** Fixed-position 5x5 recipe. No shifting, mirroring, power cost or crafting time. */
public final class SigilRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<SigilRecipe> TYPE=IRecipeType.register("bewitchment:sigil_table");
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack result;
    public SigilRecipe(ResourceLocation id,NonNullList<Ingredient> inputs,ItemStack result){
        if(inputs.size()!=25 || inputs.stream().anyMatch(i->i==Ingredient.EMPTY) || result.isEmpty() || result.getCount()!=1)throw new JsonSyntaxException("Sigil table requires 25 occupied ingredients and one result");
        this.id=id;this.inputs=NonNullList.create();this.inputs.addAll(inputs);this.result=result.copy();
    }
    @Override public boolean matches(Inventory inventory,World world){if(inventory.getContainerSize()!=25)return false;for(int i=0;i<25;i++)if(!inputs.get(i).test(inventory.getItem(i)))return false;return true;}
    @Override public ItemStack assemble(Inventory inventory){return result.copy();}
    @Override public ItemStack getResultItem(){return result.copy();}
    @Override public NonNullList<Ingredient> getIngredients(){NonNullList<Ingredient> copy=NonNullList.create();copy.addAll(inputs);return copy;}
    @Override public boolean canCraftInDimensions(int width,int height){return width==5 && height==5;}
    @Override public ResourceLocation getId(){return id;}
    @Override public IRecipeSerializer<?> getSerializer(){return ModMachines.SIGIL_RECIPE.get();}
    @Override public IRecipeType<?> getType(){return TYPE;}
    @Override public boolean isSpecial(){return true;}
    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<SigilRecipe>{
        @Override public SigilRecipe fromJson(ResourceLocation id,JsonObject json){
            NonNullList<Ingredient> inputs=NonNullList.create();for(JsonElement value:JSONUtils.getAsJsonArray(json,"ingredients"))inputs.add(Ingredient.fromJson(value));
            return new SigilRecipe(id,inputs,ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json,"result")));
        }
        @Override public SigilRecipe fromNetwork(ResourceLocation id,PacketBuffer buffer){
            int count=buffer.readVarInt();if(count!=25)throw new IllegalArgumentException("Invalid sigil grid size");
            NonNullList<Ingredient> inputs=NonNullList.create();for(int i=0;i<count;i++)inputs.add(Ingredient.fromNetwork(buffer));return new SigilRecipe(id,inputs,buffer.readItem());
        }
        @Override public void toNetwork(PacketBuffer buffer,SigilRecipe recipe){buffer.writeVarInt(25);for(Ingredient input:recipe.inputs)input.toNetwork(buffer);buffer.writeItem(recipe.result);}
    }
}
