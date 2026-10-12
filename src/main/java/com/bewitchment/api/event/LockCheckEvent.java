package com.bewitchment.api.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Pre/key cancellation grants access. Post cancellation denies it before any payment.
 * Simulation events must never mutate inventory, experience or world state.
 * Targets are immutable in this port; a door always reports its lower half. */
@Cancelable
public class LockCheckEvent extends Event {
    private final PlayerEntity user;
    private final BlockPos lock;
    private final boolean simulation;
    public LockCheckEvent(PlayerEntity user,BlockPos lock,boolean simulation){this.user=user;this.lock=lock.immutable();this.simulation=simulation;}
    public PlayerEntity getUser(){return user;}
    public BlockPos getLock(){return lock;}
    public boolean isSimulation(){return simulation;}
    @Cancelable
    public static class KeyCheckEvent extends LockCheckEvent {
        private final ItemStack key;
        public KeyCheckEvent(PlayerEntity user,BlockPos lock,boolean simulation,ItemStack key){super(user,lock,simulation);this.key=key.copy();}
        public ItemStack getKey(){return key.copy();}
    }
    @Cancelable
    public static class LockCheckedEvent extends LockCheckEvent {
        private boolean opened,sendMessage;
        public LockCheckedEvent(PlayerEntity user,BlockPos lock,boolean simulation,boolean opened,boolean sendMessage){super(user,lock,simulation);this.opened=opened;this.sendMessage=sendMessage;}
        public boolean isOpened(){return opened;}
        public void setResult(boolean opened){this.opened=opened;}
        public boolean shouldSendMessage(){return sendMessage;}
        public void setSendMessage(boolean value){sendMessage=value;}
    }
}
