package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;

public enum WitchArmorMaterial implements IArmorMaterial {
    INSTANCE;
    @Override public int getDurabilityForSlot(EquipmentSlotType slot) { return new int[]{13,15,16,11}[slot.getIndex()] * 8; }
    @Override public int getDefenseForSlot(EquipmentSlotType slot) { return new int[]{1,2,3,1}[slot.getIndex()]; }
    @Override public int getEnchantmentValue() { return 20; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.WITCHES_STITCHING.get()); }
    @Override public String getName() { return "bewitchment:witches"; }
    @Override public float getToughness() { return 0; }
    @Override public float getKnockbackResistance() { return 0; }
}
