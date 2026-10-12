package com.bewitchment.entity;

import com.bewitchment.client.LegacyBafometyrModel;
import net.minecraft.util.HandSide;
import net.minecraft.util.registry.Bootstrap;

public final class BafometyrCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean close(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        int shots=0;for(int tick=0;tick<800;tick++)if(BafometyrRules.fires(tick,100))shots++;
        check(shots==60,"Six shots per eighty ticks over ten cycles");
        check(BafometyrRules.fires(0,5) && BafometyrRules.fires(5,5) && !BafometyrRules.fires(6,5),"Opening burst boundary");
        check(!BafometyrRules.fires(79,5) && BafometyrRules.fires(80,5) && BafometyrRules.fires(85,5) && !BafometyrRules.fires(86,5),"Repeated burst boundary");
        check(!BafometyrRules.fires(0,4) && !BafometyrRules.fires(0,0) && BafometyrRules.fires(0,4.001),"Strictly beyond two blocks");
        check(!BafometyrRules.fires(-1,9) && !BafometyrRules.fires(0,Double.NaN) && !BafometyrRules.fires(0,Double.POSITIVE_INFINITY),"Invalid firing inputs excluded");
        check(BafometyrRules.spread(16)==1 && BafometyrRules.spread(256)==2,"Legacy fourth-root spread");
        check(BafometyrRules.spread(0)==0 && BafometyrRules.spread(-1)==0,"Safe zero spread");
        for(int frame=0;frame<8;frame++)check(BafometyrRules.flame(frame)==frame && BafometyrRules.flame(frame+8)==frame,"Eight-frame texture cycle");
        check(BafometyrRules.flame(-1)==7,"Texture index always bounded");
        check(BafometyrRules.nextAttackTimer(40)==39 && BafometyrRules.nextAttackTimer(1)==0 && BafometyrRules.nextAttackTimer(0)==0 && BafometyrRules.nextAttackTimer(Integer.MIN_VALUE)==0,"Attack timer decays without underflow");
        check(BafometyrRules.prey(true,false,true,true,true,false),"Player branch ignores creature immunities");
        check(BafometyrRules.prey(false,true,true,true,false,false),"Hellhound/Cleaver rival branch accepts demons");
        check(BafometyrRules.prey(false,false,false,false,false,false),"Ordinary living prey accepted");
        check(!BafometyrRules.prey(false,false,true,false,false,false) && !BafometyrRules.prey(false,false,false,true,false,false) && !BafometyrRules.prey(false,false,false,false,true,false),"Generic fire-immune, demon and undead exclusions");
        check(!BafometyrRules.prey(true,true,false,false,false,true),"Besmirched protects all proactive branches");
        check(FeuerwurmRules.summoner("malformed")==null,"Malformed saved owner safe");
        LegacyBafometyrModel model=new LegacyBafometyrModel();
        model.pose(0,0,0,90,30,false,0,HandSide.RIGHT,0);
        check(close(model.head.yRot,(float)Math.PI/2) && close(model.head.xRot,(float)Math.PI/6),"Head tracking");
        check(close(model.body.y,-7.1F) && close(model.leftLeg01.xRot,.69F) && close(model.rightLeg01.xRot,.69F),"Original torso and digitigrade pose");
        model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,40);
        check(close(model.rightArm.zRot,2.25F) && model.rightArm.yRot==.75F,"Fireball arm pose including idle sway");
        model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,0);
        check(close(model.rightArm.zRot,.1F) && model.rightArm.yRot==0,"Fireball pose clears for next creature");
        model.riding=true;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,0);
        check(close(model.rightLeg01.xRot,-1.4137167F) && model.rightLeg01.yRot>0 && model.leftLeg01.yRot<0,"Riding joint pose");
        model.riding=false;model.attackTime=.5F;model.pose(0,0,0,0,0,false,0,HandSide.LEFT,0);
        check(model.leftArm.xRot<model.rightArm.xRot,"Attack uses actual left arm");
        model.attackTime=0;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT,0);
        check(model.body.yRot==0 && model.leftArm.x==5 && model.rightArm.x==-5 && model.leftArm.z==0 && model.rightArm.z==0,"Attack body and pivots reset");
        check(model.leftLeg01.yRot==0 && model.rightLeg01.yRot==0 && close(model.leftLeg01.zRot,.104719755F) && close(model.rightLeg01.zRot,-.104719755F),"Riding pose cannot leak into next entity");
        model.pose(0,0,0,0,90,true,1,HandSide.RIGHT,0);
        check(close(model.head.xRot,-(float)Math.PI/4),"Flying head pose");
        System.out.println("PASS: "+checks+" Bafometyr fireball cadence, spread, flame cycle, targeting and original animation checks.");
    }
}
