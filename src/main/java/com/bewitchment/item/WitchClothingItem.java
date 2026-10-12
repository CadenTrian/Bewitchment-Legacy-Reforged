package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.api.distmarker.*;
import java.util.Locale;

public final class WitchClothingItem extends ArmorItem {
    public final String family, texture;
    public final boolean pointed;
    public WitchClothingItem(String family, String texture, EquipmentSlotType slot, boolean pointed) {
        super(WitchArmorMaterial.INSTANCE, slot, new Properties().tab(ModItems.TAB));
        this.family = family; this.texture = texture; this.pointed = pointed;
    }
    public boolean faithVariant(ItemStack stack) {
        return pointed && family.equals("witches") && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("faith");
    }
    @Override public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "bewitchment:textures/models/armor/" + texture + (faithVariant(stack) ? "_variant" : "") + ".png";
    }
    @Override public void appendHoverText(ItemStack stack, net.minecraft.world.World world, java.util.List<net.minecraft.util.text.ITextComponent> lines, ITooltipFlag flag) {
        if (family.equals("alchemist") || family.equals("green_witch")) {
            lines.add(new net.minecraft.util.text.TranslationTextComponent("tooltip.bewitchment.clothing_set").withStyle(net.minecraft.util.text.TextFormatting.GRAY));
            lines.add(new net.minecraft.util.text.TranslationTextComponent("tooltip.bewitchment." + family + "_set").withStyle(net.minecraft.util.text.TextFormatting.DARK_GREEN));
        }
    }
    @OnlyIn(Dist.CLIENT) @Override @SuppressWarnings("unchecked")
    public <A extends net.minecraft.client.renderer.entity.model.BipedModel<?>> A getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlotType slot, A original) {
        return (A)com.bewitchment.client.model.armor.ModelWitchesArmor.getInstance(slot, pointed);
    }
}
