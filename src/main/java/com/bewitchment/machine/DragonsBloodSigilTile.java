package com.bewitchment.machine;

import com.bewitchment.item.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;

/** One server-owned sigil per trapdoor or lower door half. */
public final class DragonsBloodSigilTile extends TileEntity implements ITickableTileEntity {
    public final ChestSigilData sigil = new ChestSigilData();
    private boolean visible;
    public DragonsBloodSigilTile() { this(ModMachines.DRAGONS_BLOOD_SIGIL.get()); }
    DragonsBloodSigilTile(TileEntityType<?> type) { super(type); }
    public boolean hasSigil() { return level != null && level.isClientSide ? visible : sigil.kind() != null; }
    public void activate(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (level == null || level.isClientSide || player.isSpectator() || !player.isAlive()
                || player.distanceToSqr(worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5) > 64) return;
        ItemStack held = player.getItemInHand(hand);
        // Commit cooldown before effect callbacks; interactions with either half share this state.
        DoorSigilInteraction.Result result = DoorSigilInteraction.apply(sigil, player.getUUID(),
                held.getItem() instanceof ChestSigilItem ? ((ChestSigilItem)held.getItem()).kind : null,
                held.getItem() instanceof TaglockItem && TaglockItem.bound(held) ? held.getTag().getString("boundId") : null,
                player.abilities.mayBuild && level.mayInteract(player, worldPosition)
                        && player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), held),
                () -> held.shrink(1), kind -> { setChanged(); kind.apply(player); });
        if (result == DoorSigilInteraction.Result.INSTALLED) {
            changed(); player.displayClientMessage(new TranslationTextComponent("message.bewitchment.chest_sigil_installed"), true);
        } else if (result == DoorSigilInteraction.Result.TOGGLED || result == DoorSigilInteraction.Result.INVALID_TAGLOCK) {
            if (result == DoorSigilInteraction.Result.TOGGLED) setChanged();
            player.displayClientMessage(new TranslationTextComponent(result == DoorSigilInteraction.Result.TOGGLED
                    ? "message.bewitchment.chest_sigil_toggled" : "message.bewitchment.chest_sigil_invalid"), true);
        }
    }
    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public void tick() {
        if (level != null && !level.isClientSide && sigil.cooldown() > 0) { sigil.tick(); setChanged(); }
    }
    @Override public CompoundNBT save(CompoundNBT tag) { super.save(tag); tag.put("DoorSigil", sigil.save()); return tag; }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        sigil.load(tag.contains("DoorSigil", 10) ? tag.getCompound("DoorSigil") : tag);
        visible = sigil.kind() != null;
    }
    /** Send only presence for particles: no player list or cooldown in chunk/update packets. */
    @Override public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.save(new CompoundNBT()); tag.putBoolean("HasSigil", sigil.kind() != null); return tag;
    }
    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) { visible = packet.getTag().getBoolean("HasSigil"); }
    @Override public void handleUpdateTag(BlockState state, CompoundNBT tag) { visible = tag.getBoolean("HasSigil"); }
}
