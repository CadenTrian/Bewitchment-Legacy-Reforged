package com.bewitchment.registry;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;
import java.util.function.Supplier;

/** Legacy scorned-brick properties and Chisel IDs, with vanilla building shapes. */
public final class ModScornedBricks {
    private static AbstractBlock.Properties properties() {
        return AbstractBlock.Properties.copy(Blocks.STONE).strength(25.2F, 1001)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops();
    }
    public static final RegistryObject<Block> BRICKS = add("scorned_bricks", () -> new Block(properties()));
    public static final Map<String, RegistryObject<Block>> DESIGNS = new LinkedHashMap<>();
    static {
        for (String design : Arrays.asList("symbol", "bevel", "hellish", "raw", "raw_cracked", "small", "soft", "small_tiles", "medium_tiles",
                "triple", "braid", "layers", "road", "ornate", "panel", "prism", "slanted"))
            DESIGNS.put(design, add("scorned_bricks_" + design, () -> new Block(properties())));
    }
    public static final RegistryObject<Block> CRACKED = add("cracked_scorned_bricks", () -> new Block(properties()));
    public static final RegistryObject<Block> CHISELED = add("chiseled_scorned_bricks", () -> new Block(properties()));
    public static final RegistryObject<Block> STAIRS = add("scorned_brick_stairs", () -> new StairsBlock(() -> BRICKS.get().defaultBlockState(), properties()));
    public static final RegistryObject<Block> SLAB = add("scorned_bricks_slab", () -> new SlabBlock(properties()));
    public static final RegistryObject<Block> FENCE = add("scorned_brick_fence", () -> new FenceBlock(properties()));
    public static final RegistryObject<Block> WALL = add("scorned_brick_wall", () -> new WallBlock(properties()));
    public static final RegistryObject<Block> ALTAR = add("scorned_brick_witches_altar", () -> new com.bewitchment.block.WitchesAltarBlock(properties()));
    private static RegistryObject<Block> add(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    public static void init() {}
    private ModScornedBricks() {}
}
