package com.bewitchment.machine;

import com.google.gson.JsonSyntaxException;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import java.util.Locale;

/** Named finite and continuous ritual effects. */
public enum RitualEffect {
    LESSER_CURSE_CLEANSING, GREATER_CURSE_CLEANSING, ITEM, SOLAR_GLORY, HIGH_MOON, DELUGE, DROUGHT, SANDS_OF_TIME, PERCEPTION, DRAW_SMALL, DRAW_MEDIUM, DRAW_LARGE,
    HUNGRY_FLAMES, FRENZIED_GROWTH, CONJURE_WITCH, CONJURE_WITHER, TELEPORT, LESSER_HELLMOUTH, NATURES_BLESSING, BIOME_SHIFT, SOWING_SALT, CALL_OF_THE_WILD, CONJURE_IMP, HELLMOUTH, GREATER_HELLMOUTH, SPIRITUAL_RIFT, WEDNESDAY, CONJURE_DEMON, CONJURE_LEONARD, CONJURE_BAPHOMET;

    public boolean cleansing() { return this == LESSER_CURSE_CLEANSING || this == GREATER_CURSE_CLEANSING; }
    public boolean landscape() { return this == BIOME_SHIFT || this == SOWING_SALT; }

    public boolean continuous() { return this == SANDS_OF_TIME || this == PERCEPTION; }
    public boolean consumesOnStart() { return continuous() || this == HUNGRY_FLAMES; }
    public boolean conjuration() { return this == CONJURE_WITCH || this == CONJURE_WITHER || this == CONJURE_IMP || this == CONJURE_DEMON || this == CONJURE_LEONARD || this == CONJURE_BAPHOMET; }
    public int drawingRing() { return this == DRAW_SMALL ? 0 : this == DRAW_MEDIUM ? 1 : this == DRAW_LARGE ? 2 : -1; }
    public boolean instant() { return drawingRing() >= 0; }

    public String id() { return name().toLowerCase(Locale.ROOT); }
    public static RitualEffect parse(String id) {
        for (RitualEffect effect : values()) if (effect.id().equals(id)) return effect;
        throw new JsonSyntaxException("Unknown ritual effect: " + id);
    }
    /** Only activation checks day/night: sunrise during a cast must not waste the offerings. */
    public String failure(boolean overworld, boolean daytime) {
        if (this == WEDNESDAY) return "wednesday_disabled";
        if (this == SPIRITUAL_RIFT) return daytime ? "rift_night" : null;
        if (this == CONJURE_IMP) return daytime ? "imp_night" : null;
        if (this == CONJURE_DEMON) return daytime ? "demon_night" : null;
        if (this == CONJURE_LEONARD) return daytime ? "leonard_night" : null;
        if (this == CONJURE_BAPHOMET) return daytime ? "baphomet_night" : null;
        if (cleansing() || this == ITEM || this == PERCEPTION || instant() || this == HUNGRY_FLAMES || this == FRENZIED_GROWTH || conjuration() || this == TELEPORT || (this == LESSER_HELLMOUTH || this == HELLMOUTH || this == GREATER_HELLMOUTH) || this == BIOME_SHIFT || this == CALL_OF_THE_WILD) return null;
        if (!overworld) return "overworld";
        if (this == SOLAR_GLORY && daytime) return "no_day";
        if (this == HIGH_MOON && !daytime) return "no_night";
        return null;
    }
    public String failure(ServerWorld world) {
        if (this == WEDNESDAY) return WednesdayRules.failure(com.bewitchment.config.RitualConfig.WEDNESDAY.get(), java.time.LocalDate.now());
        return (this == LESSER_HELLMOUTH || this == HELLMOUTH || this == GREATER_HELLMOUTH) ? LesserHellmouth.failure(world) : failure(world.dimension().equals(World.OVERWORLD), world.isDay());
    }

    public static long nextTime(long time, int target) {
        if (target < 0 || target >= 24000) throw new IllegalArgumentException("Invalid day phase");
        long delta = Math.floorMod(target - Math.floorMod(time, 24000L), 24000L);
        return time > Long.MAX_VALUE - delta ? Long.MAX_VALUE : time + delta;
    }
    public static int rainDuration(int roll) {
        if (roll < 0 || roll >= 8400) throw new IllegalArgumentException("Invalid rain roll");
        return 3600 + roll;
    }
    interface Environment {
        long dayTime();
        void setDayTime(long value);
        int rainRoll();
        void startRain(int ticks);
        void stopRain();
        void startStorm();
        void stopStorm();
    }
    void complete(Environment world) {
        switch (this) {
            case SOLAR_GLORY: world.setDayTime(nextTime(world.dayTime(), 6000)); break;
            case HIGH_MOON: world.setDayTime(nextTime(world.dayTime(), 17600)); break;
            case DELUGE: world.startRain(rainDuration(world.rainRoll())); break;
            case DROUGHT: world.stopRain(); break;
            case NATURES_BLESSING: world.startStorm(); break;
            case SOWING_SALT: world.stopStorm(); break;
            default: break;
        }
    }
    public void complete(ServerWorld world) {
        if (this == ITEM || !world.dimension().equals(World.OVERWORLD)) return;
        complete(new Environment() {
            public long dayTime() { return world.getDayTime(); }
            public void setDayTime(long value) { world.setDayTime(value); }
            public int rainRoll() { return world.random.nextInt(8400); }
            public void startRain(int ticks) {
                IServerWorldInfo data = (IServerWorldInfo)world.getLevelData();
                // A previous /weather clear timer would otherwise immediately undo the ritual.
                data.setClearWeatherTime(0);
                data.setRainTime(ticks);
                data.setRaining(true);
            }
            public void stopRain() { world.getLevelData().setRaining(false); }
            public void startStorm() {
                IServerWorldInfo data = (IServerWorldInfo)world.getLevelData();
                data.setClearWeatherTime(0);
                data.setRaining(true); data.setThundering(true);
            }
            public void stopStorm() {
                IServerWorldInfo data = (IServerWorldInfo)world.getLevelData();
                data.setRaining(false); data.setThundering(false);
            }
        });
    }
}
