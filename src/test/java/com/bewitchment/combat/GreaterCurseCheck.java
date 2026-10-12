package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.concurrent.atomic.AtomicInteger;

public final class GreaterCurseCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){
        com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);
        return !result.getReader().canRead() && result.getContext().getCommand()!=null;
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Kind kind:new Kind[]{Kind.LIGHTNING_ROD,Kind.PALOR_PESTILENCE}){
            check(!kind.lesser() && !kind.positive(),"Greater negative classification "+kind);
            check(Kind.fromId(kind.id)==kind,"Canonical identity "+kind);
            CurseData data=new CurseData();check(data.add(kind,1),"Apply "+kind);
            data.second();check(!data.add(kind,2) && data.remaining(kind)==23980,"Reapplication preserves remaining time");
            CurseData copy=new CurseData();copy.load(data.save());check(copy.remaining(kind)==23980,"NBT roundtrip "+kind);
            check(copy.purify(()->{throw new AssertionError("Greater curse consumed random roll");})==0 && copy.remaining(kind)==23980,"Purity cannot affect greater curse");
            check(copy.remove(kind) && copy.active().isEmpty(),"Explicit removal allowed");
            CompoundNBT last=new CompoundNBT();last.putInt(kind.id,20);copy.load(last);copy.second();check(copy.active().isEmpty(),"Expiry boundary "+kind);
            check(EnvironmentalCurseRules.proc(kind,0) && EnvironmentalCurseRules.proc(kind,Math.nextDown(kind.chance)),"Inclusive zero and exclusive upper boundary");
            check(!EnvironmentalCurseRules.proc(kind,kind.chance) && !EnvironmentalCurseRules.proc(kind,Double.NaN),"No proc at boundary or malformed roll");
        }
        check(Kind.LIGHTNING_ROD.chance==.002 && Kind.PALOR_PESTILENCE.chance==.0004,"Original distinct proc chances");
        for(int mask=0;mask<4;mask++)check(GreaterCurseEffects.lightning((mask&1)!=0,(mask&2)!=0)==(mask==3),"Both rain exposure and wetness required "+mask);
        check(GreaterCurseEffects.LIGHTNING_DAMAGE==3,"Legacy direct damage amount");
        EffectInstance[] effects=GreaterCurseEffects.pestilence(),second=GreaterCurseEffects.pestilence();
        check(effects.length==2,"Exactly two pestilence effects");
        check(effects[0].getEffect()==Effects.WITHER && effects[1].getEffect()==Effects.POISON,"Original effect order and identities");
        for(int i=0;i<2;i++){
            check(effects[i].getDuration()==600,"Thirty-second duration");
            check(effects[i].getAmplifier()==0,"Level one");
            check(effects[i]!=second[i],"Fresh effect instances for each application");
            check(!effects[i].isAmbient() && effects[i].isVisible(),"Original potion display flags");
        }
        CurseData all=new CurseData();for(Kind kind:Kind.values())all.add(kind,1);
        check(all.active().size()==17,"Seventeen implemented curse and contract identities coexist");
        AtomicInteger rolls=new AtomicInteger();check(all.purify(()->{rolls.incrementAndGet();return 0;})==5 && rolls.get()==5,"Only five lesser curses receive rolls");
        check(all.active().size()==12 && all.remaining(Kind.SOLAR_HATRED)==24000 && all.remaining(Kind.LIGHTNING_ROD)==24000 && all.remaining(Kind.PALOR_PESTILENCE)==24000,"All greater timers preserved");
        check(all.clear()==12 && all.active().isEmpty(),"Operator clear can remove greater curses");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();CurseCommands.register(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null);
        CommandSource guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(Kind kind:new Kind[]{Kind.LIGHTNING_ROD,Kind.PALOR_PESTILENCE}){
            check(parses(dispatcher,op,"curse @s add "+kind.id+" 1"),"Add command available");
            check(parses(dispatcher,op,"curse @s remove "+kind.id),"Remove command available");
            check(!parses(dispatcher,op,"curse @s add "+kind.id+" 0"),"Zero days rejected");
            check(!parses(dispatcher,op,"curse @s add "+kind.id+" 89479"),"Overflowing days rejected");
            check(!parses(dispatcher,guest,"curse @s add "+kind.id+" 1"),"Operator permission required");
        }
        System.out.println("PASS: "+checks+" greater curse checks");
    }
}
