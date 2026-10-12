package com.bewitchment.machine;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class OvenRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<OvenRecipe> TYPE = IRecipeType.register("bewitchment:oven");
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack output, byproduct;
    public final float chance;
    public final boolean requiresJar;

    public OvenRecipe(ResourceLocation id, Ingredient ingredient, ItemStack output, ItemStack byproduct, float chance, boolean requiresJar) {
        if (!Float.isFinite(chance) || chance < 0 || chance > 1) throw new JsonSyntaxException("Oven chance must be between 0 and 1");
        if (output.isEmpty() || output.getCount() > output.getMaxStackSize()
                || byproduct.getCount() > byproduct.getMaxStackSize()) throw new JsonSyntaxException("Invalid oven output size");
        this.id = id; this.ingredient = ingredient; this.output = output.copy(); this.byproduct = byproduct.copy();
        this.chance = chance; this.requiresJar = requiresJar;
    }
    @Override public boolean matches(Inventory inventory, World world) { return ingredient.test(inventory.getItem(0)); }
    @Override public ItemStack assemble(Inventory inventory) { return output.copy(); }
    public ItemStack getByproduct() { return byproduct.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem() { return output; }
    @Override public ResourceLocation getId() { return id; }
    @Override public IRecipeSerializer<?> getSerializer() { return ModMachines.OVEN_RECIPE.get(); }
    @Override public IRecipeType<?> getType() { return TYPE; }
    @Override public boolean isSpecial() { return true; }

    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<OvenRecipe> {
        @Override public OvenRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new OvenRecipe(id, Ingredient.fromJson(json.get("ingredient")), ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "result")),
                    json.has("byproduct") ? ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "byproduct")) : ItemStack.EMPTY,
                    JSONUtils.getAsFloat(json, "chance", 0), JSONUtils.getAsBoolean(json, "requires_jar", true));
        }
        @Override public OvenRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            return new OvenRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem(), buffer.readItem(), buffer.readFloat(), buffer.readBoolean());
        }
        @Override public void toNetwork(PacketBuffer buffer, OvenRecipe recipe) {
            recipe.ingredient.toNetwork(buffer); buffer.writeItem(recipe.output); buffer.writeItem(recipe.byproduct);
            buffer.writeFloat(recipe.chance); buffer.writeBoolean(recipe.requiresJar);
        }
    }
}
