package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class SentinelSigilCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int roll=0;roll<3;roll++){
            Set<Object> identities=Collections.newSetFromMap(new IdentityHashMap<>());
            int spawned=SentinelSigil.spawn(roll,Object::new,e->{check(identities.add(e),"Fresh entity for each insertion");return true;});
            check(spawned==roll+1 && identities.size()==roll+1,"Exact one-to-three count");
        }
        AtomicInteger attempts=new AtomicInteger();
        check(SentinelSigil.spawn(2,()->{attempts.incrementAndGet();return new Object();},e->false)==0 && attempts.get()==3,"Rejected insertions are bounded without retries");
        attempts.set(0);
        check(SentinelSigil.spawn(2,()->null,e->{attempts.incrementAndGet();return true;})==0 && attempts.get()==0,"Failed creation never reaches insertion");
        attempts.set(0);
        check(SentinelSigil.spawn(2,Object::new,e->attempts.incrementAndGet()!=2)==2 && attempts.get()==3,"One failed insertion does not suppress other fresh candidates");
        check(SentinelSigil.count(Integer.MIN_VALUE)==1 && SentinelSigil.count(Integer.MAX_VALUE)==3,"Malformed count remains bounded");
        AxisAlignedBB box=new AxisAlignedBB(-1,0,-1,1,256,1);
        check(SentinelSigil.fits(box,-1,1,-1,1),"Full box may touch border and height bounds");
        check(!SentinelSigil.fits(box.move(0,-.01,0),-1,1,-1,1),"Reject below world");
        check(!SentinelSigil.fits(box.move(0,.01,0),-1,1,-1,1),"Reject above world");
        for(double dx:new double[]{-.01,.01})check(!SentinelSigil.fits(box.move(dx,0,0),-1,1,-1,1),"Reject X border crossing");
        for(double dz:new double[]{-.01,.01})check(!SentinelSigil.fits(box.move(0,0,dz),-1,1,-1,1),"Reject Z border crossing");
        check(!SentinelSigil.fits(new AxisAlignedBB(Double.NaN,1,1,2,2,2),-10,10,-10,10),"Reject nonfinite position");
        check(!Kind.SENTINEL.positive && Kind.SENTINEL.cooldown==600,"Legacy negative targeting and cooldown");
        check(Kind.fromId("bewitchment:sigil_sentinel")==Kind.SENTINEL,"Persisted identity");
        check(Kind.SENTINEL.effects().isEmpty(),"Summoning adds no placeholder potion");
        System.out.println("PASS: "+checks+" Sentinel summon checks");
    }
}
