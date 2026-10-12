package com.bewitchment.machine;

import net.minecraft.nbt.CompoundNBT;

/** Records which continuous effect consumed the offerings before a saved run can resume. */
public final class RitualConsumption {
    private String effect = "";
    public boolean consumed() { return !effect.isEmpty(); }
    public void reset() { effect = ""; }
    public void start(RitualRecipe recipe, RitualInventory inventory) {
        effect = recipe.effect.consumesOnStart() ? recipe.effect.id() : "";
        if (consumed()) for (int i = 0; i < inventory.getSlots(); i++) inventory.extractItem(i, 1, false);
    }
    public boolean accepts(RitualRecipe recipe) { return recipe.acceptsRun(effect); }
    public void save(CompoundNBT tag, boolean active) {
        if (active && consumed()) tag.putString("ConsumedEffect", effect); else tag.remove("ConsumedEffect");
    }
    public void load(CompoundNBT tag, boolean active) { effect = active ? tag.getString("ConsumedEffect") : ""; }
}
