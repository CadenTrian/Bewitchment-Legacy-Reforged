package com.bewitchment.trade;

import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public final class VillageTradesCheck {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    public static void main(String[] args)throws Exception{
        Bootstrap.bootStrap();JsonArray manifest=new JsonArray();int definitions=0;
        int[][] sizes={{4,2,4,2,1},{6,2,5,3,3}};
        for(boolean witch:new boolean[]{false,true})for(int level=1;level<=5;level++){
            List<VillageTrades.Entry> entries=VillageTrades.entries(witch,level);check(entries.size()==sizes[witch?1:0][level-1],"Original offers per tier");
            for(VillageTrades.Entry entry:entries){
                definitions++;JsonObject row=new JsonObject();row.addProperty("profession",witch?"hedge_witch":"alchemist");row.addProperty("level",level);row.addProperty("item",entry.item);row.addProperty("buying",entry.buying);row.addProperty("emeraldMin",entry.emeraldMin);row.addProperty("emeraldMax",entry.emeraldMax);row.addProperty("quantityMin",entry.quantityMin);row.addProperty("quantityMax",entry.quantityMax);manifest.add(row);
                Set<String> seen=new HashSet<>();
                for(int seed=0;seed<128;seed++){
                    MerchantOffer offer=VillageTrades.offer(entry,level,new Random(seed*0x9E3779B97F4A7C15L),key->{seen.add(key);return entry.item.equals("#weapons")?Items.IRON_SWORD:Items.APPLE;});
                    ItemStack cost=offer.getBaseCostA(),result=offer.getResult(),emeralds=entry.buying?result:cost,goods=entry.buying?cost:result;
                    check(emeralds.getItem()==Items.EMERALD&&emeralds.getCount()>=entry.emeraldMin&&emeralds.getCount()<=entry.emeraldMax,"Emerald direction and inclusive price range");
                    check(goods.getCount()>=entry.quantityMin&&goods.getCount()<=entry.quantityMax,"Original quantity range");
                    check(offer.getMaxUses()==7&&offer.getXp()>0,"Stock and modern progression XP");
                    for(int use=0;use<7;use++)offer.increaseUses();check(offer.isOutOfStock(),"Seven trades exhaust stock");
                    MerchantOffer restored=new MerchantOffer(offer.createTag());check(restored.isOutOfStock()&&ItemStack.matches(restored.getResult(),offer.getResult())&&ItemStack.matches(restored.getBaseCostA(),offer.getBaseCostA()),"Save/load retains price, output, enchantments and stock");
                    restored.resetUses();check(!restored.isOutOfStock()&&restored.getUses()==0,"Vanilla workstation restocking can reset uses");
                }
                for(String id:VillageTrades.pool(entry.item))check(seen.contains(VillageTrades.id(id)),"Every random pool choice reachable");
            }
        }
        check(definitions==32,"All 32 original trade definitions");
        check(Collections.frequency(VillageTrades.pool("#saplings"),"elder_sapling")==2,"Legacy double Elder weighting");
        check(VillageTrades.pool("#seeds").size()==11&&new HashSet<>(VillageTrades.pool("#seeds")).size()==11,"Eleven distinct seeds");
        for(int bound:new int[]{-1,0,6}){boolean rejected=false;try{VillageTrades.entries(true,bound);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid level rejected");}
        Files.createDirectories(Paths.get("build"));Files.write(Paths.get("build/village-trades.json"),new GsonBuilder().setPrettyPrinting().create().toJson(manifest).getBytes(StandardCharsets.UTF_8));
        System.out.println("PASS: "+checks+" village trade price, selection, stock and persistence checks across ten tiers");
    }
}
