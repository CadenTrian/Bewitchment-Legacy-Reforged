package com.bewitchment.client;

import com.bewitchment.machine.GlyphTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public final class GlyphRenderer extends TileEntityRenderer<GlyphTile> {
    public GlyphRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }
    @Override public void render(GlyphTile tile, float partial, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        for (int i = 0; i < 10; i++) {
            ItemStack stack = tile.inventory.getStackInSlot(i); if (stack.isEmpty()) continue;
            double angle = i * Math.PI * 2 / 10;
            matrix.pushPose(); matrix.translate(.5 + .35 * Math.cos(angle), .2, .5 + .35 * Math.sin(angle));
            matrix.mulPose(Vector3f.YP.rotationDegrees((float)(angle * 180 / Math.PI))); matrix.scale(.2F, .2F, .2F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
            matrix.popPose();
        }
    }
}
