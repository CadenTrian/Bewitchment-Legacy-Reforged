package com.bewitchment.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;

/** Equipment branch order and independent combat rolls from the MIT legacy source. */
public final class CambionRules {
    private CambionRules(){}
    public static Item chest(int roll){return roll==3?Items.CHAINMAIL_CHESTPLATE:roll==0?Items.GOLDEN_CHESTPLATE:roll==5?Items.LEATHER_CHESTPLATE:Items.AIR;}
    public static Item weapon(int b,int d){return b<2?Items.GOLDEN_SWORD:d<2?Items.GOLDEN_AXE:d>4?Items.IRON_AXE:b>4?Items.IRON_SWORD:Items.AIR;}
    public static int attackEffect(int fire,int poison){return fire<5?1:poison<35?2:0;}
    public static boolean canPickUp(Item item){return item instanceof SwordItem || item instanceof ArmorItem || item instanceof ShieldItem;}
    public static boolean futurePrey(ResourceLocation id){return new ResourceLocation("bewitchment","ghost").equals(id) || new ResourceLocation("bewitchment","werewolf").equals(id);}
    public static boolean prey(LivingEntity target){return target instanceof HellhoundEntity || target instanceof FeuerwurmEntity || target instanceof BlackDogEntity
        || target instanceof ZombieEntity || target instanceof SkeletonEntity || target instanceof SpiderEntity || futurePrey(target.getType().getRegistryName());}
}
