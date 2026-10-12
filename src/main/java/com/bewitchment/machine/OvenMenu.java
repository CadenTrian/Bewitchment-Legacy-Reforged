package com.bewitchment.machine;

import com.bewitchment.registry.ModBlocks;
import com.bewitchment.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tags.ItemTags;
import net.minecraft.inventory.Inventory;

public final class OvenMenu extends Container {
    private final OvenTile tile;
    public final IIntArray data;

    public OvenMenu(int id, PlayerInventory player) { this(id, player, null, new ItemStackHandler(5), new IntArray(3)); }
    public OvenMenu(int id, PlayerInventory player, OvenTile tile) { this(id, player, tile, tile.inventory, tile.data); }
    private OvenMenu(int id, PlayerInventory player, OvenTile tile, ItemStackHandler inventory, IIntArray data) {
        super(ModMachines.OVEN_MENU.get(), id);
        this.tile = tile; this.data = data;
        int[][] positions = {{44,55},{80,55},{44,19},{116,19},{116,55}};
        for (int i = 0; i < 5; i++) {
            final int index = i;
            addSlot(new SlotItemHandler(inventory, i, positions[i][0], positions[i][1]) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return index < 3 && (index == 0 ? ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0
                            : index == 1 ? stack.getItem() == ModItems.EMPTY_JAR.get() : true);
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    @Override public boolean stillValid(PlayerEntity player) {
        return tile == null || (!tile.isRemoved() && player.level.getBlockState(tile.getBlockPos()).is(ModBlocks.WITCHES_OVEN.get())
                && player.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5) <= 64);
    }
    @Override public ItemStack quickMoveStack(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(); ItemStack original = stack.copy();
        if (index < 5) {
            if (!moveItemStackTo(stack, 5, 41, true)) return ItemStack.EMPTY;
        } else {
            int target = stack.getItem() == ModItems.EMPTY_JAR.get() ? 1
                    : stack.getItem().is(ItemTags.SAPLINGS)
                        || player.level.getRecipeManager().getRecipeFor(OvenRecipe.TYPE, new Inventory(stack), player.level).isPresent()
                        || player.level.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, new Inventory(stack), player.level).isPresent() ? 2
                    : ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0 ? 0 : 2;
            if (!moveItemStackTo(stack, target, target + 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
