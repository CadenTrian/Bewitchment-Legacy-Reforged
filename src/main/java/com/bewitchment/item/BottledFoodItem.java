package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

public final class BottledFoodItem extends Item {
    private final boolean tea;
    public BottledFoodItem(boolean tea) {
        super(new Properties().tab(ModItems.TAB).food(new Food.Builder().nutrition(tea ? 6 : 5)
                .saturationMod(tea ? 0.55F : 0.8F).build()).craftRemainder(Items.GLASS_BOTTLE));
        this.tea = tea;
    }
    @Override public UseAction getUseAnimation(ItemStack stack) { return tea ? UseAction.DRINK : UseAction.EAT; }
    @Override public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, world, entity);
        if (!world.isClientSide && tea && world.random.nextFloat() < 0.2F) entity.removeAllEffects();
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild) return result;
        if (result.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.inventory.add(bottle)) player.drop(bottle, false);
        }
        return result;
    }
}
