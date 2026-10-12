package com.bewitchment.item;

import net.minecraft.item.*;
import net.minecraft.util.NonNullList;

/** Original creative-only testing reserve; finite power, not free accessory activation. */
public final class BastardsGrimoireItem extends GrimoireMagiaItem {
    public static final int RESERVE = 2147483646;
    @Override public int storageCapacity() { return RESERVE; }
    @Override public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
        if (!allowdedIn(tab)) return;
        ItemStack stack = new ItemStack(this);
        setPower(stack, RESERVE);
        items.add(stack); // Legacy added the same full stack twice; expose one unambiguous entry.
    }
}
