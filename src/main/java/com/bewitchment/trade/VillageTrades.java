package com.bewitchment.trade;

import net.minecraft.item.*;
import net.minecraft.item.MerchantOffer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.function.Function;

/** Original Hedge Witch and Alchemist prices/pools, grouped by the five modern levels. */
public final class VillageTrades {
    public static final class Entry {
        public final String item;
        public final boolean buying;
        public final int emeraldMin,emeraldMax,quantityMin,quantityMax;
        private Entry(String item,boolean buying,int a,int b,int c,int d){this.item=item;this.buying=buying;emeraldMin=a;emeraldMax=b;quantityMin=c;quantityMax=d;}
    }
    private static Entry sell(String item,int a,int b,int c,int d){return new Entry(item,false,a,b,c,d);}
    private static Entry buy(String item,int a,int b,int c,int d){return new Entry(item,true,a,b,c,d);}
    private static List<Entry> level(Entry...entries){return Collections.unmodifiableList(Arrays.asList(entries));}
    private static final List<List<Entry>> WITCH=Collections.unmodifiableList(Arrays.asList(
        level(buy("tallow",1,3,6,10),sell("empty_jar",1,2,3,5),sell("juniper_berries",1,2,3,5),sell("elderberries",1,2,3,5),sell("#seeds",1,2,2,5),sell("oak_apple_gall",1,2,3,5)),
        level(sell("#saplings",1,3,2,3),sell("#seeds",4,6,2,5)),
        level(sell("ravens_feather",1,3,5,10),sell("owlets_wing",1,3,3,5),buy("wood_ash",1,3,10,20),sell("#weapons",3,7,1,1),sell("#seeds",6,7,3,10)),
        level(sell("heart",3,6,1,2),sell("#plants",3,5,2,5),sell("#seeds",3,12,2,5)),
        level(sell("#accessories",8,16,1,1),sell("#seeds",2,11,2,7),sell("thyrsus",3,12,1,1))));
    private static final List<List<Entry>> ALCHEMIST=Collections.unmodifiableList(Arrays.asList(
        level(sell("minecraft:glass_bottle",1,3,5,9),sell("silver_ingot",3,5,2,5),buy("salt",1,3,10,16),buy("minecraft:gold_ingot",1,2,10,16)),
        level(sell("stone_ichor",2,4,1,3),sell("fiery_unguent",4,6,1,3)),
        level(sell("dragons_blood_resin",3,5,3,5),sell("bottled_frostfire",3,5,3,5),buy("amethyst",1,3,2,5),buy("garnet",1,3,2,5)),
        level(sell("demonic_elixir",3,6,1,2),sell("cleansing_balm",3,6,1,3)),
        level(sell("dragons_blood_sapling",2,4,1,1))));
    public static List<Entry> entries(boolean witch,int level){if(level<1||level>5)throw new IllegalArgumentException("Villager level must be 1..5");return (witch?WITCH:ALCHEMIST).get(level-1);}
    public static String id(String name){return name.contains(":")?name:"bewitchment:"+name;}
    public static List<String> pool(String name){
        switch(name){
            case "#seeds":return Arrays.asList("minecraft:wheat_seeds","minecraft:melon_seeds","minecraft:pumpkin_seeds","garlic_seeds","aconitum_seeds","belladonna_seeds","hellebore_seeds","mandrake_seeds","white_sage_seeds","wormwood_seeds","minecraft:beetroot_seeds");
            case "#saplings":return Arrays.asList("cypress_sapling","juniper_sapling","elder_sapling","elder_sapling");
            case "#plants":return Arrays.asList("embergrass","torchwood","spanish_moss");
            case "#weapons":return Arrays.asList("athame","boline");
            case "#accessories":return Arrays.asList("fortunes_favor","triskelion","gluttons_sash","nazar","token_of_remedies","horseshoe","girdle_of_the_dryads","hellish_bauble","vampiric_amulet","hecates_visage");
            default:if(name.startsWith("#"))throw new IllegalArgumentException("Unknown trade pool: "+name);return Collections.singletonList(name);
        }
    }
    public static int price(Random random,int min,int max){return min==max?min:min+random.nextInt(max-min+1);}
    public static MerchantOffer offer(Entry entry,int level,Random random){return offer(entry,level,random,key->{ResourceLocation id=new ResourceLocation(key);if(!ForgeRegistries.ITEMS.containsKey(id))throw new IllegalStateException("Missing village trade item: "+id);return ForgeRegistries.ITEMS.getValue(id);});}
    static MerchantOffer offer(Entry entry,int level,Random random,Function<String,Item> items){
        int amount=price(random,entry.quantityMin,entry.quantityMax);
        List<String> pool=pool(entry.item);String selected=pool.get(pool.size()==1?0:random.nextInt(pool.size()));
        ItemStack goods=new ItemStack(items.apply(id(selected)),amount);
        if(entry.item.equals("#weapons")&&random.nextInt(10)>=5)goods=EnchantmentHelper.enchantItem(random,goods,random.nextInt(31),false);
        ItemStack emeralds=new ItemStack(Items.EMERALD,price(random,entry.emeraldMin,entry.emeraldMax));
        // Seven uses preserves the old offer default; XP and demand adapt it to modern villager progression.
        int xp=new int[]{2,10,20,30,30}[level-1];
        return new MerchantOffer(entry.buying?goods:emeralds,entry.buying?emeralds:goods,7,xp,.05F);
    }
    private VillageTrades(){}
}
