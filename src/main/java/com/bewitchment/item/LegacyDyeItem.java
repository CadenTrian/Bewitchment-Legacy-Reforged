package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import java.util.function.Supplier;

/** Delegate sheep interaction without replacing vanilla's global dye-color lookup. */
public final class LegacyDyeItem extends Item {
    private final Supplier<Item> vanilla;
    public LegacyDyeItem(Supplier<Item> vanilla) { super(new Properties().tab(ModItems.TAB)); this.vanilla = vanilla; }
    @Override public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        return vanilla.get().interactLivingEntity(stack, player, target, hand);
    }
}
