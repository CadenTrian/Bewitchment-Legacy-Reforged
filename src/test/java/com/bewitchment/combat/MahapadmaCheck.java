package com.bewitchment.combat;

import com.bewitchment.machine.*;
import com.bewitchment.machine.CurseData.Kind;
import com.bewitchment.item.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.*;

public final class MahapadmaCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> d,CommandSource s,String t){com.mojang.brigadier.ParseResults<CommandSource> r=d.parse(t,s);return !r.getReader().canRead() && r.getContext().getCommand()!=null;}
    public static void main(String[] args){
        Bootstrap.bootStrap();Kind kind=Kind.MAHAPADMA;UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
        check(!kind.lesser() && !kind.positive() && kind.chance==0,"One-shot greater negative identity");
        BlockPos center=new BlockPos(20,64,-20);List<BlockPos> cells=MahapadmaCurse.candidates(center,p->true);
        check(cells.size()==45 && new HashSet<>(cells).size()==45,"Exactly 45 distinct immutable positions");
        check(cells.contains(center.offset(-1,-2,-1)) && cells.contains(center.offset(1,2,1)),"Inclusive enclosure bounds");
        check(cells.stream().allMatch(p->Math.abs(p.getX()-20)<=1 && Math.abs(p.getY()-64)<=2 && Math.abs(p.getZ()+20)<=1),"No writes outside enclosure");
        check(MahapadmaCurse.candidates(center,p->false).isEmpty(),"Denied terrain excluded");
        check(MahapadmaCurse.candidates(center,p->p.getY()==64).size()==9,"Mixed terrain filter");
        CurseData pending=new CurseData();pending.add(kind,1);pending.add(Kind.DARKNESS,2);
        check(!MahapadmaCurse.consume(pending,false,false) && pending.remaining(kind)==24000,"Dead player defers effect");
        check(!MahapadmaCurse.consume(pending,true,true) && pending.remaining(kind)==24000,"Spectator defers effect");
        check(pending.purify(()->0)==1 && pending.remaining(kind)==24000,"Purity leaves greater contract");
        check(MahapadmaCurse.consume(pending,true,false) && pending.remaining(kind)==0,"Eligible tick consumes pending effect");
        CurseData reloaded=new CurseData();reloaded.load(pending.save());
        check(!MahapadmaCurse.consume(reloaded,true,false),"Consumed effect cannot replay after persistence");
        for(int amount=6;amount<=11;amount++){
            CompoundNBT tag=DonationContractData.create(kind,amount,1,17-amount);
            check(DonationContractData.read(tag)!=null && !DonationContractData.read(tag).complete(),"Valid unsigned triple");
            check(DonationContractData.donate(tag,owner,new ItemStack(Items.ICE,64))==0,"Unsigned task cannot donate");
            check(DonationContractData.sign(tag,owner,"Signer"),"Sign triple task");
            check(DonationContractData.donate(tag,other,new ItemStack(Items.ICE,64))==0,"Other owner cannot donate");
            ItemStack ice=new ItemStack(Items.ICE,64);ice.getOrCreateTag().putString("test","preserved");
            check(DonationContractData.donate(tag,owner,ice)==amount && ice.getCount()==64-amount && ice.getTag().getString("test").equals("preserved"),"Exact ice donation preserves leftovers");
            check(DonationContractData.donate(tag,owner,new ItemStack(Items.PACKED_ICE,64))==0,"Packed ice not a substitute");
            check(!DonationContractData.read(tag).complete(),"One ingredient insufficient");
            ItemStack water=new ItemStack(Items.WATER_BUCKET);check(DonationContractData.donate(tag,owner,water)==1 && water.isEmpty(),"Water bucket consumed including container");
            check(DonationContractData.donate(tag,owner,new ItemStack(Items.WATER_BUCKET))==0,"Completed water quota consumes nothing");
            check(!DonationContractData.read(tag).complete(),"Two ingredients insufficient");
            check(DonationContractData.donate(tag,owner,new ItemStack(Items.SLIME_BALL,64))==17-amount,"Third ingredient donated");
            DonationContractData complete=DonationContractData.read(tag.copy());check(complete.complete(),"All counters survive NBT copy");
            CurseData target=new CurseData();check(!complete.redeem(other,target) && target.active().isEmpty(),"Wrong signer cannot redeem onto target");
            check(complete.redeem(owner,target) && target.remaining(kind)==24000,"Negative redemption queues one day");
            target.second();check(!complete.redeem(owner,target) && target.remaining(kind)==23980,"Duplicate preserves timer");
            check(MahapadmaCurse.consume(target,true,false) && complete.redeem(owner,target),"A later task may queue after execution");
        }
        for(int water:new int[]{0,2,6,11}){boolean rejected=false;try{DonationContractData.create(kind,6,water,6);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"Invalid water total rejected");}
        CompoundNBT valid=DonationContractData.create(kind,6,1,6);
        for(int variant=0;variant<7;variant++){
            CompoundNBT bad=valid.copy();ListNBT list=bad.getList("items",10);
            switch(variant){case 0:list.remove(2);break;case 1:list.add(list.getCompound(0).copy());break;case 2:list.getCompound(2).putInt("amountComplete",1);break;case 3:list.getCompound(1).putInt("amountTotal",6);break;case 4:list.getCompound(2).putString("item","minecraft:magma_cream");break;case 5:list.getCompound(2).putInt("amountComplete",7);break;default:list.getCompound(2).putString("amountTotal","6");}
            check(DonationContractData.read(bad)==null,"Malformed triple rejected "+variant);
        }
        ItemStack lock=new ItemStack(new TaglockItem());check(KillContractItem.targetId(lock)==null,"Empty taglock rejected");
        lock.getOrCreateTag().putString("boundId","bad");check(KillContractItem.targetId(lock)==null,"Malformed target rejected");
        lock.getTag().putString("boundId",other.toString());check(other.equals(KillContractItem.targetId(lock)),"Canonical taglock resolves target UUID");
        ItemStack fake=new ItemStack(Items.PAPER);fake.setTag(lock.getTag().copy());check(KillContractItem.targetId(fake)==null,"NBT on another item cannot target");
        check(lock.getCount()==1 && other.equals(KillContractItem.targetId(lock)),"Reading taglock does not consume it");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();KillContractTasks.commands(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        for(int permission:new int[]{0,2}){CommandSource source=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,permission,"test",new StringTextComponent("test"),null,null);check(parses(dispatcher,source,"contract @s bewitchment:mahapadma")== (permission==2),"Command permission "+permission);}
        System.out.println("PASS: "+checks+" Mahapadma checks");
    }
}