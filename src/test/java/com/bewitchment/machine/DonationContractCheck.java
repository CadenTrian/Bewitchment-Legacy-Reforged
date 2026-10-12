package com.bewitchment.machine;

import com.bewitchment.combat.*;
import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.UUID;

public final class DonationContractCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);return !result.getReader().canRead() && result.getContext().getCommand()!=null;}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID owner=UUID.randomUUID(),other=UUID.randomUUID();KillContractItem item=new KillContractItem();
        for(Kind kind:new Kind[]{Kind.SALAMANDRINE_TOLERANCE,Kind.DESICCATION,Kind.IGNITION})for(int first=6;first<=11;first++){
            int second=17-first;CompoundNBT tag=DonationContractData.create(kind,first,second);ItemStack offered=new ItemStack(DonationContractData.ingredient(kind,0),64);
            check(DonationContractData.donate(tag,owner,offered)==0 && offered.getCount()==64,"Unsigned task consumes nothing");
            check(DonationContractData.sign(tag,owner,"Signer") && !DonationContractData.sign(tag,other,"Other"),"Sign once");
            check(DonationContractData.donate(tag,other,offered)==0 && offered.getCount()==64,"Other owner cannot donate");
            offered.getOrCreateTag().putString("keep","yes");check(DonationContractData.donate(tag,owner,offered)==first && offered.getCount()==64-first && offered.getTag().getString("keep").equals("yes"),"Consume exact requirement and preserve remainder NBT");
            check(DonationContractData.donate(tag,owner,offered)==0 && !DonationContractData.read(tag).complete(),"Completed first ingredient cannot overfill or finish whole task");
            ItemStack partial=new ItemStack(DonationContractData.ingredient(kind,1),2);check(DonationContractData.donate(tag,owner,partial)==2 && partial.isEmpty(),"Partial delivery fully consumed");
            CompoundNBT saved=tag.copy();check(DonationContractData.read(saved).done(0)==first && DonationContractData.read(saved).done(1)==2,"Independent per-item progress persists");
            ItemStack rest=new ItemStack(DonationContractData.ingredient(kind,1),64);check(DonationContractData.donate(saved,owner,rest)==second-2 && DonationContractData.read(saved).complete(),"Second ingredient completes task");
            CurseData effects=new CurseData();check(!DonationContractData.read(saved).redeem(other,effects) && DonationContractData.read(saved).redeem(owner,effects) && effects.remaining(kind)==168000,"Owner-only seven-day redemption");
            effects.second();check(!DonationContractData.read(saved).redeem(owner,effects) && effects.remaining(kind)==167980,"Duplicate does not refresh effect");
            ItemStack completed=new ItemStack(item);completed.setTag(saved);check(item.isFoil(completed) && KillContractItem.signed(completed),"Donation completion glint and signed model");
        }
        check(DonationContractData.ingredient(Kind.SALAMANDRINE_TOLERANCE,0)==Items.BLAZE_ROD && DonationContractData.ingredient(Kind.SALAMANDRINE_TOLERANCE,1)==Items.GHAST_TEAR,"Original Salamandrine ingredients");
        check(DonationContractData.ingredient(Kind.DESICCATION,0)==Items.FERMENTED_SPIDER_EYE && DonationContractData.ingredient(Kind.DESICCATION,1)==Items.POISONOUS_POTATO,"Original Desiccation ingredients");
        Inventory inventory=new Inventory(3);ItemStack first=KillContractItem.donationTask(item,Kind.DESICCATION,6,6),second=first.copy();DonationContractData.sign(first.getTag(),owner,"Signer");DonationContractData.sign(second.getTag(),owner,"Signer");inventory.setItem(0,first);inventory.setItem(1,second);ItemStack offered=new ItemStack(Items.POISONOUS_POTATO,20);
        check(DonationContractTasks.donateFirst(inventory,owner,offered)==6 && offered.getCount()==14 && KillContractItem.donationData(second).done(1)==0,"Only first matching task consumes one pickup attempt");
        check(DonationContractTasks.donateFirst(inventory,owner,offered)==6 && offered.getCount()==8 && KillContractItem.donationData(second).done(1)==6,"Already funded ingredient skipped on next attempt");
        check(DonationContractTasks.donateFirst(inventory,owner,offered)==0 && offered.getCount()==8,"No excess consumed after both funded");
        ItemStack wrong=new ItemStack(Items.POTATO,64);check(DonationContractTasks.donateFirst(inventory,owner,wrong)==0 && wrong.getCount()==64,"Ordinary potato does not substitute");
        for(boolean delay:new boolean[]{false,true}){
            check(DonationContractTasks.pickupAllowed(null,owner,delay)==!delay,"Unowned drop delay gate");check(DonationContractTasks.pickupAllowed(owner,owner,delay)==!delay,"Owned drop delay gate");check(!DonationContractTasks.pickupAllowed(other,owner,delay),"Other owner's drop not donated");
        }
        CompoundNBT valid=DonationContractData.create(Kind.DESICCATION,6,11);DonationContractData.sign(valid,owner,"Signer");
        for(int i=0;i<10;i++){
            CompoundNBT bad=valid.copy();CompoundNBT entry=bad.getList("items",10).getCompound(0);
            switch(i){case 0:entry.putInt("amountTotal",5);break;case 1:entry.putInt("amountTotal",12);break;case 2:entry.putInt("amountComplete",-1);break;case 3:entry.putInt("amountComplete",7);break;case 4:entry.putString("item","minecraft:potato");break;case 5:entry.putString("amountTotal","6");break;case 6:bad.getList("items",10).remove(1);break;case 7:bad.putInt("mobsTotal",6);break;case 8:bad.putString("boundId","invalid");break;default:bad.remove("boundName");}
            ItemStack payment=new ItemStack(Items.FERMENTED_SPIDER_EYE,64);check(DonationContractData.read(bad)==null && DonationContractData.donate(bad,owner,payment)==0 && payment.getCount()==64,"Malformed task cannot consume items "+i);
        }
        check(DonationContractData.read(null)==null && DonationContractData.read(new CompoundNBT())==null,"Missing data rejected");
        ItemStack stacked=KillContractItem.donationTask(item,Kind.DESICCATION,6,6);stacked.setCount(2);check(KillContractItem.donationData(stacked)==null,"Stacked task invalid");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();KillContractTasks.commands(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null),guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(String id:new String[]{"salamandrine_tolerance","desiccation","ignition"}){check(parses(dispatcher,op,"contract @s bewitchment:"+id),"Donation command parses");check(!parses(dispatcher,guest,"contract @s bewitchment:"+id),"Donation command requires operator");}
        System.out.println("PASS: "+checks+" donation contract checks");
    }
}
