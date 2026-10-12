package com.bewitchment.loot;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/** Restored targets from the legacy athame table. Extra rolls supplement ordinary mob drops. */
public final class AthameLoot {
    public static void register() { MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, AthameLoot::drops); }
    private static void drops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntityLiving();
        if (victim instanceof com.bewitchment.entity.GhostEntity && ((com.bewitchment.entity.GhostEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.CleaverEntity && ((com.bewitchment.entity.CleaverEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.ShadowPersonEntity && ((com.bewitchment.entity.ShadowPersonEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.ImpEntity && ((com.bewitchment.entity.ImpEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.BlackDogEntity && ((com.bewitchment.entity.BlackDogEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.FeuerwurmEntity && ((com.bewitchment.entity.FeuerwurmEntity)victim).spectral()
                || victim instanceof com.bewitchment.entity.HellhoundEntity && ((com.bewitchment.entity.HellhoundEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.CambionEntity && ((com.bewitchment.entity.CambionEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.DrudenEntity && ((com.bewitchment.entity.DrudenEntity)victim).spectral()) return;
        Entity source = event.getSource().getEntity();
        if (victim.level.isClientSide || !victim.level.getGameRules().getBoolean(net.minecraft.world.GameRules.RULE_DOMOBLOOT)
                || !event.isRecentlyHit() || !(source instanceof LivingEntity)
                || event.getSource().getDirectEntity() != source
                || ((LivingEntity) source).getMainHandItem().getItem() != ModItems.ATHAME.get()) return;
        if (victim instanceof PlayerEntity || victim instanceof VillagerEntity || victim instanceof WitchEntity || victim instanceof AbstractIllagerEntity)
            add(event, ModItems.HEART.get(), 1);
        if (victim instanceof PlayerEntity) {
            int count = AthameRolls.head(victim.getRandom(), event.getLootingLevel());
            if (count > 0) {
                ItemStack head = new ItemStack(Items.PLAYER_HEAD, count);
                head.getOrCreateTag().put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), ((PlayerEntity) victim).getGameProfile()));
                event.getDrops().add(new ItemEntity(victim.level, victim.getX(), victim.getY() + 0.5, victim.getZ(), head));
            }
        }
        if (source instanceof PlayerEntity && (victim instanceof AnimalEntity || victim instanceof PlayerEntity || victim instanceof VillagerEntity)) {
            PlayerEntity player = (PlayerEntity) source;
            ItemStack bottles = player.getOffhandItem();
            if (bottles.getItem() == Items.GLASS_BOTTLE) {
                ItemStack blood = new ItemStack(ModItems.BOTTLE_OF_BLOOD.get());
                if (!player.abilities.instabuild) bottles.shrink(1);
                if (bottles.isEmpty()) player.setItemInHand(Hand.OFF_HAND, blood);
                else if (!player.inventory.add(blood)) player.drop(blood, false);
            }
        }
        if (victim instanceof ZombifiedPiglinEntity) {
            add(event, ModItems.SPECTRAL_DUST.get(), 3); add(event, ModItems.HOOF.get(), 2);
        } else if (victim instanceof ZombieEntity) {
            head(event, Items.ZOMBIE_HEAD); add(event, ModItems.SPECTRAL_DUST.get(), 1);
        }
        if (victim instanceof ZombieVillagerEntity) add(event, ModItems.SPECTRAL_DUST.get(), 1);
        if (victim instanceof WitherEntity) add(event, ModItems.SPECTRAL_DUST.get(), 6);
        if (victim instanceof WitherSkeletonEntity) {
            head(event, Items.WITHER_SKELETON_SKULL); add(event, ModItems.SPECTRAL_DUST.get(), 2);
        } else if (victim instanceof AbstractSkeletonEntity) {
            head(event, Items.SKELETON_SKULL); add(event, ModItems.SPECTRAL_DUST.get(), 1);
        }
        if (victim instanceof CreeperEntity) head(event, Items.CREEPER_HEAD);
        if (victim instanceof BlazeEntity) { add(event, ModItems.ECTOPLASM.get(), 2); add(event, Items.BLAZE_ROD, 4); }
        if (victim instanceof GhastEntity) { add(event, ModItems.ECTOPLASM.get(), 8); add(event, Items.GHAST_TEAR, 6); }
        if (victim instanceof SilverfishEntity) add(event, ModItems.SILVER_NUGGET.get(), 2);
        if (victim instanceof EndermiteEntity) add(event, ModItems.DIMENSIONAL_SAND.get(), 2);
        if (victim instanceof EndermanEntity) { add(event, ModItems.ECTOPLASM.get(), 2); add(event, ModItems.DIMENSIONAL_SAND.get(), 4); }
        if (victim instanceof com.bewitchment.entity.ShadowPersonEntity) add(event, ModItems.ECTOPLASM.get(), 4);
        if (victim instanceof com.bewitchment.entity.CleaverEntity) { add(event, ModItems.HEART.get(), 1); add(event, ModItems.HOOF.get(), 2); add(event, Items.BEEF, 2); add(event, Items.BLAZE_POWDER, 4); add(event, ModItems.HELLHOUND_HORN.get(), 2); }
        if (victim instanceof com.bewitchment.entity.GhostEntity) { add(event, ModItems.ECTOPLASM.get(), 4); add(event, ModItems.SPECTRAL_DUST.get(), 1); }
        if (victim instanceof VexEntity) { add(event, ModItems.ECTOPLASM.get(), 4); add(event, ModItems.SPECTRAL_DUST.get(), 1); }
        // Legacy elder guardians match both the guardian and elder predicates, so roll both.
        if (victim instanceof GuardianEntity) add(event, ModItems.EYE_OF_OLD.get(), 1);
        if (victim instanceof ElderGuardianEntity) add(event, ModItems.EYE_OF_OLD.get(), 3);
        if (victim instanceof PigEntity || victim instanceof CowEntity || victim instanceof SheepEntity || victim instanceof AbstractHorseEntity)
            add(event, ModItems.HOOF.get(), 4);
        if (victim instanceof AbstractHorseEntity && victim.getMobType() == CreatureAttribute.UNDEAD) add(event, ModItems.SPECTRAL_DUST.get(), 2);
        if (victim instanceof WolfEntity) add(event, ModItems.TONGUE_OF_DOG.get(), 1);
        if (victim instanceof RabbitEntity) add(event, Items.RABBIT_FOOT, 1);
        if (victim instanceof com.bewitchment.entity.SnakeEntity) add(event, ModItems.ADDERS_FORK.get(), 3);
        if (victim instanceof com.bewitchment.entity.CambionEntity) {
            add(event, ModItems.HELLHOUND_HORN.get(), 2);add(event, ModItems.HEART.get(), 1);
            add(event, Items.ROTTEN_FLESH, 2);add(event, ModItems.ECTOPLASM.get(), 1);
        }
        if (victim instanceof com.bewitchment.entity.DrudenEntity) {
            add(event, com.bewitchment.registry.ModTreeBlocks.CYPRESS.sapling.get().asItem(), 1);
            add(event, ModItems.HEART.get(), 1);add(event, ModItems.OAK_APPLE_GALL.get(), 4);
            add(event, ModItems.HOOF.get(), 2);add(event, ModItems.HELLHOUND_HORN.get(), 2);
        }
        if (victim instanceof com.bewitchment.entity.ImpEntity) {
            add(event, ModItems.HEART.get(), 1);add(event, ModItems.HOOF.get(), 2);
            add(event, Items.BLAZE_POWDER, 4);add(event, ModItems.HELLHOUND_HORN.get(), 2);
        }
        if (victim instanceof com.bewitchment.entity.BlackDogEntity) {
            add(event, ModItems.TONGUE_OF_DOG.get(), 1);add(event, ModItems.ECTOPLASM.get(), 4);add(event, ModItems.SPECTRAL_DUST.get(), 1);
        }
        if (victim instanceof com.bewitchment.entity.HellhoundEntity) {
            add(event, ModItems.TONGUE_OF_DOG.get(), 1);add(event, ModItems.HELLHOUND_HORN.get(), 2);add(event, Items.BLAZE_POWDER, 4);
        }
        if (victim instanceof com.bewitchment.entity.FeuerwurmEntity) {
            add(event, ModItems.ADDERS_FORK.get(), 3);add(event, Items.BLAZE_POWDER, 2);add(event, ModItems.HELLHOUND_HORN.get(), 2);
        }
        if (victim instanceof com.bewitchment.entity.RavenEntity) add(event, ModItems.RAVENS_FEATHER.get(), 4);
        if (victim instanceof com.bewitchment.entity.OwlEntity) add(event, ModItems.OWLETS_WING.get(), 2);
        if (victim instanceof com.bewitchment.entity.LizardEntity) add(event, ModItems.LIZARD_LEG.get(), 4);
        if (victim instanceof com.bewitchment.entity.ToadEntity) add(event, ModItems.TOE_OF_FROG.get(), 4);
    }
    private static void add(LivingDropsEvent event, Item item, int max) {
        spawn(event, item, AthameRolls.material(event.getEntityLiving().getRandom(), max, event.getLootingLevel()));
    }
    private static void head(LivingDropsEvent event, Item item) {
        spawn(event, item, AthameRolls.head(event.getEntityLiving().getRandom(), event.getLootingLevel()));
    }
    private static void spawn(LivingDropsEvent event, Item item, int count) {
        LivingEntity victim = event.getEntityLiving();
        if (victim instanceof com.bewitchment.entity.GhostEntity && ((com.bewitchment.entity.GhostEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.CleaverEntity && ((com.bewitchment.entity.CleaverEntity)victim).spectral()) return;
        if (victim instanceof com.bewitchment.entity.ShadowPersonEntity && ((com.bewitchment.entity.ShadowPersonEntity)victim).spectral()) return;
        while (count > 0) {
            int size = Math.min(count, item.getMaxStackSize());
            event.getDrops().add(new ItemEntity(victim.level, victim.getX(), victim.getY() + 0.5, victim.getZ(), new ItemStack(item, size)));
            count -= size;
        }
    }
    private AthameLoot() {}
}
