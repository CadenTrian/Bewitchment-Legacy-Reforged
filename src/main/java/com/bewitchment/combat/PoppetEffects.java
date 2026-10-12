package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.PoppetItem;
import com.bewitchment.item.PoppetItem.Kind;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class PoppetEffects {
    private static final ITag.INamedTag<EntityType<?>> SPIRITS = EntityTypeTags.createOptional(new ResourceLocation("bewitchment", "spirits"));
    static boolean spend(PlayerEntity player, Kind kind) {
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack stack = player.inventory.getItem(slot);
            if (PoppetItem.spend(stack, kind, player.getUUID())) {
                if (stack.isEmpty()) player.playSound(SoundEvents.ITEM_BREAK, 1, 1);
                player.inventory.setChanged();
                return true;
            }
        }
        return com.bewitchment.machine.PoppetShelfNetwork.spend(player, kind);
    }
    public static Kind protection(DamageSource source) {
        if (source == DamageSource.FALL) return Kind.EARTH;
        if (source == DamageSource.ON_FIRE || source == DamageSource.IN_FIRE) return Kind.FLAME;
        if (source == DamageSource.STARVE) return Kind.HUNGER;
        if (source == DamageSource.DROWN) return Kind.WATER;
        return null;
    }
    public static float protectedDamage(Kind kind, float amount) {
        if (kind == Kind.EARTH) return amount / 4;
        if (kind == Kind.FLAME || kind == Kind.SPIRIT) return amount * 0.6F;
        if (kind == Kind.HUNGER || kind == Kind.WATER) return 0;
        return amount;
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity) || event.getEntityLiving().level.isClientSide || event.getAmount() <= 0) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        Kind protection = protection(event.getSource());
        if (protection != null && spend(player, protection)) {
            event.setAmount(protectedDamage(protection, event.getAmount()));
            if (protection == Kind.HUNGER) player.addEffect(new EffectInstance(Effects.SATURATION, 100, 1));
            if (protection == Kind.WATER) {
                player.addEffect(new EffectInstance(Effects.WATER_BREATHING, 200));
                event.setCanceled(true);
            }
        }
        Entity source = event.getSource().getEntity();
        if (source instanceof MobEntity) {
            MobEntity mob = (MobEntity)source;
            if (spend(player, Kind.BINDING)) {
                mob.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 500, 2));
                mob.playSound(SoundEvents.LEASH_KNOT_PLACE, 5, 1);
            }
            ItemStack held = mob.getMainHandItem();
            if (!held.isEmpty() && spend(player, Kind.CLUMSY)) {
                // Copy before clearing the hand, so the dropped entity owns its stack.
                mob.spawnAtLocation(held.copy());
                mob.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                mob.playSound(SoundEvents.PLAYER_BIG_FALL, 2, 1);
            }
            // Unlike the legacy handler, unrelated creatures do not waste Spirit's Bane charges.
            if (SPIRITS.contains(mob.getType()) && spend(player, Kind.SPIRIT)) {
                event.setAmount(protectedDamage(Kind.SPIRIT, event.getAmount()));
                player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2, 1);
            }
        } else if (source instanceof PlayerEntity && spend(player, Kind.WASTING)) {
            // The original cast to EntityLiving crashed on player attackers.
            ((PlayerEntity)source).addEffect(new EffectInstance(Effects.HUNGER, 200, 1));
            source.playSound(SoundEvents.ZOMBIE_AMBIENT, 2, 1);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void voodoo(com.bewitchment.api.event.VoodooEvent event) {
        if (event.getTarget().level.isClientSide) return;
        boolean protectedTarget = event.getTarget() instanceof PlayerEntity
                ? spend((PlayerEntity)event.getTarget(), Kind.VOODOO_PROTECTION)
                : com.bewitchment.machine.PoppetShelfNetwork.spend(event.getTarget(), Kind.VOODOO_PROTECTION);
        if (protectedTarget) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity) || event.getEntityLiving().level.isClientSide) return;
        PlayerEntity player = (PlayerEntity)event.getEntityLiving();
        if (spend(player, Kind.DEATH)) {
            event.setCanceled(true);
            player.setHealth(Math.min(4, player.getMaxHealth()));
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 1000, 1));
            player.playSound(SoundEvents.EVOKER_CAST_SPELL, 2, 1);
        }
    }
    private PoppetEffects() {}
}
