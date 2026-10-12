package com.bewitchment.registry;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.ModItemTier;
import com.bewitchment.item.ModArmorMaterial;
import com.bewitchment.item.BottledFrostfireItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Food;
import net.minecraft.item.SwordItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Material IDs retained from the MIT-licensed legacy ModObjects registry. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Bewitchment.MOD_ID);
    public static final RegistryObject<Item> CONTRACT = ITEMS.register("contract", com.bewitchment.item.KillContractItem::new);

    public static final ItemGroup TAB = new ItemGroup(Bewitchment.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.SILVER_INGOT.get());
        }
    };

    public static final RegistryObject<Item> SIGIL_PURITY = ITEMS.register("sigil_purity", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.PURITY));
    public static final RegistryObject<Item> SIGIL_FAILURE = ITEMS.register("sigil_failure", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.FAILURE));
    public static final RegistryObject<Item> SIGIL_SENTINEL = ITEMS.register("sigil_sentinel", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.SENTINEL));
    public static final RegistryObject<Item> SIGIL_RUIN = ITEMS.register("sigil_ruin", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.RUIN));
    public static final RegistryObject<Item> SIGIL_BATTLE = ITEMS.register("sigil_battle", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.BATTLE));
    public static final RegistryObject<Item> SIGIL_MENDING = ITEMS.register("sigil_mending", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.MENDING));
    public static final RegistryObject<Item> SIGIL_BINDING = ITEMS.register("sigil_binding", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.BINDING));
    public static final RegistryObject<Item> SIGIL_CLEANSING = ITEMS.register("sigil_cleansing", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.CLEANSING));
    public static final RegistryObject<Item> SIGIL_LUCK = ITEMS.register("sigil_luck", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.LUCK));
    public static final RegistryObject<Item> SIGIL_DISORIENTATION = ITEMS.register("sigil_disorientation", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.DISORIENTATION));
    public static final RegistryObject<Item> SIGIL_SHRIEKING = ITEMS.register("sigil_shrieking", () -> new com.bewitchment.item.ChestSigilItem(com.bewitchment.item.ChestSigilItem.Kind.SHRIEKING));
    public static final RegistryObject<Item> AMETHYST = material("amethyst");
    public static final RegistryObject<Item> CLEAVER_SWORD = ITEMS.register("cleaver_sword", com.bewitchment.item.CleaverSwordItem::new);
    public static final RegistryObject<Item> SKELETON_KEY = ITEMS.register("skeleton_key", com.bewitchment.item.SkeletonKeyItem::new);
    public static final RegistryObject<Item> JUNIPER_KEY = ITEMS.register("juniper_key", () -> new com.bewitchment.item.JuniperKeyItem(false));
    public static final RegistryObject<Item> JUNIPER_KEY_RING = ITEMS.register("juniper_key_ring", () -> new com.bewitchment.item.JuniperKeyItem(true));
    public static final RegistryObject<Item> LEONARDS_WAND = ITEMS.register("leonards_wand", com.bewitchment.item.LeonardsWandItem::new);
    public static final RegistryObject<Item> CADUCEUS = ITEMS.register("caduceus", com.bewitchment.item.CaduceusItem::new);
    public static final RegistryObject<Item> THYRSUS = ITEMS.register("thyrsus", com.bewitchment.item.ThyrsusItem::new);
    public static final RegistryObject<Item> GARNET = material("garnet");
    public static final RegistryObject<Item> OPAL = material("opal");
    public static final RegistryObject<Item> SILVER_INGOT = material("silver_ingot");
    public static final RegistryObject<Item> COLD_IRON_INGOT = material("cold_iron_ingot");
    public static final RegistryObject<Item> SILVER_PLATE = material("silver_plate");
    public static final RegistryObject<Item> COLD_IRON_PLATE = material("cold_iron_plate");
    public static final RegistryObject<Item> SILVER_NUGGET = material("silver_nugget");
    public static final RegistryObject<Item> COLD_IRON_NUGGET = material("cold_iron_nugget");
    public static final RegistryObject<Item> UNFIRED_JAR = material("unfired_jar");
    public static final RegistryObject<Item> EMPTY_JAR = material("empty_jar");
    public static final RegistryObject<Item> WOOD_ASH = material("wood_ash");
    public static final RegistryObject<Item> TALLOW = material("tallow");
    public static final RegistryObject<Item> FLYING_OINTMENT = material("flying_ointment");
    public static final RegistryObject<Item> SILVER_ARROW = ITEMS.register("silver_arrow", () -> new com.bewitchment.item.MetalArrowItem(false));
    public static final RegistryObject<Item> COLD_IRON_ARROW = ITEMS.register("cold_iron_arrow", () -> new com.bewitchment.item.MetalArrowItem(true));
    public static final RegistryObject<Item> SPIRIT_STRING = material("spirit_string");
    public static final RegistryObject<Item> WITCHES_STITCHING = material("witches_stitching");
    public static final RegistryObject<Item> GOLDEN_THREAD = material("golden_thread");
    public static final RegistryObject<Item> DIABOLICAL_VEIN = material("diabolical_vein");
    public static final RegistryObject<Item> PURE_FILAMENT = material("pure_filament");
    public static final RegistryObject<Item> SANGUINE_CLOTH = material("sanguine_cloth");
    public static final RegistryObject<Item> ECTOPLASM = material("ectoplasm");
    public static final RegistryObject<Item> DIMENSIONAL_SAND = material("dimensional_sand");
    public static final RegistryObject<Item> GARLIC_BREAD = ITEMS.register("garlic_bread", () ->
            new Item(new Item.Properties().tab(TAB).food(new Food.Builder().nutrition(7).saturationMod(0.8F).build())));
    public static final RegistryObject<Item> GARLIC_GRILLED = ITEMS.register("garlic_grilled", () ->
            new Item(new Item.Properties().tab(TAB).food(new Food.Builder().nutrition(4).saturationMod(0.5F).build())));
    public static final RegistryObject<Item> GARLIC = ITEMS.register("garlic", () ->
            new Item(new Item.Properties().tab(TAB).food(new Food.Builder().nutrition(2).saturationMod(0.2F).build())));
    public static final RegistryObject<Item> WHITE_SAGE = material("white_sage");
    public static final RegistryObject<Item> GARLIC_SEEDS = ITEMS.register("garlic_seeds", () ->
            new BlockNamedItem(ModBlocks.CROP_GARLIC.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> WHITE_SAGE_SEEDS = ITEMS.register("white_sage_seeds", () ->
            new BlockNamedItem(ModBlocks.CROP_WHITE_SAGE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> OAK_SPIRIT = fume("oak_spirit");
    public static final RegistryObject<Item> SPRUCE_HEART = fume("spruce_heart");
    public static final RegistryObject<Item> BIRCH_SOUL = fume("birch_soul");
    public static final RegistryObject<Item> CLOUDY_OIL = fume("cloudy_oil");
    public static final RegistryObject<Item> ACACIA_RESIN = fume("acacia_resin");
    public static final RegistryObject<Item> CLEANSING_BALM = fume("cleansing_balm");
    public static final RegistryObject<Item> FIERY_UNGUENT = fume("fiery_unguent");
    public static final RegistryObject<Item> DROPLET_OF_WISDOM = fume("droplet_of_wisdom");
    public static final RegistryObject<Item> STONE_ICHOR = fume("stone_ichor");
    public static final RegistryObject<Item> EBB_OF_DEATH = fume("ebb_of_death");
    public static final RegistryObject<Item> LIQUID_WITCHCRAFT = fume("liquid_witchcraft");
    public static final RegistryObject<Item> DEMONIC_ELIXIR = fume("demonic_elixir");
    public static final RegistryObject<Item> HEAVEN_EXTRACT = fume("heaven_extract");
    public static final RegistryObject<Item> LIZARD_LEG = material("lizard_leg");
    public static final RegistryObject<Item> OWLETS_WING = material("owlets_wing");
    public static final RegistryObject<Item> RAVENS_FEATHER = material("ravens_feather");
    public static final RegistryObject<Item> ADDERS_FORK = material("adders_fork");
    public static final RegistryObject<Item> TOE_OF_FROG = material("toe_of_frog");
    public static final RegistryObject<Item> HELLHOUND_HORN = material("hellhound_horn");
    public static final RegistryObject<Item> BOX_OF_SEALED_EVIL = ITEMS.register("box_of_sealed_evil", com.bewitchment.item.SealedEvilBoxItem::new);
    public static final RegistryObject<Item> DEMON_HEART = material("demon_heart");
    public static final RegistryObject<Item> SNAKE_VENOM = ITEMS.register("snake_venom", () -> new Item(new Item.Properties().tab(TAB).craftRemainder(net.minecraft.item.Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> BOTTLED_HELLFIRE = ITEMS.register("bottled_hellfire", () -> new com.bewitchment.item.BottledHellfireItem(ModBlocks.HELLFIRE.get()));
    public static final RegistryObject<Item> STEW_OF_THE_GROTESQUE = ITEMS.register("stew_of_the_grotesque", com.bewitchment.item.GrotesqueStewItem::new);
    public static final RegistryObject<Item> PENTACLE = material("pentacle");
    public static final RegistryObject<Item> WAYSTONE = ITEMS.register("waystone", com.bewitchment.item.WaystoneItem::new);
    public static final RegistryObject<Item> BONE_NEEDLE = material("bone_needle");
    public static final RegistryObject<Item> ATHAME = ITEMS.register("athame", com.bewitchment.item.AthameItem::new);
    public static final RegistryObject<Item> BOLINE = ITEMS.register("boline", com.bewitchment.item.BolineItem::new);
    public static final RegistryObject<Item> TAGLOCK = ITEMS.register("taglock", com.bewitchment.item.TaglockItem::new);
    public static final RegistryObject<Item> DRAGONS_BLOOD_RESIN = ITEMS.register("dragons_blood_resin", () -> new com.bewitchment.item.LegacyDyeItem(() -> net.minecraft.item.Items.RED_DYE));
    public static final RegistryObject<Item> BOTTLE_OF_BLOOD = ITEMS.register("bottle_of_blood", com.bewitchment.item.BloodBottleItem::new);
    public static final RegistryObject<Item> HEART = ITEMS.register("heart", () -> new Item(new Item.Properties().tab(TAB)
            .food(new Food.Builder().nutrition(8).saturationMod(0.8F).meat().alwaysEat()
                    .effect(() -> new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.CONFUSION, 750, 3), 1)
                    .effect(() -> new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.HUNGER, 750, 3), 1).build())));
    public static final RegistryObject<Item> SPECTRAL_DUST = material("spectral_dust");
    public static final RegistryObject<Item> HOOF = material("hoof");
    public static final RegistryObject<Item> TONGUE_OF_DOG = material("tongue_of_dog");
    public static final RegistryObject<Item> EYE_OF_OLD = material("eye_of_old");
    public static final RegistryObject<Item> SWIRL_OF_DEPTHS = fume("swirl_of_depths");
    public static final RegistryObject<Item> OIL_OF_VITRIOL = fume("oil_of_vitriol");
    public static final RegistryObject<Item> INK_CAP_DYE = ITEMS.register("ink_cap_dye", () -> new com.bewitchment.item.LegacyDyeItem(() -> net.minecraft.item.Items.BLUE_DYE));
    public static final RegistryObject<Item> IRON_GALL_INK = ITEMS.register("iron_gall_ink", () -> new com.bewitchment.item.LegacyDyeItem(() -> net.minecraft.item.Items.BLACK_DYE));
    public static final RegistryObject<Item> CATECHU_BROWN = ITEMS.register("catechu_brown", () -> new com.bewitchment.item.LegacyDyeItem(() -> net.minecraft.item.Items.BROWN_DYE));
    public static final RegistryObject<Item> JUNIPER_TEA = ITEMS.register("juniper_tea", () -> new com.bewitchment.item.BottledFoodItem(true));
    public static final RegistryObject<Item> ELDERBERRY_JELLY = ITEMS.register("elderberry_jelly", () -> new com.bewitchment.item.BottledFoodItem(false));
    public static final RegistryObject<Item> OAK_APPLE_GALL = material("oak_apple_gall");
    public static final RegistryObject<Item> ESSENCE_OF_VITALITY = fume("essence_of_vitality");
    public static final RegistryObject<Item> JUNIPER_BERRIES = ITEMS.register("juniper_berries", () ->
            new Item(new Item.Properties().tab(TAB).food(new Food.Builder().nutrition(1).saturationMod(0.5F).build())));
    public static final RegistryObject<Item> OTHERWORLDLY_TEARS = fume("otherworldly_tears");
    public static final RegistryObject<Item> ELDERBERRIES = ITEMS.register("elderberries", () ->
            new Item(new Item.Properties().tab(TAB).food(new Food.Builder().nutrition(1).saturationMod(0.5F)
                    .effect(() -> new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.REGENERATION, 100), 0.1F).build())));
    public static final RegistryObject<Item> STONE_WITCHES_ALTAR = ITEMS.register("stone_witches_altar", () -> new BlockItem(ModBlocks.STONE_WITCHES_ALTAR.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> DISTILLERY = ITEMS.register("distillery", () ->
            new BlockItem(ModBlocks.DISTILLERY.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> WITCHES_OVEN = ITEMS.register("witches_oven", () ->
            new BlockItem(ModBlocks.WITCHES_OVEN.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt", () ->
            new BlockItem(ModBlocks.SALT_BARRIER.get(), new Item.Properties().tab(TAB)) {
                @Override
                public String getDescriptionId() {
                    return "item.bewitchment.salt";
                }
            });
    public static final RegistryObject<Item> SALT_ORE = ITEMS.register("salt_ore", () ->
            new BlockItem(ModBlocks.SALT_ORE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> BLOCK_OF_SALT = ITEMS.register("block_of_salt", () ->
            new BlockItem(ModBlocks.BLOCK_OF_SALT.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> BOTTLED_FROSTFIRE = ITEMS.register("bottled_frostfire", () ->
            new BottledFrostfireItem(ModBlocks.FROSTFIRE.get(), new Item.Properties().tab(TAB)));

    static {
        tools("silver", ModItemTier.SILVER);
        tools("cold_iron", ModItemTier.COLD_IRON);
        armor("silver", ModArmorMaterial.SILVER);
        armor("cold_iron", ModArmorMaterial.COLD_IRON);
    }

    private static void armor(String metal, ModArmorMaterial material) {
        ITEMS.register(metal + "_helmet", () -> new ArmorItem(material, EquipmentSlotType.HEAD, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_chestplate", () -> new ArmorItem(material, EquipmentSlotType.CHEST, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_leggings", () -> new ArmorItem(material, EquipmentSlotType.LEGS, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_boots", () -> new ArmorItem(material, EquipmentSlotType.FEET, new Item.Properties().tab(TAB)));
    }

    private static void tools(String metal, ModItemTier tier) {
        ITEMS.register(metal + "_sword", () -> new SwordItem(tier, 3, -2.4F, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_pickaxe", () -> new PickaxeItem(tier, 1, -2.8F, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_axe", () -> new AxeItem(tier, 5.0F, -3.1F, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_shovel", () -> new ShovelItem(tier, 1.5F, -3.0F, new Item.Properties().tab(TAB)));
        ITEMS.register(metal + "_hoe", () -> new HoeItem(tier, -tier.getLevel(), tier.getLevel() - 3.0F,
                new Item.Properties().tab(TAB)));
    }

    private static RegistryObject<Item> material(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(TAB)));
    }

    private static RegistryObject<Item> fume(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(TAB).craftRemainder(EMPTY_JAR.get())));
    }

    private ModItems() {}
}

