package com.bewitchment.entity;
import com.bewitchment.client.LegacyWerewolfModel;
import net.minecraft.util.registry.Bootstrap;

public final class WerewolfCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean close(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        String[] names={"brown","arctic","black","red","timber","grey"};
        for(int i=0;i<6;i++)check(WerewolfRules.skin(i)==i && WerewolfRules.texture(i).equals(names[i]),"Original skin-to-texture mapping");
        for(int i:new int[]{-1,6,Integer.MIN_VALUE,Integer.MAX_VALUE})check(WerewolfRules.skin(i)==0,"Invalid skin repaired");
        check(WerewolfRules.variant(true,true,true,true)==1 && WerewolfRules.variant(true,true,true,false)==5,"Snowy takes priority");
        check(WerewolfRules.variant(false,true,true,true)==3 && WerewolfRules.variant(false,true,true,false)==2,"Coniferous before swamp");
        check(WerewolfRules.variant(false,false,true,true)==3 && WerewolfRules.variant(false,false,true,false)==0,"Swamp pair");
        check(WerewolfRules.variant(false,false,false,true)==4 && WerewolfRules.variant(false,false,false,false)==0,"Other biome pair");
        for(int phase=0;phase<8;phase++)check(WerewolfRules.canSpawn(false,phase)==(phase==0) && !WerewolfRules.canSpawn(true,phase),"All moon/day combinations");
        check(!WerewolfRules.canSpawn(false,-1) && !WerewolfRules.canSpawn(false,8),"Invalid phases excluded");
        check(WerewolfRules.vanishes(true,false),"Unnamed daytime removal");
        check(!WerewolfRules.vanishes(true,true) && !WerewolfRules.vanishes(false,false),"Names and night preserve entity");
        int pulses=0;for(int i=0;i<400;i++)if(WerewolfRules.heals(i,true))pulses++;
        check(pulses==12,"Six-tick healing bursts");
        check(WerewolfRules.heals(205,true) && !WerewolfRules.heals(206,true),"Healing boundary");
        check(!WerewolfRules.heals(200,false) && !WerewolfRules.heals(-1,true),"No full-health or negative-tick heal");
        check(WerewolfRules.damage(2.5)==2 && WerewolfRules.damage(3.9)==3,"Legacy integer attack conversion");
        check(new WerewolfEntity.GroupData(5).variant==5 && new WerewolfEntity.GroupData(6).variant==0,"Validated group variant");
        LegacyWerewolfModel model=new LegacyWerewolfModel();
        model.pose(0,0,0,0,0,0,true);
        check(model.head.xRot==2 && model.tail01.xRot==1,"Original resting head and tail rotations");
        check(model.bipedLeftLeg.xRot==-.5F && model.bipedRightLeg.xRot==-.5F,"Resting digitigrade leg pose");
        check(model.bipedLeftLeg.yRot==-.1F && model.bipedRightLeg.yRot==.1F,"Leg yaw mirrored");
        model.pose(0,0,0,90,30,.5F,true);
        check(close(model.head.yRot,(float)Math.PI/2) && close(model.head.xRot,2+(float)Math.PI/6),"Head tracking");
        check(close(model.rArm01.xRot,-1.75F) && model.lArm01.xRot==0,"Main-hand strike");
        model.pose(0,0,0,0,0,.5F,false);
        check(close(model.lArm01.xRot,-1.5F) && model.rArm01.xRot==0,"Offhand strike resets opposite arm");
        model.pose(0,0,0,0,0,0,true);
        check(model.chest.yRot==0 && model.lArm01.xRot==0 && model.rArm01.xRot==0,"Next idle entity clears attack pose");
        check(model.tail01.yRot==0 && model.head.yRot==0,"Next idle entity clears head and tail motion");
        model.pose(10,100,20,0,0,5,true);float angle=model.rArm01.xRot;
        model.pose(10,1,20,0,0,1,true);
        check(close(angle,model.rArm01.xRot),"Animation input bounded");
        System.out.println("PASS: "+checks+" Werewolf variant, moon, lifecycle, attack and model checks.");
    }
}
