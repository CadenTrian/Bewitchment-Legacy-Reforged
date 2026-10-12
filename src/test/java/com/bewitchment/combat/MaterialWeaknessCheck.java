package com.bewitchment.combat;

import com.bewitchment.block.PurifyingEarthBlock;
import com.bewitchment.block.SaltBarrierBlock;
import com.bewitchment.item.LegacyAccessoryItem.Kind;
import com.bewitchment.potion.DefensiveBrewEffects;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MaterialWeaknessCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean near(float a,float b){return Math.abs(a-b)<.0001F;}
    private static Set<String> tag(String name,Set<String> active) throws Exception {
        check(active.add(name),"Tag graph is acyclic");
        JsonObject json=new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/data/bewitchment/tags/entity_types/"+name+".json")),StandardCharsets.UTF_8)).getAsJsonObject();
        check(!json.get("replace").getAsBoolean(),"Built-in tags are additive");
        Set<String> values=new HashSet<>();
        for(JsonElement entry:json.getAsJsonArray("values")){
            String id=entry.isJsonObject()?entry.getAsJsonObject().get("id").getAsString():entry.getAsString();
            if(id.startsWith("#bewitchment:"))values.addAll(tag(id.substring("#bewitchment:".length()),active));
            else if(id.startsWith("thaumcraft:"))check(entry.isJsonObject()&&!entry.getAsJsonObject().get("required").getAsBoolean(),"Absent optional mod cannot fail tag loading");
            else values.add(id);
        }
        active.remove(name);return values;
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.util.registry.Bootstrap.bootStrap();
        Set<String> cold=tag("cold_iron_weakness",new HashSet<>());
        Set<String> silver=tag("silver_weakness",new HashSet<>());
        Set<String> expected=new HashSet<>(Arrays.asList("minecraft:blaze","minecraft:ghast","minecraft:vex","minecraft:enderman",
                "bewitchment:hellhound","bewitchment:feuerwurm","bewitchment:cleaver","bewitchment:demon","bewitchment:demoness","bewitchment:imp",
                "bewitchment:bafometyr","bewitchment:shadow_person","bewitchment:cambion","bewitchment:druden","bewitchment:baphomet","bewitchment:leonard",
                "bewitchment:ghost","bewitchment:black_dog"));
        check(cold.equals(expected),"All eighteen built-in cold-iron targets and no unrelated wildlife");
        Map<net.minecraft.util.ResourceLocation,net.minecraft.tags.ITag<String>> families=new HashMap<>();
        for(String family:new String[]{"demons","spirits","fae"}) families.put(new net.minecraft.util.ResourceLocation("bewitchment",family),net.minecraft.tags.ITag.fromSet(tag(family,new HashSet<>())));
        JsonObject raw=new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/data/bewitchment/tags/entity_types/cold_iron_weakness.json")),StandardCharsets.UTF_8)).getAsJsonObject();
        net.minecraft.tags.ITag.Builder builder=net.minecraft.tags.ITag.Builder.tag().addFromJson(raw,"builtin");
        net.minecraft.tags.ITag<String> nativeTag=builder.<String>build(families::get,id->expected.contains(id.toString())?id.toString():null).orElseThrow(()->new AssertionError("Native tag build failed"));
        check(new HashSet<>(nativeTag.getValues()).equals(expected),"Native tag loader resolves nested families without optional mods");
        families.put(new net.minecraft.util.ResourceLocation("bewitchment:fae"),net.minecraft.tags.ITag.fromSet(Collections.singleton("test:fairy")));
        check(builder.<String>build(families::get,id->expected.contains(id.toString())?id.toString():null).get().contains("test:fairy"),"Rebuilt tags pick up data-pack fae additions");
        check(!nativeTag.contains("test:fairy"),"Existing tag snapshot is not mutated");
        JsonObject replacement=new JsonParser().parse("{\"replace\":true,\"values\":[\"minecraft:pig\"]}").getAsJsonObject();
        builder.addFromJson(replacement,"pack");
        check(builder.<String>build(families::get,id->id.toString()).get().getValues().equals(Collections.singletonList("minecraft:pig")),"Pack replacement can remove default weakness");
        check(silver.equals(Collections.singleton("bewitchment:werewolf")),"Silver target unchanged");
        for(String id:expected){
            boolean weak=cold.contains(id);
            check(near(MaterialDamageRules.melee(8,weak,false,true,false,0,n->{throw new AssertionError("Retaliation on bonus hit");}),12),"Cold iron melee reaches "+id);
            int[] reflected={0};
            check(near(MaterialDamageRules.melee(10,false,false,false,weak,4,n->reflected[0]+=n),7.6F)&&reflected[0]==4,"Cold iron armor reaches "+id);
            check(MaterialDamageRules.arrow(weak,false)==1.5F,"Cold iron arrow reaches "+id);
            check(PurifyingEarthBlock.shouldIgnite(false,false,weak,false),"Purifying Earth reaches "+id);
            check(!PurifyingEarthBlock.shouldIgnite(false,false,weak,true),"Cold earth does not refresh burning "+id);
            check(DefensiveBrewEffects.holyTarget(false,false,weak),"Holy Water weakness branch reaches "+id);
            int[] paid={0};
            check(near(AccessoryRules.reduce(10,false,false,false,false,weak,k->k==Kind.HORSESHOE,n->{paid[0]+=n;return true;}),9)&&paid[0]==20,"Horseshoe reaches "+id);
            check(SaltBarrierBlock.blocks(false,true,false,false,weak,false),"Salt weakness branch reaches "+id);
        }
        for(boolean player:new boolean[]{false,true}) for(boolean target:new boolean[]{false,true}) for(boolean tool:new boolean[]{false,true}) for(boolean attacker:new boolean[]{false,true}) for(int armor=0;armor<=4;armor++){
            int[] reflected={0};
            float actual=MaterialDamageRules.melee(20,target,player,tool,attacker,armor,n->reflected[0]+=n);
            float expectedDamage=target&&tool ? player?45:30 : attacker?20*(1-.06F*armor):20;
            check(near(actual,expectedDamage),"Melee multiplier and armor precedence");
            check(reflected[0]==(target&&tool?0:attacker?armor:0),"One retaliation only on armor branch");
        }
        check(MaterialDamageRules.arrow(true,true)==2.25F,"Weak player arrow amount restored");
        check(MaterialDamageRules.arrow(false,true)==0&&MaterialDamageRules.arrow(false,false)==0,"Immune arrows have no extra hit");
        check(MaterialDamageRules.weakness(false,true)==1,"Player alone does not grant weakness");
        check(!PurifyingEarthBlock.shouldIgnite(true,true,false,false),"Werewolf remains exempt from silver earth");
        check(PurifyingEarthBlock.shouldIgnite(true,true,true,false),"Cold weakness overrides silver exception");
        int[] reflected={0};
        float silverHit=MaterialDamageRules.melee(100,false,false,false,true,2,n->reflected[0]+=n);
        float both=MaterialDamageRules.melee(silverHit,false,false,false,true,2,n->reflected[0]+=n);
        check(near(both,77.44F)&&reflected[0]==4,"Mixed armor sequential silver/cold reductions");
        System.out.println("PASS: "+checks+" material weakness checks");
    }
}