package com.bewitchment.machine;

import com.bewitchment.combat.KillContractTasks;
import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.*;

public final class ContractGenerationCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random edge(boolean high){return new Random(0){@Override public int nextInt(int bound){return high?bound-1:0;}};}
    private static boolean parses(CommandDispatcher<CommandSource> d,CommandSource s,String text){com.mojang.brigadier.ParseResults<CommandSource> r=d.parse(text,s);return !r.getReader().canRead() && r.getContext().getCommand()!=null;}
    private static void rejects(Runnable action,String message){boolean rejected=false;try{action.run();}catch(IllegalArgumentException|NullPointerException ex){rejected=true;}check(rejected,message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();KillContractItem item=new KillContractItem();
        check(ContractTasks.kinds().size()==6 && new HashSet<>(ContractTasks.kinds()).size()==6,"Six unique original tasks");
        check(ContractTasks.kinds().stream().filter(k->!k.positive()).count()==1 && ContractTasks.kinds().contains(Kind.MAHAPADMA),"Exactly one negative task");
        boolean immutable=false;try{ContractTasks.kinds().clear();}catch(UnsupportedOperationException ex){immutable=true;}check(immutable,"Pool cannot be externally cleared");
        for(Kind kind:ContractTasks.kinds()){
            for(boolean high:new boolean[]{false,true}){
                ItemStack stack=ContractTasks.generate(item,kind,edge(high));CompoundNBT tag=stack.getTag();
                check(stack.getCount()==1 && stack.getMaxStackSize()==1 && !KillContractItem.signed(stack),"Fresh unsigned unstackable task");
                check(!tag.contains("boundId") && !tag.contains("boundName") && tag.getString("contract").equals(kind.id),"Canonical identity without signature");
                if(KillContractData.supported(kind)){
                    KillContractData data=KillContractData.read(tag);
                    check(data!=null && data.total==(high?10:6) && data.done==0,"Kill count inclusive boundary");
                    check(!tag.contains("items"),"Kill task has no donation schema");
                }else{
                    DonationContractData data=DonationContractData.read(tag);check(data!=null && !data.complete(),"Valid incomplete donation schema");
                    for(int i=0;i<DonationContractData.size(kind);i++)check(data.done(i)==0 && data.total(i)==(DonationContractData.ingredient(kind,i)==Items.WATER_BUCKET?1:high?11:6),"Donation inclusive boundary and water exception");
                    check(!tag.contains("mobsTotal") && !tag.contains("mobsComplete"),"Donation task has no kill schema");
                }
                check(!item.isFoil(stack),"New task does not glint");
            }
            ItemStack first=ContractTasks.generate(item,kind,new Random(82)),second=ContractTasks.generate(item,kind,new Random(82));
            check(first.getTag().equals(second.getTag()),"Same seed reproduces requirements");
            first.getTag().putString("boundId",UUID.randomUUID().toString());check(!second.getTag().contains("boundId"),"Generated task NBT is independent");
            ItemStack creative=ContractTasks.creative(item,kind);check(!KillContractItem.signed(creative) && !item.isFoil(creative),"Creative task unsigned and incomplete");
        }
        for(int index=0;index<6;index++){
            final int chosen=index;
            Random rng=new Random(0){int call;@Override public int nextInt(int bound){if(call++==0){check(bound==6,"One uniform six-way draw");return chosen;}return 0;}};
            check(ContractTasks.random(item,rng).getTag().getString("contract").equals(ContractTasks.kinds().get(index).id),"Every pool slot reachable");
        }
        List<Integer> bounds=new ArrayList<>();Random sequence=new Random(0){int call;@Override public int nextInt(int bound){bounds.add(bound);return call++%bound;}};
        DonationContractData triple=DonationContractData.read(ContractTasks.generate(item,Kind.MAHAPADMA,sequence).getTag());
        check(bounds.equals(Arrays.asList(5,6,6,6)),"Legacy unused kill and water draws retained");
        check(triple.total(0)==7 && triple.total(1)==1 && triple.total(2)==9,"Donation requirements rolled independently");
        Map<Kind,Integer> frequency=new EnumMap<>(Kind.class);Random sample=new Random(1234);boolean valid=true;
        for(int i=0;i<6000;i++){ItemStack stack=ContractTasks.random(item,sample);Kind k=Kind.fromId(stack.getTag().getString("contract"));frequency.merge(k,1,Integer::sum);valid &= KillContractItem.data(stack)!=null || KillContractItem.donationData(stack)!=null;}
        check(valid,"All 6000 seeded tasks pass strict schema readers");
        check(frequency.size()==6 && frequency.values().stream().allMatch(n->n>800 && n<1200),"Seeded sample covers all tasks without gross weighting bias");
        for(Kind kind:Kind.values())if(!ContractTasks.kinds().contains(kind))rejects(()->ContractTasks.generate(item,kind,new Random(0)),"Non-task curse excluded");
        rejects(()->ContractTasks.generate(item,null,new Random(0)),"Null kind rejected");
        rejects(()->ContractTasks.random(Items.PAPER,new Random(0)),"Wrong output item rejected");
        rejects(()->ContractTasks.random(item,null),"Null RNG rejected");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();KillContractTasks.commands(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        for(int permission:new int[]{0,1,2,4}){
            CommandSource source=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,permission,"test",new StringTextComponent("test"),null,null);
            check(parses(dispatcher,source,"contract @s random")== (permission>=2),"Random command permissions");
            for(Kind kind:ContractTasks.kinds())check(parses(dispatcher,source,"contract @s "+kind.id)== (permission>=2),"Named task command permissions");
            check(!parses(dispatcher,source,"contract @s bewitchment:darkness"),"Non-task curse command rejected");
        }
        System.out.println("PASS: "+checks+" contract generation checks (including 6000 seeded tasks)");
    }
}