package com.bewitchment.machine;

import com.google.gson.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistryEntry;
import java.util.*;

public final class DistilleryRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<DistilleryRecipe> TYPE = IRecipeType.register("bewitchment:distillery");
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final List<ItemStack> outputs;
    public DistilleryRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, List<ItemStack> outputs) {
        if (ingredients.isEmpty() || ingredients.size() > 6 || outputs.isEmpty() || outputs.size() > 6)
            throw new JsonSyntaxException("Distillery needs 1-6 ingredients and outputs");
        for (ItemStack output : outputs) if (output.isEmpty() || output.getCount() > output.getMaxStackSize())
            throw new JsonSyntaxException("Invalid distillery output size");
        this.id = id; this.ingredients = ingredients; this.outputs = outputs;
    }
    @Override public boolean matches(Inventory inventory, World world) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) if (!inventory.getItem(i).isEmpty()) inputs.add(inventory.getItem(i));
        return inputs.size() == ingredients.size() && RecipeMatcher.findMatches(inputs, ingredients) != null;
    }
    public List<ItemStack> getOutputs() {
        List<ItemStack> copy = new ArrayList<>(); for (ItemStack stack : outputs) copy.add(stack.copy()); return copy;
    }
    @Override public ItemStack assemble(Inventory inventory) { return outputs.get(0).copy(); }
    @Override public ItemStack getResultItem() { return outputs.get(0); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public IRecipeSerializer<?> getSerializer() { return ModMachines.DISTILLERY_RECIPE.get(); }
    @Override public IRecipeType<?> getType() { return TYPE; }
    @Override public boolean isSpecial() { return true; }
    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<DistilleryRecipe> {
        @Override public DistilleryRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (JsonElement input : JSONUtils.getAsJsonArray(json, "ingredients")) inputs.add(Ingredient.fromJson(input));
            List<ItemStack> outputs = new ArrayList<>();
            for (JsonElement output : JSONUtils.getAsJsonArray(json, "results")) outputs.add(ShapedRecipe.itemFromJson(output.getAsJsonObject()));
            return new DistilleryRecipe(id, inputs, outputs);
        }
        @Override public DistilleryRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            int count = buffer.readVarInt(); if (count < 1 || count > 6) throw new IllegalArgumentException("Invalid ingredient count");
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (int i = 0; i < count; i++) inputs.add(Ingredient.fromNetwork(buffer));
            count = buffer.readVarInt(); if (count < 1 || count > 6) throw new IllegalArgumentException("Invalid output count");
            List<ItemStack> outputs = new ArrayList<>(); for (int i = 0; i < count; i++) outputs.add(buffer.readItem());
            return new DistilleryRecipe(id, inputs, outputs);
        }
        @Override public void toNetwork(PacketBuffer buffer, DistilleryRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size()); for (Ingredient input : recipe.ingredients) input.toNetwork(buffer);
            buffer.writeVarInt(recipe.outputs.size()); for (ItemStack output : recipe.outputs) buffer.writeItem(output);
        }
    }
}
