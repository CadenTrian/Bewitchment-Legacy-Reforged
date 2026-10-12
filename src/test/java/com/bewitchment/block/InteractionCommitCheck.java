package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class InteractionCommitCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(boolean eligible:new boolean[]{false,true})for(boolean client:new boolean[]{false,true})for(boolean accepts:new boolean[]{false,true}){
            List<String> actions=new ArrayList<>();
            ActionResultType result=BlockInteractionCommit.apply(eligible,client,()->{actions.add("change");return accepts;},()->actions.add("inventory"));
            if(!eligible)check(result==ActionResultType.PASS&&actions.isEmpty(),"Ineligible use does not read or mutate world");
            else if(client)check(result==ActionResultType.SUCCESS&&actions.isEmpty(),"Client predicts without inventory changes");
            else if(!accepts)check(result==ActionResultType.FAIL&&actions.equals(Collections.singletonList("change")),"Failed mutation gives no reward or tool wear");
            else check(result==ActionResultType.CONSUME&&actions.equals(Arrays.asList("change","inventory")),"One world commit before one inventory action");
        }
        try{BlockInteractionCommit.apply(true,false,()->{throw new IllegalStateException("denied");},()->{throw new AssertionError("Paid after throwing mutation");});throw new AssertionError("Exception lost");}catch(IllegalStateException expected){check(true,"Throwing world mutation cannot award items");}
        for(int count:new int[]{1,2,16,64})for(boolean creative:new boolean[]{false,true})for(boolean space:new boolean[]{false,true}){
            ItemStack hand=new ItemStack(Items.GLASS_BOTTLE,count), otherHand=new ItemStack(Items.STICK,7), filled=new ItemStack(Items.POTION);
            List<String> actions=new ArrayList<>();List<ItemStack> received=new ArrayList<>();
            BottledFireCollection.deliver(hand,filled,creative,s->{actions.add("hand");received.add(s);},s->{actions.add("inventory");if(space)received.add(s);return space;},s->{actions.add("drop");received.add(s);});
            check(hand.getCount()==(creative?count:count-1)&&otherHand.getCount()==7,"Only selected hand pays, creative retains bottles");
            check(received.size()==1&&received.get(0)==filled&&filled.getCount()==1,"Exactly one filled bottle delivered");
            check(actions.equals(!creative&&count==1?Collections.singletonList("hand"):space?Collections.singletonList("inventory"):Arrays.asList("inventory","drop")),"Empty hand, inventory and full-inventory routes");
        }
        for(int count:new int[]{1,2,16,64})for(boolean accepts:new boolean[]{false,true}){
            ItemStack ore=new ItemStack(Items.IRON_ORE,count),output=new ItemStack(Items.IRON_INGOT);
            ore.getOrCreateTag().putString("marker","preserved");
            int[] calls={0};
            boolean result=FrostfireConversion.convert(ore,output,s->{calls[0]++;check(ore.getCount()==count-1,"Input reserved before insertion callback");check(s!=output&&s.getItem()==Items.IRON_INGOT&&s.getCount()==1,"Output handed off as independent single ingot");return accepts;});
            check(result==accepts&&calls[0]==1&&ore.getCount()==count-(accepts?1:0),"Accepted output consumes one; canceled insertion restores ore");
            check(ore.getTag().getString("marker").equals("preserved"),"Input metadata retained");
        }
        for(int count:new int[]{1,64}){
            ItemStack ore=new ItemStack(Items.IRON_ORE,count);
            try{FrostfireConversion.convert(ore,new ItemStack(Items.IRON_INGOT),s->{throw new IllegalStateException("callback");});throw new AssertionError("Exception lost");}catch(IllegalStateException expected){check(ore.getCount()==count&&ore.getItem()==Items.IRON_ORE,"Throwing insertion restores even last ore");}
        }
        check(!FrostfireConversion.convert(ItemStack.EMPTY,new ItemStack(Items.IRON_INGOT),s->{throw new AssertionError("Empty source insertion");}),"Empty ore cannot produce ingots");
        ItemStack ore=new ItemStack(Items.IRON_ORE,1);
        check(!FrostfireConversion.convert(ore,ItemStack.EMPTY,s->true)&&ore.getCount()==1,"Empty output cannot consume ore");
        check(FrostfireConversion.convert(ore,new ItemStack(Items.IRON_INGOT),s->{check(!FrostfireConversion.convert(ore,s,n->{throw new AssertionError("Recursive duplication");}),"Reentrant callback cannot reuse reserved last ore");return true;})&&ore.isEmpty(),"One last ore produces at most one ingot");
        DragonsBloodLog log=new DragonsBloodLog();
        check(!log.defaultBlockState().getValue(DragonsBloodLog.NATURAL)&&!log.defaultBlockState().getValue(DragonsBloodLog.SLASHED),"Placed logs retain unproductive defaults");
        for(BlockState state:log.getStateDefinition().getPossibleStates()){
            BlockState slashed=state.setValue(DragonsBloodLog.SLASHED,true);
            check(slashed.getValue(RotatedPillarBlock.AXIS)==state.getValue(RotatedPillarBlock.AXIS)&&slashed.getValue(DragonsBloodLog.NATURAL)==state.getValue(DragonsBloodLog.NATURAL),"Slashing preserves axis and natural marker");
        }
        System.out.println("PASS: "+checks+" interaction commit checks");
    }
}