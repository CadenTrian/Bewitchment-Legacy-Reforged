package com.bewitchment.client;

import com.bewitchment.Bewitchment;
import com.bewitchment.registry.ModBlocks;
import com.bewitchment.machine.ModMachines;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private static int glyphColor(int type) { return type == 0 ? 0xe3dc3c : type == 2 ? 0xbb0000 : type == 3 ? 0x770077 : 0xffffff; }
    @SubscribeEvent
    public static void verifyRenderers(net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            if (!(net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.getRenderer(new com.bewitchment.machine.JuniperChestTile()) instanceof JuniperChestRenderer))
                throw new IllegalStateException("Missing Juniper Chest renderer");
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Juniper Chest renderer verified");
            if (!(net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.getRenderer(new com.bewitchment.machine.DragonsBloodChestTile()) instanceof DragonsBloodChestRenderer))
                throw new IllegalStateException("Missing Dragon Blood Chest renderer");
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Dragon Blood Chest renderer verified");
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.GLYPH.get(), GlyphRenderer::new);
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModRituals.GLYPH.get(), RenderType.cutout());
            net.minecraft.client.Minecraft.getInstance().getBlockColors().register((state, world, pos, tint) ->
                    glyphColor(state.getValue(com.bewitchment.block.GlyphBlock.TYPE)), com.bewitchment.registry.ModRituals.GLYPH.get());
                        java.util.Map<net.minecraft.entity.EntityType<?>, net.minecraft.client.renderer.entity.EntityRenderer<?>> creatureRenderers = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().renderers;
            if (!(creatureRenderers.get(com.bewitchment.registry.ModCreatures.BAPHOMET.get()) instanceof CreatureRenderers.Baphomet) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.LEONARD.get()) instanceof CreatureRenderers.Leonard) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.DEMONESS.get()) instanceof CreatureRenderers.Demoness) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.DEMON.get()) instanceof CreatureRenderers.Demon) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.GHOST.get()) instanceof CreatureRenderers.Ghost) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.CLEAVER.get()) instanceof CreatureRenderers.Cleaver) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.BAFOMETYR.get()) instanceof CreatureRenderers.Bafometyr) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.SHADOW_PERSON.get()) instanceof CreatureRenderers.ShadowPerson) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.LIZARD.get()) instanceof CreatureRenderers.Lizard)
                    || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.TOAD.get()) instanceof CreatureRenderers.Toad) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.SNAKE.get()) instanceof CreatureRenderers.Snake) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.RAVEN.get()) instanceof CreatureRenderers.Raven) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.OWL.get()) instanceof CreatureRenderers.Owl) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.FEUERWURM.get()) instanceof CreatureRenderers.Feuerwurm) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.HELLHOUND.get()) instanceof CreatureRenderers.Hellhound) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.BLACK_DOG.get()) instanceof CreatureRenderers.BlackDog) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.IMP.get()) instanceof CreatureRenderers.Imp) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.WEREWOLF.get()) instanceof CreatureRenderers.Werewolf) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.DRUDEN.get()) instanceof CreatureRenderers.Druden) || !(creatureRenderers.get(com.bewitchment.registry.ModCreatures.CAMBION.get()) instanceof CreatureRenderers.Cambion))
                throw new IllegalStateException("Missing Bewitchment creature renderer after client setup");
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Lizard, Toad, Snake, Raven, Owl, Feuerwurm, Hellhound, Black Dog, Imp, Cambion, Druden, Werewolf, Shadow Person, Bafometyr, Cleaver, Ghost, Demon and Demoness, Leonard and Baphomet renderers verified");
            for(com.bewitchment.entity.BroomRules.Kind kind:com.bewitchment.entity.BroomRules.Kind.values())
                if(!(creatureRenderers.get(com.bewitchment.registry.ModBrooms.ENTITIES.get(kind).get()) instanceof BroomRenderer))throw new IllegalStateException("Missing broom renderer: "+kind);
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment four broom renderers verified");
            Object renderer = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().renderers
                    .get(com.bewitchment.registry.ModProjectiles.METAL_ARROW.get());
            if (!(renderer instanceof MetalArrowRenderer))
                throw new IllegalStateException("Missing Bewitchment metal-arrow renderer after client setup");
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment metal-arrow renderer verified after client setup");
        });
    }
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        for(com.bewitchment.entity.BroomRules.Kind kind:com.bewitchment.entity.BroomRules.Kind.values())
            net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModBrooms.ENTITIES.get(kind).get(),BroomRenderer::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.GHOST.get(), CreatureRenderers.Ghost::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.DEMON.get(), CreatureRenderers.Demon::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.DEMONESS.get(), CreatureRenderers.Demoness::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.BAPHOMET.get(), CreatureRenderers.Baphomet::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.LEONARD.get(), CreatureRenderers.Leonard::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.CLEAVER.get(), CreatureRenderers.Cleaver::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.BAFOMETYR.get(), CreatureRenderers.Bafometyr::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.SHADOW_PERSON.get(), CreatureRenderers.ShadowPerson::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.LIZARD.get(), CreatureRenderers.Lizard::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.TOAD.get(), CreatureRenderers.Toad::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.RAVEN.get(), CreatureRenderers.Raven::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.OWL.get(), CreatureRenderers.Owl::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.SNAKE.get(), CreatureRenderers.Snake::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.FEUERWURM.get(), CreatureRenderers.Feuerwurm::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.HELLHOUND.get(), CreatureRenderers.Hellhound::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.IMP.get(), CreatureRenderers.Imp::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.WEREWOLF.get(), CreatureRenderers.Werewolf::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.DRUDEN.get(), CreatureRenderers.Druden::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.CAMBION.get(), CreatureRenderers.Cambion::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModCreatures.BLACK_DOG.get(), CreatureRenderers.BlackDog::new);
        // Forge snapshots renderer factories before queued client setup work runs.
        // RenderingRegistry is thread-safe and must be called directly in this event.
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.bewitchment.registry.ModProjectiles.METAL_ARROW.get(), MetalArrowRenderer::new);
        event.enqueueWork(() -> {
            net.minecraft.item.ItemModelsProperties.register(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("bewitchment:cold_iron_sword")),
                    new net.minecraft.util.ResourceLocation("bewitchment:legacy_variant"),(stack,world,entity)->com.bewitchment.item.LegacyItemVariants.sword(stack.getHoverName().getString()));
            net.minecraft.item.ItemModelsProperties.register(com.bewitchment.registry.ModItems.EYE_OF_OLD.get(),
                    new net.minecraft.util.ResourceLocation("bewitchment:legacy_variant"),(stack,world,entity)->com.bewitchment.item.LegacyItemVariants.eye(stack.getHoverName().getString()));
            net.minecraft.item.ItemModelsProperties.register(com.bewitchment.registry.ModAccessories.GRIMOIRE.get(),
                    new net.minecraft.util.ResourceLocation("bewitchment", "alice"),
                    (stack, world, entity) -> com.bewitchment.item.GrimoireMagiaItem.aliceVariant(stack) ? 1 : 0);
            RenderTypeLookup.setRenderLayer(ModBlocks.SIGIL.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.BRAZIER.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.CRYSTAL_BALL.get(), RenderType.translucent());
            ScreenManager.register(ModMachines.SIGIL_TABLE_MENU.get(), SigilTableScreen::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.SIGIL_TABLE.get(), RenderType.cutout());
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.DRAGONS_BLOOD_CHEST.get(), DragonsBloodChestRenderer::new);
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.JUNIPER_CHEST.get(), JuniperChestRenderer::new);
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.POPPET_SHELF.get(), PoppetShelfRenderer::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.SPANISH_MOSS.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.SPANISH_MOSS_END.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.FROSTFLOWER.get(), RenderType.cutout());
            net.minecraft.item.ItemModelsProperties.register(com.bewitchment.registry.ModClothing.ITEMS.get("witches_hat").get(),
                    new net.minecraft.util.ResourceLocation("bewitchment", "faith"),
                    (stack, world, entity) -> ((com.bewitchment.item.WitchClothingItem)stack.getItem()).faithVariant(stack) ? 1 : 0);
            ScreenManager.register(ModMachines.SPINNING_WHEEL_MENU.get(), SpinningWheelScreen::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.SPINNING_WHEEL.get(), RenderType.cutout());
            net.minecraft.client.Minecraft.getInstance().getItemColors().register((stack, tint) -> tint == 0 ? 0xe6c44f : 0xffffff,
                    com.bewitchment.registry.ModItems.SNAKE_VENOM.get());
            RenderTypeLookup.setRenderLayer(ModBlocks.HELLFIRE.get(), RenderType.cutout());
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.STATUE.get(), StatueRenderer::new);
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModLegacyDecor.BLOCKS.get("embittered_brick_witches_altar").get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModLegacyDecor.BLOCKS.get("dragons_blood_resin_block").get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModIceBlocks.ICE.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModIceBlocks.ICE_STAIRS.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModIceBlocks.ICE_SLAB.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModIceBlocks.ICE_FENCE.get(), RenderType.translucent());
            com.bewitchment.registry.ModSiphoningFlowers.FLOWERS.values().forEach(flower -> RenderTypeLookup.setRenderLayer(flower.get(), RenderType.cutout()));
            net.minecraft.item.ItemModelsProperties.register(com.bewitchment.registry.ModItems.TAGLOCK.get(), new net.minecraft.util.ResourceLocation("bewitchment", "bound"),
                    (stack, world, entity) -> com.bewitchment.item.TaglockItem.bound(stack) ? 1 : 0);
            net.minecraft.item.ItemModelsProperties.register(com.bewitchment.registry.ModItems.CONTRACT.get(), new net.minecraft.util.ResourceLocation("bewitchment", "bound"),
                    (stack, world, entity) -> com.bewitchment.item.KillContractItem.signed(stack)?1:0);
            net.minecraft.client.Minecraft.getInstance().getItemColors().register((stack, tint) -> tint == 0 ? 0x9e0000 : 0xffffff,
                    com.bewitchment.registry.ModItems.BOTTLE_OF_BLOOD.get());
            net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntityRenderer(ModMachines.PLACED_ITEM.get(), PlacedItemRenderer::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.EMBERGRASS.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.TORCHWOOD.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModCoquina.BLUE_INK_CAP.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModCoquina.ALTAR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModScornedBricks.ALTAR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.WITCHES_CAULDRON.get(), RenderType.translucent());
            net.minecraft.client.Minecraft.getInstance().getBlockColors().register((state, world, pos, tint) -> 0x3F76E4, ModBlocks.WITCHES_CAULDRON.get());
            com.bewitchment.registry.ModHerbs.CROPS.values().forEach(crop -> RenderTypeLookup.setRenderLayer(crop.get(), RenderType.cutout()));
            com.bewitchment.registry.ModCandles.BLOCKS.values().forEach(candle -> RenderTypeLookup.setRenderLayer(candle.get(), RenderType.cutout()));
            for (com.bewitchment.registry.WoodFamily family : new com.bewitchment.registry.WoodFamily[]{
                    com.bewitchment.registry.ModTreeBlocks.JUNIPER, com.bewitchment.registry.ModTreeBlocks.CYPRESS, com.bewitchment.registry.ModTreeBlocks.DRAGONS_BLOOD}) {
                RenderTypeLookup.setRenderLayer(family.leaves.get(), RenderType.cutoutMipped());
                RenderTypeLookup.setRenderLayer(family.sapling.get(), RenderType.cutout());
                RenderTypeLookup.setRenderLayer(family.door.get(), RenderType.cutout());
                RenderTypeLookup.setRenderLayer(family.trapdoor.get(), RenderType.cutout());
            }
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModTreeBlocks.POTTED_ELDER.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModTreeBlocks.POTTED_JUNIPER.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModTreeBlocks.POTTED_CYPRESS.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(com.bewitchment.registry.ModTreeBlocks.POTTED_DRAGONS_BLOOD.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.ELDER_SAPLING.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.ELDER_DOOR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.ELDER_TRAPDOOR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.ELDER_LEAVES.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.STONE_WITCHES_ALTAR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.OBSIDIAN_WITCHES_ALTAR.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.NETHER_BRICK_WITCHES_ALTAR.get(), RenderType.cutout());
            ScreenManager.register(ModMachines.OVEN_MENU.get(), OvenScreen::new);
            ScreenManager.register(ModMachines.DISTILLERY_MENU.get(), DistilleryScreen::new);
            RenderTypeLookup.setRenderLayer(ModBlocks.DISTILLERY.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(ModBlocks.WITCHES_OVEN.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.CROP_GARLIC.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.CROP_WHITE_SAGE.get(), RenderType.cutout());
            // The legacy ore models layer transparent mineral textures over stone.
            RenderTypeLookup.setRenderLayer(ModBlocks.SILVER_ORE.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.AMETHYST_ORE.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.GARNET_ORE.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.OPAL_ORE.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.SALT_ORE.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.SALT_BARRIER.get(), RenderType.cutout());
            RenderTypeLookup.setRenderLayer(ModBlocks.FROSTFIRE.get(), RenderType.cutout());
        });
    }

    private ClientSetup() {}
}

