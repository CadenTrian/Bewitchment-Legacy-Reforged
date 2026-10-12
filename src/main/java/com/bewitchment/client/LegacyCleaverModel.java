package com.bewitchment.client;
import com.bewitchment.entity.CleaverEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
/**
 * cleaver - cybercat5555
 * Created using Tabula 7.0.1
 */
public class LegacyCleaverModel extends BipedModel<CleaverEntity> {
	public ModelRenderer loincloth;
	public ModelRenderer loinclothBack;
	public ModelRenderer leftLeg02;
	public ModelRenderer leftLeg03;
	public ModelRenderer leftHoof;
	public ModelRenderer rightLeg02;
	public ModelRenderer rightLeg03;
	public ModelRenderer rightHoof;
	public ModelRenderer upperJaw;
	public ModelRenderer snout;
	public ModelRenderer lowerJaw;
	public ModelRenderer lEar00;
	public ModelRenderer rEar00;
	public ModelRenderer lHorn01;
	public ModelRenderer rHorn01;
	public ModelRenderer upperTeethL;
	public ModelRenderer upperTeethR;
	public ModelRenderer lowerTeethL;
	public ModelRenderer lowerTeethR;
	public ModelRenderer lowerTeethM;
	public ModelRenderer lEar01;
	public ModelRenderer rEar01;
	public ModelRenderer lHorn02a;
	public ModelRenderer lHorn02b;
	public ModelRenderer lHorn02c;
	public ModelRenderer lHorn02d;
	public ModelRenderer lHorn03;
	public ModelRenderer lHorn04;
	public ModelRenderer rHorn02a;
	public ModelRenderer rHorn02b;
	public ModelRenderer rHorn02c;
	public ModelRenderer rHorn02d;
	public ModelRenderer rHorn03;
	public ModelRenderer rHorn04;
	public ModelRenderer loincloth02;

