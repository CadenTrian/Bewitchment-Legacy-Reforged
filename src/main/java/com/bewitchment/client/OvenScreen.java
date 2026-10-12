package com.bewitchment.client;

import com.bewitchment.machine.OvenMenu;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public final class OvenScreen extends ContainerScreen<OvenMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bewitchment", "textures/gui/witches_oven.png");
    public OvenScreen(OvenMenu menu, PlayerInventory inventory, ITextComponent title) { super(menu, inventory, title); }
    @Override public void render(MatrixStack pose, int x, int y, float partialTick) {
        renderBackground(pose); super.render(pose, x, y, partialTick); renderTooltip(pose, x, y);
    }
    @Override protected void renderBg(MatrixStack pose, float partialTick, int x, int y) {
        RenderSystem.color4f(1,1,1,1); minecraft.getTextureManager().bind(TEXTURE);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.data.get(0) > 0) {
            int flame = Math.min(13, menu.data.get(0) * 13 / Math.max(1, menu.data.get(1)));
            blit(pose, leftPos + 44, topPos + 50 - flame, 176, 12 - flame, 14, flame + 1);
        }
        blit(pose, leftPos + 76, topPos + 19, 176, 14, menu.data.get(2) * 24 / 200 + 1, 16);
    }
}
