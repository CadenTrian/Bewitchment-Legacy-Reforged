package com.bewitchment.entity;
import com.bewitchment.client.LegacyImpModel;
import net.minecraft.util.registry.Bootstrap;
public final class ImpCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int i=0;i<7;i++)check(ImpRules.skin(i)==i,"Seven skin variants preserved");
        for(int i:new int[]{-1,7,Integer.MIN_VALUE,Integer.MAX_VALUE})check(ImpRules.skin(i)==0,"Malformed skin repaired");
        int timer=10;for(int i=0;i<10;i++)timer=ImpRules.nextAttackTimer(timer);
        check(timer==0,"Attack expires after ten ticks");
        check(ImpRules.nextAttackTimer(Integer.MIN_VALUE)==0 && ImpRules.nextAttackTimer(Integer.MAX_VALUE)==9,"Timer extremes cannot overflow");
        check(ImpRules.lavaHeals(20,true) && ImpRules.lavaHeals(40,true),"Lava heals each second");
        check(!ImpRules.lavaHeals(19,true) && !ImpRules.lavaHeals(20,false) && !ImpRules.lavaHeals(-20,true),"No healing outside lava cadence");
        check(ImpRules.prey(true,false,1.99,false) && !ImpRules.prey(true,false,2,false),"Player radius uses strict squared distance two");
        check(ImpRules.prey(false,true,100,false),"Rival demons do not share player proximity gate");
        check(!ImpRules.prey(false,false,0,false),"Other nearby mobs are not prey");
        check(!ImpRules.prey(true,false,0,true) && !ImpRules.prey(false,true,0,true),"Clothing protects players and rival species");
        check(ImpRules.arm(10,0)==-.5F && ImpRules.arm(5,0)==-3.5F,"Triangle swing endpoints restored");
        boolean bounded=true;for(int i=1;i<=10;i++)for(int j=0;j<=10;j++){float arm=ImpRules.arm(i,j/10F);bounded&=Float.isFinite(arm) && arm>=-3.5F && arm<=-.5F;}
        check(bounded,"All interpolated attack angles bounded");
        LegacyImpModel model=new LegacyImpModel();
        check(model.bipedBody!=null && model.bipedHead!=null && model.bipedLeftArm!=null,"Original model constructs without hidden vanilla biped parts");
        model.pose(1,.8F,20,30,10,5,0,123,false);
        check(model.bipedRightArm.xRot==-3.5F && model.bipedLeftArm.xRot==-3.5F,"Both arms enter strike pose");
        check(model.leftWing00.zRot==-model.rightWing00.zRot,"Idle wings mirror");
        model.pose(0,0,0,0,0,0,0,0,false);
        check(model.bipedLeftArm.xRot==0 && model.bipedRightArm.xRot==0,"Next idle entity resets attack pose");
        check(model.bipedLeftLeg.xRot==-.26F && model.bipedRightLeg.xRot==-.26F && model.tail00.yRot==0,"Resting legs and tail reset");
        check(model.bipedHead.xRot==0 && model.bipedHead.yRot==0,"Head tracking resets");
        model.pose(5,Float.MAX_VALUE,0,0,0,0,0,0,true);
        check(Math.abs(model.bipedLeftArm.xRot)<=.3F && Math.abs(model.tail00.yRot)<=.65F,"Extreme walk amplitude bounded");
        check(model.bipedHead.xRot==-(float)Math.PI/4,"Legacy gliding pose");
        System.out.println("PASS: "+checks+" Imp variants, targeting, timers, lava and model checks.");
    }
}
