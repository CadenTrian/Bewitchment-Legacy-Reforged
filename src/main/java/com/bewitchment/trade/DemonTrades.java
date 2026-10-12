package com.bewitchment.trade;

import com.bewitchment.machine.ContractTasks;
import com.google.gson.*;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/** Legacy career offers. Merchant AI and career progression are separate consumers. */
public final class DemonTrades {
    public static final class Entry {
        public final String type,item,enchantment;
        public final int min,max,level;
        private Entry(JsonObject json){
            type=json.get("type").getAsString();item=text(json,"item");enchantment=text(json,"enchantment");
            min=number(json,"min",1);max=number(json,"max",1);level=number(json,"level",0);
            if(!Arrays.asList("item","random_enchant","enchant","idol","statue","contract").contains(type)
                || min<1 || max<min || max>64 || (type.equals("item") || type.contains("enchant")) && item.isEmpty()
                || type.contains("enchant") && (level<1 || level>30) || type.equals("enchant") && enchantment.isEmpty())throw new IllegalArgumentException("Invalid demon offer");
        }
        public boolean discounted(){return type.equals("item") || type.equals("enchant");}
    }
    private static String text(JsonObject j,String key){return j.has(key)?j.get(key).getAsString():"";}
    private static int number(JsonObject j,String key,int fallback){return j.has(key)?j.get(key).getAsInt():fallback;}
    private static final Map<String,List<List<Entry>>> TABLE=load();
    private static Map<String,List<List<Entry>>> load(){
        Map<String,List<List<Entry>>> table=new LinkedHashMap<>();
        try(InputStream in=DemonTrades.class.getResourceAsStream("/data/bewitchment/legacy/demon_trades.json")){
            if(in==null)throw new IllegalStateException("Missing demon trade table");
            JsonObject root=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            for(String career:new String[]{"noble","despot","apocryphal_teacher"}){
                JsonArray levels=root.getAsJsonArray(career);if(levels==null || levels.size()!=5)throw new IllegalArgumentException("Five career levels required");
                List<List<Entry>> parsed=new ArrayList<>();for(JsonElement level:levels){List<Entry> entries=new ArrayList<>();for(JsonElement entry:level.getAsJsonArray())entries.add(new Entry(entry.getAsJsonObject()));if(entries.isEmpty())throw new IllegalArgumentException("Empty trade level");parsed.add(Collections.unmodifiableList(entries));}
                table.put(career,Collections.unmodifiableList(parsed));
            }
            return Collections.unmodifiableMap(table);
        }catch(IOException ex){throw new IllegalStateException("Cannot read demon trades",ex);}
    }
    public static Set<String> careers(){return TABLE.keySet();}
    public static List<Entry> entries(String career,int level){if(!TABLE.containsKey(career) || level<1 || level>5)throw new IllegalArgumentException("Unknown career or level");return TABLE.get(career).get(level-1);}
    public static List<Entry> select(String career,int level,Random random){
        List<Entry> pool=new ArrayList<>(entries(career,level));Collections.shuffle(pool,random);
        return Collections.unmodifiableList(new ArrayList<>(pool.subList(0,Math.min(level<5?2:1,pool.size()))));
    }
    public static ItemStack price(Random random,boolean reduced){
        Item[] items={Items.BLAZE_ROD,Items.DIAMOND,Items.GOLD_INGOT,Items.MAGMA_CREAM};
        int[] min={4,5,15,8},max={15,7,30,18};int choice=random.nextInt(4),count=min[choice]+random.nextInt(max[choice]-min[choice]+1);
        return new ItemStack(items[choice],reduced?count/2:count);
    }
    private static Item required(Function<String,Item> resolve,String id){Item item=resolve.apply(id);if(item==null || item==Items.AIR)throw new IllegalArgumentException("Missing demon trade item: "+id);return item;}
    public static MerchantOffer generate(Entry entry,Random random,boolean reduced){return generate(entry,random,reduced,id->ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));}
    public static MerchantOffer generate(Entry entry,Random random,boolean reduced,Function<String,Item> resolve){
        ItemStack result;ItemStack cost;
        if(entry.type.equals("item") || entry.type.equals("enchant")){
            Item item=required(resolve,entry.item);int count=entry.min+random.nextInt(entry.max-entry.min+1);
            result=new ItemStack(item,Math.min(count,item.getMaxStackSize()));
            if(entry.type.equals("enchant")){
                Enchantment enchantment=ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(entry.enchantment));
                if(enchantment==null)throw new IllegalArgumentException("Unknown enchantment");
                if(item==Items.ENCHANTED_BOOK)EnchantedBookItem.addEnchantment(result,new EnchantmentData(enchantment,entry.level));else result.enchant(enchantment,entry.level);
            }
            cost=price(random,reduced);
        }else{
            cost=price(random,false);
            switch(entry.type){
                case "random_enchant":result=EnchantmentHelper.enchantItem(random,new ItemStack(required(resolve,entry.item)),entry.level,false);break;
                case "contract":result=ContractTasks.random(required(resolve,"bewitchment:contract"),random);break;
                case "idol":case "statue":
                    String[] demons={"leonard","baphomet","lilith","herne","moloch"},materials={"stone","gold","nether_brick","scorned_brick"};
                    String demon=demons[random.nextInt(5)],material=materials[random.nextInt(4)];
                    result=new ItemStack(required(resolve,"bewitchment:"+material+"_"+demon+"_"+entry.type));break;
                default:throw new IllegalArgumentException("Unsupported offer type");
            }
        }
        // Legacy normal offers start with seven uses; contracts are permanently single-use.
        return new MerchantOffer(cost,result,entry.type.equals("contract")?1:7,0,0);
    }
    private DemonTrades(){}
}