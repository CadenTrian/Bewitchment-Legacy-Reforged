package com.bewitchment.item;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.*;
import top.theillusivec4.curios.api.SlotContext;
import java.util.List;
import javax.annotation.Nullable;

public final class DryadGirdleItem extends LegacyAccessoryItem {
    public DryadGirdleItem() { super(Kind.GIRDLE); }
    @Override public void inventoryTick(ItemStack stack, World world, Entity owner, int slot, boolean selected) {
        // Curios calls inventoryTick with -1. Carried stacks must lose their bark.
        if (!world.isClientSide && slot >= 0) BarkArmor.clear(stack);
    }
    @Override public boolean onEntityItemUpdate(ItemStack stack, ItemEntity dropped) {
        if (!dropped.level.isClientSide) BarkArmor.clear(stack);
        return false;
    }
    @Override public void onUnequip(SlotContext context, ItemStack replacement, ItemStack stack) {
        if (!context.getWearer().level.isClientSide && replacement.getItem() != this) {
            BarkArmor.clear(stack);
            // Curios passes its previous-stack snapshot here. Also clear the moved live stack.
            if (context.getWearer() instanceof net.minecraft.entity.player.PlayerEntity) {
                net.minecraft.entity.player.PlayerEntity player = (net.minecraft.entity.player.PlayerEntity)context.getWearer();
                for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                    ItemStack carried = player.inventory.getItem(i);
                    if (carried.getItem() == this) BarkArmor.clear(carried);
                }
                if (player.inventory.getCarried().getItem() == this) BarkArmor.clear(player.inventory.getCarried());
                player.inventory.setChanged();
            }
        }
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> lines, ITooltipFlag flag) {
        lines.add(new TranslationTextComponent("tooltip.bewitchment.girdle_bark", BarkArmor.get(stack)));
    }
    @OnlyIn(Dist.CLIENT)
    @Override public boolean canRender(String id, int index, LivingEntity wearer, ItemStack stack) { return true; }
    @OnlyIn(Dist.CLIENT)
    @Override public void render(String id, int index, com.mojang.blaze3d.matrix.MatrixStack matrix,
            net.minecraft.client.renderer.IRenderTypeBuffer buffer, int light, LivingEntity wearer,
            float limbSwing, float limbSwingAmount, float partialTicks, float age, float yaw, float pitch, ItemStack stack) {
        com.bewitchment.client.DryadGirdleRenderer.render(matrix, buffer, light, wearer, stack);
    }
}
