package com.bewitchment.machine;

import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Check shipped item references after registration, before world tags are bound. */
public final class BundledRecipeAudit {
    public static void verify() {
        BrewHooks.verify();
        BundledProgressionAudit.verify();
        BundledBroomAudit.verify();
        int count=0;
        try (BufferedReader index=new BufferedReader(resource("bewitchment-recipes.txt"))) {
            for(String path; (path=index.readLine())!=null;) {
                if(path.isEmpty())continue;
                try (Reader reader=resource("data/bewitchment/recipes/"+path)) {
                    JsonObject json=new JsonParser().parse(reader).getAsJsonObject();
                    RecipeItemReferences.validate(json, id->ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id)));
                    ResourceLocation serializer=new ResourceLocation(json.get("type").getAsString());
                    if(!ForgeRegistries.RECIPE_SERIALIZERS.containsKey(serializer))throw new IllegalStateException("Unknown recipe serializer: "+serializer);
                    count++;
                } catch(Exception error) {throw new IllegalStateException("Bundled recipe failed: "+path,error);}
            }
        } catch(IOException error) {throw new IllegalStateException("Cannot read bundled recipe index",error);}
        if(count==0)throw new IllegalStateException("Empty bundled recipe index");
        AltarItemUpgrades.Upgrade upgrade=AltarItemUpgrades.get(new net.minecraft.item.ItemStack(com.bewitchment.registry.ModItems.FLYING_OINTMENT.get()));
        if(upgrade==null||upgrade.kind!=AltarItemUpgrades.Kind.CUP||upgrade.gain!=1||upgrade.multiplier!=1.17||upgrade.bonus!=0)
            throw new IllegalStateException("Flying Ointment altar upgrade mismatch");
        LogManager.getLogger().info("Bewitchment: verified item references and serializers in {} bundled recipes; Flying Ointment altar upgrade verified",count);
    }
    private static Reader resource(String path)throws IOException {
        InputStream stream=BundledRecipeAudit.class.getClassLoader().getResourceAsStream(path);
        if(stream==null)throw new FileNotFoundException(path);
        return new InputStreamReader(stream,StandardCharsets.UTF_8);
    }
    private BundledRecipeAudit(){}
}
