package com.bewitchment.entity;
import com.bewitchment.client.LegacyLeonardModel;
import com.bewitchment.potion.MortalCoilEffect;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;
public final class LeonardCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int period:new int[]{100,200,450}){int pulses=0;for(int i=0;i<period*2;i++)if(LeonardRules.pulse(i,period))pulses++;check(pulses==12,"Six active ticks each period");check(LeonardRules.pulse(5,period) && !LeonardRules.pulse(6,period) && LeonardRules.pulse(period,period),"Pulse edges");}
        check(!LeonardRules.pulse(-1,100) && !LeonardRules.pulse(1,0),"Invalid pulse inputs");
        for(int bits=0;bits<8;bits++)for(int levels:new int[]{0,1,2,3})for(int timer:new int[]{0,1,24000})check(LeonardRules.stew((bits&1)!=0,(bits&2)!=0,(bits&4)!=0,levels,timer)==(bits==7 && levels>=2 && timer==0),"Stew eligibility combinations");
        check(LeonardRules.timer(-1)==0 && LeonardRules.timer(Integer.MAX_VALUE)==24000 && LeonardRules.timer(123)==123,"Persisted cooldown bounds");
        AttributeModifierMap a=LeonardRules.attributes(AttributeModifierMap.builder()).build();check(a.getBaseValue(Attributes.MAX_HEALTH)==500 && a.getBaseValue(Attributes.ATTACK_DAMAGE)==7,"Original health and damage");check(a.getBaseValue(Attributes.ARMOR)==13.616 && a.getBaseValue(Attributes.ARMOR_TOUGHNESS)==6.16,"Original armor");check(a.getBaseValue(Attributes.MOVEMENT_SPEED)==.5 && a.getBaseValue(Attributes.KNOCKBACK_RESISTANCE)==.9,"Original movement");
        MortalCoilEffect coil=new MortalCoilEffect();for(int amp:new int[]{0,1,255})for(int duration:new int[]{-1,0,1,2,6000})check(coil.isDurationEffectTick(duration,amp)==(duration==1),"Only final tick triggers Mortal Coil");check(coil.getCategory()==EffectType.HARMFUL && !coil.isInstantenous(),"Noninstant harmful countdown");
        check(GreaterDemonEntity.class.isAssignableFrom(LeonardEntity.class),"Pledge-aware boss foundation");
        LegacyLeonardModel model=new LegacyLeonardModel();check(model.mHorn04!=null && model.lClaw03!=null && model.rClaw03!=null && model.beard!=null,"Original horns feet beard");
        check(model.body!=null && model.head!=null && model.rightArm!=null,"Biped animation and held-item roots");
        System.out.println("PASS: "+checks+" Leonard combat cadence, stew eligibility, attributes, model and Mortal Coil checks");
    }
}