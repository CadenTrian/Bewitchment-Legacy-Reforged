package com.bewitchment.machine;

/** Server-side contract for linked altar power sources. Draining must be all-or-nothing. */
public interface MagicPowerSource {
    boolean tryDrainMagic(int amount);
}
