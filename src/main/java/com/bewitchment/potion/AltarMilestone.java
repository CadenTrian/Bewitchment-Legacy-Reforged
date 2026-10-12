package com.bewitchment.potion;
import com.google.gson.*;
import net.minecraft.advancements.*;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Parses the bundled milestone at startup; never grants progress to a player. */
public final class AltarMilestone {
    public static JsonObject resource(String name){
        String path="/data/bewitchment/advancements/"+name+".json";
        try(InputStream input=AltarMilestone.class.getResourceAsStream(path)){
            if(input==null)throw new IllegalStateException("Missing "+path);
            try(Reader reader=new InputStreamReader(input,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}
        }catch(IOException ex){throw new IllegalStateException("Cannot read "+path,ex);}
    }
    public static Advancement parse(JsonObject rootJson,JsonObject altarJson){
        ResourceLocation rootId=new ResourceLocation("bewitchment:root");LootPredicateManager predicates=new LootPredicateManager();
        Advancement root=Advancement.Builder.fromJson(rootJson,new ConditionArrayParser(rootId,predicates)).build(rootId);
        Advancement.Builder builder=Advancement.Builder.fromJson(altarJson,new ConditionArrayParser(TargetedBrewEffect.ALTAR,predicates));
        if(!builder.canBuild(id->id.equals(rootId)?root:null))throw new IllegalStateException("Invalid altar advancement parent");
        return builder.build(TargetedBrewEffect.ALTAR);
    }
    public static void validate(){
        Advancement altar=parse(resource("root"),resource("crafted_altar"));
        if(altar.getCriteria().size()!=6||altar.getRequirements().length!=1||altar.getRequirements()[0].length!=6)throw new IllegalStateException("Altar milestone must accept any of six variants");
        org.apache.logging.log4j.LogManager.getLogger().info("Verified original altar advancement: six inventory alternatives and root display");
    }
    private AltarMilestone(){}
}
