package com.bewitchment.client;

import com.bewitchment.block.StatueBlock;
import com.bewitchment.machine.StatueTile;
import com.bewitchment.client.model.block.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import java.util.*;

public final class StatueRenderer extends TileEntityRenderer<StatueTile> {
    private static final Map<String, Model> MODELS = new HashMap<>();
    static {
        MODELS.put("baphomet_idol", new ModelBaphometIdol()); MODELS.put("baphomet_statue", new ModelBaphometStatue());
        MODELS.put("herne_idol", new ModelHerneIdol()); MODELS.put("herne_statue", new ModelHerneStatue());
        MODELS.put("leonard_idol", new ModelLeonardIdol()); MODELS.put("leonard_statue", new ModelLeonardStatue());
        MODELS.put("lilith_idol", new ModelLilithIdol()); MODELS.put("lilith_statue", new ModelLilithStatue());
        MODELS.put("moloch_idol", new ModelMolochIdol()); MODELS.put("moloch_statue", new ModelMolochStatue());
    }
    public StatueRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }
    @Override public void render(StatueTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        if (!(tile.getBlockState().getBlock() instanceof StatueBlock)) return;
        StatueBlock block = (StatueBlock)tile.getBlockState().getBlock();
        renderStatue(block, tile.getBlockState().getValue(StatueBlock.FACING).toYRot() + 180, matrix, buffer, light, overlay);
    }
    static void renderStatue(StatueBlock block, float angle, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        String kind = block.idol ? "idol" : "statue";
        Model model = MODELS.get(block.deity + "_" + kind);
        ResourceLocation texture = new ResourceLocation("bewitchment", "textures/block/" + kind + "/" + block.material + "_" + block.deity + ".png");
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.mulPose(Vector3f.YP.rotationDegrees(angle));
        model.renderToBuffer(matrix, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay, 1, 1, 1, 1);
        matrix.popPose();
    }
}
