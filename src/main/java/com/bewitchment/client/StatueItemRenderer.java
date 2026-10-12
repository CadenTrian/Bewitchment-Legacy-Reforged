package com.bewitchment.client;

import com.bewitchment.block.StatueBlock;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.*;

public final class StatueItemRenderer extends ItemStackTileEntityRenderer {
    @Override public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transform, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem) || !(((BlockItem)stack.getItem()).getBlock() instanceof StatueBlock)) return;
        StatueBlock block = (StatueBlock)((BlockItem)stack.getItem()).getBlock();
        // GUI models carry the legacy framing; normalizing by height shrinks tall statues twice.
        float scale = transform == ItemCameraTransforms.TransformType.GUI ? 1F : 1F / block.height;
        matrix.pushPose();
        matrix.translate(0.5, 0, 0.5); matrix.scale(scale, scale, scale); matrix.translate(-0.5, 0, -0.5);
        StatueRenderer.renderStatue(block, 0, matrix, buffer, light, overlay);
        matrix.popPose();
    }
}
