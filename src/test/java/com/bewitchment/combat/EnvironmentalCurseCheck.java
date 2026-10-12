package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import com.bewitchment.potion.MagicWeaknessEffect;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class EnvironmentalCurseCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        Bootstrap.bootStrap();CurseData data=new CurseData();
        for(Kind kind:new Kind[]{Kind.DARKNESS,Kind.MISFORTUNE,Kind.CONFLAGRATION,Kind.OVERHEATING,Kind.HYDROPHOBIA,Kind.SOLAR_HATRED}){
            check(Kind.fromId(kind.id)==kind,"Canonical identity "+kind);
            check(data.add(kind,1) && data.remaining(kind)==24000,"Independent duration "+kind);
            check(EnvironmentalCurseRules.proc(kind,0) && EnvironmentalCurseRules.proc(kind,Math.nextDown(kind.chance)),"Proc below boundary "+kind);
            check(!EnvironmentalCurseRules.proc(kind,kind.chance) && !EnvironmentalCurseRules.proc(kind,-1) && !EnvironmentalCurseRules.proc(kind,Double.NaN),"Proc boundary rejection "+kind);
            check(kind.lesser()==(kind!=Kind.SOLAR_HATRED) && !kind.positive(),"Legacy classification "+kind);
        }
        CurseData saved=new CurseData();saved.load(data.save());check(saved.active().size()==6,"All six persist together");
        saved.second();check(saved.remaining(Kind.SOLAR_HATRED)==23980 && data.remaining(Kind.SOLAR_HATRED)==24000,"Greater curse has independent persisted timer");
        AtomicInteger rolls=new AtomicInteger();check(saved.purify(()->{rolls.incrementAndGet();return 0;})==5 && rolls.get()==5,"Purity rolls only for five lesser curses");
        check(saved.active().size()==1 && saved.remaining(Kind.SOLAR_HATRED)==23980,"Purity preserves greater curse duration");
        check(saved.purify(()->{throw new AssertionError("Greater curse roll");})==0,"Greater-only state spends no random rolls");
        check(saved.remove(Kind.SOLAR_HATRED) && saved.active().isEmpty(),"Operator removal can remove greater curse");
        check(Kind.OVERHEATING.chance==.001 && Kind.CONFLAGRATION.chance==1 && Kind.HYDROPHOBIA.chance==1 && Kind.SOLAR_HATRED.chance==1,"Original environmental proc frequencies");
        for(int light=0;light<=15;light++)check(EnvironmentalCurseRules.sunlight(true,light,0)==(light==15),"Exact skylight threshold "+light);
        for(int darkening:new int[]{1,4,11})check(!EnvironmentalCurseRules.sunlight(true,15,darkening),"Weather/night darkening blocks sunlight");
        check(!EnvironmentalCurseRules.sunlight(false,15,0),"Dimensions without skylight excluded");
        for(int tick:new int[]{0,20,40,100})check(EnvironmentalCurseRules.conflagration(tick),"Conflagration second boundary");
        for(int tick:new int[]{1,19,21,39})check(!EnvironmentalCurseRules.conflagration(tick),"No extra Conflagration refresh");
        check(EnvironmentalCurseRules.wetDamage(10,true,true)==15,"Wet cursed final damage scales by 1.5");
        check(EnvironmentalCurseRules.wetDamage(10,false,true)==10,"Dry cursed player unchanged");
        check(EnvironmentalCurseRules.wetDamage(10,true,false)==10,"Uncursed wet player unchanged");
        for(float value:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY})check(Float.floatToIntBits(EnvironmentalCurseRules.wetDamage(value,true,true))==Float.floatToIntBits(value),"Invalid damage left unchanged");
        check(EnvironmentalCurseRules.wetDamage(Float.MAX_VALUE,true,true)==Float.MAX_VALUE,"Finite damage does not overflow to infinity");
        check(MagicWeaknessEffect.damage(EnvironmentalCurseRules.wetDamage(10,true,true),0,true)==18,"Hydrophobia multiplier precedes Magic Weakness addition");
        SubscribeEvent wet=PlayerCurses.class.getMethod("wetDamage",LivingDamageEvent.class).getAnnotation(SubscribeEvent.class);
        check(wet.priority()==EventPriority.HIGHEST && !wet.receiveCanceled(),"Explicit wet-damage event order and canceled-event handling");
        System.out.println("PASS: "+checks+" environmental curse checks");
    }
}
