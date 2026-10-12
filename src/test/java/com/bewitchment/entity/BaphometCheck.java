package com.bewitchment.entity;

import com.bewitchment.client.LegacyBaphometModel;
import com.bewitchment.machine.CurseData;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class BaphometCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        AttributeModifierMap a=BaphometRules.attributes(AttributeModifierMap.builder()).build();
        check(a.getBaseValue(Attributes.MAX_HEALTH)==666 && a.getBaseValue(Attributes.ATTACK_DAMAGE)==7,"Original health and attack");
        check(a.getBaseValue(Attributes.ARMOR)==13.616 && a.getBaseValue(Attributes.ARMOR_TOUGHNESS)==6.66,"Original defenses");
        check(a.getBaseValue(Attributes.MOVEMENT_SPEED)==.7 && a.getBaseValue(Attributes.KNOCKBACK_RESISTANCE)==.9,"Original movement");
        for(int period:new int[]{80,600}){int count=0;for(int tick=0;tick<period*2;tick++)if(BaphometRules.pulse(tick,period))count++;check(count==12,"Six active ticks per cycle");}
        check(!BaphometRules.pulse(-1,80)&&!BaphometRules.pulse(0,0),"Invalid pulse inputs");
        for(int tick:new int[]{0,5,6,79,80,85,86})for(double distance:new double[]{0,4,4.01,100,Double.NaN,Double.POSITIVE_INFINITY})
            check(BaphometRules.fireball(tick,distance)==(tick%80<=5&&distance>4&&Double.isFinite(distance)),"Fireball range and cadence");
        for(int cooldown:new int[]{-1,0,1,40})for(double distance:new double[]{0,100,100.01,400,Double.NaN,Double.POSITIVE_INFINITY})
            check(BaphometRules.pull(distance,cooldown)==(cooldown<=0&&distance>100&&Double.isFinite(distance)),"Strict ten-block pull threshold");
        for(float fraction:new float[]{1,.5F,.499F,.3F,.299F,.1F})for(int timer=0;timer<=180;timer++){
            BaphometRules.Summons next=BaphometRules.summons(fraction,timer);
            int expectedKind=fraction<.5F&&timer==0?1:fraction<.3F&&timer==1?2:0;
            check(next.kind==expectedKind,"Sequential summon threshold and shared timer");
            check(next.timer==(expectedKind==0?Math.max(0,timer-1):180),"One timer decrement or reset per active tick");
        }
        check(BaphometRules.timer(-1,180)==0&&BaphometRules.timer(Integer.MAX_VALUE,180)==180,"Saved summon timer bounds");
        check(BaphometRules.timer(Integer.MAX_VALUE,40)==40,"Saved pull timer bounds");
        CurseData data=new CurseData();
        check("notpledged".equals(BaphometRules.contractFailure(false,100,data)),"Pledge required");
        check("lowlevel".equals(BaphometRules.contractFailure(true,5,data)),"Six levels required");
        check(BaphometRules.contractFailure(true,6,data)==null,"Exactly six accepted");
        data.add(CurseData.Kind.RETURN_TO_SENDER,7);data.add(CurseData.Kind.MAHAPADMA,1);
        check(BaphometRules.contractFailure(true,6,data)==null,"Non-contract blessing and negative contract do not block");
        Set<CurseData.Kind> seen=EnumSet.noneOf(CurseData.Kind.class);
        for(int seed=0;seed<128;seed++){
            CurseData fresh=new CurseData();CurseData.Kind kind=BaphometRules.grant(fresh,new Random(seed*0x9E3779B97F4A7C15L));seen.add(kind);
            check(BaphometRules.CONTRACTS.contains(kind)&&fresh.remaining(kind)==7*24000,"Only original positive contracts for seven days");
            check("alreadyhascontract".equals(BaphometRules.contractFailure(true,100,fresh)),"Active positive contract blocks purchase");
            check(BaphometRules.grant(fresh,new Random(seed))==null&&fresh.active().size()==1,"No second grant or extension");
            CurseData loaded=new CurseData();loaded.load(fresh.save());check(loaded.remaining(kind)==7*24000,"Contract persistence");
        }
        check(seen.size()==5&&!seen.contains(CurseData.Kind.RETURN_TO_SENDER),"All five genuine positive contracts reachable");
        LegacyBaphometModel model=new LegacyBaphometModel();
        check(model.goatHead!=null&&model.leftWing00!=null&&model.rightWing00!=null,"Original head and wing geometry");
        check(model.body!=null&&model.rightArm!=null&&model.leftLeg!=null,"Rendering and held-item roots");
        check(GreaterDemonEntity.class.isAssignableFrom(BaphometEntity.class),"Pledge-aware greater-demon foundation");
        System.out.println("PASS: "+checks+" Baphomet attributes, combat cadence, summon ordering, contracts, persistence and model checks");
    }
}
