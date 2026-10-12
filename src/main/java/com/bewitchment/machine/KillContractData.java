package com.bewitchment.machine;

import net.minecraft.nbt.CompoundNBT;
import java.util.UUID;

/** Only the two restored kill objectives are accepted; malformed tasks cannot redeem. */
public final class KillContractData {
    public final CurseData.Kind kind;
    public final int total,done;
    public final UUID owner;
    public final String name;
    private KillContractData(CurseData.Kind kind,int total,int done,UUID owner,String name){this.kind=kind;this.total=total;this.done=done;this.owner=owner;this.name=name;}
    public static boolean supported(CurseData.Kind kind){return kind==CurseData.Kind.BERSERKER || kind==CurseData.Kind.DEVOURING_RAGE;}
    public static CompoundNBT create(CurseData.Kind kind,int total){
        if(!supported(kind) || total<6 || total>10)throw new IllegalArgumentException("Unsupported kill contract");
        CompoundNBT tag=new CompoundNBT();tag.putString("contract",kind.id);tag.putInt("mobsTotal",total);tag.putInt("mobsComplete",0);return tag;
    }
    public static KillContractData read(CompoundNBT tag){
        if(tag==null || !tag.contains("contract",8) || !tag.contains("mobsTotal",3) || !tag.contains("mobsComplete",3) || tag.contains("items"))return null;
        CurseData.Kind kind=CurseData.Kind.fromId(tag.getString("contract"));int total=tag.getInt("mobsTotal"),done=tag.getInt("mobsComplete");
        if(!supported(kind) || total<6 || total>10 || done<0 || done>total)return null;
        UUID owner=null;String name="";
        if(tag.contains("boundId") || tag.contains("boundName")){
            if(!tag.contains("boundId",8) || !tag.contains("boundName",8))return null;
            owner=ChestSigilData.parse(tag.getString("boundId"));name=tag.getString("boundName");if(owner==null || name.isEmpty() || name.length()>64)return null;
        }
        if(owner==null && done!=0)return null;
        return new KillContractData(kind,total,done,owner,name);
    }
    public boolean complete(){return owner!=null && done==total;}
    public static boolean sign(CompoundNBT tag,UUID owner,String name){KillContractData data=read(tag);if(data==null || data.owner!=null || owner==null || name==null || name.isEmpty() || name.length()>64)return false;tag.putString("boundId",owner.toString());tag.putString("boundName",name);return true;}
    public static boolean advance(CompoundNBT tag,UUID player,boolean qualifies){KillContractData data=read(tag);if(data==null || !qualifies || player==null || !player.equals(data.owner) || data.complete())return false;tag.putInt("mobsComplete",data.done+1);return true;}
    public boolean redeem(UUID player,CurseData effects){return player!=null && player.equals(owner) && complete() && effects.add(kind,7);}
}
