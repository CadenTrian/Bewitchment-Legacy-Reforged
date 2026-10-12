package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class ParanoiaCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();CurseData data=new CurseData();
        check(!data.paranoiaTick(0) && data.paranoiaDelay()==1150,"Inactive state does not advance");
        data.add(Kind.PARANOIA,1);
        check(!Kind.PARANOIA.lesser() && !Kind.PARANOIA.positive() && Kind.PARANOIA.chance==1,"Greater negative every-tick curse");
        for(int i=0;i<1149;i++)if(data.paranoiaTick(0))throw new AssertionError("Early sound");
        check(data.paranoiaDelay()==1,"Full initial countdown");
        check(data.paranoiaTick(9) && data.paranoiaDelay()==1150,"Sound allowed on final countdown tick and resets delay");
        for(int i=0;i<1150;i++)data.paranoiaTick(10);
        check(data.paranoiaDelay()==0,"Failed boundary roll leaves ready timer");
        check(!data.paranoiaTick(10) && !data.paranoiaTick(-1) && !data.paranoiaTick(100),"Ten-percent roll range enforced");
        check(data.paranoiaTick(0),"Later successful roll plays without another countdown");
        data.paranoiaTick(99);CompoundNBT saved=data.save();CurseData copy=new CurseData();copy.load(saved);
        check(copy.paranoiaDelay()==1149 && copy.remaining(Kind.PARANOIA)==24000,"Duration and sound delay persist together");
        copy.paranoiaTick(99);check(data.paranoiaDelay()==1149 && copy.paranoiaDelay()==1148,"Players never share a countdown");
        check(!copy.add(Kind.PARANOIA,2) && copy.paranoiaDelay()==1148,"Duplicate application does not reset sound timer");
        check(copy.purify(()->{throw new AssertionError("Purity roll");})==0 && copy.paranoiaDelay()==1148,"Purity leaves greater curse and delay intact");
        copy.remove(Kind.PARANOIA);check(!copy.save().contains("paranoiaDelay") && !copy.paranoiaTick(0),"Removal omits delay and stops sounds");
        copy.add(Kind.PARANOIA,1);check(copy.paranoiaDelay()==1150,"Fresh application resets delay");
        saved.putInt("paranoiaDelay",Integer.MAX_VALUE);copy.load(saved);check(copy.paranoiaDelay()==1150,"Oversized saved delay clamps");
        saved.putInt("paranoiaDelay",-1);copy.load(saved);check(copy.paranoiaDelay()==0,"Negative saved delay clamps");
        saved.remove("paranoiaDelay");copy.load(saved);check(copy.paranoiaDelay()==1150,"Missing delay receives initial value");
        saved.putInt(Kind.PARANOIA.id,20);copy.load(saved);copy.second();check(!copy.paranoiaTick(0) && !copy.save().contains("paranoiaDelay"),"Expiry stops sounds and drops delay metadata");
        EffectInstance blindness=ParanoiaCurse.blindness();check(blindness.getEffect()==Effects.BLINDNESS && blindness.getDuration()==256 && blindness.getAmplifier()==3,"Original Blindness IV for 256 ticks");
        check(blindness!=ParanoiaCurse.blindness() && blindness.isVisible() && !blindness.isAmbient(),"Fresh effect and original display flags");
        Set<ResourceLocation> sounds=new HashSet<>();
        for(int i=0;i<35;i++){ResourceLocation sound=ParanoiaCurse.sound(i);sounds.add(sound);check(sound.getNamespace().equals("minecraft") || sound.getNamespace().equals("bewitchment"),"Sound choice identity "+i);}
        check(ParanoiaCurse.soundCount()==35 && sounds.size()==34,"Thirty-five choices preserve duplicate glass weighting");
        check(ParanoiaCurse.sound(14).equals(ParanoiaCurse.sound(32)),"Original repeated glass break");
        check(ParanoiaCurse.sound(3).toString().equals("bewitchment:werewolf_howl") && ParanoiaCurse.sound(23).toString().equals("bewitchment:chalk_scribble"),"Original howl and chalk choices");
        check(ParanoiaCurse.sound(24).toString().equals("bewitchment:cleaver_living") && ParanoiaCurse.sound(25).toString().equals("bewitchment:bafometyr_living"),"Original demon sound choices");
        check(ParanoiaCurse.sound(34).toString().equals("minecraft:entity.enderman.stare"),"Legacy default choice");
        check(ParanoiaCurse.sound(-1).equals(ParanoiaCurse.sound(34)) && ParanoiaCurse.sound(35).equals(ParanoiaCurse.sound(0)),"Out-of-range selector remains bounded");
        System.out.println("PASS: "+checks+" Paranoia checks");
    }
}
