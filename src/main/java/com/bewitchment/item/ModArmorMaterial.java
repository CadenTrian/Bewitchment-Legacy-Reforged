package com.bewitchment.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

/** Armor values retained from the MIT-licensed legacy material definitions. */
public enum ModArmorMaterial implements IArmorMaterial {
    SILVER("silver", 11, new int[]{2, 4, 5, 2}, 25, SoundEvents.ARMOR_EQUIP_GOLD),
    COLD_IRON("cold_iron", 16, new int[]{2, 5, 6, 2}, 9, SoundEvents.ARMOR_EQUIP_IRON);

    private final String name;
    private final int durabilityMultiplier;
    private final int[] defense;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final ITag.INamedTag<Item> repairTag;

    ModArmorMaterial(String name, int durabilityMultiplier, int[] defense, int enchantability, SoundEvent equipSound) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defense = defense;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.repairTag = ItemTags.createOptional(new ResourceLocation("forge", "ingots/" + name));
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slot) {
        switch (slot) {
            case FEET: return 13 * durabilityMultiplier;
            case LEGS: return 15 * durabilityMultiplier;
            case CHEST: return 16 * durabilityMultiplier;
            case HEAD: return 11 * durabilityMultiplier;
            default: throw new IllegalArgumentException("Not an armor slot: " + slot);
        }
    }

    @Override public int getDefenseForSlot(EquipmentSlotType slot) { return defense[slot.getIndex()]; }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(repairTag); }
    @Override public String getName() { return "bewitchment:" + name; }
    @Override public float getToughness() { return 0; }
    @Override public float getKnockbackResistance() { return 0; }
}
