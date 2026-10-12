package com.bewitchment.machine;

import com.bewitchment.combat.KillContractTasks;
import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.CurseData.Kind;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import java.util.UUID;

public final class KillContractCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> dispatcher,CommandSource source,String text){com.mojang.brigadier.ParseResults<CommandSource> result=dispatcher.parse(text,source);return !result.getReader().canRead() && result.getContext().getCommand()!=null;}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
        for(Kind kind:new Kind[]{Kind.BERSERKER,Kind.DEVOURING_RAGE})for(int total=6;total<=10;total++){
            CompoundNBT tag=KillContractData.create(kind,total);check(KillContractData.read(tag).owner==null && !KillContractData.read(tag).complete(),"Fresh unsigned task");
            check(!KillContractData.advance(tag,owner,true),"Unsigned kills do not count");
            check(KillContractData.sign(tag,owner,"Signer"),"Sign valid task");check(!KillContractData.sign(tag,other,"Other"),"Cannot rebind signer");
            check(!KillContractData.advance(tag,other,true) && !KillContractData.advance(tag,owner,false),"Wrong owner and wrong prey do not advance");
            for(int i=0;i<total;i++)if(!KillContractData.advance(tag,owner,true))throw new AssertionError("Progress refused");
            check(KillContractData.read(tag).complete() && !KillContractData.advance(tag,owner,true),"Progress stops exactly at objective");
            KillContractData loaded=KillContractData.read(tag.copy());check(loaded.owner.equals(owner) && loaded.total==total && loaded.done==total,"Serialized task and owner persist");
            CurseData effects=new CurseData();check(!loaded.redeem(other,effects) && effects.active().isEmpty(),"Wrong player cannot redeem");
            check(loaded.redeem(owner,effects) && effects.remaining(kind)==168000,"Seven-day direct reward");effects.second();
            check(!loaded.redeem(owner,effects) && effects.remaining(kind)==167980,"Duplicate reward refused without refreshing timer");
        }
        for(int mask=0;mask<8;mask++){
            check(KillContractTasks.qualifies(Kind.BERSERKER,(mask&1)!=0,(mask&2)!=0,(mask&4)!=0)==((mask&3)!=0),"Berserker prey classification "+mask);
            check(KillContractTasks.qualifies(Kind.DEVOURING_RAGE,(mask&1)!=0,(mask&2)!=0,(mask&4)!=0)==((mask&4)!=0),"Devouring prey classification "+mask);
        }
        check(!KillContractTasks.qualifies(Kind.DESICCATION,true,true,true),"Unsupported kind cannot earn kill progress");
        for(int total:new int[]{-1,0,5,11,Integer.MAX_VALUE}){boolean rejected=false;try{KillContractData.create(Kind.BERSERKER,total);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Out-of-range objective rejected");}
        check(KillContractData.read(null)==null && KillContractData.read(new CompoundNBT())==null,"Missing task data rejected");
        CompoundNBT valid=KillContractData.create(Kind.BERSERKER,6);KillContractData.sign(valid,owner,"Signer");
        for(int i=0;i<9;i++){
            CompoundNBT bad=valid.copy();switch(i){case 0:bad.putString("contract","bewitchment:desiccation");break;case 1:bad.putInt("mobsComplete",-1);break;case 2:bad.putInt("mobsComplete",7);break;case 3:bad.putString("mobsTotal","6");break;case 4:bad.putString("boundId","bad");break;case 5:bad.remove("boundName");break;case 6:bad.putString("boundName","");break;case 7:bad.put("items",new ListNBT());break;default:bad.remove("mobsComplete");}
            check(KillContractData.read(bad)==null && !KillContractData.advance(bad,owner,true),"Malformed task inert "+i);
        }
        CompoundNBT unsigned=KillContractData.create(Kind.BERSERKER,6);unsigned.putInt("mobsComplete",6);check(KillContractData.read(unsigned)==null,"Unsigned forged completion rejected");
        check(!KillContractData.read(valid).redeem(owner,new CurseData()),"Incomplete signed task cannot redeem");
        KillContractItem item=new KillContractItem();ItemStack stack=KillContractItem.task(item,Kind.DEVOURING_RAGE,6);check(stack.getMaxStackSize()==1 && !item.isFoil(stack),"Unstackable unsigned item");
        KillContractData.sign(stack.getTag(),owner,"Signer");for(int i=0;i<6;i++)KillContractData.advance(stack.getTag(),owner,true);check(item.isFoil(stack),"Completed item glints");
        stack.setCount(2);check(KillContractItem.data(stack)==null && !item.isFoil(stack),"Malformed stacked contracts rejected");
        NonNullList<ItemStack> creative=NonNullList.create();item.fillItemCategory(ItemGroup.TAB_SEARCH,creative);check(creative.size()==6 && creative.stream().filter(s->KillContractItem.data(s)!=null && KillContractItem.data(s).total==6).count()==2,"Two valid creative task variants");
        check(!item.isEnchantable(new ItemStack(item)) && !item.isBookEnchantable(new ItemStack(item),new ItemStack(Items.ENCHANTED_BOOK)),"Contract cannot be enchanted");
        CommandDispatcher<CommandSource> dispatcher=new CommandDispatcher<>();KillContractTasks.commands(new RegisterCommandsEvent(dispatcher,Commands.EnvironmentType.ALL));
        CommandSource op=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,2,"test",new StringTextComponent("test"),null,null),guest=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,0,"test",new StringTextComponent("test"),null,null);
        for(String id:new String[]{"berserker","devouring_rage"}){check(parses(dispatcher,op,"contract @s bewitchment:"+id),"Task command parses");check(!parses(dispatcher,guest,"contract @s bewitchment:"+id),"Task command requires operator");}
        check(!parses(dispatcher,op,"contract @s bewitchment:unknown"),"Unknown task not issued");
        System.out.println("PASS: "+checks+" kill contract checks");
    }
}
