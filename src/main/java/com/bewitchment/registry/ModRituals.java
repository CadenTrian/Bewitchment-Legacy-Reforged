package com.bewitchment.registry;

import com.bewitchment.block.GlyphBlock;
import com.bewitchment.item.ChalkItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public final class ModRituals {
    public static final net.minecraftforge.registries.DeferredRegister<net.minecraft.util.SoundEvent> SOUNDS =
            net.minecraftforge.registries.DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS, "bewitchment");
    public static final RegistryObject<net.minecraft.util.SoundEvent> CHALK_SOUND = SOUNDS.register("chalk_scribble", () ->
            new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("bewitchment", "chalk_scribble")));
    public static final RegistryObject<Block> GLYPH = ModBlocks.BLOCKS.register("glyph", GlyphBlock::new);
    public static final RegistryObject<Item> RITUAL_CHALK = ModItems.ITEMS.register("ritual_chalk", () -> new ChalkItem(1));
    public static final RegistryObject<Item> FOCAL_CHALK = ModItems.ITEMS.register("focal_chalk", () -> new ChalkItem(0));
    public static final RegistryObject<Item> FIERY_CHALK = ModItems.ITEMS.register("fiery_chalk", () -> new ChalkItem(2));
    public static final RegistryObject<Item> PHASING_CHALK = ModItems.ITEMS.register("phasing_chalk", () -> new ChalkItem(3));
    public static final RegistryObject<Block> PURIFYING_EARTH = ModBlocks.BLOCKS.register("purifying_earth", com.bewitchment.block.PurifyingEarthBlock::new);
    public static final RegistryObject<Item> PURIFYING_EARTH_ITEM = ModItems.ITEMS.register("purifying_earth", () ->
            new net.minecraft.item.BlockItem(PURIFYING_EARTH.get(), new Item.Properties().tab(ModItems.TAB)));
    public static void init() {}
    private ModRituals() {}
}
