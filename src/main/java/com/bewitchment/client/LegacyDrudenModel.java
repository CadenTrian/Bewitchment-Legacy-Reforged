package com.bewitchment.client;
import com.bewitchment.entity.DrudenEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
/**
 * drude - cybercat5555
 * Created using Tabula 5.1.0
 */
public class LegacyDrudenModel extends BipedModel<DrudenEntity> {
	public ModelRenderer bipedBody;
	public ModelRenderer boobs;
	public ModelRenderer bipedHead;
	public ModelRenderer bipedLeftArm;
	public ModelRenderer bipedRightArm;
	public ModelRenderer lowerBody;
	public ModelRenderer hair00;
	public ModelRenderer hair01;
	public ModelRenderer leftAntler00;
	public ModelRenderer rightAntler00;
	public ModelRenderer hood;
	public ModelRenderer leftAntler01a;
	public ModelRenderer leftAntler07a;
	public ModelRenderer leftAntler01b;
	public ModelRenderer leftAntler01c;
	public ModelRenderer leftAntler01d;
	public ModelRenderer leftAntler02;
	public ModelRenderer leftAntler06a;
	public ModelRenderer leftAntler03a;
	public ModelRenderer leftAntler04a;
	public ModelRenderer leftAntler05a;
	public ModelRenderer leftAntler03b;
	public ModelRenderer leftAntler03c;
	public ModelRenderer leftAntler04b;
	public ModelRenderer leftAntler05b;
	public ModelRenderer leftAntler06b;
	public ModelRenderer leftAntler07b;
	public ModelRenderer rightAntler01a;
	public ModelRenderer rightAntler07a;
	public ModelRenderer rightAntler01b;
	public ModelRenderer rightAntler01c;
	public ModelRenderer rightAntler01d;
	public ModelRenderer rightAntler02;
	public ModelRenderer rightAntler06a;
	public ModelRenderer rightAntler03a;
	public ModelRenderer rightAntler04a;
	public ModelRenderer rightAntler05a;
	public ModelRenderer rightAntler03b;
	public ModelRenderer rightAntler03c;
	public ModelRenderer rightAntler04b;
	public ModelRenderer rightAntler05b;
	public ModelRenderer rightAntler06b;
	public ModelRenderer rightAntler07b;
	public ModelRenderer leftArmLeaves00;
	public ModelRenderer leftClaw00;
	public ModelRenderer leftClaw01;
	public ModelRenderer leftClaw02;
	public ModelRenderer leftArmLeaves01;
	public ModelRenderer rightArmLeaves00;
	public ModelRenderer rightClaw00;
	public ModelRenderer rightClaw01;
	public ModelRenderer rightClaw02;
	public ModelRenderer rightArmLeaves01;
	public ModelRenderer backSkirt00;
	public ModelRenderer leftSkirt;
	public ModelRenderer rightSkirt;
	public ModelRenderer bipedLeftLeg;
	public ModelRenderer bipedRightLeg;
	public ModelRenderer backSkirt01;
	public ModelRenderer leftLeg01;
	public ModelRenderer leftLeg02;
	public ModelRenderer leftHoof;
	public ModelRenderer rightLeg01;
	public ModelRenderer rightLeg02;
	public ModelRenderer rightHoof;

