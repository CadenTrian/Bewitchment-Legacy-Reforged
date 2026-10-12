package com.bewitchment.client;

import com.bewitchment.entity.CambionEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.HandSide;

/** The legacy body is the sole render root; limbs are children, not separate roots. */
public class CambionModelBase extends BipedModel<CambionEntity> {
    public CambionModelBase(){super(0,0,64,64);}
    @Override public void setupAnim(CambionEntity entity,float swing,float amount,float age,float yaw,float pitch){
        leftArmPose=entity.getOffhandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM;
        rightArmPose=entity.getMainHandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM;
        if(entity.getMainArm()==HandSide.LEFT){ArmPose pose=leftArmPose;leftArmPose=rightArmPose;rightArmPose=pose;}
        crouching=entity.isCrouching();
        super.setupAnim(entity,swing,amount,age,yaw,pitch);
        body.y=-2; // Original torso origin, retained after vanilla pose setup.
    }
    @Override public void renderToBuffer(MatrixStack matrices,IVertexBuilder vertices,int light,int overlay,float r,float g,float b,float a){
        body.render(matrices,vertices,light,overlay,r,g,b,a);
    }
    @Override public void translateToHand(HandSide side,MatrixStack matrices){
        body.translateAndRotate(matrices);
        matrices.translate(.020,.1,0);
        super.translateToHand(side,matrices);
    }
}
