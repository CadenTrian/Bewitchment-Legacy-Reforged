package com.bewitchment.item;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

/** Material values from the legacy TOOL_SILVER and TOOL_COLD_IRON definitions. */
public enum ModItemTier implements IItemTier {
    SILVER(1, 131, 12.0F, 1.5F, 22, "silver"),
    COLD_IRON(2, 260, 6.0F, 2.0F, 14, "cold_iron");

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final ITag.INamedTag<Item> repairTag;

    ModItemTier(int level, int uses, float speed, float damage, int enchantability, String metal) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
        this.repairTag = ItemTags.createOptional(new ResourceLocation("forge", "ingots/" + metal));
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchantability; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairTag);
    }
}
