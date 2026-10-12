package com.bewitchment.trade;

import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.CurseData;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class DemonMerchantStateCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random edge(boolean high){return new Random(0){@Override public int nextInt(int bound){return high?bound-1:0;}};}
    private static final DemonMerchantState.Factory SIMPLE=(entry,random,discount)->new MerchantOffer(new ItemStack(Items.GOLD_INGOT,discount?7:15),new ItemStack(Items.APPLE),7,0,0);
    private static void reset(DemonMerchantState state,Random random,DemonMerchantState.Factory factory){boolean early=false;for(int i=0;i<39;i++)early|=state.tick(false,false,random,factory);check(!early,"Reset not early");check(state.tick(false,false,random,factory),"Reset fires on tick forty");}
    private static void rejects(CompoundNBT tag,String message){boolean rejected=false;try{DemonMerchantState.load(tag);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID buyer=UUID.randomUUID();
        for(String career:DemonTrades.careers()){
            DemonMerchantState state=new DemonMerchantState(career);check(state.level()==0 && state.offers().isEmpty(),"Lazy unopened merchant");
            check(DemonMerchantState.load(state.save()).career().equals(career),"Unopened round trip");
            state.open(new Random(0),false,SIMPLE);check(state.level()==1 && state.offers().size()==2,"Two initial offers");
            MerchantOffer original=state.offers().get(0);state.open(new Random(1),true,SIMPLE);check(state.offers().get(0)==original,"Reopening cannot reroll prices");
            for(int next=2;next<=5;next++){
                MerchantOffer offer=state.offers().get(state.offers().size()-1);
                DemonMerchantState.TradeResult result=state.completeTrade(offer,buyer,edge(false));
                check(result.accepted && result.scheduledReset && result.xp==8 && offer.getUses()==1,"First purchase grants XP and schedules reset");
                for(int i=0;i<45;i++)state.tick(true,false,edge(false),SIMPLE);
                check(state.resetTicks()==40,"Trading pauses reset countdown");
                CompoundNBT before=state.save();DemonMerchantState copy=DemonMerchantState.load(before);
                check(copy.resetTicks()==40 && buyer.equals(copy.lastBuyer()),"Pending reset and buyer persist");
                reset(copy,edge(false),SIMPLE);state=copy;
                check(state.level()==next && state.offers().size()==Math.min(next,4)*2+(next==5?1:0),"Exactly one level advanced");
                check(!state.tick(false,false,edge(false),SIMPLE),"Regeneration signal emitted once");
            }
            MerchantOffer last=state.offers().get(8);state.completeTrade(last,buyer,edge(false));reset(state,edge(false),SIMPLE);
            check(state.level()==5 && state.offers().size()==9,"Level five never appends duplicate offers");
            check(DemonMerchantState.load(state.save()).save().equals(state.save()),"Full state round trip");
        }
        for(boolean high:new boolean[]{false,true}){
            DemonMerchantState state=new DemonMerchantState("noble");state.open(new Random(0),false,SIMPLE);
            MerchantOffer spent=state.offers().get(0),unspent=state.offers().get(1);
            for(int i=0;i<7;i++)state.completeTrade(spent,buyer,edge(false));
            check(!state.completeTrade(spent,buyer,edge(false)).accepted && spent.getUses()==7,"Exhausted offer cannot be purchased");
            for(int i=0;i<39;i++)state.tick(false,false,edge(high),SIMPLE);state.tick(false,false,edge(high),SIMPLE);
            MerchantOffer restocked=state.offers().get(0);check(restocked.getUses()==7 && restocked.getMaxUses()==(high?19:9),"Restock adds two to twelve capacity without resetting uses");
            check(state.offers().get(1)==unspent && unspent.getMaxUses()==7,"Unexhausted stock unchanged");
            check(!state.completeTrade(spent,buyer,edge(false)).accepted,"Replaced stale offer rejected");
            MerchantOffer fake=new MerchantOffer(new ItemStack(Items.GOLD_INGOT),new ItemStack(Items.APPLE),7,0,0);check(!state.completeTrade(fake,buyer,edge(false)).accepted,"Foreign offer rejected");
        }
        DemonMerchantState state=new DemonMerchantState("despot");state.open(edge(false),false,SIMPLE);
        MerchantOffer offer=state.offers().get(0);check(state.completeTrade(offer,buyer,edge(true)).xp==11,"First-purchase maximum XP");
        UUID second=UUID.randomUUID();DemonMerchantState.TradeResult regular=state.completeTrade(offer,second,edge(true));check(regular.xp==6 && !regular.scheduledReset && buyer.equals(state.lastBuyer()),"Non-reset trade keeps last reset buyer");
        for(int i=0;i<20;i++)state.tick(false,false,edge(false),SIMPLE);
        check(state.resetTicks()==20,"Half reset elapsed");state.completeTrade(offer,second,edge(false));check(state.resetTicks()==40 && second.equals(state.lastBuyer()),"One-in-five reset restarts countdown");
        CompoundNBT saved=state.save();state.completeTrade(offer,buyer,edge(true));check(DemonMerchantState.load(saved).offers().get(0).getUses()==3,"Saved snapshot independent of later trades");
        for(int i=0;i<39;i++)state.tick(false,false,edge(false),SIMPLE);
        CompoundNBT before=state.save();boolean failed=false;
        try{state.tick(false,false,edge(false),(entry,r,d)->{throw new IllegalStateException("injected");});}catch(IllegalStateException ex){failed=true;}
        check(failed && state.save().equals(before),"Failed generation leaves reset and offers intact");
        check(state.tick(false,false,edge(false),SIMPLE),"Failed generation can be retried");
        AtomicInteger discounted=new AtomicInteger();DemonMerchantState discounts=new DemonMerchantState("noble");discounts.open(edge(false),false,SIMPLE);discounts.completeTrade(discounts.offers().get(0),buyer,edge(false));
        for(int i=0;i<40;i++)discounts.tick(false,true,edge(false),(entry,r,d)->{if(d)discounted.incrementAndGet();return SIMPLE.create(entry,r,d);});
        check(discounted.get()==2 && discounts.offers().get(0).getBaseCostA().getCount()==15 && discounts.offers().get(2).getBaseCostA().getCount()==7,"Only newly generated offers use current discount");
        KillContractItem item=new KillContractItem();DemonMerchantState contracts=new DemonMerchantState("noble");
        contracts.open(edge(false),false,(entry,r,d)->new MerchantOffer(new ItemStack(Items.GOLD_INGOT),KillContractItem.task(item,CurseData.Kind.BERSERKER,6),1,0,0));
        MerchantOffer contract=contracts.offers().get(0);contracts.completeTrade(contract,buyer,edge(false));for(int i=0;i<40;i++)contracts.tick(false,false,edge(false),SIMPLE);
        check(contracts.offers().get(0)==contract && contract.isOutOfStock() && contract.getMaxUses()==1,"Contract never restocks");
        CompoundNBT valid=state.save();for(int i=0;i<10;i++){
            CompoundNBT bad=valid.copy();switch(i){case 0:bad.putInt("version",2);break;case 1:bad.putInt("careerId",0);break;case 2:bad.putInt("careerLevel",6);break;case 3:bad.putInt("resetTicks",41);break;case 4:bad.putString("lastBuyer","bad");break;case 5:bad.remove("recipeList");break;case 6:bad.getCompound("recipeList").getList("Recipes",10).remove(0);break;case 7:bad.getCompound("recipeList").getList("Recipes",10).getCompound(0).putInt("uses",-1);break;case 8:bad.getCompound("recipeList").getList("Recipes",10).getCompound(0).putInt("maxUses",0);break;default:bad.getCompound("recipeList").getList("Recipes",10).getCompound(0).putFloat("priceMultiplier",Float.NaN);}
            rejects(bad,"Malformed merchant state rejected");
        }
        DemonMerchantState noXp=new DemonMerchantState("noble");noXp.open(edge(false),false,(entry,r,d)->{CompoundNBT n=SIMPLE.create(entry,r,d).createTag();n.putBoolean("rewardExp",false);return new MerchantOffer(n);});
        DemonMerchantState.TradeResult noXpResult=noXp.completeTrade(noXp.offers().get(0),buyer,edge(false));check(noXpResult.accepted && noXpResult.scheduledReset && noXpResult.xp==0,"No-XP flag suppresses reward without suppressing progression");
        DemonMerchantState saturated=new DemonMerchantState("noble");saturated.open(edge(false),false,SIMPLE);CompoundNBT huge=saturated.save();
        CompoundNBT first=huge.getCompound("recipeList").getList("Recipes",10).getCompound(0);first.putInt("uses",Integer.MAX_VALUE-1);first.putInt("maxUses",Integer.MAX_VALUE);saturated=DemonMerchantState.load(huge);
        saturated.completeTrade(saturated.offers().get(0),buyer,edge(false));for(int i=0;i<40;i++)saturated.tick(false,false,edge(true),SIMPLE);
        check(saturated.offers().get(0).getMaxUses()==Integer.MAX_VALUE && saturated.offers().get(0).isOutOfStock(),"Restock capacity saturates without integer overflow");
        CompoundNBT wrongList=new DemonMerchantState("noble").save();ListNBT strings=new ListNBT();strings.add(StringNBT.valueOf("bad"));wrongList.getCompound("recipeList").put("Recipes",strings);rejects(wrongList,"Wrong list element type rejected even while unopened");
        Set<String> careers=new HashSet<>();for(int i=0;i<3;i++){final int choice=i;careers.add(DemonMerchantState.random(new Random(0){@Override public int nextInt(int bound){check(bound==3,"Three-way career draw");return choice;}}).career());}check(careers.size()==3,"Every career reachable");
        System.out.println("PASS: "+checks+" demon merchant lifecycle checks");
    }
}