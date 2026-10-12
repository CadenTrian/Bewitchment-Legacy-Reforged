package com.bewitchment.client;

import com.bewitchment.client.model.bauble.*;
import com.bewitchment.item.BarkArmor;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Cached legacy geometry attached to the wearer's animated torso. */
public final class DryadGirdleRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bewitchment", "textures/models/bauble/girdle_of_the_dryad.png");
    private static final ModelGirdleOfTheDryad BARE = new ModelGirdleOfTheDryad();
    private static final ModelGirdleOfTheDryad ARMORED = new ModelGirdleOfTheDryadArmor();
    public static void render(MatrixStack matrix, IRenderTypeBuffer buffer, int light, LivingEntity wearer, ItemStack stack) {
        if (wearer.isInvisible()) return;
        EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(wearer);
        if (!(renderer instanceof LivingRenderer) || !(((LivingRenderer<?, ?>)renderer).getModel() instanceof BipedModel)) return;
        BipedModel<?> body = (BipedModel<?>)((LivingRenderer<?, ?>)renderer).getModel();
        ModelGirdleOfTheDryad model = wearer.getItemBySlot(EquipmentSlotType.CHEST).isEmpty() ? BARE : ARMORED;
        model.bark = BarkArmor.get(stack);
        matrix.pushPose();
        body.body.translateAndRotate(matrix);
        model.renderToBuffer(matrix, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrix.popPose();
    }
    private DryadGirdleRenderer() {}
}