	public LegacyCleaverModel(float modelSize) {
		super(modelSize);
		this.texWidth = 64;
		this.texHeight = 64;
		this.lHorn03 = new ModelRenderer(this, 38, 43);
		this.lHorn03.setPos(0.0F, -2.7F, 0.0F);
		this.lHorn03.addBox(-0.5F, -2.5F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(lHorn03, 0.13962634015954636F, 0.0F, -0.3490658503988659F);
		this.rHorn02d = new ModelRenderer(this, 39, 38);
		this.rHorn02d.mirror = true;
		this.rHorn02d.setPos(0.0F, 0.0F, 0.0F);
		this.rHorn02d.addBox(-0.2F, -3.0F, -0.2F, 1, 3, 1, 0.0F);
		this.leftHoof = new ModelRenderer(this, 25, 52);
		this.leftHoof.setPos(0.0F, 5.2F, -0.7F);
		this.leftHoof.addBox(-1.5F, -0.7F, -1.9F, 3, 2, 4, 0.0F);
		this.rEar00 = new ModelRenderer(this, 47, 0);
		this.rEar00.mirror = true;
		this.rEar00.setPos(-3.4F, -5.5F, 0.0F);
		this.rEar00.addBox(-5.0F, -0.5F, -1.5F, 5, 1, 3, 0.0F);
		this.setRotateAngle(rEar00, -0.6981317007977318F, 0.08726646259971647F, -0.3490658503988659F);
		this.leftArm = new ModelRenderer(this, 40, 16);
		this.leftArm.setPos(5.0F, 2.0F, 0.0F);
		this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 14, 4, 0.0F);
		this.setRotateAngle(leftArm, -0.0F, 0.0F, -0.10000736613927509F);
		this.upperJaw = new ModelRenderer(this, 0, 46);
		this.upperJaw.mirror = true;
		this.upperJaw.setPos(0.0F, -3.8F, -4.1F);
		this.upperJaw.addBox(-3.0F, -0.65F, -4.7F, 6, 3, 5, 0.0F);
		this.rightLeg03 = new ModelRenderer(this, 24, 43);
		this.rightLeg03.mirror = true;
		this.rightLeg03.setPos(0.0F, 5.4F, 0.0F);
		this.rightLeg03.addBox(-1.51F, -0.4F, -1.5F, 3, 6, 3, 0.0F);
		this.setRotateAngle(rightLeg03, -0.4363323129985824F, 0.0F, 0.0F);
		this.rightLeg = new ModelRenderer(this, 0, 16);
		this.rightLeg.mirror = true;
		this.rightLeg.setPos(-2.0F, 12.4F, 0.1F);
		this.rightLeg.addBox(-2.2F, -0.9F, -2.0F, 4, 9, 4, 0.0F);
		this.setRotateAngle(rightLeg, -0.3490658503988659F, 0.0F, 0.10471975511965977F);
		this.lHorn02c = new ModelRenderer(this, 39, 38);
		this.lHorn02c.setPos(0.0F, 0.0F, 0.0F);
		this.lHorn02c.addBox(-0.8F, -3.0F, -0.2F, 1, 3, 1, 0.0F);
		this.leftLeg03 = new ModelRenderer(this, 24, 43);
		this.leftLeg03.setPos(0.0F, 5.4F, 0.0F);
		this.leftLeg03.addBox(-1.49F, -0.4F, -1.5F, 3, 6, 3, 0.0F);
		this.setRotateAngle(leftLeg03, -0.4363323129985824F, 0.0F, 0.0F);
		this.rHorn01 = new ModelRenderer(this, 37, 38);
		this.rHorn01.mirror = true;
		this.rHorn01.setPos(-2.8F, -6.8F, 2.0F);
		this.rHorn01.addBox(-1.0F, -2.5F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotateAngle(rHorn01, 0.08726646259971647F, 0.0F, -0.7853981633974483F);
		this.snout = new ModelRenderer(this, 0, 35);
		this.snout.setPos(0.0F, -5.0F, -2.3F);
		this.snout.addBox(-2.5F, -2.05F, -6.4F, 5, 3, 6, 0.0F);
		this.setRotateAngle(snout, 0.3490658503988659F, 0.0F, 0.0F);
		this.lowerTeethR = new ModelRenderer(this, 24, 58);
		this.lowerTeethR.mirror = true;
		this.lowerTeethR.setPos(-2.2F, -0.4F, -3.5F);
		this.lowerTeethR.addBox(-0.2F, -2.0F, -1.4F, 1, 2, 4, 0.0F);
		this.lowerTeethM = new ModelRenderer(this, 35, 61);
		this.lowerTeethM.setPos(0.0F, -0.4F, -3.5F);
		this.lowerTeethM.addBox(-1.5F, -1.1F, -1.4F, 3, 1, 0, 0.0F);
		this.rHorn02b = new ModelRenderer(this, 39, 38);
		this.rHorn02b.mirror = true;
		this.rHorn02b.setPos(0.0F, 0.0F, 0.0F);
		this.rHorn02b.addBox(-0.2F, -3.0F, -0.8F, 1, 3, 1, 0.0F);
		this.head = new ModelRenderer(this, 0, 0);
		this.head.setPos(0.0F, 0.0F, 0.0F);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.rHorn02a = new ModelRenderer(this, 39, 38);
		this.rHorn02a.mirror = true;
		this.rHorn02a.setPos(-0.1F, -1.9F, 0.2F);
		this.rHorn02a.addBox(-0.8F, -3.0F, -0.8F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rHorn02a, 0.2792526803190927F, 0.0F, 0.2617993877991494F);
		this.lEar01 = new ModelRenderer(this, 48, 5);
		this.lEar01.setPos(0.0F, -0.9F, 0.0F);
		this.lEar01.addBox(0.0F, -0.5F, -1.0F, 4, 1, 2, 0.0F);
		this.setRotateAngle(lEar01, 0.0F, 0.0F, 0.22759093446006054F);
		this.lHorn02d = new ModelRenderer(this, 39, 38);
		this.lHorn02d.setPos(0.0F, 0.0F, 0.0F);
		this.lHorn02d.addBox(-0.2F, -3.0F, -0.2F, 1, 3, 1, 0.0F);
		this.leftLeg02 = new ModelRenderer(this, 24, 33);
		this.leftLeg02.setPos(0.3F, 6.7F, -0.7F);
		this.leftLeg02.addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3, 0.0F);
		this.setRotateAngle(leftLeg02, 0.767944870877505F, 0.0F, 0.10471975511965977F);
		this.leftLeg = new ModelRenderer(this, 0, 16);
		this.leftLeg.setPos(2.1F, 12.4F, 0.1F);
		this.leftLeg.addBox(-1.8F, -0.9F, -2.0F, 4, 9, 4, 0.0F);
		this.setRotateAngle(leftLeg, -0.3490658503988659F, 0.0F, -0.10471975511965977F);
		this.rEar01 = new ModelRenderer(this, 48, 5);
		this.rEar01.mirror = true;
		this.rEar01.setPos(0.0F, -0.9F, 0.0F);
		this.rEar01.addBox(-4.0F, -0.5F, -1.0F, 4, 1, 2, 0.0F);
		this.setRotateAngle(rEar01, 0.0F, 0.0F, -0.22759093446006054F);
		this.lHorn02b = new ModelRenderer(this, 39, 38);
		this.lHorn02b.setPos(0.0F, 0.0F, 0.0F);
		this.lHorn02b.addBox(-0.2F, -3.0F, -0.8F, 1, 3, 1, 0.0F);
		this.rHorn04 = new ModelRenderer(this, 43, 43);
		this.rHorn04.mirror = true;
		this.rHorn04.setPos(0.0F, -2.3F, 0.0F);
		this.rHorn04.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(rHorn04, 0.13962634015954636F, 0.0F, 0.3490658503988659F);
		this.loincloth02 = new ModelRenderer(this, 48, 43);
		this.loincloth02.setPos(0.0F, 7.7F, 0.0F);
		this.loincloth02.addBox(-3.0F, 0.05F, -0.55F, 6, 6, 1, 0.0F);
		this.setRotateAngle(loincloth02, 0.3490658503988659F, 0.0F, 0.0F);
		this.upperTeethL = new ModelRenderer(this, 52, 58);
		this.upperTeethL.setPos(2.0F, 2.2F, -2.8F);
		this.upperTeethL.addBox(-1.2F, 0.0F, -1.7F, 2, 2, 4, 0.0F);
		this.lHorn01 = new ModelRenderer(this, 37, 38);
		this.lHorn01.setPos(2.8F, -6.8F, 2.0F);
		this.lHorn01.addBox(-1.0F, -2.5F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotateAngle(lHorn01, 0.08726646259971647F, 0.0F, 0.7853981633974483F);
		this.rightLeg02 = new ModelRenderer(this, 24, 33);
		this.rightLeg02.mirror = true;
		this.rightLeg02.setPos(-0.3F, 6.7F, -0.7F);
		this.rightLeg02.addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3, 0.0F);
		this.setRotateAngle(rightLeg02, 0.767944870877505F, 0.0F, -0.10471975511965977F);
		this.lHorn04 = new ModelRenderer(this, 43, 43);
		this.lHorn04.setPos(0.0F, -2.3F, 0.0F);
		this.lHorn04.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(lHorn04, 0.13962634015954636F, 0.0F, -0.3490658503988659F);
		this.rightHoof = new ModelRenderer(this, 25, 52);
		this.rightHoof.mirror = true;
		this.rightHoof.setPos(0.0F, 5.2F, -0.7F);
		this.rightHoof.addBox(-1.5F, -0.7F, -1.9F, 3, 2, 4, 0.0F);
		this.body = new ModelRenderer(this, 16, 16);
		this.body.setPos(0.0F, -5.7F, 0.0F);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 13, 4, 0.0F);
		this.lEar00 = new ModelRenderer(this, 47, 0);
		this.lEar00.setPos(3.4F, -5.5F, 0.0F);
		this.lEar00.addBox(0.0F, -0.5F, -1.5F, 5, 1, 3, 0.0F);
		this.setRotateAngle(lEar00, -0.6981317007977318F, 0.08726646259971647F, 0.3490658503988659F);
		this.lowerTeethL = new ModelRenderer(this, 24, 58);
		this.lowerTeethL.setPos(2.2F, -0.4F, -3.5F);
		this.lowerTeethL.addBox(-0.8F, -2.0F, -1.4F, 1, 2, 4, 0.0F);
		this.loincloth = new ModelRenderer(this, 48, 34);
		this.loincloth.setPos(0.0F, 10.6F, -1.5F);
		this.loincloth.addBox(-3.0F, 0.0F, -0.5F, 6, 8, 1, 0.0F);
		this.setRotateAngle(loincloth, -0.3490658503988659F, 0.0F, 0.0F);
		this.loinclothBack = new ModelRenderer(this, 48, 51);
		this.loinclothBack.setPos(0.0F, 10.6F, 1.6F);
		this.loinclothBack.addBox(-3.5F, 0.05F, -0.25F, 7, 6, 1, 0.0F);
		this.lHorn02a = new ModelRenderer(this, 39, 38);
		this.lHorn02a.setPos(0.1F, -1.9F, 0.2F);
		this.lHorn02a.addBox(-0.8F, -3.0F, -0.8F, 1, 3, 1, 0.0F);
		this.setRotateAngle(lHorn02a, 0.2792526803190927F, 0.0F, -0.2617993877991494F);
		this.rightArm = new ModelRenderer(this, 40, 16);
		this.rightArm.mirror = true;
		this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 14, 4, 0.0F);
		this.setRotateAngle(rightArm, 0.0F, 0.0F, 0.10000736613927509F);
		this.lowerJaw = new ModelRenderer(this, 0, 55);
		this.lowerJaw.setPos(0.0F, -0.9F, -3.5F);
		this.lowerJaw.addBox(-2.5F, -0.5F, -5.0F, 5, 1, 6, 0.0F);
		this.setRotateAngle(lowerJaw, -0.03490658503988659F, 0.0F, 0.0F);
		this.upperTeethR = new ModelRenderer(this, 52, 58);
		this.upperTeethR.mirror = true;
		this.upperTeethR.setPos(-2.0F, 2.2F, -2.8F);
		this.upperTeethR.addBox(-0.8F, 0.0F, -1.7F, 2, 2, 4, 0.0F);
		this.rHorn02c = new ModelRenderer(this, 39, 38);
		this.rHorn02c.mirror = true;
		this.rHorn02c.setPos(0.0F, 0.0F, 0.0F);
		this.rHorn02c.addBox(-0.8F, -3.0F, -0.2F, 1, 3, 1, 0.0F);
		this.rHorn03 = new ModelRenderer(this, 38, 43);
		this.rHorn03.mirror = true;
		this.rHorn03.setPos(0.0F, -2.7F, 0.0F);
		this.rHorn03.addBox(-0.5F, -2.5F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rHorn03, 0.13962634015954636F, 0.0F, 0.3490658503988659F);
		this.lHorn02a.addChild(this.lHorn03);
		this.rHorn02a.addChild(this.rHorn02d);
		this.leftLeg03.addChild(this.leftHoof);
		this.head.addChild(this.rEar00);
		this.body.addChild(this.leftArm);
		this.head.addChild(this.upperJaw);
		this.rightLeg02.addChild(this.rightLeg03);
		this.body.addChild(this.rightLeg);
		this.lHorn02a.addChild(this.lHorn02c);
		this.leftLeg02.addChild(this.leftLeg03);
		this.head.addChild(this.rHorn01);
		this.head.addChild(this.snout);
		this.lowerJaw.addChild(this.lowerTeethR);
		this.lowerJaw.addChild(this.lowerTeethM);
		this.rHorn02a.addChild(this.rHorn02b);
		this.body.addChild(this.head);
		this.rHorn01.addChild(this.rHorn02a);
		this.lEar00.addChild(this.lEar01);
		this.lHorn02a.addChild(this.lHorn02d);
		this.leftLeg.addChild(this.leftLeg02);
		this.body.addChild(this.leftLeg);
		this.rEar00.addChild(this.rEar01);
		this.lHorn02a.addChild(this.lHorn02b);
		this.rHorn03.addChild(this.rHorn04);
		this.loincloth.addChild(this.loincloth02);
		this.upperJaw.addChild(this.upperTeethL);
		this.head.addChild(this.lHorn01);
		this.rightLeg.addChild(this.rightLeg02);
		this.lHorn03.addChild(this.lHorn04);
		this.rightLeg03.addChild(this.rightHoof);
		this.head.addChild(this.lEar00);
		this.lowerJaw.addChild(this.lowerTeethL);
		this.body.addChild(this.loincloth);
		this.body.addChild(this.loinclothBack);
		this.lHorn01.addChild(this.lHorn02a);
		this.body.addChild(this.rightArm);
		this.head.addChild(this.lowerJaw);
		this.upperJaw.addChild(this.upperTeethR);
		this.rHorn02a.addChild(this.rHorn02c);
		this.rHorn02a.addChild(this.rHorn03);
	}

	@Override
	public void renderToBuffer(MatrixStack matrices,IVertexBuilder vertices,int light,int overlay,float r,float g,float b,float a) { body.render(matrices,vertices,light,overlay,r,g,b,a); }

	@SuppressWarnings("incomplete-switch")
	public void setupAnim(CleaverEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		leftArmPose=entityIn.getOffhandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM;rightArmPose=entityIn.getMainHandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM;
        if(entityIn.getMainArm()==HandSide.LEFT){ArmPose swap=leftArmPose;leftArmPose=rightArmPose;rightArmPose=swap;}
        pose(limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch,entityIn.getFallFlyingTicks()>4,entityIn.getDeltaMovement().lengthSqr(),entityIn.swingingArm==Hand.OFF_HAND?entityIn.getMainArm().getOpposite():entityIn.getMainArm(),entityIn.attackTimer()); }
    public void pose(float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch,boolean flag,double velocitySquared,HandSide attackingArm,int timer) {
        leftLeg.yRot=0;rightLeg.yRot=0;leftLeg.zRot=-.104719755F;rightLeg.zRot=.104719755F;
		
		this.head.yRot = netHeadYaw * 0.017453292F;

		if (flag) {
			this.head.xRot = -((float) Math.PI / 4F);
		} else {
			this.head.xRot = headPitch * 0.017453292F;
		}

		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;
		float f = 1.0F;

		if (flag) {
			f = (float)velocitySquared;
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		float swingMod = 0.6F;
		this.rightArm.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
		this.leftArm.xRot = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.leftLeg.xRot = MathHelper.sin(limbSwing * 0.8665F + (float) Math.PI) * swingMod * limbSwingAmount - 0.26F;
		this.rightLeg.xRot = MathHelper.cos(limbSwing * 0.8665F) * swingMod * limbSwingAmount - 0.26F;

		if (this.riding) {
			this.rightArm.xRot += -((float) Math.PI / 5F);
			this.leftArm.xRot += -((float) Math.PI / 5F);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = ((float) Math.PI / 10F);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = -((float) Math.PI / 10F);
			this.leftLeg.zRot = -0.07853982F;
		}

		this.rightArm.yRot = 0.0F;
		this.rightArm.zRot = 0.0F;

		switch (this.leftArmPose) {
			case EMPTY:
				this.leftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
				this.leftArm.yRot = 0.5235988F;
				break;
			case ITEM:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float) Math.PI / 10F);
				this.leftArm.yRot = 0.0F;
		}

		switch (this.rightArmPose) {
			case EMPTY:
				this.rightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
				this.rightArm.yRot = -0.5235988F;
				break;
			case ITEM:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float) Math.PI / 10F);
				this.rightArm.yRot = 0.0F;
		}

		if (this.attackTime > 0.0F) {
			HandSide enumhandside = attackingArm;
			ModelRenderer modelrenderer = this.getArm(enumhandside);
			float f1 = this.attackTime;
			this.body.yRot = MathHelper.sin(MathHelper.sqrt(f1) * ((float) Math.PI * 2F)) * 0.2F;

			if (enumhandside == HandSide.LEFT) {
				this.body.yRot *= -1.0F;
			}

			this.rightArm.z = MathHelper.sin(this.body.yRot) * 5.0F;
			this.rightArm.x = -MathHelper.cos(this.body.yRot) * 5.0F;
			this.leftArm.z = -MathHelper.sin(this.body.yRot) * 5.0F;
			this.leftArm.x = MathHelper.cos(this.body.yRot) * 5.0F;
			this.rightArm.yRot += this.body.yRot;
			this.leftArm.yRot += this.body.yRot;
			this.leftArm.xRot += this.body.yRot;
			f1 = 1.0F - this.attackTime;
			f1 = f1 * f1;
			f1 = f1 * f1;
			f1 = 1.0F - f1;
			float f2 = MathHelper.sin(f1 * (float) Math.PI);
			float f3 = MathHelper.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelrenderer.xRot = (float) ((double) modelrenderer.xRot - ((double) f2 * 1.2D + (double) f3));
			modelrenderer.yRot += this.body.yRot * 2.0F;
			modelrenderer.zRot += MathHelper.sin(this.attackTime * (float) Math.PI) * -0.4F;
		}

		this.rightArm.zRot += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.rightArm.xRot += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		this.leftArm.xRot -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

		if (this.rightArmPose == BipedModel.ArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot;
			this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
			this.rightArm.xRot = -((float) Math.PI / 2F) + this.head.xRot;
			this.leftArm.xRot = -((float) Math.PI / 2F) + this.head.xRot;
		} else if (this.leftArmPose == BipedModel.ArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
			this.leftArm.yRot = 0.1F + this.head.yRot;
			this.rightArm.xRot = -((float) Math.PI / 2F) + this.head.xRot;
			this.leftArm.xRot = -((float) Math.PI / 2F) + this.head.xRot;
		}

		this.lowerJaw.xRot = 0.0F;
		int i = timer;
		if (i > 0) {
			this.lowerJaw.xRot = 0.75f;
		}

		hat.copyFrom(head);

		
	}

	@Override
	public void translateToHand(HandSide side,MatrixStack matrices) { body.translateAndRotate(matrices); matrices.translate(.025,-.3,0); super.translateToHand(side,matrices); }

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
