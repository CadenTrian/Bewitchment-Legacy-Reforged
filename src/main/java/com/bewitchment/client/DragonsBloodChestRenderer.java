package com.bewitchment.client;

import com.bewitchment.block.DragonsBloodChestBlock;
import com.bewitchment.machine.DragonsBloodChestTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

/** Original 1.12 ModelChest geometry and 64x64 texture layout. */
public final class DragonsBloodChestRenderer extends TileEntityRenderer<DragonsBloodChestTile> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("bewitchment","textures/block/dragons_blood_chest.png");
    private final ModelRenderer lid=new ModelRenderer(64,64,0,0);
    private final ModelRenderer latch=new ModelRenderer(64,64,0,0);
    private final ModelRenderer base=new ModelRenderer(64,64,0,19);
    public DragonsBloodChestRenderer(TileEntityRendererDispatcher dispatcher){
        super(dispatcher);
        lid.addBox(0,-5,-14,14,5,14);lid.setPos(1,7,15);
        latch.addBox(-1,-2,-15,2,4,1);latch.setPos(8,7,15);
        base.addBox(0,0,0,14,10,14);base.setPos(1,6,1);
    }
    public static float lidRotation(float openness){float remaining=1-openness;return -(1-remaining*remaining*remaining)*(float)Math.PI/2;}
    @Override public void render(DragonsBloodChestTile tile,float partial,MatrixStack matrix,IRenderTypeBuffer buffer,int light,int overlay){
        matrix.pushPose();matrix.translate(0,1,1);matrix.scale(1,-1,-1);
        matrix.translate(.5,.5,.5);matrix.mulPose(Vector3f.YP.rotationDegrees(tile.getBlockState().getValue(DragonsBloodChestBlock.FACING).toYRot()));matrix.translate(-.5,-.5,-.5);
        lid.xRot=lidRotation(tile.getOpenNess(partial));latch.xRot=lid.xRot;
        IVertexBuilder vertices=buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        lid.render(matrix,vertices,light,overlay);latch.render(matrix,vertices,light,overlay);base.render(matrix,vertices,light,overlay);
        matrix.popPose();
    }
}
