package com.bewitchment.machine;

import com.bewitchment.registry.ModBlocks;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.items.*;

public final class DistilleryMenu extends Container {
    private final DistilleryTile tile;
    public final IIntArray data;
    public DistilleryMenu(int id, PlayerInventory player) { this(id, player, null, new ItemStackHandler(13), new IntArray(3)); }
    public DistilleryMenu(int id, PlayerInventory player, DistilleryTile tile) { this(id, player, tile, tile.inventory, tile.data); }
    private DistilleryMenu(int id, PlayerInventory player, DistilleryTile tile, ItemStackHandler inventory, IIntArray data) {
        super(ModMachines.DISTILLERY_MENU.get(), id); this.tile = tile; this.data = data;
        for (int i = 0; i < 13; i++) {
            final int index = i;
            int n = i == 0 ? 0 : (i - 1) % 6;
            int x = i == 0 ? 80 : (i < 7 ? 18 : 124) + (n % 2) * 18;
            int y = i == 0 ? 58 : 17 + (n / 2) * 18;
            addSlot(new SlotItemHandler(inventory, i, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return index == 0 ? stack.getItem() == Items.BLAZE_POWDER : index < 7; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    @Override public boolean stillValid(PlayerEntity player) {
        return tile == null || (!tile.isRemoved() && player.level.getBlockState(tile.getBlockPos()).is(ModBlocks.DISTILLERY.get())
                && player.distanceToSqr(tile.getBlockPos().getX()+0.5, tile.getBlockPos().getY()+0.5, tile.getBlockPos().getZ()+0.5) <= 64);
    }
    @Override public ItemStack quickMoveStack(PlayerEntity player, int index) {
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 13) { if (!moveItemStackTo(stack, 13, 49, true)) return ItemStack.EMPTY; }
        else if (stack.getItem() == Items.BLAZE_POWDER) {
            if (!moveItemStackTo(stack, 0, 1, false) && !moveItemStackTo(stack, 1, 7, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 1, 7, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack); return original;
    }
}
