package com.bewitchment.client;
import com.bewitchment.entity.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.MathHelper;
/**
 * snake - cybercat5555
 * Created using Tabula 5.1.0
 */
@SuppressWarnings("WeakerAccess")
public class LegacySnakeModel extends EntityModel<SnakeEntity> {
	public final ModelRenderer body;
	public final ModelRenderer tail00;
	public final ModelRenderer neck00;
	public final ModelRenderer tail00b;
	public final ModelRenderer tail01;
	public final ModelRenderer tail02;
	public final ModelRenderer tail02b;
	public final ModelRenderer tail03;
	public final ModelRenderer tail04;
	public final ModelRenderer neck00b;
	public final ModelRenderer neck01;
	public final ModelRenderer head;
	public final ModelRenderer tongue;
	public final ModelRenderer upperJaw;
	public final ModelRenderer leftUpperJaw;
	public final ModelRenderer rightUpperJaw;
	public final ModelRenderer lowerJaw;
	public final ModelRenderer eyes;
	public final ModelRenderer leftfang;
	public final ModelRenderer rightfang;

	public LegacySnakeModel() {
		this.texWidth = 64;
		this.texHeight = 64;
		this.tail00b = new ModelRenderer(this, 46, 18);
		this.tail00b.setPos(0, 0, 0);
		this.tail00b.addBox(-2.3f, -1.5f, 0.5f, 2, 3, 7, 0);
		this.neck00b = new ModelRenderer(this, 43, 28);
		this.neck00b.setPos(0, 0, 0);
		this.neck00b.addBox(-2.3f, -1.49f, -8, 2, 3, 8, 0);
		this.body = new ModelRenderer(this, 0, 31);
		this.body.setPos(0, 22.5f, -3.4f);
		this.body.addBox(-2.5f, -1.8f, -0.4f, 5, 4, 8, 0);
		this.tail02 = new ModelRenderer(this, 23, 45);
		this.tail02.setPos(0, 0, 5.9f);
		this.tail02.addBox(-1.5f, -1.1f, -1, 3, 2, 7, 0);
		this.upperJaw = new ModelRenderer(this, 0, 0);
		this.upperJaw.setPos(0, -1.3f, -2.8f);
		this.upperJaw.addBox(-1.5f, -1.1f, -4.4f, 3, 2, 5, 0);
		this.setRotateAngle(upperJaw, 0.09f, 0, 0);
		this.tail04 = new ModelRenderer(this, 43, 54);
		this.tail04.setPos(0, 0.5f, 7.8f);
		this.tail04.addBox(-0.5f, -0.5f, -0.6f, 1, 1, 8, 0);
		this.eyes = new ModelRenderer(this, 40, 0);
		this.eyes.setPos(0, -2, -3.9f);
		this.eyes.addBox(-2, -0.5f, -0.5f, 4, 1, 1, 0);
		this.leftUpperJaw = new ModelRenderer(this, 17, 0);
		this.leftUpperJaw.setPos(0.9f, -0.9f, -3);
		this.leftUpperJaw.addBox(-0.5f, -1, -3.7f, 2, 2, 5, 0);
		this.setRotateAngle(leftUpperJaw, 0, 0.19f, 0);
		this.rightUpperJaw = new ModelRenderer(this, 17, 0);
		this.rightUpperJaw.mirror = true;
		this.rightUpperJaw.setPos(-1.9f, -0.9f, -3);
		this.rightUpperJaw.addBox(-0.5f, -1, -4.07f, 2, 2, 5, 0);
		this.setRotateAngle(rightUpperJaw, 0, -0.19f, 0);
		this.tail02b = new ModelRenderer(this, 22, 55);
		this.tail02b.setPos(0, 0.5f, 0);
		this.tail02b.addBox(-1.5f, 0, -1, 3, 1, 7, 0);
		this.tail00 = new ModelRenderer(this, 27, 22);
		this.tail00.setPos(0, 0, 6.7f);
		this.tail00.addBox(-0.7f, -1.5f, 0.6f, 3, 3, 7, 0);
		this.neck01 = new ModelRenderer(this, 0, 45);
		this.neck01.setPos(0, 0, -8.6f);
		this.neck01.addBox(-2, -1.5f, -6, 4, 3, 7, 0);
		this.tongue = new ModelRenderer(this, 25, 0);
		this.tongue.setPos(0, 0.4f, -2);
		this.tongue.addBox(-1.5f, 0, -5, 3, 0, 7, 0);
		this.neck00 = new ModelRenderer(this, 27, 33);
		this.neck00.setPos(0, 0, -0.2f);
		this.neck00.addBox(-0.7f, -1.49f, -8, 3, 3, 8, 0);
		this.tail01 = new ModelRenderer(this, 0, 45);
		this.tail01.setPos(0, 0, 7.4f);
		this.tail01.addBox(-2, -1.49f, -0.5f, 4, 3, 7, 0);
		this.rightfang = new ModelRenderer(this, 0, 0);
		this.rightfang.setPos(-1.4f, -0.5f, -3.6f);
		this.rightfang.addBox(0, 0, -0.5f, 0, 2, 1, 0);
		this.tail03 = new ModelRenderer(this, 41, 40);
		this.tail03.setPos(0, 0.5f, 5.8f);
		this.tail03.addBox(-1, -1, -1, 2, 2, 9, 0);
		this.head = new ModelRenderer(this, 0, 22);
		this.head.setPos(0, 0.8f, -5.9f);
		this.head.addBox(-2.5f, -2.4f, -3, 5, 3, 3, 0);
		this.lowerJaw = new ModelRenderer(this, 0, 8);
		this.lowerJaw.setPos(0, 0.1f, -2);
		this.lowerJaw.addBox(-1.5f, -0.5f, -5, 3, 1, 5, 0);
		this.leftfang = new ModelRenderer(this, 0, 0);
		this.leftfang.setPos(1.4f, -0.5f, -3.6f);
		this.leftfang.addBox(0, 0, -0.5f, 0, 2, 1, 0);
		this.tail00.addChild(this.tail00b);
		this.neck00.addChild(this.neck00b);
		this.tail01.addChild(this.tail02);
		this.head.addChild(this.upperJaw);
		this.tail03.addChild(this.tail04);
		this.head.addChild(this.eyes);
		this.head.addChild(this.leftUpperJaw);
		this.head.addChild(this.rightUpperJaw);
		this.tail02.addChild(this.tail02b);
		this.body.addChild(this.tail00);
		this.neck00.addChild(this.neck01);
		this.head.addChild(this.tongue);
		this.body.addChild(this.neck00);
		this.tail00.addChild(this.tail01);
		this.upperJaw.addChild(this.rightfang);
		this.tail02.addChild(this.tail03);
		this.neck01.addChild(this.head);
		this.head.addChild(this.lowerJaw);
		this.upperJaw.addChild(this.leftfang);
	}

	
    @Override public void renderToBuffer(MatrixStack matrices, IVertexBuilder vertices, int light, int overlay, float r, float g, float b, float a) { body.render(matrices, vertices, light, overlay, r, g, b, a); }
    @Override public void setupAnim(SnakeEntity entity,float swing,float amount,float age,float yaw,float pitch) {
        pose(age,yaw,pitch,SnakeRules.sway((float)(entity.getDeltaMovement().x+entity.getDeltaMovement().z)*20),entity.isInSittingPose(),entity.hissTime()<=60);
    }
    public void pose(float age,float yaw,float pitch,float motion,boolean sitting,boolean hissing) {
        float time=age*.4F,angle=SnakeRules.sway(motion);
        neck00.yRot=.2617994F+angle*MathHelper.sin(time-1);
        neck01.yRot=-.2617994F+angle*MathHelper.sin(time+1);
        neck00.xRot=pitch*((float)Math.PI/180);neck01.xRot=0;
        body.yRot=angle*MathHelper.sin(time+10)+yaw*((float)Math.PI/180);
        tail00.yRot=.2617994F+angle*MathHelper.sin(time);
        tail01.yRot=.2617994F+angle*MathHelper.sin(time+2);
        tail02.yRot=-.5235988F+angle*MathHelper.sin(time+3);
        tail03.yRot=-.2617994F+angle*MathHelper.sin(time+1);
        tail04.yRot=.6981317F+angle*MathHelper.sin(time);
        head.xRot=0;head.yRot=0;head.zRot=0;
        tongue.z=hissing?-6.48F:-2;body.z=hissing?1.08F:-3.4F;
        if(sitting){
            neck00.yRot=-.09110619F;neck01.yRot=-1.2747885F;neck01.xRot=-.27314404F;
            body.yRot=1.0939724F;tail00.yRot=.95609134F;tail01.yRot=1.2747885F;
            tail02.yRot=.8196066F;tail03.yRot=1.548107F;tail04.yRot=1.5934856F;
            head.xRot=.13665928F;head.yRot=-.7740535F;head.zRot=-.18203784F;
        }
    }
    private void setRotateAngle(ModelRenderer part,float x,float y,float z) {part.xRot=x;part.yRot=y;part.zRot=z;}
}