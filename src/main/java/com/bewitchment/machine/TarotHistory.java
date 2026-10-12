package com.bewitchment.machine;

import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.*;
import java.util.UUID;
import java.util.function.ToIntFunction;

/** History derives from saved vanilla statistics; no parallel kill counters. */
public final class TarotHistory {
    public enum Category { NOTHING, FORTUNE, EFFECTS, CURSES, ATTIRE, MONSTER_KILLS, BOSSES, PLEDGES, PEOPLE_KILLS, COMPANIONS, PROTECTION }
    public static Category choose(int roll){Category[] pool=Category.values();return pool[Math.floorMod(roll,pool.length)];}
    public static int add(int total,int count){return (int)Math.min(Integer.MAX_VALUE,(long)Math.max(0,total)+Math.max(0,count));}
    public static int monsterKills(Iterable<EntityType<?>> types,ToIntFunction<EntityType<?>> kills){
        int total=0;
        for(EntityType<?> type:types)if(type.getCategory()==EntityClassification.MONSTER)total=add(total,kills.applyAsInt(type));
        return total;
    }
    public static int peopleKills(int players,int villagers){return add(players,villagers);}
    public static boolean defeatedBoss(int... counts){for(int count:counts)if(count>0)return true;return false;}
    public static boolean pledged(DemonPledges data,UUID target){return data.has(target,DemonPledges.Patron.LEONARD)||data.has(target,DemonPledges.Patron.BAPHOMET);}
    private TarotHistory(){}
}
