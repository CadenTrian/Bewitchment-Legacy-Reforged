package com.bewitchment.potion;

import com.bewitchment.entity.HellhoundEntity;
import com.bewitchment.entity.WerewolfEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import javax.annotation.Nullable;

/** Original targeted magic damage plus data-pack compatibility tags. */
public final class TargetedBrewEffect extends InstantEffect {
    public static final ResourceLocation ALTAR=new ResourceLocation("bewitchment:crafted_altar");
    private static final ITag.INamedTag<EntityType<?>> WOLFSBANE=EntityTypeTags.createOptional(new ResourceLocation("bewitchment:wolfsbane_weakness"));
    private static final ITag.INamedTag<EntityType<?>> DEVIANTS=EntityTypeTags.createOptional(new ResourceLocation("bewitchment:deviants_decomposure_weakness"));
    private final boolean wolfsbane;
    public TargetedBrewEffect(boolean wolfsbane){super(wolfsbane?EffectType.BENEFICIAL:EffectType.HARMFUL,wolfsbane?0x3f00ff:0xdc7f10);this.wolfsbane=wolfsbane;}
    public static void init(){}
    public static boolean legacyCanine(String name){return name.contains("Werewolf")||name.endsWith("EntityCoyote")||name.endsWith("EntityFeralWolf")||name.endsWith("EntityFox");}
    public static boolean canine(boolean wolf,boolean fox,boolean werewolf,boolean hellhound,boolean tagged,String name){return wolf||fox||werewolf||hellhound||tagged||legacyCanine(name);}
    public static boolean deviant(boolean illager,boolean witch,boolean advancedPlayer,boolean tagged){return illager||witch||advancedPlayer||tagged;}
    public static float damage(int amplifier){return 8F*UtilityBrewEffect.level(amplifier);}
    public static boolean witchPlayer(ServerPlayerEntity player){
        Advancement advancement=player.getServer().getAdvancements().getAdvancement(ALTAR);
        return advancement!=null&&player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
    private static boolean tagged(LivingEntity target,ITag.INamedTag<EntityType<?>> tag){return EntityTypeTags.getAllTags().getTagOrEmpty(tag.getName()).contains(target.getType());}
    public boolean susceptible(LivingEntity target){
        if(wolfsbane)return canine(target instanceof WolfEntity,target instanceof FoxEntity,target instanceof WerewolfEntity,target instanceof HellhoundEntity,tagged(target,WOLFSBANE),target.getClass().getName());
        return deviant(target instanceof AbstractIllagerEntity,target instanceof WitchEntity,target instanceof ServerPlayerEntity&&witchPlayer((ServerPlayerEntity)target),tagged(target,DEVIANTS));
    }
    @Override public void applyEffectTick(LivingEntity target,int amplifier){applyInstantenousEffect(null,null,target,amplifier,1);}
    @Override public void applyInstantenousEffect(@Nullable Entity source,@Nullable Entity owner,LivingEntity target,int amplifier,double intensity){
        if(target.level.isClientSide||!target.isAlive()||!susceptible(target))return;
        // Legacy direct magic: no attacker attribution and no distance/intensity scaling.
        target.hurt(DamageSource.MAGIC,damage(amplifier));
    }
}
