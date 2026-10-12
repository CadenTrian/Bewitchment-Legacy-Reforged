package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import com.bewitchment.registry.ModBlocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModMachines {
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Bewitchment.MOD_ID);
    public static final RegistryObject<TileEntityType<DragonsBloodSigilTile>> DRAGONS_BLOOD_SIGIL = TILES.register("dragons_blood", () ->
            TileEntityType.Builder.of(DragonsBloodSigilTile::new, com.bewitchment.registry.ModTreeBlocks.DRAGONS_BLOOD.door.get(),
                    com.bewitchment.registry.ModTreeBlocks.DRAGONS_BLOOD.trapdoor.get()).build(null));
    public static final RegistryObject<TileEntityType<BrazierTile>> BRAZIER = TILES.register("brazier", () -> TileEntityType.Builder.of(BrazierTile::new, ModBlocks.BRAZIER.get()).build(null));
    public static final RegistryObject<TileEntityType<PlacedSigilTile>> PLACED_SIGIL = TILES.register("sigil", () ->
            TileEntityType.Builder.of(PlacedSigilTile::new, ModBlocks.SIGIL.get()).build(null));
    public static final RegistryObject<TileEntityType<SigilTableTile>> SIGIL_TABLE = TILES.register("sigil_table", () ->
            TileEntityType.Builder.of(SigilTableTile::new, ModBlocks.SIGIL_TABLE.get()).build(null));
    public static final RegistryObject<TileEntityType<DragonsBloodChestTile>> DRAGONS_BLOOD_CHEST = TILES.register("dragons_blood_chest", () ->
            TileEntityType.Builder.of(DragonsBloodChestTile::new, ModBlocks.DRAGONS_BLOOD_CHEST.get()).build(null));
    public static final RegistryObject<TileEntityType<JuniperChestTile>> JUNIPER_CHEST = TILES.register("juniper_chest", () ->
            TileEntityType.Builder.of(JuniperChestTile::new, ModBlocks.JUNIPER_CHEST.get()).build(null));
    public static final RegistryObject<TileEntityType<GlyphTile>> GLYPH = TILES.register("glyph", () ->
            TileEntityType.Builder.of(GlyphTile::new, com.bewitchment.registry.ModRituals.GLYPH.get()).build(null));
    public static final RegistryObject<TileEntityType<PoppetShelfTile>> POPPET_SHELF = TILES.register("poppet_shelf", () ->
            TileEntityType.Builder.of(PoppetShelfTile::new, com.bewitchment.registry.ModPoppetShelves.BLOCKS.values().stream()
                    .map(RegistryObject::get).toArray(net.minecraft.block.Block[]::new)).build(null));
    public static final RegistryObject<TileEntityType<StatueTile>> STATUE = TILES.register("statue", () ->
            TileEntityType.Builder.of(StatueTile::new, com.bewitchment.registry.ModStatues.BLOCKS.values().stream()
                    .map(RegistryObject::get).toArray(net.minecraft.block.Block[]::new)).build(null));
    public static final DeferredRegister<ContainerType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Bewitchment.MOD_ID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Bewitchment.MOD_ID);
    public static final RegistryObject<IRecipeSerializer<IncenseRecipe>> INCENSE_RECIPE = RECIPES.register("incense", IncenseRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<BrazierRecipe>> BRAZIER_RECIPE = RECIPES.register("brazier_curse", BrazierRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<SigilRecipe>> SIGIL_RECIPE = RECIPES.register("sigil_table", SigilRecipe.Serializer::new);
    public static final RegistryObject<ContainerType<SigilTableMenu>> SIGIL_TABLE_MENU = MENUS.register("sigil_table", () ->
            IForgeContainerType.create((id, inventory, buffer) -> new SigilTableMenu(id, inventory)));
    public static final RegistryObject<IRecipeSerializer<JuniperKeyRecipe>> DUPLICATE_KEY_RECIPE = RECIPES.register("duplicate_key", () -> new net.minecraft.item.crafting.SpecialRecipeSerializer<>(id -> new JuniperKeyRecipe(id,true)));
    public static final RegistryObject<IRecipeSerializer<JuniperKeyRecipe>> KEY_RING_RECIPE = RECIPES.register("juniper_key_ring", () -> new net.minecraft.item.crafting.SpecialRecipeSerializer<>(id -> new JuniperKeyRecipe(id,false)));
    public static final RegistryObject<IRecipeSerializer<LeonardsWandRecipe>> WAND_RECIPE = RECIPES.register("leonards_wand", () -> new net.minecraft.item.crafting.SpecialRecipeSerializer<>(LeonardsWandRecipe::new));
    public static final RegistryObject<IRecipeSerializer<RitualRecipe>> RITUAL_RECIPE = RECIPES.register("ritual", RitualRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<SpinningWheelRecipe>> SPINNING_WHEEL_RECIPE = RECIPES.register("spinning_wheel", SpinningWheelRecipe.Serializer::new);
    public static final RegistryObject<TileEntityType<SpinningWheelTile>> SPINNING_WHEEL = TILES.register("spinning_wheel", () ->
            TileEntityType.Builder.of(SpinningWheelTile::new, ModBlocks.SPINNING_WHEEL.get()).build(null));
    public static final RegistryObject<ContainerType<SpinningWheelMenu>> SPINNING_WHEEL_MENU = MENUS.register("spinning_wheel", () ->
            IForgeContainerType.create((id, inventory, buffer) -> new SpinningWheelMenu(id, inventory)));
    public static final RegistryObject<TileEntityType<SiphoningFlowerTile>> SIPHONING_FLOWER = TILES.register("siphoning_flower", () ->
            TileEntityType.Builder.of(SiphoningFlowerTile::new, com.bewitchment.registry.ModSiphoningFlowers.FLOWERS.values().stream()
                    .map(RegistryObject::get).toArray(net.minecraft.block.Block[]::new)).build(null));
    public static final RegistryObject<IRecipeSerializer<OvenRecipe>> OVEN_RECIPE = RECIPES.register("oven", OvenRecipe.Serializer::new);
    public static final RegistryObject<TileEntityType<PlacedItemTile>> PLACED_ITEM = TILES.register("placed_item", () ->
            TileEntityType.Builder.of(PlacedItemTile::new, ModBlocks.PLACED_ITEM.get()).build(null));
    public static final RegistryObject<IRecipeSerializer<CauldronRecipe>> CAULDRON_RECIPE = RECIPES.register("cauldron", CauldronRecipe.Serializer::new);
    public static final RegistryObject<TileEntityType<CauldronTile>> CAULDRON = TILES.register("witches_cauldron", () ->
            TileEntityType.Builder.of(CauldronTile::new, ModBlocks.WITCHES_CAULDRON.get()).build(null));
    public static final RegistryObject<IRecipeSerializer<DistilleryRecipe>> DISTILLERY_RECIPE = RECIPES.register("distillery", DistilleryRecipe.Serializer::new);
    public static final RegistryObject<TileEntityType<DistilleryTile>> DISTILLERY = TILES.register("distillery", () ->
            TileEntityType.Builder.of(DistilleryTile::new, ModBlocks.DISTILLERY.get()).build(null));
    public static final RegistryObject<ContainerType<DistilleryMenu>> DISTILLERY_MENU = MENUS.register("distillery", () ->
            IForgeContainerType.create((id, inventory, buffer) -> new DistilleryMenu(id, inventory)));
    public static final RegistryObject<TileEntityType<OvenTile>> OVEN = TILES.register("witches_oven", () ->
            TileEntityType.Builder.of(OvenTile::new, ModBlocks.WITCHES_OVEN.get()).build(null));
    public static final RegistryObject<ContainerType<OvenMenu>> OVEN_MENU = MENUS.register("witches_oven", () ->
            IForgeContainerType.create((id, inventory, buffer) -> new OvenMenu(id, inventory)));
    public static final RegistryObject<TileEntityType<AltarTile>> ALTAR = TILES.register("witches_altar", () -> TileEntityType.Builder.of(AltarTile::new,
            ModBlocks.STONE_WITCHES_ALTAR.get(), ModBlocks.OBSIDIAN_WITCHES_ALTAR.get(), ModBlocks.NETHER_BRICK_WITCHES_ALTAR.get(),
            com.bewitchment.registry.ModCoquina.ALTAR.get(), com.bewitchment.registry.ModScornedBricks.ALTAR.get(),
            com.bewitchment.registry.ModLegacyDecor.BLOCKS.get("embittered_brick_witches_altar").get()).build(null));
    private ModMachines() {}
}

