package com.bewitchment.client;

import com.bewitchment.machine.SpinningWheelMenu;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

public final class SpinningWheelScreen extends ContainerScreen<SpinningWheelMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bewitchment", "textures/gui/spinning_wheel.png");
    public SpinningWheelScreen(SpinningWheelMenu menu, PlayerInventory inventory, ITextComponent title) { super(menu, inventory, title); }
    @Override public void render(MatrixStack pose, int x, int y, float partialTick) {
        renderBackground(pose); super.render(pose, x, y, partialTick); renderTooltip(pose, x, y);
        if (x >= leftPos + 85 && x < leftPos + 109 && y >= topPos + 33 && y < topPos + 50)
            renderTooltip(pose, new TranslationTextComponent("tooltip.bewitchment.spinning_wheel_power"), x, y);
    }
    @Override protected void renderBg(MatrixStack pose, float partialTick, int x, int y) {
        RenderSystem.color4f(1,1,1,1); minecraft.getTextureManager().bind(TEXTURE);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        blit(pose, leftPos + 85, topPos + 33, 176, 0, menu.data.get(0) * 24 / 200, 17);
    }
}
