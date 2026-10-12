package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class EverflameCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);return !result.getReader().canRead() && result.getContext().getCommand()!=null;}
    public static void main(String[] args){
        Bootstrap.bootStrap();Kind kind=Kind.EVERFLAME;
        check(!kind.lesser() && !kind.positive() && kind.chance==.0002,"Legacy greater negative classification and proc chance");
        check(Kind.fromId("bewitchment:everflame")==kind,"Canonical identity");
        for(double roll:new double[]{0,Math.nextDown(.0002)})check(EnvironmentalCurseRules.proc(kind,roll),"Proc below boundary");
        for(double roll:new double[]{.0002,1,-1,Double.NaN,Double.POSITIVE_INFINITY})check(!EnvironmentalCurseRules.proc(kind,roll),"Invalid or out of range proc");
        check(EverflameCurse.ignitionRoll(0) && EverflameCurse.ignitionRoll(Math.nextDown(.1)),"Ignition lower and upper interior boundaries");
        for(double roll:new double[]{.1,1,-1,Double.NaN,Double.POSITIVE_INFINITY})check(!EverflameCurse.ignitionRoll(roll),"Ignition excludes boundary and malformed rolls");
        for(int mask=0;mask<32;mask++)check(EverflameCurse.eligible((mask&1)!=0,(mask&2)!=0,(mask&4)!=0,(mask&8)!=0,(mask&16)!=0)==(mask==31),"All placement conditions required "+mask);
        BlockPos center=new BlockPos(-16,64,15);AtomicInteger rolls=new AtomicInteger(),visits=new AtomicInteger();
        List<BlockPos> all=EverflameCurse.candidates(center,()->{rolls.incrementAndGet();return 0;},p->{visits.incrementAndGet();return true;});
        check(all.size()==125 && new HashSet<>(all).size()==125 && rolls.get()==125 && visits.get()==125,"Exactly 125 distinct positions and independent rolls");
        check(all.contains(center.offset(-2,-2,-2)) && all.contains(center.offset(2,2,2)) && all.contains(center),"Inclusive cube boundaries and center");
        check(all.stream().allMatch(p->Math.abs(p.getX()-center.getX())<=2 && Math.abs(p.getY()-center.getY())<=2 && Math.abs(p.getZ()-center.getZ())<=2),"No positions outside legacy range");
        check(all.stream().noneMatch(p->p instanceof BlockPos.Mutable),"Candidate positions do not alias iteration cursor");
        check(EverflameCurse.candidates(center,()->.1,p->{throw new AssertionError("Unselected position queried");}).isEmpty(),"Failed rolls do not query blocks");
        check(EverflameCurse.candidates(center,()->0,p->false).isEmpty(),"No placement when every support is invalid");
        check(EverflameCurse.candidates(center,()->0,p->p.equals(center)).equals(Collections.singletonList(center)),"Mixed eligibility preserves only allowed positions");
        CurseData data=new CurseData();check(data.add(kind,7),"Apply for seven days");data.second();check(!data.add(kind,1) && data.remaining(kind)==167980,"Duplicate does not refresh duration");
        CurseData loaded=new CurseData();loaded.load(data.save());check(loaded.remaining(kind)==167980,"Persistent timer");
        check(loaded.purify(()->{throw new AssertionError("Greater curse received Purity roll");})==0,"Purity leaves Everflame");
        check(loaded.remove(kind) && loaded.active().isEmpty(),"Explicit removal");CompoundNBT last=new CompoundNBT();last.putInt(kind.id,20);loaded.load(last);loaded.second();check(loaded.active().isEmpty(),"Final second expires");
        CurseData target=new CurseData(),caster=new CurseData();target.add(Kind.RETURN_TO_SENDER,1);CurseCasting.Outcome outcome=CurseCasting.apply(caster,target,kind,1);
        check(outcome.added && !outcome.reflected && target.remaining(kind)==24000 && target.remaining(Kind.RETURN_TO_SENDER)==24000 && caster.active().isEmpty(),"Greater cast bypasses ward without consuming it");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();CurseCommands.register(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null),guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(String action:new String[]{"add bewitchment:everflame 1","cast bewitchment:everflame 7","remove bewitchment:everflame"}){check(parses(dispatcher,op,"curse @s "+action),"Operator command "+action);check(!parses(dispatcher,guest,"curse @s "+action),"Guest denied "+action);}
        check(!parses(dispatcher,op,"curse @s add bewitchment:everflame 0"),"Zero duration denied");
        System.out.println("PASS: "+checks+" Everflame checks");
    }
}
