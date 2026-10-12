package com.bewitchment.item;

import com.bewitchment.entity.MetalArrowEntity;
import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

public final class MetalArrowItem extends ArrowItem {
    private final boolean coldIron;
    public MetalArrowItem(boolean coldIron) { super(new Properties().tab(ModItems.TAB)); this.coldIron = coldIron; }
    @Override public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) { return new MetalArrowEntity(world, shooter, coldIron); }
}
