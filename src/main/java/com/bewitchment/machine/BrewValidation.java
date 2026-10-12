package com.bewitchment.machine;
import com.bewitchment.Bewitchment;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class BrewValidation {
    @SubscribeEvent public static void setup(FMLCommonSetupEvent event){event.enqueueWork(()->{
        for(String id:BrewIngredients.ids()){
            Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));if(item==null || item==Items.AIR)throw new IllegalStateException("Missing brew ingredient "+id);
            ItemStack stack=new ItemStack(item);if(BrewIngredients.effects(stack).isEmpty() || BrewIngredients.plan(java.util.Collections.singletonList(stack),false,0).potion().isEmpty())throw new IllegalStateException("Invalid brew ingredient "+id);
        }
        com.bewitchment.potion.AltarMilestone.validate();
        com.bewitchment.potion.MendingEffect.validateConversionHook();
        com.bewitchment.potion.SporeCloudRules.validateMappings(ForgeRegistries.BLOCKS::getValue);
        com.bewitchment.potion.HellworldRules.validateMappings(ForgeRegistries.BLOCKS::getValue);
        com.bewitchment.potion.IceworldRules.validateMappings(ForgeRegistries.BLOCKS::getValue);
        com.bewitchment.potion.CitrinitasRules.validateMappings(ForgeRegistries.BLOCKS::getValue);
        for(Item resin:new Item[]{com.bewitchment.registry.ModItems.ACACIA_RESIN.get(),com.bewitchment.registry.ModItems.BIRCH_SOUL.get()}){
        ItemStack jar=BrewIngredients.returned(new ItemStack(resin));
        if(jar.getItem()!=com.bewitchment.registry.ModItems.EMPTY_JAR.get()||jar.getCount()!=1||BrewIngredients.supported(jar))throw new IllegalStateException("Invalid color brew jar return");
        }
        String[][] expected={
            {"bewitchment:hellhound_horn","hellworld","1"},{"minecraft:ice","iceworld","1"},{"minecraft:packed_ice","iceworld","1"},{"minecraft:snow_block","iceworld","1"},{"bewitchment:birch_soul","citrinitas","1"},{"bewitchment:acacia_resin","rubedo","1"},{"minecraft:cobweb","arachnophobia","1"},{"minecraft:coal","desertification","1"},{"minecraft:charcoal","desertification","1"},
            {"minecraft:mushroom_stew","spore_cloud","1"},
            {"minecraft:rotten_flesh","blight","1"},
            {"minecraft:golden_apple","mending","1"},{"minecraft:enchanted_golden_apple","mending","1"},
            {"minecraft:blue_orchid","fertility","1"},
            {"minecraft:pumpkin","harvest","1"},{"minecraft:carved_pumpkin","harvest","1"},
            {"minecraft:cactus","ruin","1"},{"minecraft:dead_bush","ruin","1"},
            {"wormwood","pesticide","1"},{"aconitum","wolfsbane","1"},{"elderberries","deviants_decomposure","1"},
            {"minecraft:popped_chorus_fruit","deflection","600"},{"minecraft:fire_charge","volatility","900"},
            {"minecraft:bone","rage","1"},{"minecraft:cookie","friendship","1"},{"demon_heart","fear","600"},
            {"minecraft:milk_bucket","absence","1"},{"minecraft:melon_slice","purification","1"},{"minecraft:fermented_spider_eye","corruption","1"},
            {"minecraft:poppy","sleeping","1"},{"toe_of_frog","cursed_leaps","1"},{"cloudy_oil","butterfingers","1"},{"tallow","disrobing","1"},
            {"dragons_blood_resin","magic_resistance","1200"},{"stone_ichor","bulk","600"},{"oil_of_vitriol","corrosion","600"},
            {"minecraft:feather","grace","600"},{"minecraft:paper","paper_skin","600"},{"minecraft:diamond","rhino_hide","600"},
            {"coquina","shell_armor","600"},{"white_sage","holy_water","1"},{"garlic","holy_water","1"},{"minecraft:allium","holy_water","1"},
            {"minecraft:ender_pearl","revealing","1"},{"minecraft:gold_nugget","sinking","400"},{"minecraft:iron_nugget","sinking","400"},{"cold_iron_nugget","sinking","400"}
        };
        for(String[] row:expected){
            ResourceLocation id=new ResourceLocation(row[0].contains(":")?row[0]:"bewitchment:"+row[0]);
            java.util.List<net.minecraft.potion.EffectInstance> effects=BrewIngredients.effects(new ItemStack(ForgeRegistries.ITEMS.getValue(id)));
            if(effects.size()!=1||!effects.get(0).getEffect().getRegistryName().equals(new ResourceLocation("bewitchment",row[1]))||effects.get(0).getDuration()!=Integer.parseInt(row[2]))throw new IllegalStateException("Incorrect brewing mapping "+id);
        }
        if(BrewIngredients.effects(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bewitchment:eye_of_old")))).size()!=3)throw new IllegalStateException("Eye of Old must contribute three effects");
        org.apache.logging.log4j.LogManager.getLogger().info("Verified {} direct brewing ingredients against live registries",BrewIngredients.ids().size());
        org.apache.logging.log4j.LogManager.getLogger().info("Verified {} custom brew mappings and three-effect Eye of Old",expected.length);
    });}
    private BrewValidation(){}
}
