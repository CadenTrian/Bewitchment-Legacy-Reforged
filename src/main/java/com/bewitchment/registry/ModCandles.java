package com.bewitchment.registry;

import com.bewitchment.block.CandleBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModCandles {
    public static final Map<String, RegistryObject<Block>> BLOCKS = new LinkedHashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) add(color.getName() + "_candle", false);
        for (String metal : new String[]{"iron", "gold", "silver"}) add("candelabra_" + metal, true);
    }
    private static void add(String name, boolean candelabra) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, () -> new CandleBlock(candelabra));
        BLOCKS.put(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
    }
    public static void init() {}
    private ModCandles() {}
}
