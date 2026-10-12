package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.world.World;

/** Legacy food behavior with a server-side greater-demon pledge hook. */
public final class GrotesqueStewItem extends Item {
    public GrotesqueStewItem() {
        super(new Properties().tab(ModItems.TAB).craftRemainder(Items.BOWL).food(new Food.Builder()
                .nutrition(8).saturationMod(1.25F).alwaysEat()
                .effect(() -> new EffectInstance(Effects.CONFUSION, 750, 3), 1)
                .effect(() -> new EffectInstance(Effects.WEAKNESS, 750, 3), 1)
                .effect(() -> new EffectInstance(Effects.DIG_SLOWDOWN, 750, 3), 1)
                .effect(() -> new EffectInstance(Effects.HUNGER, 750, 3), 1)
                .effect(() -> new EffectInstance(Effects.POISON, 750, 3), 1).build()));
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, world, entity);
        if (!world.isClientSide && entity instanceof PlayerEntity) com.bewitchment.combat.PledgeEffects.eat((PlayerEntity)entity);
        if (entity instanceof PlayerEntity && ((PlayerEntity)entity).isCreative()) return result;
        if (result.isEmpty()) return new ItemStack(Items.BOWL);
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            ItemStack bowl = new ItemStack(Items.BOWL);
            if (!player.inventory.add(bowl)) player.drop(bowl, false);
        }
        return result;
    }
}
