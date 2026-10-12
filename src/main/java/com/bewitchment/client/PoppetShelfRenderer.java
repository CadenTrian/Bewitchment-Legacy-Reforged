package com.bewitchment.client;

import com.bewitchment.block.PoppetShelfBlock;
import com.bewitchment.machine.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.*;

public final class PoppetShelfRenderer extends TileEntityRenderer<PoppetShelfTile> {
    public PoppetShelfRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }
    @Override public void render(PoppetShelfTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        Direction facing = tile.getBlockState().getValue(PoppetShelfBlock.FACING);
        for (int slot = 0; slot < tile.inventory.getSlots(); slot++) {
            ItemStack stack = tile.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            Vector3d center = ShelfLayout.center(facing, slot);
            matrix.pushPose(); matrix.translate(center.x, center.y, center.z);
            matrix.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot())); matrix.scale(.25F, .25F, .25F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
            matrix.popPose();
        }
    }
}
