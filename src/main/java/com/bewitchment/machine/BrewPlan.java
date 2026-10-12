package com.bewitchment.machine;

import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import java.util.*;

/** Pure legacy potion calculation. Does not consume ingredients or drain a cauldron. */
public final class BrewPlan {
    public enum Form { DRINK, SPLASH, LINGERING }
    public final int potency,durationMultiplier,bottles;
    private final List<EffectInstance> effects;
    private final Form form;
    public static int bottles(boolean alchemist,int leonards){
        if(leonards<0)throw new IllegalArgumentException("Negative Leonard count");
        return (int)Math.min(Integer.MAX_VALUE,3L+(alchemist?1:0)+leonards);
    }
    public static Form form(boolean gunpowder,boolean owlet){return gunpowder?(owlet?Form.LINGERING:Form.SPLASH):Form.DRINK;}
    public static int order(ResourceLocation id){
        if(id==null || !id.getNamespace().equals("bewitchment"))return 0;
        String name=id.getPath();if(name.equals("magic_resistance") || name.equals("magic_weakness"))return -1;
        return name.equals("absence") || name.equals("purification") || name.equals("corruption")?1:0;
    }
    public BrewPlan(Collection<EffectInstance> base,int redstone,int glowstone,boolean alchemist,int pledgedLeonards,boolean gunpowder,boolean owlet,boolean hideParticles){
        if(base==null || redstone<0 || redstone>10 || glowstone<0 || glowstone>10 || pledgedLeonards<0)throw new IllegalArgumentException("Invalid brew inputs");
        potency=Math.min(pledgedLeonards>0?3:2,(alchemist?2:1)+glowstone);
        durationMultiplier=Math.min(3,1+redstone);bottles=bottles(alchemist,pledgedLeonards);form=form(gunpowder,owlet);
        List<EffectInstance> first=new ArrayList<>(),middle=new ArrayList<>(),last=new ArrayList<>();
        for(EffectInstance effect:new LinkedHashSet<>(base)){
            if(effect==null || effect.getDuration()<0 || effect.getAmplifier()<0 || effect.getAmplifier()>255)throw new IllegalArgumentException("Invalid base effect");
            // Preserve legacy truncation, including a one-tick effect becoming zero ticks.
            int ticks=(int)(effect.getDuration()*(1d/potency)*durationMultiplier);
            int amp=Math.min(255,effect.getAmplifier()+potency-1);
            EffectInstance result=new EffectInstance(effect.getEffect(),ticks,amp,effect.isAmbient(),!hideParticles);
            int order=order(effect.getEffect().getRegistryName());(order<0?first:order>0?last:middle).add(result);
        }
        Collections.reverse(last);first.addAll(middle);first.addAll(last);effects=Collections.unmodifiableList(first);
    }
    public List<EffectInstance> effects(){List<EffectInstance> copy=new ArrayList<>();for(EffectInstance effect:effects)copy.add(new EffectInstance(effect));return Collections.unmodifiableList(copy);}
    public ItemStack potion(){
        ItemStack stack=new ItemStack(form==Form.LINGERING?Items.LINGERING_POTION:form==Form.SPLASH?Items.SPLASH_POTION:Items.POTION);
        PotionUtils.setCustomEffects(stack,effects());stack.getOrCreateTag().putInt("CustomPotionColor",PotionUtils.getColor(effects));stack.getOrCreateTag().putBoolean("bewitchment_brew",true);return stack;
    }
}