package com.bewitchment.registry;

import com.bewitchment.block.PoppetShelfBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModPoppetShelves {
    public static final Map<String, RegistryObject<Block>> BLOCKS = new LinkedHashMap<>();
    static {
        for (String wood : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "elder", "juniper", "cypress", "dragons_blood"}) {
            String name = "poppet_shelf_" + wood;
            RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, PoppetShelfBlock::new);
            BLOCKS.put(wood, block);
            ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        }
    }
    public static void init() {}
    private ModPoppetShelves() {}
}
