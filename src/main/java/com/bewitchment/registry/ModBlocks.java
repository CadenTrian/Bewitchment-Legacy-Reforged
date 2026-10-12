package com.bewitchment.registry;

import com.bewitchment.Bewitchment;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.state.properties.BlockStateProperties;
import com.bewitchment.block.SaltBarrierBlock;
import com.bewitchment.block.FrostfireBlock;
import com.bewitchment.block.WitchesOvenBlock;
import com.bewitchment.block.HerbCropBlock;
import com.bewitchment.block.DistilleryBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bewitchment.MOD_ID);
    public static final RegistryObject<Block> BRAZIER = blockWithItem("brazier", com.bewitchment.block.BrazierBlock::new);
    public static final RegistryObject<Block> CRYSTAL_BALL = blockWithItem("crystal_ball", com.bewitchment.block.CrystalBallBlock::new);
    public static final RegistryObject<Block> SIGIL = BLOCKS.register("sigil", com.bewitchment.block.PlacedSigilBlock::new);
    public static final RegistryObject<Block> SIGIL_TABLE = blockWithItem("sigil_table", com.bewitchment.block.SigilTableBlock::new);
    public static final RegistryObject<Block> DRAGONS_BLOOD_CHEST = blockWithItem("dragons_blood_chest", com.bewitchment.block.DragonsBloodChestBlock::new);
    public static final RegistryObject<Block> JUNIPER_CHEST = blockWithItem("juniper_chest", com.bewitchment.block.JuniperChestBlock::new);
    public static final RegistryObject<Block> SPANISH_MOSS = blockWithItem("spanish_moss", () -> new com.bewitchment.block.SpanishMossBlock(false));
    public static final RegistryObject<Block> SPANISH_MOSS_END = BLOCKS.register("spanish_moss_end", () -> new com.bewitchment.block.SpanishMossBlock(true));
    public static final RegistryObject<Block> FROSTFLOWER = blockWithItem("frostflower", com.bewitchment.block.FrostflowerBlock::new);
    public static final RegistryObject<Block> SPINNING_WHEEL = blockWithItem("spinning_wheel", com.bewitchment.block.SpinningWheelBlock::new);
    public static final RegistryObject<Block> HELLFIRE = BLOCKS.register("hellfire", com.bewitchment.block.HellfireBlock::new);
    public static final RegistryObject<Block> ELDER_WOOD = blockWithItem("elder_wood", () -> new net.minecraft.block.RotatedPillarBlock(AbstractBlock.Properties.copy(Blocks.OAK_LOG)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_PLANKS = blockWithItem("elder_planks", () -> new Block(AbstractBlock.Properties.copy(Blocks.OAK_PLANKS)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 20; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_LEAVES = blockWithItem("elder_leaves", () -> new net.minecraft.block.LeavesBlock(AbstractBlock.Properties.copy(Blocks.OAK_LEAVES)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 60; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 30; }
    });
    public static final RegistryObject<Block> ELDER_SAPLING = blockWithItem("elder_sapling", () -> new net.minecraft.block.SaplingBlock(new com.bewitchment.world.ElderTrees(), AbstractBlock.Properties.copy(Blocks.OAK_SAPLING)));

    public static final RegistryObject<Block> ELDER_STAIRS = blockWithItem("elder_wood_stairs", () -> new net.minecraft.block.StairsBlock(() -> ELDER_PLANKS.get().defaultBlockState(), AbstractBlock.Properties.copy(Blocks.OAK_STAIRS)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 20; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_SLAB = blockWithItem("elder_wood_slab", () -> new net.minecraft.block.SlabBlock(AbstractBlock.Properties.copy(Blocks.OAK_SLAB)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 20; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_FENCE = blockWithItem("elder_fence", () -> new net.minecraft.block.FenceBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 20; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_FENCE_GATE = blockWithItem("elder_fence_gate", () -> new net.minecraft.block.FenceGateBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE_GATE)) {
        @Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 20; }
        @Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 5; }
    });
    public static final RegistryObject<Block> ELDER_DOOR = blockWithItem("elder_door", () -> new net.minecraft.block.DoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_DOOR)) {});
    public static final RegistryObject<Block> ELDER_TRAPDOOR = blockWithItem("elder_trapdoor", () -> new net.minecraft.block.TrapDoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_TRAPDOOR)) {});
    public static final RegistryObject<Block> ELDER_BUTTON = blockWithItem("elder_button", () -> new net.minecraft.block.WoodButtonBlock(AbstractBlock.Properties.copy(Blocks.OAK_BUTTON)) {});
    public static final RegistryObject<Block> ELDER_PRESSURE_PLATE = blockWithItem("elder_pressure_plate", () -> new net.minecraft.block.PressurePlateBlock(net.minecraft.block.PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties.copy(Blocks.OAK_PRESSURE_PLATE)) {});

    private static RegistryObject<Block> blockWithItem(String name, java.util.function.Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    public static final RegistryObject<Block> SILVER_ORE = ore("silver_ore", false);
    public static final RegistryObject<Block> AMETHYST_ORE = ore("amethyst_ore", true);
    public static final RegistryObject<Block> GARNET_ORE = ore("garnet_ore", true);
    public static final RegistryObject<Block> OPAL_ORE = ore("opal_ore", true);
    public static final RegistryObject<Block> SALT_ORE = BLOCKS.register("salt_ore", () ->
            new OreBlock(AbstractBlock.Properties.copy(Blocks.COAL_ORE)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(0).requiresCorrectToolForDrops()) {
                @Override
                protected int xpOnDrop(java.util.Random random) {
                    return MathHelper.nextInt(random, 0, 2);
                }
            });
    public static final RegistryObject<Block> BLOCK_OF_SALT = BLOCKS.register("block_of_salt", () ->
            new Block(AbstractBlock.Properties.copy(Blocks.SAND).strength(1, 3).sound(SoundType.SAND)
                    .harvestTool(ToolType.SHOVEL)));
    public static final RegistryObject<Block> SALT_BARRIER = BLOCKS.register("salt_barrier", SaltBarrierBlock::new);
    public static final RegistryObject<Block> FROSTFIRE = BLOCKS.register("frostfire", FrostfireBlock::new);
    public static final RegistryObject<Block> WITCHES_OVEN = BLOCKS.register("witches_oven", WitchesOvenBlock::new);
    public static final RegistryObject<Block> WITCHES_CAULDRON = blockWithItem("witches_cauldron", com.bewitchment.block.WitchesCauldronBlock::new);
    public static final RegistryObject<Block> PLACED_ITEM = BLOCKS.register("placed_item", com.bewitchment.block.PlacedItemBlock::new);
    public static final RegistryObject<Block> EMBERGRASS = blockWithItem("embergrass", () -> new com.bewitchment.block.MagicalPlantBlock(false));
    public static final RegistryObject<Block> TORCHWOOD = blockWithItem("torchwood", () -> new com.bewitchment.block.MagicalPlantBlock(true));
    public static final RegistryObject<Block> STONE_WITCHES_ALTAR = BLOCKS.register("stone_witches_altar", com.bewitchment.block.WitchesAltarBlock::new);
    public static final RegistryObject<Block> OBSIDIAN_WITCHES_ALTAR = blockWithItem("obsidian_witches_altar", com.bewitchment.block.WitchesAltarBlock::new);
    public static final RegistryObject<Block> NETHER_BRICK_WITCHES_ALTAR = blockWithItem("nether_brick_witches_altar", com.bewitchment.block.WitchesAltarBlock::new);
    public static final RegistryObject<Block> GOBLET = blockWithItem("goblet", () -> new com.bewitchment.block.GobletBlock(false));
    public static final RegistryObject<Block> FILLED_GOBLET = blockWithItem("filled_goblet", () -> new com.bewitchment.block.GobletBlock(true));
    public static final RegistryObject<Block> DISTILLERY = BLOCKS.register("distillery", DistilleryBlock::new);
    public static final RegistryObject<Block> CROP_GARLIC = BLOCKS.register("crop_garlic", () -> new HerbCropBlock(ModItems.GARLIC_SEEDS));
    public static final RegistryObject<Block> CROP_WHITE_SAGE = BLOCKS.register("crop_white_sage", () -> new HerbCropBlock(ModItems.WHITE_SAGE_SEEDS));
    public static final RegistryObject<Block> BLOCK_OF_SILVER = storage("block_of_silver", Blocks.IRON_BLOCK);
    public static final RegistryObject<Block> BLOCK_OF_COLD_IRON = storage("block_of_cold_iron", Blocks.IRON_BLOCK);
    public static final RegistryObject<Block> BLOCK_OF_AMETHYST = storage("block_of_amethyst", Blocks.DIAMOND_BLOCK);
    public static final RegistryObject<Block> BLOCK_OF_GARNET = storage("block_of_garnet", Blocks.DIAMOND_BLOCK);
    public static final RegistryObject<Block> BLOCK_OF_OPAL = storage("block_of_opal", Blocks.DIAMOND_BLOCK);

    static {
        // Legacy Chisel designs are available directly in Reforged.
        for (String metal : new String[]{"silver", "cold_iron"}) {
            for (String design : new String[]{"symbol", "bevel", "sun", "moon", "cup",
                    "pentacle", "sword", "wand", "pentagram"}) {
                storage("block_of_" + metal + "_" + design, Blocks.IRON_BLOCK);
            }
        }
        storage("block_of_silver_old", Blocks.IRON_BLOCK);
    }

    private static RegistryObject<Block> storage(String name, Block base) {
        RegistryObject<Block> block = BLOCKS.register(name, () ->
                new Block(AbstractBlock.Properties.copy(base).harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2).requiresCorrectToolForDrops()));
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }

    private static RegistryObject<Block> ore(String name, boolean gem) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> {
            AbstractBlock.Properties properties = AbstractBlock.Properties.copy(Blocks.IRON_ORE)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(gem ? 2 : 1).requiresCorrectToolForDrops();
            return gem ? new OreBlock(properties) {
                @Override
                protected int xpOnDrop(java.util.Random random) {
                    return MathHelper.nextInt(random, 3, 7);
                }
            } : new Block(properties);
        });
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }

    private ModBlocks() {}
}

