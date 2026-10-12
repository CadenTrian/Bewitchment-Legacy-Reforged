package com.bewitchment.machine;
import java.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;

public final class CrystalBallCheck {
    private static int checks;
    private static void check(boolean result,String reason){if(!result)throw new AssertionError(reason);checks++;}
    private static class Access implements CrystalBallReading.Access {
        boolean eligible=true,taglock,pending;int balance=750,payments,assignments;Runnable onPay=()->{};
        public boolean eligible(){return eligible;}public boolean taglock(){return taglock;}public boolean pending(){return pending;}
        public boolean pay(int amount){payments++;check(amount==750,"Original reading cost");onPay.run();if(balance<amount)return false;balance-=amount;return true;}
        public void assign(){assignments++;pending=true;}
    }
    public static void main(String[] args) {
        net.minecraft.util.registry.Bootstrap.bootStrap();UUID id=UUID.randomUUID();
        for(int mask=0;mask<16;mask++) {
            Access a=new Access();a.eligible=(mask&1)!=0;a.taglock=(mask&2)!=0;a.pending=(mask&4)!=0;a.balance=(mask&8)!=0?750:749;
            int before=a.balance;CrystalBallReading.Result expected=!a.eligible?CrystalBallReading.Result.UNAVAILABLE:a.taglock?CrystalBallReading.Result.TAGLOCK:a.pending?CrystalBallReading.Result.EXISTING:a.balance<750?CrystalBallReading.Result.NO_POWER:CrystalBallReading.Result.RECEIVED;
            boolean attempts=a.eligible&&!a.taglock&&!a.pending;
            check(CrystalBallReading.read(id,a)==expected,"Eligibility, taglock, existing and power precedence");
            check(a.payments==(attempts?1:0),"Invalid readings never attempt payment");
            check(a.assignments==(expected==CrystalBallReading.Result.RECEIVED?1:0),"Only paid readings assign");
            check(a.balance==before-(expected==CrystalBallReading.Result.RECEIVED?750:0),"Payment is all-or-nothing");
        }
        for(int balance:new int[]{0,1,748,749,750,751,1500,Integer.MAX_VALUE}) {
            Access a=new Access();a.balance=balance;
            check(CrystalBallReading.read(id,a)==(balance>=750?CrystalBallReading.Result.RECEIVED:CrystalBallReading.Result.NO_POWER),"Exact cost boundary");
            if(balance>=750){check(CrystalBallReading.read(id,a)==CrystalBallReading.Result.EXISTING&&a.payments==1&&a.assignments==1,"Repeat reading cannot pay or assign again");}
        }
        Set<FortuneData.Kind> kinds=new HashSet<>();for(int roll=0;roll<FortuneData.Kind.values().length;roll++)kinds.add(CrystalBallReading.choose(roll));
        check(kinds.size()==FortuneData.Kind.values().length&&kinds.containsAll(Arrays.asList(FortuneData.Kind.values())),"Every restored fortune selectable once");
        Map<FortuneData.Kind,Integer> weights=new EnumMap<>(FortuneData.Kind.class);
        for(int roll=0;roll<FortuneData.Kind.values().length*100;roll++)weights.merge(CrystalBallReading.choose(roll),1,Integer::sum);
        for(FortuneData.Kind kind:FortuneData.Kind.values())check(weights.get(kind)==100,"All available fortunes have equal weight");
        for(int roll:new int[]{Integer.MIN_VALUE,-7,-1,0,5,6,12,Integer.MAX_VALUE})check(kinds.contains(CrystalBallReading.choose(roll)),"Selection handles every integer roll");
        Access recursive=new Access();recursive.onPay=()->check(CrystalBallReading.read(id,recursive)==CrystalBallReading.Result.BUSY,"Same player reentry blocked before payment");
        check(CrystalBallReading.read(id,recursive)==CrystalBallReading.Result.RECEIVED&&recursive.payments==1&&recursive.assignments==1,"One charge and assignment under reentry");
        Access independent=new Access(),other=new Access();independent.onPay=()->check(CrystalBallReading.read(UUID.randomUUID(),other)==CrystalBallReading.Result.RECEIVED,"Different player reading independent");
        check(CrystalBallReading.read(id,independent)==CrystalBallReading.Result.RECEIVED,"Outer reading completes");
        Access changed=new Access();changed.onPay=()->changed.pending=true;
        check(CrystalBallReading.read(id,changed)==CrystalBallReading.Result.EXISTING&&changed.assignments==0,"Payment callback cannot overwrite a new fortune");
        Access failure=new Access();failure.onPay=()->{throw new IllegalStateException("test");};
        try{CrystalBallReading.read(id,failure);throw new AssertionError("Payment exception swallowed");}catch(IllegalStateException expected){checks++;}
        failure.onPay=()->{};check(CrystalBallReading.read(id,failure)==CrystalBallReading.Result.RECEIVED,"Guard released after payment exception");
        com.bewitchment.block.CrystalBallBlock block=new com.bewitchment.block.CrystalBallBlock();
        AxisAlignedBB box=block.getShape(block.defaultBlockState(),null,BlockPos.ZERO,ISelectionContext.empty()).bounds();
        check(box.minX==3/16.0&&box.minY==0&&box.minZ==3/16.0&&box.maxX==13/16.0&&box.maxY==12/16.0&&box.maxZ==13/16.0,"Original crystal ball bounds");
        System.out.println("PASS: "+checks+" Crystal Ball payment, eligibility, repeat reading, selection, reentry and shape checks");
    }
}
