package com.bewitchment.machine;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

public final class PlacedItemTile extends TileEntity {
    private ItemStack item = ItemStack.EMPTY;
    public PlacedItemTile() { super(ModMachines.PLACED_ITEM.get()); }
    public ItemStack getItem() { return item.copy(); }
    public void setItem(ItemStack stack) {
        item = stack.copy(); if (!item.isEmpty()) item.setCount(1); changed();
    }
    public ItemStack takeItem() { ItemStack result = item; item = ItemStack.EMPTY; changed(); return result; }
    private void changed() {
        setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public CompoundNBT save(CompoundNBT tag) { super.save(tag); tag.put("Item", item.save(new CompoundNBT())); return tag; }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); item = ItemStack.of(tag.getCompound("Item")); if (!item.isEmpty()) item.setCount(1);
    }
    @Override public CompoundNBT getUpdateTag() { return save(new CompoundNBT()); }
    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager network, SUpdateTileEntityPacket packet) { load(getBlockState(), packet.getTag()); }
}
