package com.bewitchment.entity;
import com.bewitchment.client.LegacyBlackDogModel;
import net.minecraft.util.registry.Bootstrap;
public final class BlackDogCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int skin=0;skin<5;skin++)check(BlackDogRules.skin(skin)==skin,"Five body/eye variants preserved");
        for(int value:new int[]{-1,5,255,Integer.MIN_VALUE,Integer.MAX_VALUE})check(BlackDogRules.skin(value)==0,"Invalid skin uses first body and eyes");
        check(BlackDogRules.vanishes(true,false,false),"Unnamed dog vanishes on clear day");
        check(!BlackDogRules.vanishes(true,true,false),"Rain protects unnamed dog during daytime");
        check(!BlackDogRules.vanishes(false,false,false),"Dry night preserves dog");
        check(!BlackDogRules.vanishes(false,true,false),"Rainy night preserves dog");
        for(boolean day:new boolean[]{false,true})for(boolean rain:new boolean[]{false,true})check(!BlackDogRules.vanishes(day,rain,true),"Named dog exempt in every weather/time state");
        check(BlackDogRules.jaw(5)==.45F && BlackDogRules.jaw(1)==.45F,"Legacy bite opening preserved");
        check(BlackDogRules.jaw(0)==0 && BlackDogRules.jaw(-1)==0,"Inactive jaw closed");
        LegacyBlackDogModel model=new LegacyBlackDogModel();
        check(model.body!=null && model.leftHindleg!=null && model.leftforeleg!=null && model.rightHindleg!=null && model.rightforeleg!=null,"Five independent roots construct");
        model.pose(1,.8F,30,20,5,false);
        check(model.lowerJaw.xRot==.45F,"Attack opens model jaw");
        check(Math.abs(model.leftforeleg.xRot+model.leftHindleg.xRot)<.00001F,"Opposite left legs animate");
        check(Math.abs(model.rightforeleg.xRot+model.rightHindleg.xRot)<.00001F,"Opposite right legs animate");
        model.pose(0,0,0,0,0,false);
        check(model.lowerJaw.xRot==0 && model.head.xRot==0 && model.head.yRot==0,"Next resting dog resets bite and head");
        check(model.tail.yRot==0 && model.leftforeleg.xRot==0 && model.rightHindleg.xRot==0,"Stationary tail and legs reset");
        model.pose(5,Float.MAX_VALUE,0,0,0,true);
        check(Math.abs(model.leftforeleg.xRot)<=.9F && Math.abs(model.tail.yRot)<=.65F,"Movement amplitude bounded");
        check(model.head.xRot==-(float)Math.PI/4,"Gliding pose retained");
        model.pose(0,-1,0,0,0,false);
        check(model.head.xRot==0 && model.rightHindleg.xRot==0,"Gliding and negative amplitude reset");
        System.out.println("PASS: "+checks+" Black Dog variant, weather, naming and model checks.");
    }
}
