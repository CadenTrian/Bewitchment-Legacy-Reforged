package com.bewitchment.machine;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;

public final class PoppetShelfTile extends TileEntity {
    public final ShelfInventory inventory = new ShelfInventory(this::changed);
    private LazyOptional<IItemHandler> capability = LazyOptional.of(() -> inventory);
    private boolean capsInvalid;
    public PoppetShelfTile() { super(ModMachines.POPPET_SHELF.get()); }
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public void interact(PlayerEntity player, int slot) {
        if (level == null || level.isClientSide || slot < 0 || slot >= inventory.getSlots()) return;
        if (inventory.getStackInSlot(slot).isEmpty()) {
            ItemStack held = player.getMainHandItem();
            ItemStack remaining = inventory.insertItem(slot, held, false);
            player.setItemInHand(net.minecraft.util.Hand.MAIN_HAND, remaining);
        } else {
            ItemStack taken = inventory.extractItem(slot, 64, false);
            ItemHandlerHelper.giveItemToPlayer(player, taken);
        }
        player.inventory.setChanged();
    }
    @Override public void onLoad() {
        super.onLoad();
        if (capsInvalid) reviveCaps();
        PoppetShelfNetwork.add(level, worldPosition);
    }
    @Override public void setRemoved() { PoppetShelfNetwork.remove(level, worldPosition); super.setRemoved(); }
    @Override public void onChunkUnloaded() { PoppetShelfNetwork.remove(level, worldPosition); super.onChunkUnloaded(); }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); capsInvalid = true; }
    @Override public void reviveCaps() { super.reviveCaps(); capability = LazyOptional.of(() -> inventory); capsInvalid = false; }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return capability.cast();
        return super.getCapability(cap, side);
    }
    @Override public CompoundNBT save(CompoundNBT tag) { super.save(tag); tag.put("ItemStackHandler", inventory.serializeNBT()); return tag; }
    @Override public void load(BlockState state, CompoundNBT tag) { super.load(state, tag); inventory.deserializeNBT(tag.getCompound("ItemStackHandler")); }
    @Override public CompoundNBT getUpdateTag() { return save(new CompoundNBT()); }
    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) { load(getBlockState(), packet.getTag()); }
}
