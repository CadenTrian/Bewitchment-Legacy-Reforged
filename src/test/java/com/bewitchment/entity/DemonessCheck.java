package com.bewitchment.entity;

import com.bewitchment.client.LegacyDemonessModel;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.util.registry.Bootstrap;

public final class DemonessCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean near(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        check(DemonEntity.class.isAssignableFrom(DemonessEntity.class),"Legacy shared behavior inheritance");
        check(IMerchant.class.isAssignableFrom(DemonessEntity.class),"Demoness supports merchant screen");
        LegacyDemonessModel model=new LegacyDemonessModel();
        check(model.hair01!=null && model.hair02!=null && model.lowerBody!=null && model.loincloth!=null,"Distinct original geometry");
        check(model.lWingMembrane!=null && model.rWingMembrane!=null && model.tailTip02!=null,"Wings and tail geometry");
        for(float yaw:new float[]{-180,-90,0,90,180})for(float pitch:new float[]{-90,0,90}){
            model.animate(0,0,0,0,0,yaw,pitch,0);
            check(near(model.bipedHead.yRot,yaw*(float)Math.PI/180) && near(model.bipedHead.xRot,pitch*(float)Math.PI/180),"Head pose follows look direction");
        }
        for(int identity:new int[]{0,12,-15})for(float swing:new float[]{0,1,2,3}){
            model.animate(identity,0,swing,1,10,0,0,.5F);
            check(near(model.loincloth.xRot,Math.abs(model.bipedLeftLeg.xRot/10F)-.256F),"Loincloth follows leg movement");
            check(near(model.lWing01.zRot,-model.rWing01.zRot),"Opposed wings");
            check(Float.isFinite(model.tail01.yRot) && Float.isFinite(model.tail01.zRot),"Finite tail motion");
        }
        model.animate(0,10,0,0,0,0,0,0);
        check(near(model.bipedLeftArm.xRot,-.5F) && near(model.bipedRightArm.xRot,-.5F),"Attack start");
        model.animate(0,5,0,0,0,0,0,0);
        check(near(model.bipedLeftArm.xRot,-3.5F),"Attack peak");
        model.animate(0,0,0,0,0,0,0,0);
        check(near(model.bipedLeftArm.xRot,0) && near(model.bipedRightArm.xRot,0),"Attack pose does not leak to next entity");
        check(near(model.bipedLeftLeg.xRot,-.26F) && near(model.bipedRightLeg.xRot,-.26F),"Resting legs");
        check(near(model.loincloth.xRot,-.23F),"Resting loincloth");
        check(near(model.tail01.yRot,0) && near(model.lWing01.zRot,.025F),"Resting tail and idle wing");
        System.out.println("PASS: "+checks+" demoness geometry and animation checks");
    }
}