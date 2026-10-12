package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class BloodBottleItem extends Item {
    public BloodBottleItem() { super(new Properties().tab(ModItems.TAB).craftRemainder(Items.GLASS_BOTTLE)); }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) { return DrinkHelper.useDrink(world, player, hand); }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAction getUseAnimation(ItemStack stack) { return UseAction.DRINK; }
    @Override public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) return stack;
        PlayerEntity player = (PlayerEntity) entity;
        if (!world.isClientSide) {
            if (player instanceof ServerPlayerEntity) CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.getFoodData().eat(3, 0);
            player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 400));
            if (player.getRandom().nextDouble() < 0.8) player.addEffect(new EffectInstance(Effects.HUNGER, 200, 1));
        }
        if (player.abilities.instabuild) return stack;
        stack.shrink(1);
        if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        if (!world.isClientSide && !player.inventory.add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        return stack;
    }
}
