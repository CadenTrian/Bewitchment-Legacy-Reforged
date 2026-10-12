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

/** Item/world rituals with one to three typed legacy circles. */
public final class RitualRecipe implements IRecipe<Inventory> {
    public static final IRecipeType<RitualRecipe> TYPE = IRecipeType.register("bewitchment:ritual");
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;
    public final int seconds, startingPower, runningPower;
    private final int[] circles;
    public final RitualEffect effect;
    public RitualRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output, int seconds, int start, int running) {
        this(id, ingredients, output, seconds, start, running, new int[]{1, -1, -1});
    }
    public RitualRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output, int seconds, int start, int running, int[] circles) {
        this(id, ingredients, output, seconds, start, running, circles, RitualEffect.ITEM);
    }
    public RitualRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output, int seconds, int start, int running, int[] circles, RitualEffect effect) {
        if (effect == null || ingredients.isEmpty() || ingredients.size() > 10 || (output.isEmpty() && effect == RitualEffect.ITEM)
                || (!output.isEmpty() && output.getCount() > output.getMaxStackSize())
                || (effect.consumesOnStart() && !output.isEmpty())
                || (effect == RitualEffect.TELEPORT && (ingredients.size() != 1 || !output.isEmpty()))
                || (effect == RitualEffect.NATURES_BLESSING && !output.isEmpty())
                || ((effect == RitualEffect.CALL_OF_THE_WILD || effect == RitualEffect.SPIRITUAL_RIFT || effect == RitualEffect.WEDNESDAY) && !output.isEmpty())
                || (effect.landscape() && !output.isEmpty())
                || (effect.continuous() ? seconds != -1 || !output.isEmpty()
                    : effect.instant() ? seconds != 0 || !output.isEmpty() || running != 0 : seconds < 1 || seconds > 3600)
                || start < 0 || running < 0) throw new JsonSyntaxException("Invalid ritual duration, output or power");
        this.effect = effect;
        this.id = id; this.ingredients = NonNullList.create(); this.ingredients.addAll(ingredients); this.output = output.copy();
        this.seconds = seconds; this.startingPower = start; this.runningPower = running;
        if (circles.length != 3 || circles[0] < 1 || circles[0] > 4) throw new JsonSyntaxException("A non-focal small circle is required");
        for (int i = 1; i < 3; i++) if (circles[i] != -1 && (circles[i] < 1 || circles[i] > 4)) throw new JsonSyntaxException("Invalid outer circle type");
        if (circles[2] != -1 && circles[1] == -1) throw new JsonSyntaxException("Large circle requires medium circle");
        this.circles = circles.clone();
        if (effect.instant()) for (int ring = 0; ring <= effect.drawingRing(); ring++)
            if (circles[ring] == -1) throw new JsonSyntaxException("Drawing requires existing circles through the target ring");
    }
    public int circleType(int ring) { return circles[ring]; }
    /** Consumed offerings may resume only as the same continuous effect, never as an item-producing recipe. */
    public boolean acceptsRun(String consumedEffect) {
        return !effect.instant() && (effect.consumesOnStart() ? effect.id().equals(consumedEffect) : consumedEffect.isEmpty());
    }
    @Override public boolean matches(Inventory inventory, World world) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) if (!inventory.getItem(i).isEmpty()) inputs.add(inventory.getItem(i));
        return inputs.size() == ingredients.size() && RecipeMatcher.findMatches(inputs, ingredients) != null;
    }
    @Override public ItemStack assemble(Inventory inventory) { return output.copy(); }
    @Override public ItemStack getResultItem() { return output.copy(); }
    @Override public NonNullList<Ingredient> getIngredients() { NonNullList<Ingredient> copy = NonNullList.create(); copy.addAll(ingredients); return copy; }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public IRecipeSerializer<?> getSerializer() { return ModMachines.RITUAL_RECIPE.get(); }
    @Override public IRecipeType<?> getType() { return TYPE; }
    @Override public boolean isSpecial() { return true; }
    public static final class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RitualRecipe> {
        @Override public RitualRecipe fromJson(ResourceLocation id, JsonObject json) {
            int[] circles = {1, -1, -1};
            if (json.has("circles")) {
                if (json.has("circle")) throw new JsonSyntaxException("Use circle or circles, not both");
                JsonArray array = JSONUtils.getAsJsonArray(json, "circles");
                if (array.size() < 1 || array.size() > 3) throw new JsonSyntaxException("Expected one to three circle types");
                for (int i = 0; i < array.size(); i++) {
                    switch (array.get(i).getAsString()) {
                        case "normal": circles[i] = 1; break;
                        case "nether": circles[i] = 2; break;
                        case "ender": circles[i] = 3; break;
                        case "any": circles[i] = 4; break;
                        case "none": circles[i] = -1; break;
                        default: throw new JsonSyntaxException("Unknown ritual circle type");
                    }
                }
            } else if (!JSONUtils.getAsString(json, "circle", "small_normal").equals("small_normal")) throw new JsonSyntaxException("Unknown circle shorthand");
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (JsonElement input : JSONUtils.getAsJsonArray(json, "ingredients")) inputs.add(Ingredient.fromJson(input));
            RitualEffect effect = RitualEffect.parse(JSONUtils.getAsString(json, "effect", "item"));
            ItemStack output = json.has("result") ? ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "result")) : ItemStack.EMPTY;
            return new RitualRecipe(id, inputs, output,
                    JSONUtils.getAsInt(json, "seconds"), JSONUtils.getAsInt(json, "starting_power"), JSONUtils.getAsInt(json, "running_power"), circles, effect);
        }
        @Override public RitualRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            int count = buffer.readVarInt(); if (count < 1 || count > 10) throw new IllegalArgumentException("Invalid ritual ingredient count");
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (int i = 0; i < count; i++) inputs.add(Ingredient.fromNetwork(buffer));
            ItemStack output = buffer.readItem(); int seconds = buffer.readVarInt(), start = buffer.readVarInt(), running = buffer.readVarInt();
            int[] circles = {buffer.readInt(), buffer.readInt(), buffer.readInt()};
            return new RitualRecipe(id, inputs, output, seconds, start, running, circles, RitualEffect.parse(buffer.readUtf(32)));
        }
        @Override public void toNetwork(PacketBuffer buffer, RitualRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size()); for (Ingredient ingredient : recipe.ingredients) ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.output); buffer.writeVarInt(recipe.seconds); buffer.writeVarInt(recipe.startingPower); buffer.writeVarInt(recipe.runningPower);
            for (int circle : recipe.circles) buffer.writeInt(circle);
            buffer.writeUtf(recipe.effect.id());
        }
    }
}
