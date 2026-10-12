package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.GrimoireMagiaItem;
import com.bewitchment.item.LegacyAccessoryItem.Kind;
import com.bewitchment.registry.ModAccessories;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import com.bewitchment.item.BarkArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.tags.*;
import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class AccessoryEffects {
    public static final ITag.INamedTag<EntityType<?>> DEMONS = EntityTypeTags.createOptional(new ResourceLocation("bewitchment", "demons"));
    public static boolean wearing(LivingEntity wearer, Kind kind) {
        return CuriosApi.getCuriosHelper().findFirstCurio(wearer, ModAccessories.ITEMS.get(kind).get()).isPresent();
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntityLiving().level.isClientSide || !(event.getEntityLiving() instanceof PlayerEntity) || !AccessoryRules.validDamage(event.getAmount())) return;
        PlayerEntity player = (PlayerEntity)event.getEntityLiving();
        ItemStack girdle = CuriosApi.getCuriosHelper().findFirstCurio(player, ModAccessories.ITEMS.get(Kind.GIRDLE).get())
                .map(top.theillusivec4.curios.api.SlotResult::getStack).orElse(ItemStack.EMPTY);
        BarkArmor.Hit hit = BarkArmor.hit(girdle, event.getAmount(), event.getSource().isFire(), event.getSource().isMagic(), event.getSource().getEntity() != null);
        event.setAmount(hit.damage);
        if (hit.blocked) {
            event.setCanceled(true);
            player.level.playSound(null, player.blockPosition(), SoundEvents.WOOD_STEP, SoundCategory.PLAYERS, .75F, 1.9F);
            return;
        }
        boolean demon = event.getSource().getEntity() instanceof LivingEntity && DEMONS.contains(event.getSource().getEntity().getType());
        boolean coldIron = event.getSource().getEntity() instanceof LivingEntity
                && MaterialEffects.COLD_IRON_WEAK.contains(event.getSource().getEntity().getType());
        event.setAmount(AccessoryRules.reduce(event.getAmount(), event.getSource().isFire(), event.getSource().isExplosion(),
                demon, event.getSource().isMagic(), coldIron, kind -> wearing(player, kind), amount -> GrimoireMagiaItem.drain(player, amount)));
    }
    @SubscribeEvent public static void damage(LivingDamageEvent event) {
        if (event.isCanceled() || event.getEntityLiving().level.isClientSide || !(event.getEntityLiving() instanceof PlayerEntity) || !AccessoryRules.validDamage(event.getAmount())) return;
        PlayerEntity player = (PlayerEntity)event.getEntityLiving();
        if (wearing(player, Kind.VAMPIRIC) && AccessoryRules.vampiric(event.getAmount(), player.getRandom().nextDouble()) && GrimoireMagiaItem.drain(player, 50)) {
            event.setAmount(0); event.setCanceled(true);
            player.heal(2); player.addEffect(new EffectInstance(Effects.ABSORPTION, 160));
        }
    }
    private AccessoryEffects() {}
}
