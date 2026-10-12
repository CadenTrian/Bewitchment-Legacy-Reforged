package com.bewitchment.client;

import com.bewitchment.entity.BroomEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public final class BroomRenderer extends EntityRenderer<BroomEntity> {
    private final Model model=new Model();
    public BroomRenderer(EntityRendererManager manager){super(manager);shadowRadius=.4F;}
    @Override public ResourceLocation getTextureLocation(BroomEntity broom){return new ResourceLocation("bewitchment","textures/entity/"+broom.kind.id()+".png");}
    @Override public void render(BroomEntity broom,float yaw,float partial,MatrixStack pose,IRenderTypeBuffer buffers,int light){
        pose.pushPose();pose.translate(0,-.5,0);pose.mulPose(Vector3f.YP.rotationDegrees(90-com.bewitchment.entity.BroomRules.renderYaw(broom.yRotO,broom.yRot,partial)));
        model.setupAnim(broom,0,0,broom.tickCount+partial,0,0);pose.scale(1.2F,1,1);
        model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(broom))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        pose.popPose();super.render(broom,yaw,partial,pose,buffers,light);
    }
    public static final class Model extends EntityModel<BroomEntity> {
        public final ModelRenderer handle,twigsBound,twigs;
        public Model(){
            texWidth=64;texHeight=32;
            handle=new ModelRenderer(this,0,0);handle.setPos(-13,16,0);handle.addBox(0,0,0,16,1,1,.4F);
            twigsBound=new ModelRenderer(this,0,2);twigsBound.setPos(17,-1,-1);twigsBound.addBox(0,0,0,1,3,3,.5F);
            twigs=new ModelRenderer(this,39,0);twigs.setPos(18,-2,-2);twigs.addBox(0,0,0,6,5,5,.5F);
            handle.addChild(twigsBound);handle.addChild(twigs);
        }
        @Override public void setupAnim(BroomEntity entity,float swing,float amount,float age,float yaw,float pitch){
            float sin=MathHelper.sin(age*.10471975512F),cos=MathHelper.cos(age*.10471975512F),scale=entity.isVehicle()?.1F:1;
            handle.setPos(-13+.2F*sin*scale,16+.3F*sin*scale,.2F*cos*scale);
            handle.xRot=sin*(entity.isVehicle()?.01745329251F:.05235987755F);handle.yRot=cos*(entity.isVehicle()?.01745329251F:.05235987755F);
        }
        @Override public void renderToBuffer(MatrixStack pose,IVertexBuilder vertices,int light,int overlay,float r,float g,float b,float a){handle.render(pose,vertices,light,overlay,r,g,b,a);}
    }
}
