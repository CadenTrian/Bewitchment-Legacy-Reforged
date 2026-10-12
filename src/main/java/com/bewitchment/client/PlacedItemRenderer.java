package com.bewitchment.client;

import com.bewitchment.block.PlacedItemBlock;
import com.bewitchment.machine.PlacedItemTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.util.math.vector.Vector3f;

public final class PlacedItemRenderer extends TileEntityRenderer<PlacedItemTile> {
    public PlacedItemRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }
    @Override public void render(PlacedItemTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        if (tile.getItem().isEmpty()) return;
        matrix.pushPose(); matrix.translate(0.5, 0.0375, 0.5);
        matrix.mulPose(Vector3f.YP.rotationDegrees(-tile.getBlockState().getValue(PlacedItemBlock.FACING).toYRot()));
        matrix.mulPose(Vector3f.XP.rotationDegrees(90)); matrix.scale(0.5F, 0.5F, 0.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(tile.getItem(), ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
        matrix.popPose();
    }
}
