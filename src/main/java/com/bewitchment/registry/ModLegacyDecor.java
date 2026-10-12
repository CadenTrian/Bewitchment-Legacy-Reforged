package com.bewitchment.registry;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;
import java.util.function.Supplier;

/** Legacy building families, including fleeces produced by the spinning wheel. */
public final class ModLegacyDecor {
    public static final Map<String, RegistryObject<Block>> BLOCKS = new LinkedHashMap<>();
    private static AbstractBlock.Properties stone() { return AbstractBlock.Properties.copy(Blocks.STONE).harvestTool(ToolType.PICKAXE).harvestLevel(0).requiresCorrectToolForDrops(); }
    private static AbstractBlock.Properties metal() { return AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).strength(5, 30).harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops(); }
    static {
        for (String family : Arrays.asList("mossy_scorned", "crying_scorned", "despair", "ochre_despair")) {
            RegistryObject<Block> base = add(family + "_bricks", () -> new Block(stone()));
            shapes(family + "_brick", base, ModLegacyDecor::stone);
        }
        RegistryObject<Block> nethersteel = add("nethersteel", () -> new Block(metal()));
        for (String design : Arrays.asList("symbol", "bevel", "polished", "sentient", "pentacle", "pentagram", "skull", "eye", "watching_eye", "hellish", "watching_hellish"))
            add("nethersteel_" + design, () -> new Block(metal()));
        shapes("nethersteel", nethersteel, ModLegacyDecor::metal);
        for (String name : Arrays.asList("greenwitch_fleece", "besmirched_fleece", "alchemists_fleece")) add(name, Fleece::new);
        for (String name : Arrays.asList("alchemists_carpet", "hedgewitches_carpet", "besmirched_carpet")) add(name, com.bewitchment.block.DecorativeCarpet::new);
        for (String design : Arrays.asList("smooth", "shell")) add("coquina_" + design, () -> new Block(stone().strength(5, 30)));
        add("embittered_brick_wall", () -> new WallBlock(stone().strength(1.5F, 30)));
        add("embittered_brick_witches_altar", () -> new com.bewitchment.block.WitchesAltarBlock(stone().strength(1.5F, 30)));
        // Legacy resin storage copies slime's material/properties, not its bouncing/sticky behavior.
        add("dragons_blood_resin_block", () -> new Block(AbstractBlock.Properties.copy(Blocks.SLIME_BLOCK).noOcclusion()));
    }
    private static void shapes(String prefix, RegistryObject<Block> base, Supplier<AbstractBlock.Properties> properties) {
        add(prefix + "_stairs", () -> new StairsBlock(() -> base.get().defaultBlockState(), properties.get()));
        add(prefix + "_slab", () -> new SlabBlock(properties.get()));
        add(prefix + "_fence", () -> new FenceBlock(properties.get()));
        add(prefix + "_wall", () -> new WallBlock(properties.get()));
    }
    private static RegistryObject<Block> add(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, factory);
        BLOCKS.put(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    public static final class Fleece extends Block {
        Fleece() { super(Properties.copy(Blocks.WHITE_WOOL)); }
        @Override public int getFlammability(BlockState state, net.minecraft.world.IBlockReader world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.Direction face) { return 60; }
        @Override public int getFireSpreadSpeed(BlockState state, net.minecraft.world.IBlockReader world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.Direction face) { return 30; }
    }
    public static void init() {}
    private ModLegacyDecor() {}
}
