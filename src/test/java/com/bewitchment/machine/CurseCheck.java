package com.bewitchment.machine;

import com.bewitchment.combat.*;
import com.bewitchment.item.ChestSigilItem;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.concurrent.atomic.AtomicInteger;

public final class CurseCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){
        com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);
        return !result.getReader().canRead() && result.getContext().getCommand()!=null;
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();CurseData data=new CurseData();
        check(data.active().isEmpty(),"New player has no curses");
        for(CurseData.Kind kind:new CurseData.Kind[]{CurseData.Kind.DARKNESS,CurseData.Kind.MISFORTUNE}){
            check(kind.lesser() && !kind.positive(),"Restored curses are lesser and negative");
            check(CurseData.Kind.fromId(kind.id)==kind,"Stable curse identity");
            check(data.add(kind,1) && data.remaining(kind)==24000,"One day is 24000 ticks");
            check(!data.add(kind,2) && data.remaining(kind)==24000,"Duplicates do not refresh");
        }
        CompoundNBT saved=data.save();CurseData copy=new CurseData();copy.load(saved);
        check(copy.active().equals(data.active()),"NBT roundtrip identities");
        copy.second();check(copy.remaining(CurseData.Kind.DARKNESS)==23980 && data.remaining(CurseData.Kind.DARKNESS)==24000,"Independent timer copies");
        for(int i=0;i<1198;i++)copy.second();check(copy.remaining(CurseData.Kind.DARKNESS)==20,"Last second remains active");
        copy.second();check(copy.active().isEmpty(),"Both curses expire together");
        check(data.purify(()->.5)==0 && data.active().size()==2,"Purity threshold excludes one half");
        AtomicInteger rolls=new AtomicInteger();check(data.purify(()->rolls.getAndIncrement()==0?0:1)==1 && rolls.get()==2,"Independent roll for each active curse");
        check(data.remaining(CurseData.Kind.DARKNESS)==0 && data.remaining(CurseData.Kind.MISFORTUNE)==24000,"Failed purity roll preserves duration");
        check(data.purify(()->Math.nextDown(.5))==1 && data.active().isEmpty(),"Successful purity removes remaining curse");
        check(data.purify(()->{throw new AssertionError("Unexpected random draw");})==0,"No random rolls for empty curses");
        for(int days:new int[]{-1,0,CurseData.MAX_DAYS+1,Integer.MAX_VALUE})check(!data.add(CurseData.Kind.DARKNESS,days),"Reject invalid day count");
        check(data.add(CurseData.Kind.DARKNESS,CurseData.MAX_DAYS) && data.remaining(CurseData.Kind.DARKNESS)>0,"Maximum duration avoids overflow");
        check(data.clear()==1 && data.clear()==0,"Clear count is accurate");
        check(!data.remove(CurseData.Kind.DARKNESS),"Remove absent curse safely");
        saved.putInt("bewitchment:unported",24000);saved.putInt(CurseData.Kind.DARKNESS.id,-5);saved.putString(CurseData.Kind.MISFORTUNE.id,"bad");
        data.load(saved);check(data.active().isEmpty(),"Unknown, negative and wrong-type entries are ignored");
        saved.putInt(CurseData.Kind.DARKNESS.id,1);data.load(saved);data.second();check(data.active().isEmpty(),"Short malformed duration expires");
        check(CurseData.Kind.fromId("darkness")==null,"Only canonical persisted identifiers accepted");
        for(double roll:new double[]{-.1,.0005,1,Double.NaN})check(!PlayerCurses.proc(roll),"Invalid or boundary proc rejected");
        check(PlayerCurses.proc(0) && PlayerCurses.proc(Math.nextDown(.0005)),"Legacy 0.05 percent proc");
        for(int roll=0;roll<20;roll++)check(PlayerCurses.misfortuneDuration(roll)==(10+roll)*20,"Misfortune duration bucket "+roll);
        check(PlayerCurses.eligible(Effects.BLINDNESS,new ResourceLocation("minecraft:blindness")),"Blindness eligible");
        check(!PlayerCurses.eligible(Effects.POISON,new ResourceLocation("minecraft:poison")),"Poison excluded");
        check(!PlayerCurses.eligible(Effects.WITHER,new ResourceLocation("minecraft:wither")),"Wither excluded");
        check(!PlayerCurses.eligible(Effects.HARM,new ResourceLocation("minecraft:instant_damage")),"Instant effects excluded");
        check(!PlayerCurses.eligible(Effects.LUCK,new ResourceLocation("minecraft:luck")),"Beneficial effects excluded");
        check(!PlayerCurses.eligible(Effects.BLINDNESS,new ResourceLocation("bewitchment:wednesday")),"Wednesday identity excluded");
        check(!PlayerCurses.eligible(Effects.BLINDNESS,new ResourceLocation("bewitchment:mortal_coil")),"Mortal Coil identity excluded");
        check(ChestSigilItem.Kind.PURITY.positive && ChestSigilItem.Kind.PURITY.cooldown==6000,"Purity targeting and cooldown");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();CurseCommands.register(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null);
        CommandSource guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(String command:new String[]{"curse @s get","curse @s clear","curse @s add bewitchment:darkness 1","curse @s remove bewitchment:misfortune"})check(parses(dispatcher,op,command),"Operator command parses: "+command);
        for(String command:new String[]{"curse @s add bewitchment:darkness 0","curse @s add bewitchment:darkness 89479","curse @s add bewitchment:missing 1","curse @s remove bewitchment:missing"})check(!parses(dispatcher,op,command),"Invalid command rejected: "+command);
        check(!parses(dispatcher,guest,"curse @s clear"),"Permission two required");
        for(boolean lesser:new boolean[]{true,false})for(double roll:new double[]{0,.599999,.6,.899999,.9,1,-1,Double.NaN}){
            CurseData cleansing=new CurseData();for(CurseData.Kind kind:CurseData.Kind.values())cleansing.add(kind,2);
            int[] cleansingRolls={0};int eligible=0,expected=0;
            for(CurseData.Kind kind:CurseData.Kind.values())if(!kind.positive()&&(!lesser||kind.lesser()))eligible++;
            if(roll>=0&&roll<(lesser?.6:.9))expected=eligible;
            check(cleansing.cleanse(lesser,()->{cleansingRolls[0]++;return roll;})==expected,"Independent cleansing chance and boundaries");
            check(cleansingRolls[0]==eligible,"Roll only for eligible harmful curses");
            for(CurseData.Kind kind:CurseData.Kind.values()){
                boolean removed=!kind.positive()&&(!lesser||kind.lesser())&&roll>=0&&roll<(lesser?.6:.9);
                check(cleansing.remaining(kind)==(removed?0:48000),"Preserve blessing, excluded tier and failed-roll duration");
            }
            CurseData restored=new CurseData();restored.load(cleansing.save());check(restored.active().equals(cleansing.active()),"Cleansing survives save/load");
        }
        for(RitualEffect effect:new RitualEffect[]{RitualEffect.LESSER_CURSE_CLEANSING,RitualEffect.GREATER_CURSE_CLEANSING}){
            check(effect.cleansing()&&!effect.continuous()&&!effect.consumesOnStart(),"Finite completion-only cleansing");
            check(effect.failure(false,true)==null&&effect.failure(true,false)==null,"Cleansing works in all dimensions and times");
        }
        System.out.println("PASS: "+checks+" curse state, effects, Purity and command checks");
    }
}
