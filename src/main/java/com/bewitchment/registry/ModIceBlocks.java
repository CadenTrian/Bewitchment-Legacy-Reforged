package com.bewitchment.registry;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import java.util.function.Supplier;

/** Plain blocks deliberately avoid IceBlock's melting and water-on-break behavior. */
public final class ModIceBlocks {
    private static AbstractBlock.Properties ice() { return AbstractBlock.Properties.copy(Blocks.ICE).noOcclusion(); }
    private static AbstractBlock.Properties brick() {
        return AbstractBlock.Properties.of(net.minecraft.block.material.Material.ICE_SOLID).sound(SoundType.STONE)
                .strength(1.5F, 30).harvestTool(ToolType.PICKAXE).harvestLevel(0).requiresCorrectToolForDrops();
    }
    public static final RegistryObject<Block> ICE = add("perpetual_ice", () -> new GlassBlock(ice()));
    public static final RegistryObject<Block> ICE_STAIRS = add("perpetual_ice_stairs", () -> new StairsBlock(() -> ICE.get().defaultBlockState(), ice()));
    public static final RegistryObject<Block> ICE_SLAB = add("perpetual_ice_slab", () -> new SlabBlock(ice()));
    public static final RegistryObject<Block> ICE_FENCE = add("perpetual_ice_fence", () -> new FenceBlock(ice()));
    public static final RegistryObject<Block> BRICKS = add("embittered_bricks", () -> new Block(brick()));
    public static final RegistryObject<Block> CRACKED = add("cracked_embittered_bricks", () -> new Block(brick()));
    public static final RegistryObject<Block> CHISELED = add("chiseled_embittered_bricks", () -> new Block(brick()));
    public static final RegistryObject<Block> STAIRS = add("embittered_brick_stairs", () -> new StairsBlock(() -> BRICKS.get().defaultBlockState(), brick()));
    public static final RegistryObject<Block> SLAB = add("embittered_bricks_slab", () -> new SlabBlock(brick()));
    public static final RegistryObject<Block> FENCE = add("embittered_brick_fence", () -> new FenceBlock(brick()));
    private static RegistryObject<Block> add(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    public static void init() {}
    private ModIceBlocks() {}
}
