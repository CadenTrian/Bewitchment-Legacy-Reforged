package com.bewitchment.registry;

import com.bewitchment.block.SiphoningFlowerBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModSiphoningFlowers {
    public static final Map<String, RegistryObject<Block>> FLOWERS = new LinkedHashMap<>();
    static {
        for (String flower : Arrays.asList("allium", "azure_bluet", "blue_orchid", "dandelion", "oxeye_daisy", "tulip_orange", "tulip_pink", "tulip_red", "tulip_white", "poppy")) {
            String name = "flower_siphoning_" + flower;
            RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, SiphoningFlowerBlock::new);
            FLOWERS.put(flower, block);
            ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        }
    }
    public static void init() {}
    private ModSiphoningFlowers() {}
}
