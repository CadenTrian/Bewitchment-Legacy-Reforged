package com.bewitchment.trade;

import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.*;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.function.Function;

public final class DemonTradesCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean parses(CommandDispatcher<CommandSource> d,CommandSource s,String t){com.mojang.brigadier.ParseResults<CommandSource> r=d.parse(t,s);return !r.getReader().canRead() && r.getContext().getCommand()!=null;}
    private static Random edge(boolean high){return new Random(0){@Override public int nextInt(int bound){return high?bound-1:0;}};}
    public static void main(String[] args){
        Bootstrap.bootStrap();KillContractItem contract=new KillContractItem();
        Function<String,Item> resolve=id->id.equals("bewitchment:contract")?contract:id.equals("bewitchment:fortunes_favor")?Items.BOW:id.startsWith("bewitchment:")?Items.PAPER:ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        check(DemonTrades.careers().size()==3,"Three careers");
        int[][] counts={{2,4,3,3,3},{3,4,4,3,2},{4,4,3,3,4}};int c=0,total=0;
        for(String career:DemonTrades.careers()){
            for(int level=1;level<=5;level++){
                List<DemonTrades.Entry> entries=DemonTrades.entries(career,level);total+=entries.size();check(entries.size()==counts[c][level-1],"Exact legacy career pool size");
                List<DemonTrades.Entry> selected=DemonTrades.select(career,level,new Random(1));
                check(selected.size()==(level==5?1:2) && new HashSet<>(selected).size()==selected.size() && entries.containsAll(selected),"Selection count and no replacement");
                check(selected.equals(DemonTrades.select(career,level,new Random(1))),"Seeded selection reproducible");
                for(DemonTrades.Entry entry:entries){
                    MerchantOffer normal=DemonTrades.generate(entry,new Random(123),false,resolve),discount=DemonTrades.generate(entry,new Random(123),true,resolve);
                    check(!normal.getResult().isEmpty() && normal.getCostB().isEmpty() && normal.getUses()==0,"Fresh single-payment offer");
                    check(discount.getCostA().getCount()==(entry.discounted()?normal.getCostA().getCount()/2:normal.getCostA().getCount()),"Only ordinary and fixed enchanted offers discounted");
                    check(normal.getMaxUses()==(entry.type.equals("contract")?1:7) && normal.getPriceMultiplier()==0,"Legacy initial stock and no dynamic pricing");
                    if(entry.type.equals("contract")){
                        check(KillContractItem.data(normal.getResult())!=null || KillContractItem.donationData(normal.getResult())!=null,"Valid unsigned contract reward");
                        normal.increaseUses();check(normal.isOutOfStock(),"Contract exhausted after one purchase");
                    }
                    if(entry.type.equals("item")){
                        int lower=DemonTrades.generate(entry,edge(false),false,resolve).getResult().getCount(),upper=DemonTrades.generate(entry,edge(true),false,resolve).getResult().getCount();
                        check(lower==Math.min(entry.min,resolve.apply(entry.item).getMaxStackSize()) && upper==Math.min(entry.max,resolve.apply(entry.item).getMaxStackSize()),"Output range and stack cap");
                    }
                    if(entry.type.equals("enchant")){
                        Enchantment enchantment=ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(entry.enchantment));
                        check(EnchantmentHelper.getEnchantments(normal.getResult()).getOrDefault(enchantment,0)==entry.level,"Fixed enchantment preserved");
                        MerchantOffer restored=new MerchantOffer(normal.createTag());check(EnchantmentHelper.getEnchantments(restored.getResult()).getOrDefault(enchantment,0)==entry.level,"Offer NBT retains enchantment");
                    }
                    if(entry.type.equals("random_enchant"))check(!EnchantmentHelper.getEnchantments(normal.getResult()).isEmpty(),"Random enchantment generated");
                }
            }c++;
        }
        check(total==49,"All forty-nine definitions");
        Item[] currencies={Items.BLAZE_ROD,Items.DIAMOND,Items.GOLD_INGOT,Items.MAGMA_CREAM};int[] min={4,5,15,8},max={15,7,30,18};
        for(int index=0;index<4;index++)for(boolean high:new boolean[]{false,true})for(boolean reduced:new boolean[]{false,true}){
            final int choice=index;Random rng=new Random(0){int call;@Override public int nextInt(int bound){return call++==0?choice:high?bound-1:0;}};
            ItemStack price=DemonTrades.price(rng,reduced);check(price.getItem()==currencies[index] && price.getCount()==(reduced?(high?max[index]:min[index])/2:high?max[index]:min[index]),"Currency and floor-half boundary");
        }
        DemonTrades.Entry fixed=DemonTrades.entries("despot",1).get(2);MerchantOffer a=DemonTrades.generate(fixed,new Random(5),false,resolve),b=DemonTrades.generate(fixed,new Random(5),false,resolve);
        a.getResult().shrink(1);check(a.getResult().getCount()+1==b.getResult().getCount(),"Offer outputs do not alias");
        boolean rejected=false;try{DemonTrades.generate(fixed,new Random(0),false,id->null);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Missing item fails explicitly");
        for(int level:new int[]{0,6}){rejected=false;try{DemonTrades.entries("noble",level);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Invalid level rejected");}
        rejected=false;try{DemonTrades.entries("unknown",1);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Unknown career rejected");
        boolean immutable=false;try{DemonTrades.entries("noble",1).clear();}catch(UnsupportedOperationException ex){immutable=true;}check(immutable,"Definition pool immutable");
        CommandDispatcher<CommandSource> d=new CommandDispatcher<>();DemonTradePreview.commands(new RegisterCommandsEvent(d,Commands.EnvironmentType.ALL));
        for(int permission:new int[]{0,2}){CommandSource s=new CommandSource(null,Vector3d.ZERO,Vector2f.ZERO,null,permission,"test",new StringTextComponent("test"),null,null);
            for(String career:DemonTrades.careers()){check(parses(d,s,"demontrades "+career+" 1")== (permission==2),"Preview permission");check(parses(d,s,"demontrades "+career+" 5 true")== (permission==2),"Discount preview permission");}
            for(String career:DemonTrades.careers())check(parses(d,s,"demontrades progression "+career+" true")== (permission==2),"Progression simulation permission");
            check(!parses(d,s,"demontrades noble 6") && !parses(d,s,"demontrades unknown 1"),"Invalid command choices rejected");
        }
        System.out.println("PASS: "+checks+" demon trade checks");
    }
}