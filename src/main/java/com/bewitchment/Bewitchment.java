package com.bewitchment;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.bewitchment.registry.ModItems;
import com.bewitchment.registry.ModBlocks;
import com.bewitchment.world.ModOreGeneration;
import com.bewitchment.combat.MaterialEffects;
import com.bewitchment.machine.ModMachines;
import com.bewitchment.loot.HerbSeedLoot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Entry point for the Forge 1.16.5 port of Bewitchment Legacy. */
@Mod(Bewitchment.MOD_ID)
public final class Bewitchment {
    public static final String MOD_ID = "bewitchment";

    public Bewitchment() {
        com.bewitchment.registry.ModBrooms.init();
        com.bewitchment.world.LegacyStructureGeneration.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, com.bewitchment.config.StructureGenerationConfig.SPEC, "bewitchment-structures.toml");
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.world.LegacyStructureGeneration::biomes);
        com.bewitchment.registry.ModVillagers.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModCreatures.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModCreatures.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(com.bewitchment.registry.ModCreatures::attributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(com.bewitchment.registry.ModCreatures::verify);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.bewitchment.registry.ModCreatures.SPEC, "bewitchment-creatures.toml");
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.registry.ModCreatures::biomes);
        com.bewitchment.registry.ModProjectiles.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.world.MossGeneration.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModEffects.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.bewitchment.config.WorldGenerationConfig.SPEC, "bewitchment-worldgen.toml");
        com.bewitchment.world.GenerationGate.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModTreeBlocks.init();
        com.bewitchment.registry.ModSiphoningFlowers.init();
        com.bewitchment.world.DragonTree.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.registry.ModHerbs.init();
        com.bewitchment.registry.ModCandles.init();
        com.bewitchment.registry.ModCoquina.init();
        com.bewitchment.registry.ModScornedBricks.init();
        com.bewitchment.registry.ModLegacyDecor.init();
        com.bewitchment.registry.ModClothing.init();
        com.bewitchment.registry.ModPoppets.init();
        com.bewitchment.registry.ModAccessories.init();
        com.bewitchment.registry.ModRituals.init();
        com.bewitchment.registry.ModRituals.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(com.bewitchment.registry.ModAccessories::slots);
        com.bewitchment.registry.ModPoppetShelves.init();
        com.bewitchment.registry.ModStatues.init();
        com.bewitchment.registry.ModIceBlocks.init();
        com.bewitchment.world.CoquinaGeneration.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        com.bewitchment.machine.CauldronTile.init();
        com.bewitchment.world.LegacyTrees.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModMachines.TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModMachines.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModMachines.RECIPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        HerbSeedLoot.SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(ModOreGeneration::onBiomeLoading);
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.world.ElderTrees::onBiomeLoading);
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.world.LegacyTrees::onBiomeLoading);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.bewitchment.config.RitualConfig.SPEC, "bewitchment-rituals.toml");
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.bewitchment.config.ToolConfig.SPEC, "bewitchment-tools.toml");
        MaterialEffects.register();
        com.bewitchment.loot.AthameLoot.register();
        com.bewitchment.machine.PlacedItemInteraction.register();
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.world.CoquinaGeneration::onBiomeLoading);
        MinecraftForge.EVENT_BUS.addListener(com.bewitchment.world.MossGeneration::onBiomeLoading);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(com.bewitchment.machine.BundledRecipeAudit::verify).whenComplete((ignored,error)->{
            if(error!=null)org.apache.logging.log4j.LogManager.getLogger().error("Bewitchment bundled recipe audit failed",error);
        });
        event.enqueueWork(com.bewitchment.registry.ModVillagers::verify);
        event.enqueueWork(com.bewitchment.machine.JuniperChestTile::verify);
        event.enqueueWork(com.bewitchment.item.ChestSigilItem::verify);
        event.enqueueWork(com.bewitchment.registry.ModCreatures::placements);
        event.enqueueWork(com.bewitchment.machine.LandscapeRitual::verifyAccess);
        event.enqueueWork(com.bewitchment.world.DragonTree::register);
        event.enqueueWork(com.bewitchment.config.WorldGenerationConfig::capture);
        event.enqueueWork(ModOreGeneration::register);
        event.enqueueWork(com.bewitchment.world.ElderTrees::register);
        event.enqueueWork(com.bewitchment.world.LegacyTrees::register);
        event.enqueueWork(com.bewitchment.world.CoquinaGeneration::register);
        event.enqueueWork(com.bewitchment.world.MossGeneration::register);
        event.enqueueWork(com.bewitchment.world.LegacyStructureGeneration::register);
        event.enqueueWork(com.bewitchment.registry.ModProjectiles::setup);
    }
}
