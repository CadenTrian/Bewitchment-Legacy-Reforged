package com.bewitchment.machine;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
public final class BrewPlanCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    private static BrewPlan plan(int red,int glow,boolean alchemist,int leonards){return new BrewPlan(Arrays.asList(new EffectInstance(Effects.MOVEMENT_SPEED,1200)),red,glow,alchemist,leonards,false,false,false);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(boolean alchemist:new boolean[]{false,true})for(int leonards:new int[]{0,1,2})for(int red=0;red<4;red++)for(int glow=0;glow<4;glow++){
            BrewPlan p=plan(red,glow,alchemist,leonards);int potency=Math.min(leonards>0?3:2,(alchemist?2:1)+glow),duration=Math.min(3,1+red);
            check(p.potency==potency && p.durationMultiplier==duration && p.bottles==3+(alchemist?1:0)+leonards,"Bonus and modifier combinations");
            check(p.effects().get(0).getDuration()==(int)(1200*(1d/potency)*duration) && p.effects().get(0).getAmplifier()==potency-1,"Scaled duration and amplifier");
        }
        check(plan(0,0,false,1).potency==1,"Leonard unlocks cap without free potency");
        check(plan(10,10,true,1).potency==3 && plan(10,10,true,1).durationMultiplier==3,"Modifier caps");
        for(int red:new int[]{-1,11}){boolean rejected=false;try{plan(red,0,false,0);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Invalid redstone count");}
        for(int glow:new int[]{-1,11}){boolean rejected=false;try{plan(0,glow,false,0);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Invalid glowstone count");}
        boolean rejected=false;try{plan(0,0,false,-1);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Negative bonus count");
        check(BrewPlan.bottles(true,Integer.MAX_VALUE)==Integer.MAX_VALUE,"Bottle arithmetic cannot wrap");
        for(boolean gun:new boolean[]{false,true})for(boolean owl:new boolean[]{false,true}){
            BrewPlan p=new BrewPlan(Arrays.asList(new EffectInstance(Effects.POISON,400)),0,0,false,0,gun,owl,true);
            check(p.potion().getItem()==(!gun?Items.POTION:owl?Items.LINGERING_POTION:Items.SPLASH_POTION),"Owlet requires gunpowder for lingering");
            check(!p.effects().get(0).isVisible(),"Raven feather hides particles");
            check(p.potion().getTag().getBoolean("bewitchment_brew") && p.potion().getTag().contains("CustomPotionColor",3),"Legacy marker and color");
            check(PotionUtils.getMobEffects(p.potion()).get(0).getEffect()==Effects.POISON,"Potion effect NBT round trip");
        }
        EffectInstance input=new EffectInstance(Effects.MOVEMENT_SPEED,1200,0,true,false);
        List<EffectInstance> source=new ArrayList<>(Arrays.asList(input,new EffectInstance(input)));
        BrewPlan p=new BrewPlan(source,0,0,false,0,false,false,false);source.clear();input.update(new EffectInstance(Effects.MOVEMENT_SPEED,2400,2));
        check(p.effects().size()==1 && p.effects().get(0).getAmplifier()==0 && p.effects().get(0).getDuration()==1200,"Input snapshot and exact duplicate removal");
        check(p.effects().get(0).isAmbient() && p.effects().get(0).isVisible(),"Ambient retained; particles follow modifier");
        p.effects().get(0).update(new EffectInstance(Effects.MOVEMENT_SPEED,5000,3));check(p.effects().get(0).getAmplifier()==0,"Returned effects cannot mutate plan");
        ItemStack output=p.potion();output.getOrCreateTag().putBoolean("bewitchment_brew",false);check(p.potion().getTag().getBoolean("bewitchment_brew"),"Fresh independent output stacks");
        BrewPlan instant=new BrewPlan(Arrays.asList(new EffectInstance(Effects.HEAL,1)),0,1,false,0,false,false,false);check(instant.effects().get(0).getDuration()==0,"Original one-tick truncation preserved explicitly");
        BrewPlan saturated=new BrewPlan(Arrays.asList(new EffectInstance(Effects.MOVEMENT_SPEED,Integer.MAX_VALUE,255)),10,10,true,1,false,false,false);check(saturated.effects().get(0).getAmplifier()==255 && saturated.effects().get(0).getDuration()>0,"Custom amplifier bounded and duration cannot overflow");
        check(BrewPlan.order(new ResourceLocation("bewitchment:magic_weakness"))==-1 && BrewPlan.order(new ResourceLocation("bewitchment:magic_resistance"))==-1,"Magic modifiers first");
        for(String id:new String[]{"absence","purification","corruption"})check(BrewPlan.order(new ResourceLocation("bewitchment",id))==1,"Removal/conversion effects last");
        check(BrewPlan.order(new ResourceLocation("other:absence"))==0 && BrewPlan.order(null)==0,"Ordering respects namespace");
        Effect first=new Effect(EffectType.HARMFUL,0){}.setRegistryName("bewitchment:magic_weakness"),lastA=new Effect(EffectType.HARMFUL,0){}.setRegistryName("bewitchment:absence"),lastB=new Effect(EffectType.HARMFUL,0){}.setRegistryName("bewitchment:corruption");
        BrewPlan ordered=new BrewPlan(Arrays.asList(new EffectInstance(lastA,10),new EffectInstance(Effects.POISON,20),new EffectInstance(first,30),new EffectInstance(lastB,40)),0,0,false,0,false,false,false);
        List<EffectInstance> effects=ordered.effects();check(effects.get(0).getEffect()==first && effects.get(1).getEffect()==Effects.POISON && effects.get(2).getEffect()==lastB && effects.get(3).getEffect()==lastA,"Original special-effect ordering");
        System.out.println("PASS: "+checks+" brewing modifier, bonus, output, ordering and isolation checks");
    }
}