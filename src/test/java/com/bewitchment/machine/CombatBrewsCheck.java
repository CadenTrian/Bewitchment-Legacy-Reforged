package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.potion.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import java.util.concurrent.atomic.AtomicInteger;

public final class CombatBrewsCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    public static void main(String[] args)throws Exception{
        Bootstrap.bootStrap();
        for(int flags=0;flags<4;flags++)for(float damage:new float[]{-1,0,1,100,Float.NaN,Float.POSITIVE_INFINITY}){
            boolean magic=(flags&1)!=0,projectile=(flags&2)!=0;
            check(CombatBrewRules.trigger(damage,magic,projectile)==((damage==1||damage==100)&&flags!=2),"Magic or non-projectile positive damage only");
        }
        Vector3d motion=new Vector3d(1,-2,.5);
        for(int amp:new int[]{0,1,2,255}){
            Vector3d result=CombatBrewRules.deflect(motion,amp);
            check(result.x==4D*amp&&result.y==-8D*amp&&result.z==2D*amp,"Legacy multiplier preserves direction");
            check(CombatBrewRules.blast(amp)==8F*(amp+1),"Eight blast damage per level");
        }
        check(CombatBrewRules.deflect(motion,0).lengthSqr()==0,"Level one stops motion, not reversal");
        check(CombatBrewRules.deflect(motion,-1).lengthSqr()==0&&CombatBrewRules.deflect(motion,Integer.MAX_VALUE).x==1020,"Bounded malformed amplifier");
        check(CombatBrewRules.blast(-1)==8&&CombatBrewRules.blast(Integer.MAX_VALUE)==2048,"Bounded damage arithmetic");
        for(int flags=0;flags<16;flags++)check(CombatBrewRules.owner((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0)==(flags==11),"Player/alive/spectator/world gate combinations");
        check(CombatBrewRules.friendship(true,"example.Wolf")&&!CombatBrewRules.friendship(false,"example.Wolf"),"Only tameable creatures");
        check(!CombatBrewRules.friendship(true,"example.EntityDragon")&&!CombatBrewRules.friendship(true,"example.DragonPet"),"Legacy case-sensitive Dragon exclusion");
        check(CombatBrewRules.friendship(true,"example.dragon_pet"),"Class-name compatibility rule retained exactly");
        AtomicInteger pulses=new AtomicInteger();
        check(CombatBrewRules.detonate(()->{pulses.incrementAndGet();check(!CombatBrewRules.detonate(pulses::incrementAndGet),"Nested damage cannot detonate again");}),"Outer pulse runs");
        check(pulses.get()==1,"Exactly one pulse");
        check(CombatBrewRules.detonate(pulses::incrementAndGet)&&pulses.get()==2,"Later independent pulse allowed");
        boolean threw=false;try{CombatBrewRules.detonate(()->{throw new IllegalStateException("callback");});}catch(IllegalStateException ex){threw=true;}
        check(threw&&CombatBrewRules.detonate(pulses::incrementAndGet)&&pulses.get()==3,"Exception clears reentrancy guard");
        AtomicInteger other=new AtomicInteger();
        CombatBrewRules.detonate(()->{Thread thread=new Thread(()->CombatBrewRules.detonate(other::incrementAndGet));thread.start();try{thread.join();}catch(InterruptedException ex){throw new RuntimeException(ex);}});
        check(other.get()==1,"Guard is isolated across server threads");
        Effect deflect=CombatBrewRules.deflection(),volatileEffect=CombatBrewRules.volatility();
        check(deflect.getColor()==0xc000ff&&deflect.isBeneficial()&&!deflect.isInstantenous(),"Deflection metadata");
        check(volatileEffect.getColor()==0xff6000&&!volatileEffect.isBeneficial()&&!volatileEffect.isInstantenous(),"Volatility metadata");
        InfluenceBrewEffect rage=new InfluenceBrewEffect(false),friend=new InfluenceBrewEffect(true);
        check(rage.getColor()==0x660000&&!rage.isBeneficial()&&rage.isInstantenous(),"Rage metadata");
        check(friend.getColor()==0xF4C2C2&&friend.isBeneficial()&&friend.isInstantenous(),"Friendship metadata");
        // With no owner, ticking has no influence action and does not dereference a target.
        rage.applyEffectTick(null,0);friend.applyEffectTick(null,0);check(true,"Ownerless command tick does not invent a player");
        System.out.println("PASS: "+checks+" combat brew trigger, velocity, owner, metadata and recursive-damage guards");
    }
}
