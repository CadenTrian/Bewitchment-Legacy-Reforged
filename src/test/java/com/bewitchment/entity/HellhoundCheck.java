package com.bewitchment.entity;
import com.bewitchment.client.LegacyHellhoundModel;
import net.minecraft.util.registry.Bootstrap;
public final class HellhoundCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        int timer=5;
        for(int i=0;i<5;i++){check(HellhoundRules.jaw(timer)==.55F,"Five active jaw ticks");timer=HellhoundRules.nextAttackTimer(timer);}
        check(timer==0 && HellhoundRules.jaw(timer)==0,"Jaw closes after fifth tick");
        check(HellhoundRules.nextAttackTimer(0)==0 && HellhoundRules.nextAttackTimer(Integer.MIN_VALUE)==0,"Expired/malformed timer stays zero");
        check(HellhoundRules.nextAttackTimer(Integer.MAX_VALUE)==4,"Oversized timer bounded");
        check(HellhoundRules.prey(true,true,true,true,false),"Explicit species branch overrides generic exclusions");
        check(!HellhoundRules.prey(true,false,false,false,true),"Besmirched set protects explicit prey");
        check(!HellhoundRules.prey(false,false,false,false,true),"Besmirched set protects generic prey");
        check(HellhoundRules.prey(false,false,false,false,false),"Ordinary living prey eligible");
        check(!HellhoundRules.prey(false,true,false,false,false),"Fire immune generic prey excluded");
        check(!HellhoundRules.prey(false,false,true,false,false),"Demon generic prey excluded");
        check(!HellhoundRules.prey(false,false,false,true,false),"Undead generic prey excluded");
        LegacyHellhoundModel model=new LegacyHellhoundModel();
        check(model.body!=null && model.leftHindleg!=null && model.rightHindleg!=null && model.leftforeleg!=null && model.rightforeleg!=null,"All five independent model roots construct");
        model.pose(1,.8F,30,20,5,false);
        check(model.lowerJaw.xRot==.55F,"Biting pose opens jaw");
        check(Math.abs(model.leftforeleg.xRot+model.leftHindleg.xRot)<.00001F,"Fore/hind sine pair opposed");
        check(Math.abs(model.rightforeleg.xRot+model.rightHindleg.xRot)<.00001F,"Fore/hind cosine pair opposed");
        model.pose(0,0,0,0,0,false);
        check(model.lowerJaw.xRot==0 && model.head.xRot==0 && model.head.yRot==0,"Next resting entity resets head and bite");
        check(model.tail00.yRot==0 && model.leftHindleg.xRot==0 && model.rightforeleg.xRot==0,"Stationary tail/legs reset");
        model.pose(5,Float.MAX_VALUE,0,0,0,true);
        check(Math.abs(model.leftforeleg.xRot)<=.9F && Math.abs(model.tail00.yRot)<=.65F,"Extreme movement amplitude bounded");
        check(model.head.xRot==-(float)Math.PI/4,"Legacy gliding head pose");
        model.pose(0,-1,0,0,0,false);
        check(model.head.xRot==0 && model.rightHindleg.xRot==0,"Gliding and negative motion reset");
        System.out.println("PASS: "+checks+" Hellhound attack timer, prey and five-root model checks.");
    }
}
