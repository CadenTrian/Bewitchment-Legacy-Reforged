package com.bewitchment.entity;

public final class ShadowPersonRules {
    public static int savedLifetime(net.minecraft.nbt.CompoundNBT tag,int initial) {
        return tag.contains("lifeTimeTick",99)?Math.max(0,tag.getInt("lifeTimeTick")):initial;
    }
    public static boolean savedSpectral(net.minecraft.nbt.CompoundNBT tag) {
        return !tag.contains("spectral",1) || tag.getBoolean("spectral");
    }
    public static int skin(int skin) { return skin>=0 && skin<4 ? skin : 0; }
    public static int texture(int skin) { return skin(skin)%2; }
    public static int lifetime(int roll) {
        if(roll<0 || roll>30)throw new IllegalArgumentException("Invalid Shadow Person lifetime roll");
        return 600+20*roll;
    }
    public static boolean expired(boolean spectral,int remaining) { return spectral && remaining<=0; }
    public static boolean eyes(boolean invisible,boolean baby) { return !invisible && !baby; }
    public static boolean prey(boolean player,boolean villager,boolean illager,boolean witch,boolean golem) {
        return player || villager || illager || witch || golem;
    }
    private ShadowPersonRules() {}
}
