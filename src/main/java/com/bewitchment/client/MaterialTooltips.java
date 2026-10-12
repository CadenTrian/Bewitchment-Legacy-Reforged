package com.bewitchment.client;

import com.bewitchment.Bewitchment;
import com.bewitchment.combat.MaterialEffects;
import net.minecraft.item.Item;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID, value = Dist.CLIENT)
public final class MaterialTooltips {
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (MaterialEffects.SILVER_TOOLS.contains(item) || MaterialEffects.COLD_IRON_TOOLS.contains(item)) {
            event.getToolTip().add(new TranslationTextComponent("tooltip.bewitchment.material_tool").withStyle(TextFormatting.GOLD));
        }
        if (MaterialEffects.SILVER_ARMOR.contains(item) || MaterialEffects.COLD_IRON_ARMOR.contains(item)) {
            event.getToolTip().add(new TranslationTextComponent("tooltip.bewitchment.material_armor").withStyle(TextFormatting.GOLD));
        }
    }

    private MaterialTooltips() {}
}
