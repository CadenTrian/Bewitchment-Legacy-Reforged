package com.bewitchment.machine;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import java.util.UUID;

public final class SiphoningFlowerTile extends TileEntity {
    public UUID owner, boundId;
    public String boundName = "";
    public SiphoningFlowerTile() { super(ModMachines.SIPHONING_FLOWER.get()); }
    public void changed() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    public void clear() { boundId = null; boundName = ""; changed(); }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        if (boundId != null) { tag.putUUID("Bound", boundId); tag.putString("BoundName", boundName); }
        return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        boundId = tag.hasUUID("Bound") ? tag.getUUID("Bound") : null;
        boundName = boundId == null ? "" : tag.getString("BoundName");
    }
    @Override public CompoundNBT getUpdateTag() { return save(new CompoundNBT()); }
    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) { load(getBlockState(), packet.getTag()); }
}
