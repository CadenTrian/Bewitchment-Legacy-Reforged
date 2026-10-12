package com.bewitchment.registry;

import com.bewitchment.block.StatueBlock;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModStatues {
    public static final Map<String, RegistryObject<StatueBlock>> BLOCKS = new LinkedHashMap<>();
    static {
        for (String deity : Arrays.asList("baphomet", "herne", "leonard", "lilith", "moloch")) {
            for (String material : Arrays.asList("stone", "gold", "nether_brick", "scorned_brick"))
                add(deity, material, true, deity.equals("baphomet") || deity.equals("moloch") ? 1 : 2);
            for (String material : Arrays.asList("stone", "gold", "nether_brick", "scorned_brick"))
                add(deity, material, false, deity.equals("baphomet") ? 2 : deity.equals("herne") || deity.equals("lilith") ? 4 : 3);
        }
    }
    private static void add(String deity, String material, boolean idol, int height) {
        String name = material + "_" + deity + (idol ? "_idol" : "_statue");
        RegistryObject<StatueBlock> block = ModBlocks.BLOCKS.register(name, () -> {
            AbstractBlock.Properties properties = AbstractBlock.Properties.copy(material.equals("gold") ? Blocks.GOLD_BLOCK : material.equals("nether_brick") ? Blocks.NETHER_BRICKS : Blocks.STONE)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(material.equals("gold") || material.equals("scorned_brick") ? 2 : 0).requiresCorrectToolForDrops();
            if (material.equals("scorned_brick")) properties.strength(25.2F, 1001);
            return new StatueBlock(properties, deity, material, idol, height);
        });
        BLOCKS.put(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)
                .setISTER(() -> com.bewitchment.client.StatueItemRenderer::new)));
    }
    public static void init() {}
    private ModStatues() {}
}
