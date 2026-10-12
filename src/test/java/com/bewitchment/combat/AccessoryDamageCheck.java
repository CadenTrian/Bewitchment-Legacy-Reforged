package com.bewitchment.combat;

import com.bewitchment.item.*;
import com.bewitchment.item.LegacyAccessoryItem.Kind;
import com.bewitchment.potion.MagicWeaknessEffect;
import com.bewitchment.registry.ModEffects;
import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class AccessoryDamageCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean near(float a,float b){return Math.abs(a-b)<.0001F;}
    public static void main(String[] args) throws Exception {
        Bootstrap.bootStrap();
        // All source combinations: overlapping Hellish flags still charge only once.
        for(int mask=0;mask<32;mask++){
            boolean fire=(mask&1)!=0,explosion=(mask&2)!=0,demon=(mask&4)!=0,magic=(mask&8)!=0,cold=(mask&16)!=0;
            AtomicInteger paid=new AtomicInteger();
            float actual=AccessoryRules.reduce(20,fire,explosion,demon,magic,cold,k->true,c->{paid.addAndGet(c);return true;});
            float expected=20;int cost=0;if((mask&7)!=0){expected*=.85F;cost+=20;}if(magic){expected*=.9F;cost+=20;}if(cold){expected*=.9F;cost+=20;}
            check(near(actual,expected) && paid.get()==cost,"Source overlap and exact payment "+mask);
        }
        for(float invalid:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
            AtomicInteger touched=new AtomicInteger();
            float actual=AccessoryRules.reduce(invalid,true,true,true,true,true,k->{touched.incrementAndGet();return true;},c->{touched.incrementAndGet();return true;});
            check(Float.floatToIntBits(actual)==Float.floatToIntBits(invalid) && touched.get()==0,"Invalid damage touches no equipment or power");
        }
        for(int available:new int[]{0,19,20,39,40,60}){
            ItemStack book=new ItemStack(new GrimoireMagiaItem());GrimoireMagiaItem.setPower(book,available);
            float actual=AccessoryRules.reduce(20,true,false,false,true,true,k->true,c->GrimoireMagiaItem.drain(book,c));
            int defenses=Math.min(3,available/20);float expected=20;
            if(defenses>=1)expected*=.85F;if(defenses>=2)expected*=.9F;if(defenses>=3)expected*=.9F;
            check(near(actual,expected) && GrimoireMagiaItem.power(book)==available-defenses*20,"Real book affordability "+available);
        }
        AtomicInteger paid=new AtomicInteger();
        check(AccessoryRules.reduce(20,true,true,true,true,true,k->false,c->{paid.incrementAndGet();return true;})==20 && paid.get()==0,"Unequipped defenses never pay");
        List<Kind> order=new ArrayList<>();AccessoryRules.reduce(20,true,false,false,true,true,k->{order.add(k);return true;},c->true);
        check(order.equals(Arrays.asList(Kind.HELLISH,Kind.NAZAR,Kind.HORSESHOE)),"Stable defense order");
        check(near(AccessoryRules.reduce(20,true,false,false,true,true,k->k==Kind.NAZAR,c->true),18),"Missing earlier defense does not block Nazar");
        ItemStack bark=new ItemStack(Items.LEATHER);BarkArmor.set(bark,2);
        BarkArmor.Hit fire=BarkArmor.hit(bark,20,true,true,true);
        check(near(AccessoryRules.reduce(fire.damage,true,false,false,true,false,k->true,c->true),19.125F) && BarkArmor.get(bark)==1,"Bark fire vulnerability precedes paid reductions");
        float reduced=AccessoryRules.reduce(20,false,false,false,true,false,k->true,c->true);
        check(MagicWeaknessEffect.damage(reduced,0,true)==21,"Magic Weakness adds after Nazar hurt reduction");
        SubscribeEvent weakness=ModEffects.class.getMethod("magicDamage",LivingDamageEvent.class).getAnnotation(SubscribeEvent.class);
        SubscribeEvent vampiric=AccessoryEffects.class.getMethod("damage",LivingDamageEvent.class).getAnnotation(SubscribeEvent.class);
        check(weakness.priority()==EventPriority.HIGH && vampiric.priority()==EventPriority.NORMAL && !vampiric.receiveCanceled(),"Weakness precedes Vampiric and canceled events are skipped");
        check(!AccessoryRules.vampiric(21,.175) && AccessoryRules.vampiric(21,Math.nextDown(.175)),"Final damage keeps exact Vampiric proc boundary");
        System.out.println("PASS: "+checks+" accessory damage integration checks");
    }
}
