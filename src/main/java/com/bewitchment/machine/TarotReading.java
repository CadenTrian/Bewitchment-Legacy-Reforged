package com.bewitchment.machine;

import java.util.*;
import net.minecraft.nbt.CompoundNBT;

/** Paid, read-only target inspection; no fortune assignment or countdown changes. */
public final class TarotReading {
    public static final int COST=1000;
    public interface Access {
        boolean eligible(); UUID target(); boolean online(); boolean pay(int amount);
        boolean shrouded(); String reveal();
    }
    private static final ThreadLocal<Set<UUID>> ACTIVE=ThreadLocal.withInitial(HashSet::new);
    public static String read(UUID reader,Access access) {
        Set<UUID> active=ACTIVE.get();if(!active.add(reader))return "busy";
        try {
            if(!access.eligible())return "unavailable";
            if(access.target()==null)return "invalid";
            if(!access.online())return "offline";
            if(!access.pay(COST))return "no_power";
            if(access.shrouded())return "shrouded";
            return access.reveal();
        } finally {active.remove(reader);if(active.isEmpty())ACTIVE.remove();}
    }
    public static UUID boundId(CompoundNBT tag) {
        if(tag==null||!tag.contains("boundId",8))return null;
        String value=tag.getString("boundId");
        try {UUID id=UUID.fromString(value);return id.toString().equalsIgnoreCase(value)?id:null;}
        catch(IllegalArgumentException error){return null;}
    }
    /** Inputs are in head/chest/legs/feet order; unrelated carried armor does not count. */
    public static boolean shrouded(String... armor) {
        String[] expected={"cold_iron_helmet","cold_iron_chestplate","cold_iron_leggings","cold_iron_boots"};
        if(armor.length!=4)return false;
        for(int i=0;i<4;i++)if(!("bewitchment:"+expected[i]).equals(armor[i]))return false;
        return true;
    }
    public static String reveal(int roll,FortuneData.Kind fortune,boolean effects,boolean cursed,String attire) {
        switch(Math.floorMod(roll,5)) {
            case 0:return "nothing";
            case 1:return fortune==null?"no_fortune":fortune.negative?"bad_fortune":"good_fortune";
            case 2:return effects?"has_effects":"no_effects";
            case 3:return cursed?"cursed":"not_cursed";
            default:
                if("green_witch".equals(attire))return "green_witch";
                if("besmirched".equals(attire))return "besmirched";
                if("alchemist".equals(attire))return "alchemist";
                if("witches".equals(attire))return "witches";
                return "no_attire";
        }
    }
    private TarotReading(){}
}
