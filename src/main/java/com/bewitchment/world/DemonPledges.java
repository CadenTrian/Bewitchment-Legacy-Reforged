package com.bewitchment.world;

import com.bewitchment.machine.ChestSigilData;
import net.minecraft.nbt.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import java.util.*;

/** Server-wide allegiance, stored in the Overworld so travel and respawn preserve it. */
public final class DemonPledges extends WorldSavedData {
    public static final String ID="bewitchment_demon_pledges";
    public enum Patron {
        BAPHOMET("baphomet"), LEONARD("leonard");
        public final String id;
        Patron(String id){this.id=id;}
        public static Patron parse(String id){for(Patron patron:values())if(patron.id.equals(id))return patron;return null;}
    }
    private final EnumMap<Patron,Set<UUID>> players=new EnumMap<>(Patron.class);
    public DemonPledges(){super(ID);for(Patron patron:Patron.values())players.put(patron,new HashSet<>());}
    public static DemonPledges get(ServerWorld world){return world.getServer().overworld().getDataStorage().computeIfAbsent(DemonPledges::new,ID);}
    public boolean has(UUID player,Patron patron){return player!=null && patron!=null && players.get(patron).contains(player);}
    public boolean add(UUID player,Patron patron){if(player==null || patron==null)return false;boolean changed=players.get(patron).add(player);if(changed)setDirty();return changed;}
    public boolean remove(UUID player,Patron patron){if(player==null || patron==null)return false;boolean changed=players.get(patron).remove(player);if(changed)setDirty();return changed;}
    public Set<Patron> patrons(UUID player){EnumSet<Patron> result=EnumSet.noneOf(Patron.class);for(Patron patron:Patron.values())if(has(player,patron))result.add(patron);return Collections.unmodifiableSet(result);}
    public int clear(UUID player){int count=0;for(Patron patron:Patron.values())if(remove(player,patron))count++;return count;}
    @Override public CompoundNBT save(CompoundNBT tag){
        tag.putInt("version",1);ListNBT list=new ListNBT();
        for(Patron patron:Patron.values()){
            CompoundNBT entry=new CompoundNBT();entry.putString("demon",patron.id);ListNBT members=new ListNBT();
            players.get(patron).stream().map(UUID::toString).sorted().forEach(id->members.add(StringNBT.valueOf(id)));
            entry.put("players",members);list.add(entry);
        }
        tag.put("demonPledges",list);return tag;
    }
    @Override public void load(CompoundNBT tag){
        players.values().forEach(Set::clear);
        if(!tag.contains("version",3) || tag.getInt("version")!=1)return;
        ListNBT list=tag.getList("demonPledges",10);
        for(int i=0;i<list.size();i++){
            CompoundNBT entry=list.getCompound(i);Patron patron=Patron.parse(entry.getString("demon"));if(patron==null)continue;
            ListNBT members=entry.getList("players",8);
            for(int j=0;j<members.size();j++){UUID id=ChestSigilData.parse(members.getString(j));if(id!=null)players.get(patron).add(id);}
        }
    }
}