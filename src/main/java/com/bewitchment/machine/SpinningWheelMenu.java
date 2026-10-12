package com.bewitchment.machine;

import com.bewitchment.registry.ModBlocks;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.items.*;

public final class SpinningWheelMenu extends Container {
    private final SpinningWheelTile tile;
    public final IIntArray data;
    public SpinningWheelMenu(int id, PlayerInventory player) { this(id, player, null, new ItemStackHandler(6), new IntArray(2)); }
    public SpinningWheelMenu(int id, PlayerInventory player, SpinningWheelTile tile) { this(id, player, tile, tile.inventory, tile.data); }
    private SpinningWheelMenu(int id, PlayerInventory player, SpinningWheelTile tile, ItemStackHandler inventory, IIntArray data) {
        super(ModMachines.SPINNING_WHEEL_MENU.get(), id); this.tile = tile; this.data = data;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            int x = i < 4 ? 44 + (i / 2) * 18 : 116;
            int y = 25 + (i % 2) * 18;
            addSlot(new SlotItemHandler(inventory, i, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return index < 4; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    @Override public boolean stillValid(PlayerEntity player) {
        return tile == null || (!tile.isRemoved() && player.level.getBlockState(tile.getBlockPos()).is(ModBlocks.SPINNING_WHEEL.get())
                && player.distanceToSqr(tile.getBlockPos().getX()+0.5, tile.getBlockPos().getY()+0.5, tile.getBlockPos().getZ()+0.5) <= 64);
    }
    @Override public ItemStack quickMoveStack(PlayerEntity player, int index) {
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 6) { if (!moveItemStackTo(stack, 6, 42, true)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, 4, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack); return original;
    }
}
