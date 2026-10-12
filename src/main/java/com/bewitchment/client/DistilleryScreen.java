package com.bewitchment.client;

import com.bewitchment.machine.DistilleryMenu;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class DistilleryScreen extends ContainerScreen<DistilleryMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bewitchment", "textures/gui/distillery.png");
    public DistilleryScreen(DistilleryMenu menu, PlayerInventory inventory, ITextComponent title) { super(menu, inventory, title); }
    @Override public void render(MatrixStack pose, int x, int y, float partialTick) {
        renderBackground(pose); super.render(pose, x, y, partialTick); renderTooltip(pose, x, y);
        if (x >= leftPos + 76 && x < leftPos + 101 && y >= topPos + 16 && y < topPos + 33 && menu.data.get(2) == 0)
            renderTooltip(pose, new TranslationTextComponent("tooltip.bewitchment.distillery_power"), x, y);
    }
    @Override protected void renderBg(MatrixStack pose, float partialTick, int x, int y) {
        RenderSystem.color4f(1,1,1,1); minecraft.getTextureManager().bind(TEXTURE);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        blit(pose, leftPos + 76, topPos + 16, 176, 0, menu.data.get(1) * 24 / 200 + 1, 17);
        int empty = 14 - (int) Math.ceil(14 * menu.data.get(0) / 1200.0);
        blit(pose, leftPos + 81, topPos + 36 + empty, 242, empty, 14, 14 - empty);
    }
}
