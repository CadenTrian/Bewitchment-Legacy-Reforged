package com.bewitchment.machine;

import com.bewitchment.world.DemonPledges;
import com.bewitchment.world.DemonPledges.Patron;
import com.bewitchment.combat.PledgeEffects;
import com.bewitchment.entity.*;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class DemonPledgesCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID alice=UUID.fromString("12345678-1234-1234-1234-123456789012"),bob=UUID.fromString("23456789-2345-2345-2345-234567890123");
        DemonPledges data=new DemonPledges();
        check(data.patrons(alice).isEmpty(),"New player has no allegiance");
        for(Patron patron:Patron.values()){
            check(Patron.parse(patron.id)==patron,"Patron ID round-trip");
            data.setDirty(false);check(data.add(alice,patron) && data.isDirty(),"New pledge marks saved data dirty");
            data.setDirty(false);check(!data.add(alice,patron) && !data.isDirty(),"Duplicate is a clean no-op");
            check(data.has(alice,patron) && !data.has(bob,patron),"Allegiance belongs to one UUID");
        }
        check(data.patrons(alice).size()==2,"Legacy simultaneous pledges supported");
        boolean immutable=false;try{data.patrons(alice).clear();}catch(UnsupportedOperationException ex){immutable=true;}check(immutable && data.patrons(alice).size()==2,"Read view cannot mutate allegiance");
        for(String bad:new String[]{"", "BAPHOMET", "bewitchment:baphomet", "demon", null})check(Patron.parse(bad)==null,"Unknown IDs rejected");
        check(!data.add(null,Patron.BAPHOMET) && !data.add(alice,null) && !data.has(null,Patron.BAPHOMET),"Null identities rejected");
        CompoundNBT saved=data.save(new CompoundNBT());DemonPledges loaded=new DemonPledges();loaded.load(saved);
        check(loaded.patrons(alice).equals(data.patrons(alice)),"Both pledges survive NBT reload");
        check(saved.equals(loaded.save(new CompoundNBT())),"Deterministic serialized order");
        loaded.setDirty(false);check(loaded.remove(alice,Patron.BAPHOMET) && loaded.isDirty() && loaded.has(alice,Patron.LEONARD),"Revocation only affects named patron");
        loaded.setDirty(false);check(!loaded.remove(alice,Patron.BAPHOMET) && !loaded.isDirty(),"Duplicate removal is clean");
        check(loaded.clear(alice)==1 && loaded.clear(alice)==0,"Clear counts actual changes");
        check(data.has(alice,Patron.BAPHOMET),"Reloaded instance independent of original");
        data.add(bob,Patron.BAPHOMET);check(data.clear(alice)==2 && data.has(bob,Patron.BAPHOMET),"Clearing one player preserves another");
        for(int version:new int[]{-1,0,2,Integer.MAX_VALUE}){CompoundNBT malformed=saved.copy();malformed.putInt("version",version);loaded.load(malformed);check(loaded.patrons(alice).isEmpty(),"Unsupported schema fails closed");}
        CompoundNBT malformed=saved.copy();malformed.remove("version");loaded.load(malformed);check(loaded.patrons(alice).isEmpty(),"Missing schema does not grant pledges");
        malformed=saved.copy();malformed.putString("version","1");loaded.load(malformed);check(loaded.patrons(alice).isEmpty(),"Wrong version type rejected");
        malformed=saved.copy();ListNBT entries=malformed.getList("demonPledges",10);ListNBT members=entries.getCompound(0).getList("players",8);
        members.add(StringNBT.valueOf("1-1-1-1-1"));members.add(StringNBT.valueOf("not-a-uuid"));members.add(StringNBT.valueOf(alice.toString()));
        CompoundNBT duplicate=new CompoundNBT();duplicate.putString("demon","baphomet");ListNBT second=new ListNBT();second.add(StringNBT.valueOf(bob.toString()));duplicate.put("players",second);entries.add(duplicate);
        CompoundNBT unknown=duplicate.copy();unknown.putString("demon","demon");entries.add(unknown);loaded.load(malformed);
        check(loaded.has(alice,Patron.BAPHOMET) && loaded.has(bob,Patron.BAPHOMET),"Duplicate rows merge valid UUIDs");
        check(loaded.save(new CompoundNBT()).getList("demonPledges",10).getCompound(0).getList("players",8).size()==2,"Invalid and duplicate UUIDs discarded");
        loaded.load(new CompoundNBT());check(loaded.patrons(alice).isEmpty() && loaded.patrons(bob).isEmpty(),"Reload replaces previous state");
        for(Class<?> type:new Class<?>[]{BafometyrEntity.class,FeuerwurmEntity.class})check(PledgeEffects.protector(type)==Patron.BAPHOMET,"Baphomet's protected creatures");
        check(PledgeEffects.protector(CambionEntity.class)==Patron.LEONARD,"Leonard's protected creature");
        for(Class<?> type:new Class<?>[]{DemonEntity.class,DemonessEntity.class,ImpEntity.class,HellhoundEntity.class,net.minecraft.entity.monster.ZombieEntity.class})check(PledgeEffects.protector(type)==null,"Unrelated creatures remain hostile");
        check(!Pledgeable.class.isAssignableFrom(DemonEntity.class) && !Pledgeable.class.isAssignableFrom(DemonessEntity.class),"Merchant variants do not grant greater-demon pledges");
        for(Patron patron:Patron.values())for(String item:new String[]{"pentacle","green_candle","red_candle","stew_of_the_grotesque"})check(PledgeEffects.token(patron,new ResourceLocation("bewitchment",item))==(item.equals(patron==Patron.BAPHOMET?"pentacle":"green_candle")),"Exact patron token mapping");
        check(!PledgeEffects.token(Patron.BAPHOMET,new ResourceLocation("other:pentacle")) && !PledgeEffects.token(Patron.BAPHOMET,null) && !PledgeEffects.token(null,new ResourceLocation("bewitchment:pentacle")),"Token namespace and null guards");
        System.out.println("PASS: "+checks+" demon pledge persistence, corruption, isolation, protection and token checks");
    }
}