package com.bewitchment.client;
import com.bewitchment.entity.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.MathHelper;
/**
 * toad - cybercat5555
 * Created using Tabula 5.1.0
 */
@SuppressWarnings("WeakerAccess")
public class LegacyToadModel extends EntityModel<ToadEntity> {
	public final ModelRenderer body;
	public final ModelRenderer neck;
	public final ModelRenderer leftArm00;
	public final ModelRenderer rightArm00;
	public final ModelRenderer leftLeg00;
	public final ModelRenderer rightLeg00;
	public final ModelRenderer head;
	public final ModelRenderer jaw;
	public final ModelRenderer leftEye;
	public final ModelRenderer rightEye;
	public final ModelRenderer leftArm01;
	public final ModelRenderer leftHand;
	public final ModelRenderer rightArm01;
	public final ModelRenderer rightHand;
	public final ModelRenderer leftLeg01;
	public final ModelRenderer leftfoot;
	public final ModelRenderer rightLeg01;
	public final ModelRenderer rightfoot;

	public LegacyToadModel() {
		this.texWidth = 64;
		this.texHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.setPos(0, 17.7f, -1.9f);
		this.body.addBox(-4, -2.5f, 0, 8, 6, 10, 0);
		this.setRotateAngle(body, -0.36f, 0, 0);
		this.leftArm00 = new ModelRenderer(this, 16, 25);
		this.leftArm00.setPos(3, 0.6f, 1.1f);
		this.leftArm00.addBox(0, -1, -1, 2, 2, 5, 0);
		this.setRotateAngle(leftArm00, -0.82f, 0, -0.28f);
		this.leftLeg01 = new ModelRenderer(this, 39, 19);
		this.leftLeg01.setPos(0.9f, 3.6f, -0.2f);
		this.leftLeg01.addBox(-1.2f, -1, 0, 2, 2, 5, 0);
		this.setRotateAngle(leftLeg01, 0.45f, 0.14f, 0.17f);
		this.rightArm00 = new ModelRenderer(this, 16, 25);
		this.rightArm00.mirror = true;
		this.rightArm00.setPos(-3, 0.6f, 1.1f);
		this.rightArm00.addBox(-2, -1, -1, 2, 2, 5, 0);
		this.setRotateAngle(rightArm00, -0.82f, 0, 0.28f);
		this.rightHand = new ModelRenderer(this, 3, 27);
		this.rightHand.mirror = true;
		this.rightHand.setPos(-0.1f, 4.5f, -0.3f);
		this.rightHand.addBox(-1.5f, 0, -2.7f, 3, 0, 4, 0);
		this.setRotateAngle(rightHand, 0.72f, -0.09f, -0.45f);
		this.jaw = new ModelRenderer(this, 37, 10);
		this.jaw.setPos(0, 0.4f, -0.6f);
		this.jaw.addBox(-3, 0, -5, 6, 2, 6, 0);
		this.neck = new ModelRenderer(this, 0, 17);
		this.neck.setPos(0, -0.9f, 1.3f);
		this.neck.addBox(-2.5f, -1.5f, -3, 5, 5, 4, 0);
		this.setRotateAngle(neck, -0.46f, 0, 0);
		this.leftLeg00 = new ModelRenderer(this, 22, 16);
		this.leftLeg00.setPos(3.3f, -0.8f, 7.5f);
		this.leftLeg00.addBox(0, -1, -1.5f, 2, 5, 3, 0);
		this.setRotateAngle(leftLeg00, -0.49f, 0, -0.17f);
		this.leftHand = new ModelRenderer(this, 3, 27);
		this.leftHand.setPos(0.1f, 4.5f, -0.3f);
		this.leftHand.addBox(-1.5f, 0, -2.7f, 3, 0, 4, 0);
		this.setRotateAngle(leftHand, 0.72f, 0.09f, 0.45f);
		this.rightfoot = new ModelRenderer(this, 49, 25);
		this.rightfoot.mirror = true;
		this.rightfoot.setPos(0.1f, 0.6f, 4.3f);
		this.rightfoot.addBox(-1.2f, -0.5f, -5, 2, 1, 5, 0);
		this.setRotateAngle(rightfoot, 0.4f, 0.31f, 0);
		this.head = new ModelRenderer(this, 37, 0);
		this.head.setPos(0, -0.2f, -2.2f);
		this.head.addBox(-3, -1.5f, -5.7f, 6, 2, 6, 0);
		this.setRotateAngle(head, 0.87f, 0, 0);
		this.rightArm01 = new ModelRenderer(this, 32, 25);
		this.rightArm01.mirror = true;
		this.rightArm01.setPos(-1, -0.1f, 2.8f);
		this.rightArm01.addBox(-1.01f, 0, -1, 2, 5, 2, 0);
		this.setRotateAngle(rightArm01, 0.45f, 0, 0);
		this.leftArm01 = new ModelRenderer(this, 32, 25);
		this.leftArm01.setPos(1, -0.1f, 2.8f);
		this.leftArm01.addBox(-0.99f, 0, -1, 2, 5, 2, 0);
		this.setRotateAngle(leftArm01, 0.45f, 0, 0);
		this.leftfoot = new ModelRenderer(this, 49, 25);
		this.leftfoot.setPos(-0.1f, 0.6f, 4.3f);
		this.leftfoot.addBox(-1.2f, -0.5f, -5, 2, 1, 5, 0);
		this.setRotateAngle(leftfoot, 0.4f, -0.31f, 0);
		this.rightLeg01 = new ModelRenderer(this, 39, 19);
		this.rightLeg01.mirror = true;
		this.rightLeg01.setPos(-0.9f, 3.6f, -0.2f);
		this.rightLeg01.addBox(-1.2f, -1, 0, 2, 2, 5, 0);
		this.setRotateAngle(rightLeg01, 0.45f, -0.14f, -0.17f);
		this.rightLeg00 = new ModelRenderer(this, 22, 16);
		this.rightLeg00.mirror = true;
		this.rightLeg00.setPos(-3.3f, -0.8f, 7.5f);
		this.rightLeg00.addBox(-2, -1, -1.5f, 2, 5, 3, 0);
		this.setRotateAngle(rightLeg00, -0.49f, 0, 0.17f);
		this.leftEye = new ModelRenderer(this, 27, 0);
		this.leftEye.setPos(1.3f, -0.5f, -2.3f);
		this.leftEye.addBox(0, -2.1f, -1.5f, 2, 2, 3, 0);
		this.setRotateAngle(leftEye, 0, 0.18f, 0.14f);
		this.rightEye = new ModelRenderer(this, 27, 0);
		this.rightEye.mirror = true;
		this.rightEye.setPos(-1.3f, -0.5f, -2.3f);
		this.rightEye.addBox(-2, -2.1f, -1.5f, 2, 2, 3, 0);
		this.setRotateAngle(rightEye, 0, -0.18f, -0.14f);
		this.body.addChild(this.leftArm00);
		this.leftLeg00.addChild(this.leftLeg01);
		this.body.addChild(this.rightArm00);
		this.rightArm01.addChild(this.rightHand);
		this.head.addChild(this.jaw);
		this.body.addChild(this.neck);
		this.body.addChild(this.leftLeg00);
		this.leftArm01.addChild(this.leftHand);
		this.rightLeg01.addChild(this.rightfoot);
		this.neck.addChild(this.head);
		this.rightArm00.addChild(this.rightArm01);
		this.leftArm00.addChild(this.leftArm01);
		this.leftLeg01.addChild(this.leftfoot);
		this.rightLeg00.addChild(this.rightLeg01);
		this.body.addChild(this.rightLeg00);
		this.head.addChild(this.leftEye);
		this.head.addChild(this.rightEye);
	}

	
    @Override public void renderToBuffer(MatrixStack matrices, IVertexBuilder vertices, int light, int overlay, float r, float g, float b, float a) { body.render(matrices, vertices, light, overlay, r, g, b, a); }
    @Override public void setupAnim(ToadEntity entity,float swing,float amount,float age,float yaw,float pitch) {
        // Render-only motion, reset per animal without mutating synchronized data.
        float hop=CreatureRules.hop(swing,amount,entity.isInSittingPose());
        body.y=17.7F-hop*3; body.xRot=-.36651914F+hop*.35F;
        leftLeg00.xRot=-.5122541F+hop*1.4F; rightLeg00.xRot=leftLeg00.xRot;
        leftLeg01.xRot=.4537856F-hop; rightLeg01.xRot=leftLeg01.xRot;
        leftfoot.xRot=.418879F+hop; rightfoot.xRot=leftfoot.xRot;
        leftArm00.xRot=-.8159414F-hop*.7F; rightArm00.xRot=leftArm00.xRot;
        leftArm01.xRot=.4537856F+hop*.4F; rightArm01.xRot=leftArm01.xRot;
        leftfoot.yRot=entity.isInSittingPose()?1.0471976F:-.6F; rightfoot.yRot=-leftfoot.yRot;
        jaw.y=.5F+.02F*MathHelper.sin(age*.1F);
    }
    private void setRotateAngle(ModelRenderer part,float x,float y,float z) {part.xRot=x;part.yRot=y;part.zRot=z;}
}