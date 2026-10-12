package com.bewitchment.machine;

import com.bewitchment.item.SealedEvilBoxItem;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class SealedBoxCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random edge(boolean high){return new Random(0){@Override public int nextInt(int bound){return high?bound-1:0;}@Override public boolean nextBoolean(){return high;}};}
    private static int count(List<SealedBoxPlan.Gift> list,String id){return list.stream().filter(g->g.id.equals(id)).mapToInt(g->g.count).sum();}
    public static void main(String[] args){
        net.minecraft.util.registry.Bootstrap.bootStrap();
        for(int i=0;i<26;i++){
            final int choice=i;Random rng=new Random(0){boolean first=true;@Override public int nextInt(int bound){if(first){first=false;check(bound==26,"Uniform 26-way draw");return choice;}return 0;}};
            SealedBoxPlan plan=SealedBoxPlan.random(rng);check(plan.outcome==i,"Every outcome reachable");
            check(plan.gifts.stream().allMatch(g->g.count>0 && g.count<=64) && plan.spawns.stream().allMatch(g->g.count>0 && g.count<=19),"Bounded positive item and creature counts");
        }
        for(boolean high:new boolean[]{false,true}){
            SealedBoxPlan bats=SealedBoxPlan.create(6,edge(high));check(count(bats.spawns,"minecraft:bat")== (high?19:10) && count(bats.spawns,"bewitchment:raven")== (high?9:0),"Linked bat and raven counts");
            SealedBoxPlan meat=SealedBoxPlan.create(9,edge(high));check(count(meat.gifts,"minecraft:chicken")== (high?64:1) && count(meat.gifts,"minecraft:mutton")== (high?64:1),"Meat boundaries");
            check(SealedBoxPlan.create(11,edge(high)).gifts.stream().allMatch(g->g.count==(high?2:1)),"Four demonic ingredients one to two each");
            for(int outcome:new int[]{12,13,16,17})check(SealedBoxPlan.create(outcome,edge(high)).spawns.get(0).count==(high?3:1),"One to three hostile creatures");
            for(int outcome:new int[]{18,23,24})check(SealedBoxPlan.create(outcome,edge(high)).spawns.get(0).count==(high?2:1),"One to two hostile creatures");
            SealedBoxPlan swarm=SealedBoxPlan.create(25,edge(high));check(swarm.spawns.size()==2 && swarm.spawns.stream().allMatch(g->g.count==(high?4:1)),"Equal spider and silverfish counts");
            check(SealedBoxPlan.create(14,edge(high)).weatherTicks==(high?17980:6000),"Five to just under fifteen minutes of weather");
            check(SealedBoxPlan.create(8,edge(high)).gifts.get(0).id.equals(high?"bewitchment:nether_brick_moloch_statue":"bewitchment:scorned_brick_leonard_statue"),"Statue material and deity boundaries");
            check(SealedBoxPlan.create(10,edge(high)).gifts.size()==5,"Five plant gifts");
        }
        Set<String> heads=new HashSet<>();for(int i=0;i<6;i++){final int head=i;heads.add(SealedBoxPlan.create(7,new Random(0){@Override public int nextInt(int b){return head;}}).gifts.get(2).id);}check(heads.size()==6 && heads.contains("minecraft:dragon_head") && heads.contains("minecraft:player_head"),"All six legacy skull variants");
        check(count(SealedBoxPlan.create(7,edge(false)).gifts,"bewitchment:bottle_of_blood")==1 && count(SealedBoxPlan.create(7,edge(false)).gifts,"bewitchment:heart")==1,"Contract bundle fixed ingredients");
        int[][] expected={{2,24000,1,24000,0},{3,24000,1,24000,0},{19,1200,0,24000,0},{20,800,0},{21,24000,0}};
        for(int[] row:expected){SealedBoxPlan p=SealedBoxPlan.create(row[0],edge(false));check(p.potions.size()==(row.length-1)/2,"Potion count");for(int i=0;i<p.potions.size();i++)check(p.potions.get(i).ticks==row[i*2+1] && p.potions.get(i).amplifier==row[i*2+2],"Legacy potion duration and amplifier");}
        for(long time:new long[]{0,17600,23999,24000,41600,-1}){long next=SealedBoxPlan.midnight(time);check(next>=time && next-time<24000 && Math.floorMod(next,24000)==17600,"Advance only to legacy night time");}
        AtomicInteger attempts=new AtomicInteger();check(!SealedBoxPlan.place(()->{attempts.incrementAndGet();return false;}) && attempts.get()==16,"Blocked placements terminate after sixteen attempts");
        attempts.set(0);check(SealedBoxPlan.place(()->attempts.incrementAndGet()==16) && attempts.get()==16,"Last placement attempt accepted");
        attempts.set(0);check(SealedBoxPlan.place(()->{attempts.incrementAndGet();return true;}) && attempts.get()==1,"Successful placement does not repeat");
        for(int bits=0;bits<8;bits++)check(SealedEvilBoxItem.usable((bits&1)!=0,(bits&2)!=0,(bits&4)!=0)==(bits==1),"Alive non-spectator non-sneaking use");
        for(int bad:new int[]{-1,26,Integer.MAX_VALUE}){boolean rejected=false;try{SealedBoxPlan.create(bad,edge(false));}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Invalid outcomes rejected");}
        boolean immutable=false;try{SealedBoxPlan.create(7,edge(false)).gifts.clear();}catch(UnsupportedOperationException ex){immutable=true;}check(immutable,"Plan cannot be changed after drawing");
        System.out.println("PASS: "+checks+" sealed box checks");
    }
}