package com.bewitchment.registry;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import java.util.function.Supplier;

public final class ModCoquina {
    private static AbstractBlock.Properties stone() { return AbstractBlock.Properties.copy(Blocks.STONE).strength(5, 30).harvestTool(ToolType.PICKAXE).harvestLevel(0).requiresCorrectToolForDrops(); }
    public static final RegistryObject<Block> RAW = add("coquina", () -> new Block(stone()));
    public static final RegistryObject<Block> BRICKS = add("coquina_bricks", () -> new Block(stone()));
    public static final RegistryObject<Block> CHISELED = add("chiseled_coquina", () -> new Block(stone()));
    public static final RegistryObject<Block> STAIRS = add("coquina_stairs", () -> new StairsBlock(() -> RAW.get().defaultBlockState(), stone()));
    public static final RegistryObject<Block> SLAB = add("coquina_slab", () -> new SlabBlock(stone()));
    public static final RegistryObject<Block> WALL = add("coquina_wall", () -> new WallBlock(stone()));
    public static final RegistryObject<Block> FENCE = add("coquina_fence", () -> new FenceBlock(stone()));
    public static final RegistryObject<Block> BRICK_STAIRS = add("coquina_brick_stairs", () -> new StairsBlock(() -> BRICKS.get().defaultBlockState(), stone()));
    public static final RegistryObject<Block> BRICK_SLAB = add("coquina_brick_slab", () -> new SlabBlock(stone()));
    public static final RegistryObject<Block> BRICK_WALL = add("coquina_brick_wall", () -> new WallBlock(stone()));
    public static final RegistryObject<Block> BRICK_FENCE = add("coquina_brick_fence", () -> new FenceBlock(stone()));
    public static final RegistryObject<Block> ALTAR = add("coquina_witches_altar", com.bewitchment.block.WitchesAltarBlock::new);
    public static final RegistryObject<Block> BLUE_INK_CAP = add("blue_ink_cap", com.bewitchment.block.BlueInkCapBlock::new);
    private static RegistryObject<Block> add(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    public static void init() {}
    private ModCoquina() {}
}
