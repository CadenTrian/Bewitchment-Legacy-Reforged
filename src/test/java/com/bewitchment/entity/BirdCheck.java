package com.bewitchment.entity;

import com.bewitchment.client.LegacyRavenModel;
import com.bewitchment.client.LegacyOwlModel;
import net.minecraft.util.registry.Bootstrap;
import java.util.Random;

public final class BirdCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        check(BirdRules.seeksShade(true,true,false,false),"Unburned idle exposed Owl may seek shade");
        check(!BirdRules.seeksShade(false,true,false,false),"No shade search at night");
        check(!BirdRules.seeksShade(true,false,false,false),"Already sheltered Owl stays put");
        check(!BirdRules.seeksShade(true,true,true,false),"Sitting prevents shade movement");
        check(!BirdRules.seeksShade(true,true,false,true),"Combat takes precedence over shade");
        check(BirdRules.shearTimer(Integer.MIN_VALUE)==0,"Corrupt negative collection timer recovers");
        check(BirdRules.shearTimer(Integer.MAX_VALUE)==12000,"Corrupt large collection timer bounded");
        check(BirdRules.shearTimer(1)==1 && BirdRules.shearTimer(6000)==6000,"Loaded cooldown retained");
        check(BirdRules.shedTimer(0)==1 && BirdRules.shedTimer(-50)==1,"Expired shed waits for one server tick");
        check(BirdRules.shedTimer(Integer.MAX_VALUE)==11999,"Excessive shed timer repaired");
        check(BirdRules.shedTimer(6000)==6000,"Valid shed timer retained");
        Random random=new Random(3905);int[] feathers=new int[4];int min=12000,max=0;
        for(int i=0;i<50000;i++){
            int delay=BirdRules.shedDelay(random);min=Math.min(min,delay);max=Math.max(max,delay);
            int count=BirdRules.featherCount(random);if(count<1 || count>3)throw new AssertionError("Invalid feather count");feathers[count]++;
        }
        check(min==6000 && max==11999,"Legacy shed interval endpoints observed");
        check(feathers[1]>15000 && feathers[2]>15000 && feathers[3]>15000,"All three collection amounts sampled");
        check(BirdRules.peck(0,false)==0 && BirdRules.peck(-1,false)==0,"Zero/negative peck avoids division problems");
        check(BirdRules.peck(61,false)==0 && BirdRules.peck(40,true)==0,"Moving or sitting does not peck");
        boolean finite=true;for(int i=1;i<=60;i++)finite&=Float.isFinite(BirdRules.peck(i,false));
        check(finite,"Every active peck pose finite");
        check(BirdRules.flightPitch(0,0,0)==0,"Stationary flight remains level");
        check(BirdRules.flightPitch(0,1,0)<0 && BirdRules.flightPitch(0,-1,0)>0,"Vertical ascent/descent pitch valid");
        check(BirdRules.flightPitch(Double.NaN,0,0)==0 && BirdRules.flightPitch(0,Double.POSITIVE_INFINITY,0)==0,"Malformed motion does not corrupt model");
        LegacyRavenModel raven=new LegacyRavenModel();
        float originalY=raven.body.y;
        raven.pose(1,.7F,5,true,false,80,.1F);
        check(!raven.leftfoot.visible && !raven.rightLeg00.visible,"Flying Raven hides feet and legs");
        check(raven.rightWing00.zRot==-raven.leftWing00.zRot && raven.rightWing01.xRot==0,"Flight wing symmetry and reset");
        raven.pose(0,0,5,false,true,30,0);
        check(raven.leftfoot.visible && raven.body.y>originalY && raven.neck.xRot==0,"Sitting Raven lowers body without pecking");
        raven.pose(0,0,0,false,false,80,0);
        check(raven.body.y==originalY && raven.body.xRot==-.16F,"Standing Raven resets body pivot and pitch");
        check(raven.rightWing00.yRot==1.05F && raven.leftWing00.yRot==-1.05F && raven.rightWing00.zRot==0,"Grounded Raven folds wings after flight");
        check(raven.rightLeg00.xRot==.31F && raven.leftLeg00.xRot==.31F,"Grounded Raven restores legs");
        LegacyOwlModel owl=new LegacyOwlModel();
        owl.pose(7,true,false,.2F);
        check(!owl.leftfoot.visible && !owl.rightfoot.visible,"Flying Owl hides feet");
        check(owl.rightWing00.yRot==-owl.leftWing00.yRot && owl.rightWing01.zRot==0,"Flying Owl resets folded wing rotation");
        owl.pose(0,false,true,0);
        check(owl.leftfoot.visible && owl.head.xRot>0 && owl.body.xRot==0,"Sitting Owl restores feet and posture");
        owl.pose(0,false,false,0);
        check(owl.head.xRot==-.07F && owl.rightWing00.zRot==-1.22F && owl.leftWing00.zRot==1.22F,"Standing Owl pose restored");
        check(owl.rightWing01.zRot==-.44F && owl.leftWing01.zRot==.44F,"Secondary wing folds restored");
        System.out.println("PASS: "+checks+" bird timer, shedding, collection and animation checks plus 50000 seeded samples.");
    }
}
