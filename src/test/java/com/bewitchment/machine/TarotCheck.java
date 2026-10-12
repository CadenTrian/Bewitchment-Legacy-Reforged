package com.bewitchment.machine;
import java.util.*;
import net.minecraft.nbt.CompoundNBT;

public final class TarotCheck {
    private static int checks;
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);checks++;}
    private static class Access implements TarotReading.Access {
        boolean eligible=true,online=true,shrouded;UUID target=UUID.randomUUID();int power=1000,payments,reveals,shroudChecks;
        Runnable onPay=()->{},onReveal=()->{};
        public boolean eligible(){return eligible;}public UUID target(){return target;}public boolean online(){return online;}
        public boolean pay(int amount){check(amount==1000,"Original tarot cost");payments++;onPay.run();if(power<amount)return false;power-=amount;return true;}
        public boolean shrouded(){shroudChecks++;return shrouded;}public String reveal(){reveals++;onReveal.run();return "nothing";}
    }
    public static void main(String[] args){
        UUID reader=UUID.randomUUID(),target=UUID.randomUUID();CompoundNBT tag=new CompoundNBT();
        check(TarotReading.boundId(null)==null&&TarotReading.boundId(tag)==null,"Missing binding rejected");
        for(String value:new String[]{"","invalid","1-1-1-1-1",target+" "," "+target,target.toString().replace("-",""),"00000000-0000-0000-0000-00000000000z"}){tag.putString("boundId",value);check(TarotReading.boundId(tag)==null,"Malformed and noncanonical UUID rejected");}
        tag.putInt("boundId",42);check(TarotReading.boundId(tag)==null,"Wrong NBT type rejected");
        for(String value:new String[]{target.toString(),target.toString().toUpperCase(Locale.ROOT)}){tag.putString("boundId",value);tag.putString("boundName","forged display name");check(target.equals(TarotReading.boundId(tag)),"Canonical binding accepted independent of stored name");}
        String[] armor={"bewitchment:cold_iron_helmet","bewitchment:cold_iron_chestplate","bewitchment:cold_iron_leggings","bewitchment:cold_iron_boots"};
        for(int mask=0;mask<16;mask++){String[] parts=armor.clone();for(int i=0;i<4;i++)if((mask&(1<<i))==0)parts[i]=null;check(TarotReading.shrouded(parts)==(mask==15),"Only full worn set shrouds");}
        for(int i=0;i<4;i++){String[] parts=armor.clone();parts[i]=parts[i].replace("bewitchment:","other:");check(!TarotReading.shrouded(parts),"Wrong namespace rejected");}
        check(!TarotReading.shrouded(armor[1],armor[0],armor[2],armor[3])&&!TarotReading.shrouded(armor[0]),"Slot order and count enforced");
        for(FortuneData.Kind fortune:FortuneData.Kind.values())check(TarotReading.reveal(1,fortune,false,false,"").equals(fortune.negative?"bad_fortune":"good_fortune"),"Fortune polarity only, no ID disclosure");
        check(TarotReading.reveal(1,null,true,true,"witches").equals("no_fortune"),"No pending fortune");
        for(boolean flag:new boolean[]{false,true}){
            check(TarotReading.reveal(2,null,flag,!flag,"").equals(flag?"has_effects":"no_effects"),"Effects independent of curse state");
            check(TarotReading.reveal(3,null,!flag,flag,"").equals(flag?"cursed":"not_cursed"),"Curse state independent of effects");
        }
        for(String attire:new String[]{"green_witch","besmirched","alchemist","witches"})check(TarotReading.reveal(4,null,false,false,attire).equals(attire),"Restored attire family");
        check(TarotReading.reveal(4,null,false,false,null).equals("no_attire"),"No outfit yields explicit response");
        for(int roll:new int[]{Integer.MIN_VALUE,-11,-1,0,1,2,3,4,5,6,Integer.MAX_VALUE}){
            String[] expected={"nothing","no_fortune","no_effects","not_cursed","no_attire"};
            check(TarotReading.reveal(roll,null,false,false,"").equals(expected[Math.floorMod(roll,5)]),"Five-way selection remains bounded");
        }
        for(int mask=0;mask<32;mask++){
            Access a=new Access();a.eligible=(mask&1)!=0;a.target=(mask&2)!=0?target:null;a.online=(mask&4)!=0;a.power=(mask&8)!=0?1000:999;a.shrouded=(mask&16)!=0;
            int before=a.power;boolean valid=a.eligible&&a.target!=null&&a.online,paid=valid&&before>=1000;
            String expected=!a.eligible?"unavailable":a.target==null?"invalid":!a.online?"offline":before<1000?"no_power":a.shrouded?"shrouded":"nothing";
            check(TarotReading.read(reader,a).equals(expected),"Validation/payment/shrouding precedence");
            check(a.payments==(valid?1:0)&&a.power==before-(paid?1000:0),"Only valid funded readings charge");
            check(a.shroudChecks==(paid?1:0)&&a.reveals==(paid&&!a.shrouded?1:0),"No facts inspected before payment or through shrouding");
        }
        Access reentry=new Access();reentry.onPay=()->check(TarotReading.read(reader,reentry).equals("busy"),"Payment reentry cannot charge twice");
        reentry.onReveal=()->check(TarotReading.read(reader,reentry).equals("busy"),"Reveal reentry cannot charge twice");
        check(TarotReading.read(reader,reentry).equals("nothing")&&reentry.payments==1,"Single paid reveal");
        for(boolean payment:new boolean[]{true,false}){
            Access fail=new Access();Runnable boom=()->{throw new IllegalStateException("test");};if(payment)fail.onPay=boom;else fail.onReveal=boom;
            try{TarotReading.read(reader,fail);throw new AssertionError("Exception swallowed");}catch(IllegalStateException expected){checks++;}
            check(TarotReading.read(reader,new Access()).equals("nothing"),"Guard released after exception");
        }
        Access one=new Access(),two=new Access();one.onPay=()->check(TarotReading.read(UUID.randomUUID(),two).equals("nothing"),"Different reader independent");
        check(TarotReading.read(reader,one).equals("nothing"),"Outer reading completes");
        System.out.println("PASS: "+checks+" tarot binding, shrouding, fact selection, payment ordering, privacy and reentry checks");
    }
}
