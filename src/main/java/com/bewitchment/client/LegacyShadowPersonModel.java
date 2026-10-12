package com.bewitchment.client;
import com.bewitchment.entity.ShadowPersonEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
/**
 * shadowpersonClassic - cybercat5555
 * Created using Tabula 5.1.0
 */
public class LegacyShadowPersonModel extends BipedModel<ShadowPersonEntity> {
	public ModelRenderer bipedBody;
	public ModelRenderer bipedLeftArm;
	public ModelRenderer bipedRightArm;
	public ModelRenderer bipedLeftLeg;
	public ModelRenderer bipedRightLeg;
	public ModelRenderer bipedHead;
	public ModelRenderer lArm2;
	public ModelRenderer rArm2;
	public ModelRenderer lLeg02;
	public ModelRenderer rLeg02;
	public ModelRenderer hatBrim;
	public ModelRenderer hatPipe;

	public LegacyShadowPersonModel() { super(0,0,64,64);
		this.texWidth = 64;
		this.texHeight = 64;
		this.bipedBody = new ModelRenderer(this, 16, 16);
		this.bipedBody.setPos(0.0F, -1.8F, 0.0F);
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
		this.hatBrim = new ModelRenderer(this, 0, 35);
		this.hatBrim.setPos(0.0F, -5.8F, 0.0F);
		this.hatBrim.addBox(-6.0F, -0.5F, -6.0F, 12, 1, 12, 0.0F);
		this.lArm2 = new ModelRenderer(this, 40, 16);
		this.lArm2.setPos(1.0F, 6.0F, -0.0F);
		this.lArm2.addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4, 0.0F);
		this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
		this.bipedLeftLeg.setPos(1.9F, 12.0F, 0.0F);
		this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4, 0.0F);
		this.bipedRightLeg = new ModelRenderer(this, 0, 16);
		this.bipedRightLeg.mirror = true;
		this.bipedRightLeg.setPos(-1.9F, 12.0F, 0.0F);
		this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 8, 4, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 40, 16);
		this.bipedLeftArm.setPos(5.0F, 2.0F, -0.0F);
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F);
		this.setRotateAngle(bipedLeftArm, 0.0F, 0.0F, -0.10000736613927509F);
		this.bipedRightArm = new ModelRenderer(this, 40, 16);
		this.bipedRightArm.mirror = true;
		this.bipedRightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F);
		this.setRotateAngle(bipedRightArm, 0.0F, 0.0F, 0.10000736613927509F);
		this.rLeg02 = new ModelRenderer(this, 0, 16);
		this.rLeg02.mirror = true;
		this.rLeg02.setPos(0.0F, 7.9F, 0.0F);
		this.rLeg02.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.bipedHead = new ModelRenderer(this, 0, 0);
		this.bipedHead.setPos(0.0F, 0.0F, 0.0F);
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.rArm2 = new ModelRenderer(this, 40, 16);
		this.rArm2.mirror = true;
		this.rArm2.setPos(-1.0F, 6.0F, -0.0F);
		this.rArm2.addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4, 0.0F);
		this.lLeg02 = new ModelRenderer(this, 0, 16);
		this.lLeg02.setPos(0.0F, 7.9F, 0.0F);
		this.lLeg02.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.hatPipe = new ModelRenderer(this, 0, 49);
		this.hatPipe.setPos(0.0F, -0.3F, 0.0F);
		this.hatPipe.addBox(-4.5F, -3.5F, -4.5F, 9, 4, 9, 0.0F);
		this.bipedHead.addChild(this.hatBrim);
		this.bipedLeftArm.addChild(this.lArm2);
		this.bipedBody.addChild(this.bipedLeftLeg);
		this.bipedBody.addChild(this.bipedRightLeg);
		this.bipedBody.addChild(this.bipedLeftArm);
		this.bipedBody.addChild(this.bipedRightArm);
		this.bipedRightLeg.addChild(this.rLeg02);
		this.bipedBody.addChild(this.bipedHead);
		this.bipedRightArm.addChild(this.rArm2);
		this.bipedLeftLeg.addChild(this.lLeg02);
		this.hatBrim.addChild(this.hatPipe); head=bipedHead;body=bipedBody;leftArm=bipedLeftArm;rightArm=bipedRightArm;leftLeg=bipedLeftLeg;rightLeg=bipedRightLeg;
	}

	@Override
	public void renderToBuffer(MatrixStack matrices,IVertexBuilder vertices,int light,int overlay,float r,float g,float b,float a) { bipedBody.render(matrices,vertices,light,overlay,r,g,b,a); }

	@SuppressWarnings("incomplete-switch")
	public void setupAnim(ShadowPersonEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		crouching=entityIn.isCrouching(); pose(limbSwing,limbSwingAmount,ageInTicks,netHeadYaw,headPitch,entityIn.getFallFlyingTicks()>4,entityIn.getDeltaMovement().lengthSqr(),entityIn.swingingArm == Hand.OFF_HAND ? entityIn.getMainArm().getOpposite() : entityIn.getMainArm()); }
    public void pose(float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch,boolean flag,double velocitySquared,HandSide attackingArm) {
		this.bipedHead.yRot = netHeadYaw * 0.017453292F;

		if (flag) {
			this.bipedHead.xRot = -((float) Math.PI / 4F);
		} else {
			this.bipedHead.xRot = headPitch * 0.017453292F;
		}

		this.bipedBody.yRot = 0.0F;
		this.bipedRightArm.z = 0.0F;
		this.bipedRightArm.x = -5.0F;
		this.bipedLeftArm.z = 0.0F;
		this.bipedLeftArm.x = 5.0F;
		float f = 1.0F;

		if (flag) {
			f = (float)velocitySquared;
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		this.bipedRightArm.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
		this.bipedLeftArm.xRot = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		this.bipedRightArm.zRot = 0.0F;
		this.bipedLeftArm.zRot = 0.0F;
		this.bipedRightLeg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		this.bipedLeftLeg.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
		this.bipedRightLeg.yRot = 0.0F;
		this.bipedLeftLeg.yRot = 0.0F;
		this.bipedRightLeg.zRot = 0.0F;
		this.bipedLeftLeg.zRot = 0.0F;

		if (this.riding) {
			this.bipedRightArm.xRot += -((float) Math.PI / 5F);
			this.bipedLeftArm.xRot += -((float) Math.PI / 5F);
			this.bipedRightLeg.xRot = -1.4137167F;
			this.bipedRightLeg.yRot = ((float) Math.PI / 10F);
			this.bipedRightLeg.zRot = 0.07853982F;
			this.bipedLeftLeg.xRot = -1.4137167F;
			this.bipedLeftLeg.yRot = -((float) Math.PI / 10F);
			this.bipedLeftLeg.zRot = -0.07853982F;
		}

		this.bipedRightArm.yRot = 0.0F;
		this.bipedRightArm.zRot = 0.0F;

		switch (this.leftArmPose) {
			case EMPTY:
				this.bipedLeftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.bipedLeftArm.xRot = this.bipedLeftArm.xRot * 0.5F - 0.9424779F;
				this.bipedLeftArm.yRot = 0.5235988F;
				break;
			case ITEM:
				this.bipedLeftArm.xRot = this.bipedLeftArm.xRot * 0.5F - ((float) Math.PI / 10F);
				this.bipedLeftArm.yRot = 0.0F;
		}

		switch (this.rightArmPose) {
			case EMPTY:
				this.bipedRightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.bipedRightArm.xRot = this.bipedRightArm.xRot * 0.5F - 0.9424779F;
				this.bipedRightArm.yRot = -0.5235988F;
				break;
			case ITEM:
				this.bipedRightArm.xRot = this.bipedRightArm.xRot * 0.5F - ((float) Math.PI / 10F);
				this.bipedRightArm.yRot = 0.0F;
		}

		if (this.attackTime > 0.0F) {
			HandSide enumhandside = attackingArm;
			ModelRenderer modelrenderer = this.getArm(enumhandside);
			float f1 = this.attackTime;
			this.bipedBody.yRot = MathHelper.sin(MathHelper.sqrt(f1) * ((float) Math.PI * 2F)) * 0.2F;

			if (enumhandside == HandSide.LEFT) {
				this.bipedBody.yRot *= -1.0F;
			}

			this.bipedRightArm.z = MathHelper.sin(this.bipedBody.yRot) * 5.0F;
			this.bipedRightArm.x = -MathHelper.cos(this.bipedBody.yRot) * 5.0F;
			this.bipedLeftArm.z = -MathHelper.sin(this.bipedBody.yRot) * 5.0F;
			this.bipedLeftArm.x = MathHelper.cos(this.bipedBody.yRot) * 5.0F;
			this.bipedRightArm.yRot += this.bipedBody.yRot;
			this.bipedLeftArm.yRot += this.bipedBody.yRot;
			this.bipedLeftArm.xRot += this.bipedBody.yRot;
			f1 = 1.0F - this.attackTime;
			f1 = f1 * f1;
			f1 = f1 * f1;
			f1 = 1.0F - f1;
			float f2 = MathHelper.sin(f1 * (float) Math.PI);
			float f3 = MathHelper.sin(this.attackTime * (float) Math.PI) * -(this.bipedHead.xRot - 0.7F) * 0.75F;
			modelrenderer.xRot = (float) ((double) modelrenderer.xRot - ((double) f2 * 1.2D + (double) f3));
			modelrenderer.yRot += this.bipedBody.yRot * 2.0F;
			modelrenderer.zRot += MathHelper.sin(this.attackTime * (float) Math.PI) * -0.4F;
		}

		if (this.crouching) {
			this.bipedBody.xRot = 0.5F;
			this.bipedRightArm.xRot += 0.4F;
			this.bipedLeftArm.xRot += 0.4F;
			this.bipedRightLeg.z = 4.0F;
			this.bipedLeftLeg.z = 4.0F;
			this.bipedRightLeg.y = 9.0F;
			this.bipedLeftLeg.y = 9.0F;
			this.bipedHead.y = 1.0F;
		} else {
			this.bipedBody.xRot = 0.0F;
			this.bipedRightLeg.z = 0.1F;
			this.bipedLeftLeg.z = 0.1F;
			this.bipedRightLeg.y = 12.0F;
			this.bipedLeftLeg.y = 12.0F;
			this.bipedHead.y = 0.0F;
		}

		this.bipedRightArm.zRot += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.bipedLeftArm.zRot -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.bipedRightArm.xRot += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		this.bipedLeftArm.xRot -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

		if (this.rightArmPose == BipedModel.ArmPose.BOW_AND_ARROW) {
			this.bipedRightArm.yRot = -0.1F + this.bipedHead.yRot;
			this.bipedLeftArm.yRot = 0.1F + this.bipedHead.yRot + 0.4F;
			this.bipedRightArm.xRot = -((float) Math.PI / 2F) + this.bipedHead.xRot;
			this.bipedLeftArm.xRot = -((float) Math.PI / 2F) + this.bipedHead.xRot;
		} else if (this.leftArmPose == BipedModel.ArmPose.BOW_AND_ARROW) {
			this.bipedRightArm.yRot = -0.1F + this.bipedHead.yRot - 0.4F;
			this.bipedLeftArm.yRot = 0.1F + this.bipedHead.yRot;
			this.bipedRightArm.xRot = -((float) Math.PI / 2F) + this.bipedHead.xRot;
			this.bipedLeftArm.xRot = -((float) Math.PI / 2F) + this.bipedHead.xRot;
		}

		hat.copyFrom(head);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
