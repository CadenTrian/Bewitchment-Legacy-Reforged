package com.bewitchment.client;
import com.bewitchment.entity.GhostEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
/**
 * ghost - cybercat5555
 * Created using Tabula 5.1.0
 */
public class LegacyGhostModel extends EntityModel<GhostEntity> {
	public ModelRenderer bipedRightArm;
	public ModelRenderer bipedHead;
	public ModelRenderer bipedBody;
	public ModelRenderer bipedLeftArm;
	public ModelRenderer skull01;
	public ModelRenderer RArmWisp;
	public ModelRenderer bodyWisp01;
	public ModelRenderer bodyWisp02;
	public ModelRenderer lArmWisp;
	public ModelRenderer skullJaw;

	public LegacyGhostModel() {
		this.texWidth = 64;
		this.texHeight = 64;
		this.skullJaw = new ModelRenderer(this, 19, 30);
		this.skullJaw.setPos(0.0F, -1.9F, 0.9F);
		this.skullJaw.addBox(-2.5F, -1.0F, -3.5F, 5, 2, 5, 0.0F);
		this.setRotateAngle(skullJaw, 0.17453292519943295F, 0.0F, 0.0F);
		this.bodyWisp01 = new ModelRenderer(this, 33, 51);
		this.bodyWisp01.setPos(0.0F, 7.7F, 0.0F);
		this.bodyWisp01.addBox(-4.5F, 0.0F, -2.5F, 9, 6, 5, 0.0F);
		this.setRotateAngle(bodyWisp01, 0.10471975511965977F, 0.0F, 0.0F);
		this.bodyWisp02 = new ModelRenderer(this, 0, 39);
		this.bodyWisp02.setPos(0.0F, 5.8F, 0.1F);
		this.bodyWisp02.addBox(-5.0F, 0.0F, -3.0F, 10, 9, 6, 0.0F);
		this.setRotateAngle(bodyWisp02, 0.10471975511965977F, 0.0F, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 40, 16);
		this.bipedLeftArm.setPos(5.0F, 2.0F, -0.0F);
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 13, 4, 0.0F);
		this.setRotateAngle(bipedLeftArm, -1.3962634015954636F, 0.0F, -0.10000736613927509F);
		this.bipedHead = new ModelRenderer(this, 0, 0);
		this.bipedHead.setPos(0.0F, 0.0F, 0.0F);
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.bipedRightArm = new ModelRenderer(this, 40, 16);
		this.bipedRightArm.mirror = true;
		this.bipedRightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 13, 4, 0.0F);
		this.setRotateAngle(bipedRightArm, -1.3962634015954636F, 0.0F, 0.10000736613927509F);
		this.bipedBody = new ModelRenderer(this, 14, 16);
		this.bipedBody.setPos(0.0F, 0.0F, 0.0F);
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 8, 4, 0.0F);
		this.setRotateAngle(bipedBody, 0.20943951023931953F, 0.0F, 0.0F);
		this.skull01 = new ModelRenderer(this, 34, 0);
		this.skull01.setPos(0.0F, 0.0F, 0.0F);
		this.skull01.addBox(-3.5F, -7.5F, -3.0F, 7, 5, 6, 0.0F);
		this.lArmWisp = new ModelRenderer(this, 40, 34);
		this.lArmWisp.setPos(1.0F, 2.7F, 1.7F);
		this.lArmWisp.addBox(-1.5F, -4.5F, 0.1F, 3, 11, 4, 0.0F);
		this.RArmWisp = new ModelRenderer(this, 40, 34);
		this.RArmWisp.mirror = true;
		this.RArmWisp.setPos(-1.0F, 2.7F, 1.7F);
		this.RArmWisp.addBox(-1.5F, -4.5F, 0.1F, 3, 11, 4, 0.0F);
		this.skull01.addChild(this.skullJaw);
		this.bipedBody.addChild(this.bodyWisp01);
		this.bodyWisp01.addChild(this.bodyWisp02);
		this.bipedLeftArm.addChild(this.lArmWisp);
		this.bipedRightArm.addChild(this.RArmWisp);
	}

	@Override
	public void renderToBuffer(MatrixStack matrices,IVertexBuilder vertices,int light,int overlay,float r,float g,float b,float a) {
		this.bipedLeftArm.render(matrices,vertices,light,overlay,r,g,b,a*.25F);
		this.bipedHead.render(matrices,vertices,light,overlay,r,g,b,a*.25F);
		this.bipedRightArm.render(matrices,vertices,light,overlay,r,g,b,a*.25F);
		this.bipedBody.render(matrices,vertices,light,overlay,r,g,b,a*.25F);
		this.skull01.render(matrices,vertices,light,overlay,r,g,b,a*.25F);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
@Override public void setupAnim(GhostEntity entity,float swing,float amount,float age,float yaw,float pitch) {}}
