package com.bewitchment.combat;

import com.bewitchment.api.event.PlayerCurseEvent;
import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.*;

public final class CurseReflectionCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){
        com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);
        return !result.getReader().canRead() && result.getContext().getCommand()!=null;
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Kind kind:Kind.values())for(int mask=0;mask<4;mask++){
            CurseData caster=new CurseData(),target=new CurseData();
            if((mask&1)!=0)caster.add(Kind.RETURN_TO_SENDER,2);
            if((mask&2)!=0)target.add(Kind.RETURN_TO_SENDER,2);
            CurseCasting.Outcome result=CurseCasting.apply(caster,target,kind,1);
            boolean reflected=kind.lesser() && (mask&2)!=0;
            CurseData recipient=reflected?caster:target;
            boolean duplicate=kind==Kind.RETURN_TO_SENDER && (mask&2)!=0;
            check(result.reflected==reflected && result.added!=duplicate && recipient.remaining(kind)==(duplicate?48000:24000)
                && (!reflected || caster.remaining(Kind.RETURN_TO_SENDER)==0 && target.remaining(Kind.RETURN_TO_SENDER)==0),"Curse/ward combination "+kind+"/"+mask);
        }
        check(Kind.RETURN_TO_SENDER.positive() && !Kind.RETURN_TO_SENDER.lesser() && Kind.RETURN_TO_SENDER.chance==0,"Positive greater reaction ward without periodic procs");
        CurseData caster=new CurseData(),target=new CurseData();caster.add(Kind.DARKNESS,2);caster.add(Kind.RETURN_TO_SENDER,1);target.add(Kind.RETURN_TO_SENDER,1);
        CurseCasting.Outcome duplicate=CurseCasting.apply(caster,target,Kind.DARKNESS,1);
        check(duplicate.reflected && !duplicate.added && caster.remaining(Kind.DARKNESS)==48000 && caster.remaining(Kind.RETURN_TO_SENDER)==0 && target.remaining(Kind.RETURN_TO_SENDER)==0,"Legacy duplicate reflection still consumes both wards");
        CurseData self=new CurseData();self.add(Kind.RETURN_TO_SENDER,1);CurseCasting.Outcome same=CurseCasting.apply(self,self,Kind.MISFORTUNE,1);
        check(same.reflected && same.added && self.remaining(Kind.RETURN_TO_SENDER)==0 && self.remaining(Kind.MISFORTUNE)==24000,"Self-cast settles once without recursion");
        for(int days:new int[]{-1,0,CurseData.MAX_DAYS+1,Integer.MAX_VALUE}){
            target=new CurseData();target.add(Kind.RETURN_TO_SENDER,1);CurseCasting.Outcome invalid=CurseCasting.apply(new CurseData(),target,Kind.DARKNESS,days);
            check(!invalid.added && !invalid.reflected && target.remaining(Kind.RETURN_TO_SENDER)==24000,"Invalid duration preserves ward");
        }
        check(!CurseCasting.valid(null,1),"Null curse rejected");
        CurseData ward=new CurseData();ward.add(Kind.RETURN_TO_SENDER,1);check(ward.purify(()->{throw new AssertionError("Ward purification roll");})==0,"Purity preserves positive ward");
        CurseData loaded=new CurseData();loaded.load(ward.save());loaded.second();check(loaded.remaining(Kind.RETURN_TO_SENDER)==23980,"Ward persistence and duration");
        PlayerCurseEvent event=new PlayerCurseEvent(null,null,Kind.DARKNESS,7);check(PlayerCurseEvent.class.isAnnotationPresent(Cancelable.class) && event.getCurse()==Kind.DARKNESS && event.getDays()==7,"Cancelable event carries curse and days");
        event.setCurse(Kind.PARANOIA);event.setDays(2);event.setTarget(null);check(event.getCurse()==Kind.PARANOIA && event.getDays()==2 && event.getTarget()==null,"Event mutations exposed for validation");
        event.setDays(0);check(!CurseCasting.valid(event.getCurse(),event.getDays()),"Mutated invalid event duration is rejected");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();CurseCommands.register(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null);
        CommandSource guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        check(parses(dispatcher,op,"curse @s cast bewitchment:darkness 7"),"Caster-aware command parses");
        check(parses(dispatcher,op,"curse @s cast bewitchment:return_to_sender 7"),"Positive ward can be cast");
        check(parses(dispatcher,op,"curse @s add bewitchment:return_to_sender 1"),"Direct add remains available");
        check(!parses(dispatcher,guest,"curse @s cast bewitchment:darkness 7"),"Cast requires permission two");
        check(!parses(dispatcher,op,"curse @s cast bewitchment:darkness 0"),"Cast rejects zero duration");
        System.out.println("PASS: "+checks+" curse reflection checks");
    }
}
