package com.bewitchment.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.function.Supplier;

/** Shared Frostfire/Hellfire collection with vanilla selected-hand and creative behavior. */
public final class BottledFireCollection {
    public static ActionResultType collect(World world, BlockPos pos, PlayerEntity player, Hand hand,
                                           BlockRayTraceResult hit, Supplier<Item> filledItem) {
        ItemStack held = player.getItemInHand(hand);
        boolean eligible = !player.isSpectator() && !player.isShiftKeyDown() && player.abilities.mayBuild
                && held.getItem() == Items.GLASS_BOTTLE && world.mayInteract(player, pos)
                && player.mayUseItemAt(pos, hit.getDirection(), held);
        return BlockInteractionCommit.apply(eligible, world.isClientSide, () -> world.removeBlock(pos, false), () -> {
            ItemStack filled = new ItemStack(filledItem.get());
            deliver(held, filled, player.isCreative(), stack -> player.setItemInHand(hand, stack),
                    player.inventory::add, stack -> player.drop(stack, false));
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1, 1.75F);
        });
    }
    public static void deliver(ItemStack held, ItemStack filled, boolean creative,
                               java.util.function.Consumer<ItemStack> replaceHand,
                               java.util.function.Predicate<ItemStack> addInventory,
                               java.util.function.Consumer<ItemStack> drop) {
        if (!creative) held.shrink(1);
        if (held.isEmpty()) replaceHand.accept(filled);
        else if (!addInventory.test(filled)) drop.accept(filled);
    }
    private BottledFireCollection() {}
}