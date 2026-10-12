package com.bewitchment.machine;

import net.minecraft.item.*;
import net.minecraft.nbt.*;
import java.util.UUID;

public final class DonationContractData {
    public final CurseData.Kind kind;
    public final UUID owner;
    public final String name;
    private final int[] totals,done;
    private DonationContractData(CurseData.Kind kind,UUID owner,String name,int[] totals,int[] done){this.kind=kind;this.owner=owner;this.name=name;this.totals=totals;this.done=done;}
    public static boolean supported(CurseData.Kind kind){return kind==CurseData.Kind.SALAMANDRINE_TOLERANCE || kind==CurseData.Kind.DESICCATION || kind==CurseData.Kind.IGNITION || kind==CurseData.Kind.MAHAPADMA;}
    public static Item ingredient(CurseData.Kind kind,int index){if(!supported(kind) || index<0 || index>=size(kind))throw new IllegalArgumentException("Invalid donation ingredient");return kind==CurseData.Kind.MAHAPADMA?(index==0?Items.ICE:index==1?Items.WATER_BUCKET:Items.SLIME_BALL):kind==CurseData.Kind.IGNITION?(index==0?Items.BLAZE_ROD:Items.MAGMA_CREAM):kind==CurseData.Kind.SALAMANDRINE_TOLERANCE?(index==0?Items.BLAZE_ROD:Items.GHAST_TEAR):(index==0?Items.FERMENTED_SPIDER_EYE:Items.POISONOUS_POTATO);}
    public static int size(CurseData.Kind kind){return kind==CurseData.Kind.MAHAPADMA?3:2;}
    private static boolean validTotal(CurseData.Kind kind,int index,int total){return kind==CurseData.Kind.MAHAPADMA && index==1?total==1:total>=6 && total<=11;}
    public int total(int index){return totals[index];}
    public int done(int index){return done[index];}
    public static CompoundNBT create(CurseData.Kind kind,int... amounts){
        if(!supported(kind) || amounts==null || amounts.length!=size(kind))throw new IllegalArgumentException("Invalid donation task");
        for(int i=0;i<amounts.length;i++)if(!validTotal(kind,i,amounts[i]))throw new IllegalArgumentException("Invalid donation total");
        CompoundNBT tag=new CompoundNBT();tag.putString("contract",kind.id);ListNBT list=new ListNBT();
        for(int i=0;i<size(kind);i++){CompoundNBT entry=new CompoundNBT();entry.putString("item",ingredient(kind,i).getRegistryName().toString());entry.putInt("amountTotal",amounts[i]);entry.putInt("amountComplete",0);list.add(entry);}tag.put("items",list);return tag;
    }
    public static DonationContractData read(CompoundNBT tag){
        if(tag==null || !tag.contains("contract",8) || !tag.contains("items",9) || tag.contains("mobsTotal") || tag.contains("mobsComplete"))return null;
        CurseData.Kind kind=CurseData.Kind.fromId(tag.getString("contract"));if(!supported(kind))return null;
        ListNBT list=tag.getList("items",10);if(list.size()!=size(kind))return null;int[] totals=new int[size(kind)],done=new int[size(kind)];
        for(int i=0;i<size(kind);i++){CompoundNBT entry=list.getCompound(i);if(!entry.contains("item",8) || !entry.getString("item").equals(ingredient(kind,i).getRegistryName().toString()) || !entry.contains("amountTotal",3) || !entry.contains("amountComplete",3))return null;totals[i]=entry.getInt("amountTotal");done[i]=entry.getInt("amountComplete");if(!validTotal(kind,i,totals[i]) || done[i]<0 || done[i]>totals[i])return null;}
        UUID owner=null;String name="";
        if(tag.contains("boundId") || tag.contains("boundName")){if(!tag.contains("boundId",8) || !tag.contains("boundName",8))return null;owner=ChestSigilData.parse(tag.getString("boundId"));name=tag.getString("boundName");if(owner==null || name.isEmpty() || name.length()>64)return null;}
        if(owner==null)for(int progress:done)if(progress!=0)return null;
        return new DonationContractData(kind,owner,name,totals,done);
    }
    public boolean complete(){if(owner==null)return false;for(int i=0;i<totals.length;i++)if(done[i]!=totals[i])return false;return true;}
    public static boolean sign(CompoundNBT tag,UUID owner,String name){DonationContractData data=read(tag);if(data==null || data.owner!=null || owner==null || name==null || name.isEmpty() || name.length()>64)return false;tag.putString("boundId",owner.toString());tag.putString("boundName",name);return true;}
    public static int donate(CompoundNBT tag,UUID player,ItemStack offered){
        DonationContractData data=read(tag);if(data==null || player==null || !player.equals(data.owner) || offered.isEmpty())return 0;
        for(int i=0;i<size(data.kind);i++)if(offered.getItem()==ingredient(data.kind,i)){int count=Math.min(offered.getCount(),data.totals[i]-data.done[i]);if(count>0){offered.shrink(count);tag.getList("items",10).getCompound(i).putInt("amountComplete",data.done[i]+count);}return count;}return 0;
    }
    public boolean redeem(UUID player,CurseData effects){return player!=null && player.equals(owner) && complete() && effects.add(kind,kind.positive()?7:1);}
}
