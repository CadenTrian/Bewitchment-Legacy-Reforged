package com.bewitchment.entity;

import com.bewitchment.client.LegacyShadowPersonModel;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.registry.Bootstrap;

public final class ShadowPersonCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean close(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int i=0;i<4;i++)check(ShadowPersonRules.skin(i)==i && ShadowPersonRules.texture(i)==i%2,"Four skins map to two appearances");
        for(int i:new int[]{-1,4,Integer.MIN_VALUE,Integer.MAX_VALUE})check(ShadowPersonRules.skin(i)==0,"Invalid skin repaired");
        int sum=0;for(int i=0;i<=30;i++)sum+=ShadowPersonRules.lifetime(i);
        check(sum==27900 && ShadowPersonRules.lifetime(0)==600 && ShadowPersonRules.lifetime(30)==1200,"Thirty-one equally spaced lifetimes from thirty to sixty seconds");
        for(int i:new int[]{-1,31}){boolean rejected=false;try{ShadowPersonRules.lifetime(i);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid lifetime roll rejected");}
        check(ShadowPersonRules.expired(true,0) && ShadowPersonRules.expired(true,-1) && !ShadowPersonRules.expired(true,1),"Expiry boundary");
        check(!ShadowPersonRules.expired(false,0),"Explicit nonspectral creature has no timer expiry");
        CompoundNBT tag=new CompoundNBT();
        check(ShadowPersonRules.savedSpectral(tag) && ShadowPersonRules.savedLifetime(tag,800)==800,"Missing summon NBT keeps spectral constructor defaults");
        tag.putBoolean("spectral",false);tag.putInt("lifeTimeTick",37);
        check(!ShadowPersonRules.savedSpectral(tag) && ShadowPersonRules.savedLifetime(tag,800)==37,"Explicit saved state survives reload");
        tag.putInt("lifeTimeTick",-9);check(ShadowPersonRules.savedLifetime(tag,800)==0,"Negative saved lifetime expires");
        tag.putString("lifeTimeTick","invalid");tag.putString("spectral","invalid");
        check(ShadowPersonRules.savedLifetime(tag,800)==800 && ShadowPersonRules.savedSpectral(tag),"Wrong NBT types cannot disable defaults");
        check(FeuerwurmRules.summoner("not-a-uuid")==null && FeuerwurmRules.summoner("")==null,"Malformed owner identifiers safe");
        for(int bits=0;bits<32;bits++) {
            boolean actual=ShadowPersonRules.prey((bits&1)!=0,(bits&2)!=0,(bits&4)!=0,(bits&8)!=0,(bits&16)!=0);
            if(actual!=(bits!=0))throw new AssertionError("Prey combination "+bits);
        }
        check(true,"All humanoid prey combinations; unrelated creatures excluded");
        check(ShadowPersonRules.eyes(false,false) && !ShadowPersonRules.eyes(true,false) && !ShadowPersonRules.eyes(false,true),"Eyes suppressed for invisible or baby creatures");
        LegacyShadowPersonModel model=new LegacyShadowPersonModel();
        check(model.head==model.bipedHead && model.body==model.bipedBody && model.leftArm==model.bipedLeftArm && model.rightArm==model.bipedRightArm,"Vanilla hooks alias real legacy body parts");
        check(model.leftLeg==model.bipedLeftLeg && model.rightLeg==model.bipedRightLeg,"Leg aliases avoid hidden superclass parts");
        model.pose(0,0,0,90,30,false,0,HandSide.RIGHT);
        check(close(model.head.yRot,(float)Math.PI/2) && close(model.head.xRot,(float)Math.PI/6),"Head tracking");
        check(close(model.body.y,-1.8F) && model.leftLeg.y==12,"Original body and leg origins");
        model.pose(0,0,0,0,80,true,1,HandSide.RIGHT);
        check(close(model.head.xRot,-(float)Math.PI/4),"Flying head pose");
        model.riding=true;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT);
        check(close(model.rightLeg.xRot,-1.4137167F) && model.leftLeg.yRot<0 && model.rightLeg.yRot>0,"Riding legs");
        model.riding=false;model.crouching=true;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT);
        check(model.body.xRot==.5F && model.leftLeg.y==9 && model.head.y==1,"Crouched pose");
        model.crouching=false;model.attackTime=.5F;model.pose(0,0,0,0,0,false,0,HandSide.LEFT);
        check(model.leftArm.xRot<model.rightArm.xRot,"Left handed attack animates real left limb");
        model.attackTime=0;model.pose(0,0,0,0,0,false,0,HandSide.RIGHT);
        check(model.body.xRot==0 && model.body.yRot==0 && model.head.y==0 && model.leftLeg.y==12,"Next idle creature resets shared pose");
        check(model.rightArm.xRot==0 && model.leftArm.xRot==0 && model.head.xRot==0,"Attack and head pose do not leak");
        System.out.println("PASS: "+checks+" Shadow Person skin, lifetime, NBT, prey, eye and original animation checks.");
    }
}
