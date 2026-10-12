package com.bewitchment.registry;

import com.bewitchment.block.HerbCropBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModHerbs {
    public static final Map<String, RegistryObject<Block>> CROPS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> SEEDS = new LinkedHashMap<>();
    static {
        for (String name : new String[]{"aconitum", "belladonna", "hellebore", "mandrake", "wormwood"}) {
            RegistryObject<Block> crop = ModBlocks.BLOCKS.register("crop_" + name, () -> new HerbCropBlock(() -> SEEDS.get(name).get()));
            CROPS.put(name, crop);
            SEEDS.put(name, ModItems.ITEMS.register(name + "_seeds", () -> new BlockNamedItem(crop.get(), new Item.Properties().tab(ModItems.TAB))));
            ModItems.ITEMS.register(name.equals("mandrake") ? "mandrake_root" : name, () -> new Item(new Item.Properties().tab(ModItems.TAB)));
        }
    }
    public static void init() {}
    private ModHerbs() {}
}
