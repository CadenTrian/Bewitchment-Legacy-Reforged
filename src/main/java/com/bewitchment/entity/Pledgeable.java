package com.bewitchment.entity;

import com.bewitchment.world.DemonPledges.Patron;
import net.minecraft.entity.player.PlayerEntity;

/** Hook for greater-demon entities; ordinary Demon merchants are not pledgeable. */
public interface Pledgeable {
    Patron patron();
    default void pledgeChanged(PlayerEntity player,boolean pledged){}
}