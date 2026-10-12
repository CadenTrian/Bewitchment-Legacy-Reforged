package com.bewitchment.machine;

import java.util.*;
/** One server-side reading per player at a time; invalid requests never pay. */
public final class CrystalBallReading {
    public static final int COST=750;
    public enum Result { RECEIVED, EXISTING, NO_POWER, TAGLOCK, UNAVAILABLE, BUSY }
    public interface Access {
        boolean eligible(); boolean taglock(); boolean pending();
        boolean pay(int amount); void assign();
    }
    private static final ThreadLocal<Set<UUID>> ACTIVE=ThreadLocal.withInitial(HashSet::new);
    public static Result read(UUID player,Access access) {
        Set<UUID> active=ACTIVE.get();if(!active.add(player))return Result.BUSY;
        try {
            if(!access.eligible())return Result.UNAVAILABLE;
            if(access.taglock())return Result.TAGLOCK;
            if(access.pending())return Result.EXISTING;
            if(!access.pay(COST))return Result.NO_POWER;
            // Payment in the game uses synchronous altar/book operations without events.
            // Still preserve a fortune assigned by a reentrant external payment adapter.
            if(access.pending())return Result.EXISTING;
            access.assign();return Result.RECEIVED;
        } finally {active.remove(player);if(active.isEmpty())ACTIVE.remove();}
    }
    public static FortuneData.Kind choose(int roll) {
        FortuneData.Kind[] pool=FortuneData.Kind.values();return pool[Math.floorMod(roll,pool.length)];
    }
    private CrystalBallReading(){}
}
