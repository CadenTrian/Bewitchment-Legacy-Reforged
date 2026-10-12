package com.bewitchment.registry;

import com.bewitchment.item.PoppetItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModPoppets {
    public static final RegistryObject<Item> BASE = ModItems.ITEMS.register("poppet", () -> new Item(new Item.Properties().tab(ModItems.TAB)));
    public static final Map<PoppetItem.Kind, RegistryObject<Item>> ITEMS = new EnumMap<>(PoppetItem.Kind.class);
    static {
        for (PoppetItem.Kind kind : PoppetItem.Kind.values())
            ITEMS.put(kind, ModItems.ITEMS.register("poppet_" + kind.suffix, () -> kind == PoppetItem.Kind.VOODOO
                    ? new com.bewitchment.item.VoodooPoppetItem() : new PoppetItem(kind)));
    }
    public static void init() {}
    private ModPoppets() {}
}
