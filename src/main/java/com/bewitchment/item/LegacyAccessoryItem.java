package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.enchantment.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.SoundEvents;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.*;

/** Baubles effects expressed through Curios; all gameplay and power spending is server-side. */
public class LegacyAccessoryItem extends Item implements ICurioItem {
    public enum Kind {
        FORTUNE("fortunes_favor"), TRISKELION("triskelion"), SASH("gluttons_sash"),
        NAZAR("nazar"), REMEDIES("token_of_remedies"), HORSESHOE("horseshoe"),
        GIRDLE("girdle_of_the_dryads"), HELLISH("hellish_bauble"), VAMPIRIC("vampiric_amulet"), HECATE("hecates_visage");
        public final String id;
        Kind(String id) { this.id = id; }
    }
    public final Kind kind;
    public LegacyAccessoryItem(Kind kind) { super(new Properties().stacksTo(1).tab(ModItems.TAB)); this.kind = kind; }
    @Override public boolean canEquipFromUse(SlotContext context, ItemStack stack) { return true; }
    @Override public boolean canUnequip(String id, LivingEntity wearer, ItemStack stack) {
        return (wearer instanceof PlayerEntity && ((PlayerEntity)wearer).isCreative()) || !EnchantmentHelper.hasBindingCurse(stack);
    }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return enchantment == Enchantments.BINDING_CURSE; }
    @Override public ICurio.SoundInfo getEquipSound(SlotContext context, ItemStack stack) {
        if (kind == Kind.HECATE) return new ICurio.SoundInfo(SoundEvents.FIRECHARGE_USE, 1, 1);
        if (kind == Kind.GIRDLE) return new ICurio.SoundInfo(SoundEvents.WOOD_STEP, .75F, 1.9F);
        if (kind == Kind.HELLISH) return new ICurio.SoundInfo(SoundEvents.BLAZE_AMBIENT, .75F, 1.9F);
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_IRON, .75F, 1.9F);
    }
    @Override public void curioTick(String id, int index, LivingEntity wearer, ItemStack stack) {
        if (wearer.level.isClientSide || !(wearer instanceof PlayerEntity)) return;
        // A datapack/mod can provide additional slots. Only the first copy runs periodic effects.
        if (!CuriosApi.getCuriosHelper().findFirstCurio(wearer, this)
                .map(result -> result.getSlotContext().getIdentifier().equals(id) && result.getSlotContext().getIndex() == index).orElse(false)) return;
        PlayerEntity player = (PlayerEntity)wearer;
        long time = wearer.level.getGameTime();
        switch (kind) {
            case FORTUNE:
                if (time % 20 == 0 && !wearer.hasEffect(Effects.DIG_SPEED))
                    wearer.addEffect(new EffectInstance(Effects.DIG_SPEED, 100, 0, false, false));
                break;
            case TRISKELION:
                if (time % 200 == 0 && GrimoireMagiaItem.drain(player, 1))
                    wearer.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 240, 0, false, false));
                break;
            case SASH:
                if (time % 20 == 0 && player.getFoodData().needsFood() && GrimoireMagiaItem.drain(player, 5)) player.getFoodData().eat(1, 1);
                break;
            case NAZAR:
                if (wearer.tickCount % 40 == 0 && wearer.hasEffect(Effects.UNLUCK) && GrimoireMagiaItem.drain(player, 20)) wearer.removeEffect(Effects.UNLUCK);
                if (wearer.tickCount % 40 == 0 && wearer.hasEffect(com.bewitchment.registry.ModEffects.FEAR.get()) && GrimoireMagiaItem.drain(player, 20))
                    wearer.removeEffect(com.bewitchment.registry.ModEffects.FEAR.get());
                break;
            case REMEDIES:
                if (wearer.tickCount % 80 == 0 && (wearer.hasEffect(Effects.BLINDNESS) || wearer.hasEffect(Effects.CONFUSION)
                        || wearer.hasEffect(Effects.WEAKNESS)) && GrimoireMagiaItem.drain(player, 20)) {
                    wearer.removeEffect(Effects.BLINDNESS); wearer.removeEffect(Effects.CONFUSION); wearer.removeEffect(Effects.WEAKNESS);
                }
                break;
            case HORSESHOE:
                if (wearer.tickCount % 40 == 0 && GrimoireMagiaItem.drain(player, 20))
                    wearer.addEffect(new EffectInstance(Effects.LUCK, 120, 0, true, true));
                break;
            case HELLISH:
                if (wearer.tickCount % 40 == 0) wearer.removeEffect(Effects.CONFUSION);
                break;
            case GIRDLE:
                if (BarkArmor.canGrow(stack, wearer.level.getBlockState(wearer.blockPosition().below()).is(net.minecraft.block.Blocks.GRASS_BLOCK),
                        wearer.getRandom().nextDouble()) && GrimoireMagiaItem.drain(player, 20)) {
                    BarkArmor.set(stack, BarkArmor.get(stack) + 1);
                    wearer.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 80, 10, false, false));
                    wearer.level.playSound(null, wearer.blockPosition(), SoundEvents.CHORUS_FLOWER_GROW, net.minecraft.util.SoundCategory.PLAYERS, 1, 1);
                }
                break;
        }
    }
}
