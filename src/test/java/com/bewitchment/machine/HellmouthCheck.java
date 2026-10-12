package com.bewitchment.machine;

import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class HellmouthCheck {
    private static int checks;
    private static void check(boolean value,String message) { checks++; if(!value)throw new AssertionError(message); }
    // Bound and creature selection use the same bound, so distinguish them by call order.
    private static Random rolls(int... limits) {
        return new Random(0) {
            int call;
            @Override public int nextInt(int bound) {
                if(bound!=5)throw new AssertionError("Unexpected random bound");
                int current=call++;
                return current%2==0 ? limits[Math.min(current/2,limits.length-1)] : 3;
            }
        };
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        String[] expected={"feuerwurm","feuerwurm","blaze","cambion","blaze"};
        for(int i=0;i<5;i++)check(Hellmouth.id(i).equals(expected[i]),"Original weighted slot "+i);
        for(int invalid:new int[]{-1,5}) {
            boolean rejected=false;try{Hellmouth.id(invalid);}catch(IllegalArgumentException e){rejected=true;}
            check(rejected,"Reject invalid slot "+invalid);
        }
        int[] histogram=new int[7];
        for(int a=0;a<5;a++)for(int b=0;b<5;b++)for(int c=0;c<5;c++)for(int d=0;d<5;d++) {
            histogram[Hellmouth.plan(rolls(0,0,a,b,c,d,0),i->i,(e,p)->true).size()]++;
        }
        check(Arrays.equals(histogram,new int[]{0,0,125,200,180,96,24}),"Exact 625-case count distribution");
        int[] created={0},attempts={0};
        check(Hellmouth.plan(rolls(4),i->{created[0]++;return i;},(e,p)->{attempts[0]++;return false;}).isEmpty()
                && created[0]==6 && attempts[0]==96,"Failed placements stop after six times sixteen attempts");
        created[0]=0;attempts[0]=0;
        Hellmouth.plan(rolls(4),i->{created[0]++;return null;},(e,p)->{attempts[0]++;return true;});
        check(created[0]==6 && attempts[0]==0,"Null creations skipped without redraw");
        attempts[0]=0;
        check(Hellmouth.plan(rolls(0),i->i,(e,p)->++attempts[0]%16==0).size()==2 && attempts[0]==32,"Sixteenth placement attempt accepted");
        List<Integer> reservations=new ArrayList<>();
        Hellmouth.plan(rolls(4),i->i,(e,p)->{reservations.add(p.size());return true;});
        check(reservations.equals(Arrays.asList(0,1,2,3,4,5)),"Every body sees earlier reservations");
        boolean[] immutable={false};
        Hellmouth.plan(rolls(0),i->i,(e,p)->{try{p.add(99);}catch(UnsupportedOperationException ex){immutable[0]=true;}return true;});
        check(immutable[0],"Reservations cannot be mutated by placement");
        List<Integer> advances=new ArrayList<>();
        check(CallOfTheWild.insert(Arrays.asList(1,2,3),e->e==2,advances::add)==1 && advances.equals(Collections.singletonList(2)),"Shared insertion awards only successful member");
        advances.clear();
        check(CallOfTheWild.insert(Arrays.asList(1,2),e->false,advances::add)==0 && advances.isEmpty(),"All rejected yields zero and no advancement");
        check(LesserHellmouth.failure(true,false).equals("hellmouth_peaceful"),"Peaceful activation rejected");
        check(LesserHellmouth.failure(false,true).equals("no_rain"),"Rain activation rejected");
        check(LesserHellmouth.failure(false,false)==null,"Dry non-Peaceful accepted");
        RitualEffect effect=RitualEffect.parse("hellmouth");
        check(effect==RitualEffect.HELLMOUTH && !effect.consumesOnStart() && !effect.conjuration() && !effect.continuous(),"Finite wave effect");
        check(effect.failure(false,true)==null && effect.failure(true,false)==null,"No day or dimension restriction");
        RitualClock clock=new RitualClock();int[] cost={0};boolean early=false;
        for(int i=0;i<299;i++)early|=clock.advance(15,45,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED;
        check(!early && cost[0]==630,"No early completion");
        check(clock.advance(15,45,c->false)==RitualClock.Step.NO_POWER,"Last failed payment blocks wave");
        check(clock.advance(15,45,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED && cost[0]==675 && 1250+cost[0]==1925,"Fifteen paid seconds and exact total");
        RitualInventory inventory=new RitualInventory(()->{});
        ItemStack original=new ItemStack(Items.IRON_SWORD);original.setDamageValue(51);original.getOrCreateTag().putString("marker","offering");
        inventory.insertItem(0,original,false);inventory.insertItem(1,new ItemStack(Items.CHARCOAL),false);
        List<ItemStack> refund=inventory.takeAll();
        check(refund.size()==2 && refund.get(0).getDamageValue()==51 && refund.get(0).getTag().getString("marker").equals("offering"),"Original tool data retained for all-failure refund");
        check(inventory.takeAll().isEmpty(),"Settled storage cannot refund twice");
        System.out.println("PASS: "+checks+" Hellmouth pool, exact count distribution, bounded placement, insertion, refunds and payment checks.");
    }
}