	public LegacyDrudenModel() { super(0,0,128,128);
		this.texWidth = 128;
		this.texHeight = 64;
		this.bipedBody = new ModelRenderer(this, 19, 17);
		this.bipedBody.setPos(0.0F, -8.8F, 0.0F);
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 6, 4, 0.0F);
		this.leftAntler02 = new ModelRenderer(this, 49, 34);
		this.leftAntler02.setPos(0.0F, -3.1F, 0.0F);
		this.leftAntler02.addBox(-0.5F, -4.0F, -0.5F, 1, 4, 1, 0.0F);
		this.setRotateAngle(leftAntler02, 0.0F, 0.0F, -0.3141592653589793F);
		this.rightAntler01b = new ModelRenderer(this, 49, 34);
		this.rightAntler01b.mirror = true;
		this.rightAntler01b.setPos(0.0F, 0.0F, 0.0F);
		this.rightAntler01b.addBox(-0.8F, -3.5F, -0.8F, 1, 4, 1, 0.0F);
		this.rightAntler05b = new ModelRenderer(this, 49, 34);
		this.rightAntler05b.mirror = true;
		this.rightAntler05b.setPos(-0.0F, -2.7F, 0.0F);
		this.rightAntler05b.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(rightAntler05b, 0.3141592653589793F, 0.0F, -0.3141592653589793F);
		this.leftAntler00 = new ModelRenderer(this, 49, 34);
		this.leftAntler00.setPos(1.9F, -7.1F, 0.0F);
		this.leftAntler00.addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotateAngle(leftAntler00, -0.20943951023931953F, 0.0F, 0.3490658503988659F);
		this.rightClaw00 = new ModelRenderer(this, 0, 0);
		this.rightClaw00.mirror = true;
		this.rightClaw00.setPos(-0.6F, 11.2F, -1.5F);
		this.rightClaw00.addBox(-1.8F, -1.5F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(rightClaw00, -0.10471975511965977F, 0.0F, -0.22689280275926282F);
		this.boobs = new ModelRenderer(this, 0, 57);
		this.boobs.setPos(0.0F, 1.3F, -0.3F);
		this.boobs.addBox(-3.5F, 0.0F, -2.0F, 7, 4, 3, 0.0F);
		this.setRotateAngle(boobs, -0.4886921905584123F, 0.0F, 0.0F);
		this.leftAntler04a = new ModelRenderer(this, 49, 34);
		this.leftAntler04a.setPos(-0.9F, -5.9F, 0.0F);
		this.leftAntler04a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(leftAntler04a, 0.45378560551852565F, 0.0F, -0.8028514559173915F);
		this.rightAntler01a = new ModelRenderer(this, 49, 34);
		this.rightAntler01a.mirror = true;
		this.rightAntler01a.setPos(0.0F, -1.6F, 0.0F);
		this.rightAntler01a.addBox(-0.2F, -3.5F, -0.8F, 1, 4, 1, 0.0F);
		this.setRotateAngle(rightAntler01a, -0.20943951023931953F, 0.0F, -0.6108652381980153F);
		this.leftAntler06a = new ModelRenderer(this, 49, 34);
		this.leftAntler06a.setPos(0.0F, -2.5F, -0.4F);
		this.leftAntler06a.addBox(-0.5F, -0.5F, -3.8F, 1, 1, 4, 0.0F);
		this.setRotateAngle(leftAntler06a, -0.6283185307179586F, 0.6981317007977318F, 0.0F);
		this.bipedHead = new ModelRenderer(this, 0, 1);
		this.bipedHead.setPos(0.0F, 0.0F, 0.0F);
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.rightAntler02 = new ModelRenderer(this, 49, 34);
		this.rightAntler02.mirror = true;
		this.rightAntler02.setPos(0.0F, -3.1F, 0.0F);
		this.rightAntler02.addBox(-0.5F, -4.0F, -0.5F, 1, 4, 1, 0.0F);
		this.setRotateAngle(rightAntler02, 0.0F, 0.0F, 0.3141592653589793F);
		this.leftAntler01b = new ModelRenderer(this, 49, 34);
		this.leftAntler01b.setPos(0.0F, 0.0F, 0.0F);
		this.leftAntler01b.addBox(-0.8F, -3.5F, -0.8F, 1, 4, 1, 0.0F);
		this.rightClaw01 = new ModelRenderer(this, 0, 0);
		this.rightClaw01.mirror = true;
		this.rightClaw01.setPos(-0.6F, 11.2F, 0.0F);
		this.rightClaw01.addBox(-1.8F, -1.2F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(rightClaw01, 0.0F, 0.0F, -0.22689280275926282F);
		this.rightSkirt = new ModelRenderer(this, 88, 22);
		this.rightSkirt.mirror = true;
		this.rightSkirt.setPos(-2.2F, 5.8F, 0.0F);
		this.rightSkirt.addBox(-1.9F, 0.0F, -2.5F, 2, 14, 5, 0.0F);
		this.setRotateAngle(rightSkirt, -0.06981317007977318F, 0.0F, 0.08726646259971647F);
		this.rightArmLeaves00 = new ModelRenderer(this, 33, 39);
		this.rightArmLeaves00.mirror = true;
		this.rightArmLeaves00.setPos(-0.5F, 5.3F, 0.0F);
		this.rightArmLeaves00.addBox(-2.0F, 0.0F, -2.5F, 4, 6, 5, 0.0F);
		this.leftLeg02 = new ModelRenderer(this, 0, 41);
		this.leftLeg02.setPos(0.0F, 5.2F, 0.2F);
		this.leftLeg02.addBox(-1.0F, 0.0F, -1.5F, 2, 7, 3, 0.0F);
		this.setRotateAngle(leftLeg02, -0.41887902047863906F, 0.0F, 0.0F);
		this.leftAntler01d = new ModelRenderer(this, 49, 34);
		this.leftAntler01d.setPos(0.0F, 0.0F, 0.0F);
		this.leftAntler01d.addBox(-0.8F, -3.5F, -0.2F, 1, 4, 1, 0.0F);
		this.leftAntler05b = new ModelRenderer(this, 49, 34);
		this.leftAntler05b.setPos(-0.0F, -2.7F, 0.0F);
		this.leftAntler05b.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(leftAntler05b, 0.3141592653589793F, 0.0F, 0.3141592653589793F);
		this.rightAntler01c = new ModelRenderer(this, 49, 34);
		this.rightAntler01c.mirror = true;
		this.rightAntler01c.setPos(0.0F, 0.0F, 0.0F);
		this.rightAntler01c.addBox(-0.2F, -3.5F, -0.2F, 1, 4, 1, 0.0F);
		this.rightAntler03a = new ModelRenderer(this, 49, 34);
		this.rightAntler03a.mirror = true;
		this.rightAntler03a.setPos(0.0F, -3.7F, 0.0F);
		this.rightAntler03a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rightAntler03a, 0.0F, 0.0F, 0.3490658503988659F);
		this.hood = new ModelRenderer(this, 69, 0);
		this.hood.setPos(0.0F, 0.0F, 0.0F);
		this.hood.addBox(-4.5F, -8.5F, -4.5F, 9, 11, 9, 0.0F);
		this.rightAntler06a = new ModelRenderer(this, 49, 34);
		this.rightAntler06a.mirror = true;
		this.rightAntler06a.setPos(0.0F, -2.5F, -0.4F);
		this.rightAntler06a.addBox(-0.5F, -0.5F, -3.8F, 1, 1, 4, 0.0F);
		this.setRotateAngle(rightAntler06a, -0.6283185307179586F, -0.6981317007977318F, 0.0F);
		this.leftAntler03c = new ModelRenderer(this, 49, 34);
		this.leftAntler03c.setPos(-0.0F, -3.7F, 0.0F);
		this.leftAntler03c.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(leftAntler03c, 0.0F, 0.0F, -0.4363323129985824F);
		this.leftAntler04b = new ModelRenderer(this, 49, 34);
		this.leftAntler04b.setPos(-0.0F, -2.7F, 0.0F);
		this.leftAntler04b.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(leftAntler04b, -0.3141592653589793F, 0.0F, 0.0F);
		this.leftSkirt = new ModelRenderer(this, 88, 22);
		this.leftSkirt.setPos(2.2F, 5.8F, 0.0F);
		this.leftSkirt.addBox(0.0F, 0.0F, -2.5F, 2, 14, 5, 0.0F);
		this.setRotateAngle(leftSkirt, -0.06981317007977318F, 0.0F, -0.08726646259971647F);
		this.leftAntler07b = new ModelRenderer(this, 49, 34);
		this.leftAntler07b.setPos(0.1F, 0.0F, -3.4F);
		this.leftAntler07b.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
		this.setRotateAngle(leftAntler07b, 0.0F, 0.5759586531581287F, 0.0F);
		this.bipedRightLeg = new ModelRenderer(this, 0, 17);
		this.bipedRightLeg.mirror = true;
		this.bipedRightLeg.setPos(-2.1F, 8.0F, 0.1F);
		this.bipedRightLeg.addBox(-2.0F, -1.0F, -2.5F, 4, 8, 5, 0.0F);
		this.setRotateAngle(bipedRightLeg, -0.2617993877991494F, 0.0F, 0.10471975511965977F);
		this.rightAntler04b = new ModelRenderer(this, 49, 34);
		this.rightAntler04b.mirror = true;
		this.rightAntler04b.setPos(-0.0F, -2.7F, 0.0F);
		this.rightAntler04b.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
		this.setRotateAngle(rightAntler04b, -0.3141592653589793F, 0.0F, 0.0F);
		this.leftLeg01 = new ModelRenderer(this, 0, 30);
		this.leftLeg01.setPos(0.0F, 5.7F, -0.4F);
		this.leftLeg01.addBox(-1.5F, 0.0F, -2.0F, 3, 6, 4, 0.0F);
		this.setRotateAngle(leftLeg01, 0.6981317007977318F, 0.0F, 0.10471975511965977F);
		this.rightLeg01 = new ModelRenderer(this, 0, 30);
		this.rightLeg01.mirror = true;
		this.rightLeg01.setPos(0.0F, 5.7F, -0.4F);
		this.rightLeg01.addBox(-1.5F, 0.0F, -2.0F, 3, 6, 4, 0.0F);
		this.setRotateAngle(rightLeg01, 0.6981317007977318F, 0.0F, -0.10471975511965977F);
		this.lowerBody = new ModelRenderer(this, 19, 27);
		this.lowerBody.setPos(0.0F, 6.0F, 0.0F);
		this.lowerBody.addBox(-3.5F, 0.0F, -2.0F, 7, 8, 4, 0.0F);
		this.leftAntler07a = new ModelRenderer(this, 49, 34);
		this.leftAntler07a.setPos(0.0F, -2.0F, -0.4F);
		this.leftAntler07a.addBox(-0.5F, -0.5F, -3.8F, 1, 1, 4, 0.0F);
		this.setRotateAngle(leftAntler07a, -0.9075712110370513F, 0.5235987755982988F, 0.0F);
		this.rightAntler05a = new ModelRenderer(this, 49, 34);
		this.rightAntler05a.mirror = true;
		this.rightAntler05a.setPos(1.2F, -6.7F, 0.0F);
		this.rightAntler05a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rightAntler05a, -0.5235987755982988F, 0.0F, 1.3089969389957472F);
		this.leftAntler01c = new ModelRenderer(this, 49, 34);
		this.leftAntler01c.setPos(0.0F, 0.0F, 0.0F);
		this.leftAntler01c.addBox(-0.2F, -3.5F, -0.2F, 1, 4, 1, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 44, 14);
		this.bipedLeftArm.setPos(5.0F, 2.0F, -0.0F);
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 14, 4, 0.0F);
		this.setRotateAngle(bipedLeftArm, 0.0F, 0.0F, -0.10000736613927509F);
		this.rightAntler00 = new ModelRenderer(this, 49, 34);
		this.rightAntler00.mirror = true;
		this.rightAntler00.setPos(-1.9F, -7.1F, 0.0F);
		this.rightAntler00.addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F);
		this.setRotateAngle(rightAntler00, -0.20943951023931953F, 0.0F, -0.3490658503988659F);
		this.rightHoof = new ModelRenderer(this, 0, 51);
		this.rightHoof.mirror = true;
		this.rightHoof.setPos(0.0F, 6.8F, 0.0F);
		this.rightHoof.addBox(-1.5F, 0.0F, -2.9F, 3, 2, 4, 0.0F);
		this.leftHoof = new ModelRenderer(this, 0, 51);
		this.leftHoof.setPos(0.0F, 6.8F, 0.0F);
		this.leftHoof.addBox(-1.5F, 0.0F, -2.9F, 3, 2, 4, 0.0F);
		this.rightClaw02 = new ModelRenderer(this, 0, 0);
		this.rightClaw02.mirror = true;
		this.rightClaw02.setPos(-0.6F, 11.2F, 1.5F);
		this.rightClaw02.addBox(-1.8F, -1.5F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(rightClaw02, 0.10471975511965977F, 0.0F, -0.22689280275926282F);
		this.rightAntler03c = new ModelRenderer(this, 49, 34);
		this.rightAntler03c.mirror = true;
		this.rightAntler03c.setPos(-0.0F, -3.7F, 0.0F);
		this.rightAntler03c.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rightAntler03c, 0.0F, 0.0F, 0.4363323129985824F);
		this.leftClaw01 = new ModelRenderer(this, 0, 0);
		this.leftClaw01.setPos(0.6F, 11.2F, 0.0F);
		this.leftClaw01.addBox(-0.2F, -1.2F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(leftClaw01, 0.0F, 0.0F, 0.22689280275926282F);
		this.leftAntler03b = new ModelRenderer(this, 49, 34);
		this.leftAntler03b.setPos(-0.0F, -2.8F, 0.0F);
		this.leftAntler03b.addBox(-0.5F, -3.9F, -0.5F, 1, 4, 1, 0.0F);
		this.setRotateAngle(leftAntler03b, -0.10471975511965977F, 0.0F, -0.2792526803190927F);
		this.rightAntler06b = new ModelRenderer(this, 49, 34);
		this.rightAntler06b.mirror = true;
		this.rightAntler06b.setPos(-0.1F, 0.0F, -3.4F);
		this.rightAntler06b.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
		this.setRotateAngle(rightAntler06b, 0.0F, -0.5759586531581287F, 0.0F);
		this.rightAntler07a = new ModelRenderer(this, 49, 34);
		this.rightAntler07a.mirror = true;
		this.rightAntler07a.setPos(0.0F, -2.0F, -0.4F);
		this.rightAntler07a.addBox(-0.5F, -0.5F, -3.8F, 1, 1, 4, 0.0F);
		this.setRotateAngle(rightAntler07a, -0.9075712110370513F, -0.5235987755982988F, 0.0F);
		this.leftClaw00 = new ModelRenderer(this, 0, 0);
		this.leftClaw00.setPos(0.6F, 11.2F, -1.5F);
		this.leftClaw00.addBox(-0.2F, -1.5F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(leftClaw00, -0.10471975511965977F, 0.0F, 0.22689280275926282F);
		this.bipedLeftLeg = new ModelRenderer(this, 0, 17);
		this.bipedLeftLeg.setPos(2.1F, 8.0F, 0.1F);
		this.bipedLeftLeg.addBox(-2.0F, -1.0F, -2.5F, 4, 8, 5, 0.0F);
		this.setRotateAngle(bipedLeftLeg, -0.2617993877991494F, 0.0F, -0.10471975511965977F);
		this.rightAntler03b = new ModelRenderer(this, 49, 34);
		this.rightAntler03b.mirror = true;
		this.rightAntler03b.setPos(-0.0F, -2.8F, 0.0F);
		this.rightAntler03b.addBox(-0.5F, -3.9F, -0.5F, 1, 4, 1, 0.0F);
		this.setRotateAngle(rightAntler03b, -0.10471975511965977F, 0.0F, 0.2792526803190927F);
		this.rightLeg02 = new ModelRenderer(this, 0, 41);
		this.rightLeg02.mirror = true;
		this.rightLeg02.setPos(0.0F, 5.2F, 0.2F);
		this.rightLeg02.addBox(-1.0F, 0.0F, -1.5F, 2, 7, 3, 0.0F);
		this.setRotateAngle(rightLeg02, -0.41887902047863906F, 0.0F, 0.0F);
		this.rightAntler01d = new ModelRenderer(this, 49, 34);
		this.rightAntler01d.mirror = true;
		this.rightAntler01d.setPos(0.0F, 0.0F, 0.0F);
		this.rightAntler01d.addBox(-0.8F, -3.5F, -0.2F, 1, 4, 1, 0.0F);
		this.hair01 = new ModelRenderer(this, 25, 0);
		this.hair01.mirror = true;
		this.hair01.setPos(0.0F, -6.9F, 3.2F);
		this.hair01.addBox(-4.0F, 0.0F, -0.1F, 8, 8, 1, 0.0F);
		this.setRotateAngle(hair01, 0.8726646259971648F, 0.0F, 0.0F);
		this.hair00 = new ModelRenderer(this, 44, 0);
		this.hair00.setPos(0.0F, -5.1F, 3.2F);
		this.hair00.addBox(-4.0F, 0.0F, -1.0F, 8, 11, 2, 0.0F);
		this.setRotateAngle(hair00, 0.6981317007977318F, 0.0F, 0.0F);
		this.leftArmLeaves00 = new ModelRenderer(this, 33, 39);
		this.leftArmLeaves00.setPos(0.5F, 5.3F, 0.0F);
		this.leftArmLeaves00.addBox(-2.0F, 0.0F, -2.5F, 4, 6, 5, 0.0F);
		this.leftClaw02 = new ModelRenderer(this, 0, 0);
		this.leftClaw02.setPos(0.6F, 11.2F, 1.5F);
		this.leftClaw02.addBox(-0.2F, -1.5F, -0.5F, 2, 5, 1, 0.0F);
		this.setRotateAngle(leftClaw02, 0.10471975511965977F, 0.0F, 0.22689280275926282F);
		this.backSkirt01 = new ModelRenderer(this, 62, 22);
		this.backSkirt01.setPos(0.0F, 3.1F, 0.2F);
		this.backSkirt01.addBox(-4.5F, 0.0F, 0.0F, 9, 11, 2, 0.0F);
		this.setRotateAngle(backSkirt01, -0.41887902047863906F, 0.0F, 0.0F);
		this.bipedRightArm = new ModelRenderer(this, 44, 14);
		this.bipedRightArm.mirror = true;
		this.bipedRightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 14, 4, 0.0F);
		this.setRotateAngle(bipedRightArm, 0.0F, 0.0F, 0.10000736613927509F);
		this.rightArmLeaves01 = new ModelRenderer(this, 13, 39);
		this.rightArmLeaves01.mirror = true;
		this.rightArmLeaves01.setPos(0.0F, 0.0F, 0.0F);
		this.rightArmLeaves01.addBox(-2.0F, -6.0F, -2.5F, 4, 6, 5, 0.0F);
		this.rightAntler07b = new ModelRenderer(this, 49, 34);
		this.rightAntler07b.mirror = true;
		this.rightAntler07b.setPos(-0.1F, 0.0F, -3.4F);
		this.rightAntler07b.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
		this.setRotateAngle(rightAntler07b, 0.0F, -0.5759586531581287F, 0.0F);
		this.leftArmLeaves01 = new ModelRenderer(this, 13, 39);
		this.leftArmLeaves01.setPos(0.0F, 0.0F, 0.0F);
		this.leftArmLeaves01.addBox(-2.0F, -6.0F, -2.5F, 4, 6, 5, 0.0F);
		this.leftAntler03a = new ModelRenderer(this, 49, 34);
		this.leftAntler03a.setPos(0.0F, -3.7F, 0.0F);
		this.leftAntler03a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(leftAntler03a, 0.0F, 0.0F, -0.3490658503988659F);
		this.leftAntler05a = new ModelRenderer(this, 49, 34);
		this.leftAntler05a.setPos(-1.2F, -6.7F, 0.0F);
		this.leftAntler05a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(leftAntler05a, -0.5235987755982988F, 0.0F, -1.3089969389957472F);
		this.leftAntler01a = new ModelRenderer(this, 49, 34);
		this.leftAntler01a.setPos(0.0F, -1.6F, 0.0F);
		this.leftAntler01a.addBox(-0.2F, -3.5F, -0.8F, 1, 4, 1, 0.0F);
		this.setRotateAngle(leftAntler01a, -0.20943951023931953F, 0.0F, 0.6108652381980153F);
		this.rightAntler04a = new ModelRenderer(this, 49, 34);
		this.rightAntler04a.mirror = true;
		this.rightAntler04a.setPos(0.9F, -5.9F, 0.0F);
		this.rightAntler04a.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
		this.setRotateAngle(rightAntler04a, 0.45378560551852565F, 0.0F, 0.8028514559173915F);
		this.backSkirt00 = new ModelRenderer(this, 62, 22);
		this.backSkirt00.setPos(0.0F, 6.1F, 0.0F);
		this.backSkirt00.addBox(-4.5F, 0.0F, 0.0F, 9, 4, 2, 0.0F);
		this.setRotateAngle(backSkirt00, 0.41887902047863906F, 0.0F, 0.0F);
		this.leftAntler06b = new ModelRenderer(this, 49, 34);
		this.leftAntler06b.setPos(0.1F, 0.0F, -3.4F);
		this.leftAntler06b.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
		this.setRotateAngle(leftAntler06b, 0.0F, 0.5759586531581287F, 0.0F);
		this.leftAntler01a.addChild(this.leftAntler02);
		this.rightAntler01a.addChild(this.rightAntler01b);
		this.rightAntler05a.addChild(this.rightAntler05b);
		this.bipedHead.addChild(this.leftAntler00);
		this.bipedRightArm.addChild(this.rightClaw00);
		this.bipedBody.addChild(this.boobs);
		this.leftAntler02.addChild(this.leftAntler04a);
		this.rightAntler00.addChild(this.rightAntler01a);
		this.leftAntler01a.addChild(this.leftAntler06a);
		this.bipedBody.addChild(this.bipedHead);
		this.rightAntler01a.addChild(this.rightAntler02);
		this.leftAntler01a.addChild(this.leftAntler01b);
		this.bipedRightArm.addChild(this.rightClaw01);
		this.lowerBody.addChild(this.rightSkirt);
		this.bipedRightArm.addChild(this.rightArmLeaves00);
		this.leftLeg01.addChild(this.leftLeg02);
		this.leftAntler01a.addChild(this.leftAntler01d);
		this.leftAntler05a.addChild(this.leftAntler05b);
		this.rightAntler01a.addChild(this.rightAntler01c);
		this.rightAntler02.addChild(this.rightAntler03a);
		this.bipedHead.addChild(this.hood);
		this.rightAntler01a.addChild(this.rightAntler06a);
		this.leftAntler03b.addChild(this.leftAntler03c);
		this.leftAntler04a.addChild(this.leftAntler04b);
		this.lowerBody.addChild(this.leftSkirt);
		this.leftAntler07a.addChild(this.leftAntler07b);
		this.lowerBody.addChild(this.bipedRightLeg);
		this.rightAntler04a.addChild(this.rightAntler04b);
		this.bipedLeftLeg.addChild(this.leftLeg01);
		this.bipedRightLeg.addChild(this.rightLeg01);
		this.bipedBody.addChild(this.lowerBody);
		this.leftAntler00.addChild(this.leftAntler07a);
		this.rightAntler02.addChild(this.rightAntler05a);
		this.leftAntler01a.addChild(this.leftAntler01c);
		this.bipedBody.addChild(this.bipedLeftArm);
		this.bipedHead.addChild(this.rightAntler00);
		this.rightLeg02.addChild(this.rightHoof);
		this.leftLeg02.addChild(this.leftHoof);
		this.bipedRightArm.addChild(this.rightClaw02);
		this.rightAntler03b.addChild(this.rightAntler03c);
		this.bipedLeftArm.addChild(this.leftClaw01);
		this.leftAntler03a.addChild(this.leftAntler03b);
		this.rightAntler06a.addChild(this.rightAntler06b);
		this.rightAntler00.addChild(this.rightAntler07a);
		this.bipedLeftArm.addChild(this.leftClaw00);
		this.lowerBody.addChild(this.bipedLeftLeg);
		this.rightAntler03a.addChild(this.rightAntler03b);
		this.rightLeg01.addChild(this.rightLeg02);
		this.rightAntler01a.addChild(this.rightAntler01d);
		this.bipedHead.addChild(this.hair01);
		this.bipedHead.addChild(this.hair00);
		this.bipedLeftArm.addChild(this.leftArmLeaves00);
		this.bipedLeftArm.addChild(this.leftClaw02);
		this.backSkirt00.addChild(this.backSkirt01);
		this.bipedBody.addChild(this.bipedRightArm);
		this.rightArmLeaves00.addChild(this.rightArmLeaves01);
		this.rightAntler07a.addChild(this.rightAntler07b);
		this.leftArmLeaves00.addChild(this.leftArmLeaves01);
		this.leftAntler02.addChild(this.leftAntler03a);
		this.leftAntler02.addChild(this.leftAntler05a);
		this.leftAntler00.addChild(this.leftAntler01a);
		this.rightAntler02.addChild(this.rightAntler04a);
		this.lowerBody.addChild(this.backSkirt00);
		this.leftAntler06a.addChild(this.leftAntler06b);
	head=bipedHead;body=bipedBody;leftArm=bipedLeftArm;rightArm=bipedRightArm;leftLeg=bipedLeftLeg;rightLeg=bipedRightLeg;}

	private static float triangleWave(float x, float y) {
		return (Math.abs(x % y - y * 0.5f) - y * 0.25f) / (y * 0.25f);
	}

	public void setupAnim(DrudenEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		leftArmPose=entityIn.getOffhandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM; rightArmPose=entityIn.getMainHandItem().isEmpty()?ArmPose.EMPTY:ArmPose.ITEM; if(entityIn.getMainArm()==HandSide.LEFT){ArmPose p=leftArmPose;leftArmPose=rightArmPose;rightArmPose=p;} boolean flag = entityIn.getFallFlyingTicks() > 4;
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
			f = (float) (entityIn.getDeltaMovement().x * entityIn.getDeltaMovement().x + entityIn.getDeltaMovement().y * entityIn.getDeltaMovement().y + entityIn.getDeltaMovement().z * entityIn.getDeltaMovement().z);
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
		float swingMod = 0.3F;
		this.bipedRightLeg.xRot = MathHelper.sin(limbSwing * 0.8665F + (float) Math.PI) * swingMod * limbSwingAmount - 0.26F;
		this.bipedLeftLeg.xRot = MathHelper.cos(limbSwing * 0.8665F) * swingMod * limbSwingAmount - 0.26F;

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
			HandSide enumhandside = this.getAttackArm(entityIn);
			ModelRenderer modelrenderer = this.getArmForSide(enumhandside);
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

		this.hat.copyFrom(this.bipedHead);
	}

	    @Override public void renderToBuffer(MatrixStack m,IVertexBuilder v,int light,int overlay,float r,float g,float b,float a){bipedBody.render(m,v,light,overlay,r,g,b,a);}
    @Override public void translateToHand(HandSide side,MatrixStack matrices){bipedBody.translateAndRotate(matrices);matrices.translate(-.025,-.35,0);getArmForSide(side).translateAndRotate(matrices);}
    protected ModelRenderer getArmForSide(HandSide side){return side==HandSide.LEFT?bipedLeftArm:bipedRightArm;}
    private void setRotateAngle(ModelRenderer part,float x,float y,float z){part.xRot=x;part.yRot=y;part.zRot=z;}
}