package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class ContractEffectsCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);return !result.getReader().canRead() && result.getContext().getCommand()!=null;}
    public static void main(String[] args){
        Bootstrap.bootStrap();CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();CurseCommands.register(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null),guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(Kind kind:new Kind[]{Kind.BERSERKER,Kind.SALAMANDRINE_TOLERANCE,Kind.DEVOURING_RAGE,Kind.DESICCATION,Kind.IGNITION}){
            check(kind.positive() && kind.lesser() && kind.chance==0,"Positive lesser event-driven contract "+kind);
            check(Kind.fromId(kind.id)==kind,"Canonical identity "+kind);
            CurseData data=new CurseData();check(data.add(kind,7),"Seven-day application "+kind);data.second();check(!data.add(kind,1) && data.remaining(kind)==167980,"Duplicates preserve old duration "+kind);
            CurseData copy=new CurseData();copy.load(data.save());check(copy.remaining(kind)==167980,"Timer survives NBT "+kind);
            check(copy.purify(()->{throw new AssertionError("Positive contract received Purity roll");})==0 && copy.remaining(kind)==167980,"Purity preserves positive lesser contract "+kind);
            check(copy.remove(kind) && copy.active().isEmpty(),"Explicit removal "+kind);CompoundNBT last=new CompoundNBT();last.putInt(kind.id,20);copy.load(last);copy.second();check(copy.active().isEmpty(),"Expiry "+kind);
            for(String action:new String[]{"add "+kind.id+" 7","remove "+kind.id,"cast "+kind.id+" 1"}){check(parses(dispatcher,op,"curse @s "+action),"Operator path "+action);check(!parses(dispatcher,guest,"curse @s "+action),"Guest denied "+action);}
            CurseData target=new CurseData(),caster=new CurseData();target.add(Kind.RETURN_TO_SENDER,1);CurseCasting.Outcome outcome=CurseCasting.apply(caster,target,kind,1);
            check(outcome.reflected && caster.remaining(kind)==24000 && target.remaining(Kind.RETURN_TO_SENDER)==0,"Explicit cast retains original lesser-only reflection rule "+kind);
        }
        check(ContractEffects.berserker(6,20,20)==6,"Full health damage unchanged");
        check(ContractEffects.berserker(6,10,20)==12,"Half health doubles damage");
        check(ContractEffects.berserker(6,5,20)==24,"Quarter health quadruples damage");
        check(ContractEffects.berserker(6,40,20)==6,"Over-max health cannot reduce damage");
        for(float health:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY})check(ContractEffects.berserker(6,health,20)==6,"Invalid health is inert");
        for(float maximum:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY})check(ContractEffects.berserker(6,10,maximum)==6,"Invalid maximum is inert");
        check(ContractEffects.berserker(Float.MAX_VALUE,Float.MIN_VALUE,Float.MAX_VALUE)==Float.MAX_VALUE,"Extreme ratio saturates finite damage");
        for(float damage:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY})check(Float.floatToIntBits(ContractEffects.berserker(damage,10,20))==Float.floatToIntBits(damage),"Invalid damage not transformed");
        for(DamageSource source:new DamageSource[]{DamageSource.IN_FIRE,DamageSource.ON_FIRE,DamageSource.LAVA}){check(ContractEffects.salamandrineSource(source),"Exact legacy fire source");check(ContractEffects.salamandrine(9,source)==3,"One third damage");}
        for(DamageSource source:new DamageSource[]{DamageSource.HOT_FLOOR,DamageSource.MAGIC,DamageSource.FALL,DamageSource.GENERIC,new DamageSource("custom_fire").setIsFire(),null})check(ContractEffects.salamandrine(9,source)==9,"Other damage source unchanged");
        check(ContractEffects.salamandrine(ContractEffects.berserker(9,10,20),DamageSource.IN_FIRE)==6,"Attacker then victim contract ordering");
        EffectInstance regen=ContractEffects.regeneration(),strength=ContractEffects.cropStrength();
        check(regen.getEffect()==Effects.REGENERATION && regen.getDuration()==20 && regen.getAmplifier()==9,"Original Devouring Rage regeneration");
        check(ContractEffects.DEVOURING_HEAL==3,"Original direct heal");
        check(strength.getEffect()==Effects.DAMAGE_BOOST && strength.getDuration()==1200 && strength.getAmplifier()==2,"Original Desiccation Strength III");
        check(regen!=ContractEffects.regeneration() && strength!=ContractEffects.cropStrength(),"Each application has independent effect instances");
        check(!regen.isAmbient() && regen.isVisible() && !strength.isAmbient() && strength.isVisible(),"Legacy visible potion flags");
        System.out.println("PASS: "+checks+" positive contract effect checks");
    }
}
