package com.bewitchment.entity;

import com.bewitchment.client.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;

public final class CambionCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int i=0;i<4;i++)check(CreatureRules.skin(i)==i,"Four valid Cambion appearances");
        for(int i:new int[]{-1,4,Integer.MIN_VALUE,Integer.MAX_VALUE})check(CreatureRules.skin(i)==0,"Invalid appearance falls back safely");
        int fire=0,poison=0,none=0;
        for(int a=0;a<100;a++)for(int b=0;b<100;b++){
            int effect=CambionRules.attackEffect(a,b);if(effect==1)fire++;else if(effect==2)poison++;else none++;
        }
        check(fire==500,"Exactly five percent fire outcomes");
        check(poison==3325,"Independent poison roll follows failed fire roll");
        check(none==6175,"Remaining attacks have no additional effect");
        check(CambionRules.attackEffect(4,0)==1,"Fire wins when both rolls succeed");
        check(CambionRules.attackEffect(5,34)==2,"Poison boundary succeeds");
        check(CambionRules.attackEffect(5,35)==0,"Poison upper boundary excluded");
        int goldSword=0,goldAxe=0,ironAxe=0,ironSword=0,empty=0;
        for(int b=0;b<8;b++)for(int d=0;d<8;d++){
            Item item=CambionRules.weapon(b,d);
            if(item==Items.GOLDEN_SWORD)goldSword++;else if(item==Items.GOLDEN_AXE)goldAxe++;
            else if(item==Items.IRON_AXE)ironAxe++;else if(item==Items.IRON_SWORD)ironSword++;else if(item==Items.AIR)empty++;
        }
        check(goldSword==16,"Gold sword has first priority");
        check(goldAxe==12,"Gold axe second branch");
        check(ironAxe==18,"Iron axe third branch");
        check(ironSword==9,"Iron sword final branch");
        check(empty==9,"Nine unarmed equipment outcomes");
        check(CambionRules.chest(0)==Items.GOLDEN_CHESTPLATE,"Gold chest roll");
        check(CambionRules.chest(3)==Items.CHAINMAIL_CHESTPLATE,"Chainmail chest roll");
        check(CambionRules.chest(5)==Items.LEATHER_CHESTPLATE,"Leather chest roll");
        check(CambionRules.chest(1)==Items.AIR && CambionRules.chest(2)==Items.AIR && CambionRules.chest(4)==Items.AIR,"Half of chest rolls empty");
        check(CambionRules.canPickUp(Items.IRON_SWORD),"Sword pickup allowed");
        check(CambionRules.canPickUp(Items.DIAMOND_HELMET),"Armor pickup allowed");
        check(CambionRules.canPickUp(Items.SHIELD),"Shield pickup allowed");
        check(!CambionRules.canPickUp(Items.IRON_AXE),"Spawn axes are not loose-item pickup candidates");
        check(!CambionRules.canPickUp(Items.APPLE),"Ordinary items excluded");
        check(CambionRules.futurePrey(new ResourceLocation("bewitchment:ghost")),"Future ghost target");
        check(CambionRules.futurePrey(new ResourceLocation("bewitchment:werewolf")),"Future werewolf target");
        check(!CambionRules.futurePrey(new ResourceLocation("other:ghost")) && !CambionRules.futurePrey(null),"Unrelated or absent IDs excluded");
        check(new CambionEntity.GroupData(3).variant==3 && new CambionEntity.GroupData(9).variant==0,"Group appearance is validated");
        LegacyCambionModel regular=new LegacyCambionModel();LegacyCambionSlimModel slim=new LegacyCambionSlimModel();
        check(regular.head==regular.bipedHead && regular.body==regular.bipedBody,"Regular armor and animation use actual geometry");
        check(regular.leftArm==regular.bipedLeftArm && regular.rightArm==regular.bipedRightArm,"Regular held-item hooks use actual arms");
        check(slim.head==slim.bipedHead && slim.body==slim.bipedBody,"Slim armor and animation use actual geometry");
        check(slim.leftArm==slim.bipedLeftArm && slim.rightArm==slim.bipedRightArm,"Slim held-item hooks use actual arms");
        check(regular.body.y==-2 && slim.body.y==-2,"Both original torso origins retained");
        check(regular.head!=slim.head && regular.leftArm!=slim.leftArm,"Variant models do not share mutable parts");
        check(regular.texWidth==64 && regular.texHeight==64 && slim.texWidth==64 && slim.texHeight==64,"Original texture dimensions");
        System.out.println("PASS: "+checks+" Cambion checks (all 10000 combat and 64 weapon outcomes).");
    }
}
