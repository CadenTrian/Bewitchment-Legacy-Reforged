package com.bewitchment.machine;
import com.bewitchment.item.PoppetItem;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import java.util.*;

public final class TarotCompanionsCheck {
    private static int checks;
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);checks++;}
    public static void main(String[] args){
        net.minecraft.util.registry.Bootstrap.bootStrap();UUID target=UUID.randomUUID(),other=UUID.randomUUID();
        for(boolean alive:new boolean[]{false,true})for(boolean tamed:new boolean[]{false,true})for(UUID owner:new UUID[]{null,target,other})
            check(TarotCompanions.owned(alive,tamed,owner,target)==(alive&&tamed&&target.equals(owner)),"Only living tamed owned animals match");
        check(!TarotCompanions.owned(true,true,target,null),"Missing target never matches");
        check(TarotCompanions.LIMIT==4096,"Bounded production scan");
        List<Iterable<Integer>> worlds=Arrays.asList(Arrays.asList(0,1),Collections.emptyList(),Arrays.asList(2,3));
        for(int limit=0;limit<=5;limit++)for(int match=-1;match<5;match++){
            final int wanted=match;int[] visited={0};
            TarotCompanions.Result expected=match>=0&&match<4&&match<limit?TarotCompanions.Result.FOUND:limit<4?TarotCompanions.Result.INCOMPLETE:TarotCompanions.Result.NONE;
            check(TarotCompanions.scan(worlds,value->{visited[0]++;return value==wanted;},limit)==expected,"Exact search boundary across worlds");
            check(visited[0]<=limit,"Never inspect more than the budget");
        }
        check(TarotCompanions.scan(Collections.<Iterable<Integer>>emptyList(),value->{throw new AssertionError();},0)==TarotCompanions.Result.NONE,"Empty worlds need no budget");
        int[] next={0};Iterable<Integer> infinite=()->new Iterator<Integer>(){public boolean hasNext(){return true;}public Integer next(){return next[0]++;}};
        check(TarotCompanions.scan(Collections.singletonList(infinite),value->false,4096)==TarotCompanions.Result.INCOMPLETE&&next[0]==4096,"Infinite input cannot exceed entity traversal budget");
        try{TarotCompanions.scan(worlds,value->false,-1);throw new AssertionError("Negative budget accepted");}catch(IllegalArgumentException expected){checks++;}
        Iterable<Integer> forbidden=()->{throw new AssertionError("Visited a world after finding the pet");};
        check(TarotCompanions.scan(Arrays.asList(Collections.singletonList(7),forbidden),value->value==7,1)==TarotCompanions.Result.FOUND,"Stop immediately after a match");
        ItemStack totem=new ItemStack(Items.TOTEM_OF_UNDYING),stick=new ItemStack(Items.STICK);
        check(TarotProtection.heldTotem(totem,ItemStack.EMPTY)&&TarotProtection.heldTotem(ItemStack.EMPTY,totem),"Either hand can hold a totem");
        check(!TarotProtection.heldTotem(stick,ItemStack.EMPTY)&&!TarotProtection.poppet(totem,target),"A carried but unheld totem is not a poppet");
        check(totem.getCount()==1&&!totem.hasTag(),"Totem query does not consume or write NBT");
        for(PoppetItem.Kind kind:PoppetItem.Kind.values())for(int damage:new int[]{0,kind.uses-1,kind.uses,kind.uses+1}){
            ItemStack stack=new ItemStack(new PoppetItem(kind));stack.getOrCreateTag().putString("boundId",target.toString());stack.getTag().putString("custom","preserved");stack.setDamageValue(damage);
            CompoundNBT before=stack.getTag().copy();int count=stack.getCount();boolean usable=damage<kind.uses;
            check(TarotProtection.poppet(stack,target)==(usable&&kind!=PoppetItem.Kind.VOODOO),"Only usable defensive bound poppets reported");
            check(!TarotProtection.poppet(stack,other)&&!TarotProtection.poppet(stack,null),"Binding must match the target");
            check(stack.getTag().equals(before)&&stack.getCount()==count,"Inspection preserves count, charges and custom NBT");
            check(PoppetItem.usable(stack,kind,target)==PoppetItem.spend(stack.copy(),kind,target),"Read predicate agrees with real spend on an isolated copy");
        }
        ItemStack unbound=new ItemStack(new PoppetItem(PoppetItem.Kind.DEATH));
        CompoundNBT initialTag=unbound.hasTag()?unbound.getTag().copy():null;
        check(!TarotProtection.poppet(unbound,target)&&Objects.equals(initialTag,unbound.getTag()),"Unbound query preserves initial damage NBT");
        for(String value:new String[]{"bad","1-1-1-1-1",""}){unbound.getOrCreateTag().putString("boundId",value);check(!TarotProtection.poppet(unbound,target),"Malformed binding not protection");}
        unbound.getOrCreateTag().putString("boundId",target.toString());unbound.setCount(0);
        check(!TarotProtection.poppet(unbound,target)&&!PoppetItem.usable(ItemStack.EMPTY,PoppetItem.Kind.DEATH,target),"Empty stacks cannot protect");
        System.out.println("PASS: "+checks+" companion ownership/search budgets and read-only carried protection checks");
    }
}
