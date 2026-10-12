package com.bewitchment.machine;

import net.minecraft.nbt.CompoundNBT;
import java.util.*;
import java.util.function.DoubleSupplier;

/** Only implemented curse identities are accepted; duplicate application keeps the old duration. */
public final class CurseData {
    public static final int MAX_DAYS=Integer.MAX_VALUE/24000;
    public enum Kind {
        DARKNESS("darkness",true,.0005), MISFORTUNE("misfortune",true,.0005),
        CONFLAGRATION("conflagration",true,1), OVERHEATING("overheating",true,.001),
        HYDROPHOBIA("hydrophobia",true,1), SOLAR_HATRED("solar_hatred",false,1),
        LIGHTNING_ROD("lightning_rod",false,.002), PALOR_PESTILENCE("palor_pestilence",false,.0004), PARANOIA("paranoia",false,1), RETURN_TO_SENDER("return_to_sender",false,0), EVERFLAME("everflame",false,.0002),
        BERSERKER("berserker",true,0), SALAMANDRINE_TOLERANCE("salamandrine_tolerance",true,0), DEVOURING_RAGE("devouring_rage",true,0), DESICCATION("desiccation",true,0), IGNITION("ignition",true,0), MAHAPADMA("mahapadma",false,0);
        public final String id;
        public final double chance;
        private final boolean lesser;
        Kind(String path,boolean lesser,double chance){id="bewitchment:"+path;this.lesser=lesser;this.chance=chance;}
        public boolean lesser(){return lesser;}
        public boolean positive(){return this==RETURN_TO_SENDER || this==BERSERKER || this==SALAMANDRINE_TOLERANCE || this==DEVOURING_RAGE || this==DESICCATION || this==IGNITION;}
        public static Kind fromId(String id){for(Kind kind:values())if(kind.id.equals(id))return kind;return null;}
    }
    public static final int PARANOIA_DELAY=1150;
    private int paranoiaDelay=PARANOIA_DELAY;
    public int paranoiaDelay(){return paranoiaDelay;}
    public boolean paranoiaTick(int roll){
        if(remaining(Kind.PARANOIA)==0)return false;
        if(paranoiaDelay>0)paranoiaDelay--;
        if(paranoiaDelay==0 && roll>=0 && roll<10){paranoiaDelay=PARANOIA_DELAY;return true;}
        return false;
    }
    private final EnumMap<Kind,Integer> timers=new EnumMap<>(Kind.class);
    public int remaining(Kind kind){return timers.getOrDefault(kind,0);}
    public Set<Kind> active(){return Collections.unmodifiableSet(EnumSet.copyOf(timers.isEmpty()?EnumSet.noneOf(Kind.class):timers.keySet()));}
    public boolean add(Kind kind,int days){if(kind==null || days<1 || days>MAX_DAYS || timers.containsKey(kind))return false;timers.put(kind,days*24000);if(kind==Kind.PARANOIA)paranoiaDelay=PARANOIA_DELAY;return true;}
    public boolean remove(Kind kind){return timers.remove(kind)!=null;}
    public int clear(){int count=timers.size();timers.clear();return count;}
    public void second(){timers.replaceAll((kind,time)->time-20);timers.values().removeIf(time->time<=0);}
    public int purify(DoubleSupplier random){
        int removed=0;
        for(Kind kind:new ArrayList<>(timers.keySet()))if(kind.lesser() && !kind.positive()){
            double roll=random.getAsDouble();if(roll>=0 && roll<.5 && remove(kind))removed++;
        }
        return removed;
    }
    /** Rituals roll independently for harmful curses; blessings and ineligible tiers survive. */
    public int cleanse(boolean lesser,DoubleSupplier random){
        int removed=0;
        for(Kind kind:new ArrayList<>(timers.keySet()))if(!kind.positive() && (!lesser || kind.lesser())){
            double roll=random.getAsDouble();
            if(roll>=0 && roll<(lesser?.6:.9) && remove(kind))removed++;
        }
        return removed;
    }
    public CompoundNBT save(){CompoundNBT tag=new CompoundNBT();timers.forEach((kind,time)->tag.putInt(kind.id,time));if(remaining(Kind.PARANOIA)>0)tag.putInt("paranoiaDelay",paranoiaDelay);return tag;}
    public void load(CompoundNBT tag){timers.clear();paranoiaDelay=tag.contains("paranoiaDelay",3)?Math.max(0,Math.min(PARANOIA_DELAY,tag.getInt("paranoiaDelay"))):PARANOIA_DELAY;for(Kind kind:Kind.values())if(tag.contains(kind.id,3)){int time=tag.getInt(kind.id);if(time>0)timers.put(kind,time);}}
}
