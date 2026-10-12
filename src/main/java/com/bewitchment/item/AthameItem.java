package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;

public final class AthameItem extends SwordItem {
    public AthameItem() { super(ModItemTier.SILVER, 3, -2.4F, new Item.Properties().tab(ModItems.TAB)); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof EndermanEntity && attacker instanceof PlayerEntity) {
            if (!target.level.isClientSide) {
                target.hurt(DamageSource.playerAttack((PlayerEntity) attacker), 20);
                stack.hurtAndBreak(50, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
            }
            return true;
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
