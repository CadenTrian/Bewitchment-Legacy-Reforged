package com.bewitchment.machine;
import com.bewitchment.combat.FortuneCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
public final class FortuneCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final class Store implements FortuneDispatch.Store {
        CompoundNBT tag=new CompoundNBT();
        public FortuneData read(){FortuneData data=new FortuneData();data.load(tag.copy());return data;}
        public void write(FortuneData data){tag=data.save();}
        void assign(FortuneData.Kind kind){FortuneData data=new FortuneData();data.assign(kind,0);write(data);}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();FortuneData.Kind[] kinds=FortuneData.Kind.values();
        int[] minimum={60,60,60,120,120,300,60,120,1200,60,300,300,180,300,60,300,300,300,60,60,60,60,300,30,120,300,300,69,66},maximum={600,600,600,600,600,1800,600,600,10800,1200,1800,1500,1200,1800,180,1500,1500,1500,180,180,180,180,1200,900,3600,900,900,420,666},duration={500,1000,3600,666,666,215,500,0,215,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Effect[][] types={{Effects.REGENERATION},{Effects.DAMAGE_RESISTANCE},{Effects.FIRE_RESISTANCE},{Effects.LUCK},{Effects.UNLUCK},{Effects.HUNGER,Effects.CONFUSION,Effects.WEAKNESS},{Effects.DAMAGE_BOOST},{},{Effects.WITHER},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}};
        check(kinds.length==29,"Twenty-nine restored fortune identities");
        for(int i=0;i<kinds.length;i++){
            FortuneData.Kind kind=kinds[i];check(kind.minSeconds==minimum[i]&&kind.maxSeconds==maximum[i],"Original time window");check(kind.negative==(i==4||i==5||i==7||i==8||(i>=11&&i<=23)||i==25),"Original negative classification");
            check(FortuneData.Kind.parse(kind.id)==kind&&FortuneData.Kind.parse(kind.key())==kind,"Short and namespaced IDs");
            Set<Integer> delays=new HashSet<>();for(int roll=0;roll<maximum[i]-minimum[i];roll++)delays.add(kind.delay(roll));
            check(delays.size()==maximum[i]-minimum[i]&&Collections.min(delays)==minimum[i]&&Collections.max(delays)==maximum[i]-1,"Uniform discrete window with exclusive upper bound");
            for(int roll:new int[]{Integer.MIN_VALUE,-1,Integer.MAX_VALUE})check(kind.delay(roll)>=minimum[i]&&kind.delay(roll)<maximum[i],"Extreme random input remains bounded");
            for(int difficulty:new int[]{Integer.MIN_VALUE,0,1,2,3,Integer.MAX_VALUE}){
                List<EffectInstance> effects=kind.effects(difficulty);check(effects.size()==types[i].length,"Exact effect count");
                for(int e=0;e<effects.size();e++)check(effects.get(e).getEffect()==types[i][e]&&effects.get(e).getDuration()==duration[i]&&effects.get(e).getAmplifier()==(i==2?0:1+Math.max(0,Math.min(3,difficulty)))&&!effects.get(e).isVisible()&&!effects.get(e).isAmbient(),"Original durations, strength and hidden particles");
                if(!effects.isEmpty())check(effects.get(0)!=kind.effects(difficulty).get(0),"Fresh effect instances per execution");
            }
            FortuneData data=new FortuneData();data.assign(kind,0);check(data.remaining()==minimum[i]*20,"Seconds converted to online ticks");
            for(int tick=1;tick<minimum[i]*20;tick++)if(data.tick())throw new AssertionError("Premature activation");
            check(data.remaining()==1&&data.tick()&&data.remaining()==0,"Exact expiry boundary");
            CompoundNBT saved=data.save();FortuneData resumed=new FortuneData();resumed.load(saved);check(resumed.kind()==kind&&resumed.tick(),"Zero-time save activates rather than underflowing");
            check(resumed.take()==kind&&resumed.kind()==null&&resumed.remaining()==0&&!resumed.tick(),"Consumed fortune cannot tick again");
            data.assign(kind,maximum[i]-minimum[i]-1);for(int t=0;t<37;t++)data.tick();saved=data.save();resumed.load(saved);check(resumed.save().equals(saved),"Partial countdown exact round trip");
            saved.putInt("ticks",0);check(resumed.remaining()>0,"Loaded state isolated from NBT mutation");
            CompoundNBT valid=resumed.save();
            for(int corrupt=0;corrupt<7;corrupt++){
                CompoundNBT bad=valid.copy();switch(corrupt){case 0:bad.putInt("version",2);break;case 1:bad.putString("ticks","20");break;case 2:bad.putInt("ticks",-1);break;case 3:bad.putInt("ticks",maximum[i]*20);break;case 4:bad.putString("kind","other:"+kind.id);break;case 5:bad.remove("kind");break;default:bad.putLong("version",1);}
                FortuneData rejected=new FortuneData();rejected.assign(kind,0);rejected.load(bad);check(rejected.kind()==null&&rejected.remaining()==0,"Invalid saves clear stale state safely");
            }
        }
        check(FortuneData.Kind.parse(null)==null&&FortuneData.Kind.parse("unknown_fortune")==null,"Unknown fortunes not exposed");
        FortuneData replacement=new FortuneData();replacement.assign(kinds[0],0);replacement.tick();replacement.assign(kinds[5],0);check(replacement.kind()==kinds[5]&&replacement.remaining()==6000,"Replacing pending fortune resets its timer");check(!replacement.assign(null,0)&&replacement.kind()==kinds[5],"Null assignment leaves existing fortune intact");
        Store store=new Store();UUID player=UUID.randomUUID();int[] applied={0};store.assign(kinds[0]);
        check(FortuneDispatch.execute(player,store,kind->{
            applied[0]++;check(kind==kinds[0]&&store.read().kind()==null,"Consumption saved before callback");store.assign(kinds[1]);
            check(!FortuneDispatch.execute(player,store,nested->applied[0]++),"Same-player reentry blocked even with replacement fortune");
        }),"First execution succeeds");
        check(applied[0]==1&&store.read().kind()==kinds[1],"Callback assignment survives and is not executed twice");
        check(FortuneDispatch.execute(player,store,kind->applied[0]++)&&store.read().kind()==null,"Later execution can consume replacement");
        check(!FortuneDispatch.execute(player,store,kind->applied[0]++)&&applied[0]==2,"Empty fortune never applies");
        store.assign(kinds[2]);try{FortuneDispatch.execute(player,store,kind->{throw new IllegalStateException("callback");});throw new AssertionError("Exception swallowed");}catch(IllegalStateException expected){checks++;}
        check(store.read().kind()==null,"Callback exception cannot replay consumed fortune");store.assign(kinds[3]);check(FortuneDispatch.execute(player,store,kind->{}),"Guard released after exception");
        Store other=new Store();store.assign(kinds[0]);other.assign(kinds[1]);check(FortuneDispatch.execute(player,store,kind->check(FortuneDispatch.execute(UUID.randomUUID(),other,next->{}),"Separate player dispatch independent")),"Nested other player permitted");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();FortuneCommands.register(dispatcher);CommandNode<CommandSource> root=dispatcher.getRoot().getChild("fortune"),target=root.getChild("player");
        for(String verb:new String[]{"get","set","remove","execute"})check(target.getChild(verb)!=null,"Original command verb exposed");
        for(FortuneData.Kind kind:kinds)check(target.getChild("set").getChild(kind.id).getCommand()!=null&&target.getChild("set").getChild(kind.key()).getCommand()!=null,"Only supported IDs have set handlers");
        check(target.getChild("set").getChild("unknown")==null&&target.getChild("set").getChildren().size()==58,"Unknown fortune cannot mutate command state");
        for(int permission=-1;permission<=4;permission++){
            CommandSource source=new CommandSource(ICommandSource.NULL,Vector3d.ZERO,Vector2f.ZERO,null,permission,"test",new StringTextComponent("test"),null,null);
            check(root.getRequirement().test(source)==(permission>=2),"Operator permission level two enforced");
        }
        System.out.println("PASS: "+checks+" fortune effects, timer boundaries, save validation, callback lifecycle and command permission checks");
    }
}
