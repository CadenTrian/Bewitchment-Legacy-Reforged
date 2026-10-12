package com.bewitchment.machine;

import com.bewitchment.registry.ModItems;
import net.minecraft.item.*;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import java.util.*;

/** Legacy values for items available in this port; tags permit compatible gem items. */
public final class AltarItemUpgrades {
    public enum Kind { CUP, PENTACLE, SWORD, WAND }
    public static final class Upgrade {
        public final Kind kind; public final int gain, bonus; public final double multiplier;
        Upgrade(Kind kind, int gain, double multiplier, int bonus) { this.kind = kind; this.gain = gain; this.multiplier = multiplier; this.bonus = bonus; }
    }
    private static final List<ITag.INamedTag<Item>> STRONG = tags("gems/diamond", "gems/emerald", "gems/prismarine", "gems/garnet", "gems/amethyst");
    private static final List<ITag.INamedTag<Item>> SIMPLE = tags("gems/quartz", "gems/lapis", "gems/opal", "dusts/redstone", "dusts/salt");
    private static List<ITag.INamedTag<Item>> tags(String... names) {
        List<ITag.INamedTag<Item>> result = new ArrayList<>();
        for (String name : names) result.add(ItemTags.createOptional(new ResourceLocation("forge", name)));
        return result;
    }
    public static void init() {}
    public static Upgrade get(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.FLYING_OINTMENT.get()) return new Upgrade(Kind.CUP, 1, 1.17, 0);
        if (item == com.bewitchment.registry.ModAccessories.GRIMOIRE.get()) return new Upgrade(Kind.PENTACLE, 2, 1, 0);
        if (item == ModItems.CLEAVER_SWORD.get()) return new Upgrade(Kind.SWORD, 0, 1.35, 0);
        if (item == ModItems.ATHAME.get()) return new Upgrade(Kind.SWORD, 0, 1.325, 0);
        if (item == ModItems.PENTACLE.get()) return new Upgrade(Kind.PENTACLE, 3, 1, 0);
        for (ITag<Item> tag : STRONG) if (stack.getItem().is(tag)) return new Upgrade(Kind.PENTACLE, 2, 1, 0);
        for (ITag<Item> tag : SIMPLE) if (stack.getItem().is(tag)) return new Upgrade(Kind.PENTACLE, 1, 1, 0);
        if (item == Items.GLASS_BOTTLE) return new Upgrade(Kind.CUP, 0, 1.05, 0);
        if (item == ModItems.LEONARDS_WAND.get()) return new Upgrade(Kind.WAND, 0, 1, (int) (64 * 1.56));
        if (item == ModItems.CADUCEUS.get()) return new Upgrade(Kind.WAND, 0, 1, (int) (64 * 1.78));
        if (item == ModItems.THYRSUS.get()) return new Upgrade(Kind.WAND, 0, 1, (int) (64 * 1.45));
        if (item == Items.BLAZE_ROD) return new Upgrade(Kind.WAND, 0, 1, (int) (64 * 1.2));
        double multiplier = item == Items.WOODEN_SWORD ? 1.075 : item == Items.STONE_SWORD ? 1.15
                : item == Items.IRON_SWORD ? 1.225 : item == Items.GOLDEN_SWORD ? 1.3 : item == Items.DIAMOND_SWORD ? 1.325 : 0;
        ResourceLocation id = item.getRegistryName();
        if (new ResourceLocation("bewitchment", "silver_sword").equals(id)) multiplier = 1.3;
        if (new ResourceLocation("bewitchment", "cold_iron_sword").equals(id)) multiplier = 1.225;
        return multiplier > 0 ? new Upgrade(Kind.SWORD, 0, multiplier, 0) : null;
    }
    private AltarItemUpgrades() {}
}
