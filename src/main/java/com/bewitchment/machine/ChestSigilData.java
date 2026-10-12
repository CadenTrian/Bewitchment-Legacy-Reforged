package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.nbt.*;
import java.util.*;

/** Server-owned targeting and shared, loaded-tick cooldown. Never sent with inventory packets. */
public final class ChestSigilData {
    private Kind kind;
    private int cooldown;
    private final Set<UUID> players=new HashSet<>();
    public Kind kind(){return kind;}
    public int cooldown(){return cooldown;}
    public boolean install(Kind next,UUID installer){if(kind!=null || next==null || installer==null)return false;kind=next;players.clear();players.add(installer);cooldown=0;return true;}
    public static UUID parse(String value){if(value==null)return null;try{UUID id=UUID.fromString(value);return id.toString().equalsIgnoreCase(value)?id:null;}catch(IllegalArgumentException ex){return null;}}
    public boolean toggle(String value){UUID id=parse(value);if(kind==null || id==null)return false;if(!players.remove(id))players.add(id);return true;}
    public boolean targets(UUID player){return kind!=null && players.contains(player)==kind.positive;}
    public boolean trigger(UUID player){if(cooldown>0 || !targets(player))return false;cooldown=kind.cooldown;return true;}
    public void tick(){if(cooldown>0)cooldown--;}
    public CompoundNBT save(){
        CompoundNBT tag=new CompoundNBT();tag.putString("sigil",kind==null?"":kind.id());tag.putInt("cooldown",cooldown);
        ListNBT list=new ListNBT();players.stream().map(UUID::toString).sorted().forEach(id->list.add(StringNBT.valueOf(id)));tag.put("playerList",list);return tag;
    }
    public void load(CompoundNBT tag){
        players.clear();kind=Kind.fromId(tag.getString("sigil"));cooldown=kind==null?0:Math.max(0,Math.min(kind.cooldown,tag.getInt("cooldown")));
        if(kind==null)return;
        ListNBT list=tag.getList("playerList",8);for(int i=0;i<list.size();i++){UUID id=parse(list.getString(i));if(id!=null)players.add(id);}
    }
}
