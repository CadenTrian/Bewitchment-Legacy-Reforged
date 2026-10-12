package com.bewitchment.registry;

import com.bewitchment.item.LegacyAccessoryItem;
import com.bewitchment.item.GrimoireMagiaItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.*;
import java.util.*;

public final class ModAccessories {
    public static final RegistryObject<Item> GRIMOIRE = ModItems.ITEMS.register("grimoire_magia", GrimoireMagiaItem::new);
    public static final RegistryObject<Item> BASTARDS_GRIMOIRE = ModItems.ITEMS.register("bastards_grimoire", com.bewitchment.item.BastardsGrimoireItem::new);
    public static final Map<LegacyAccessoryItem.Kind, RegistryObject<Item>> ITEMS = new EnumMap<>(LegacyAccessoryItem.Kind.class);
    static {
        for (LegacyAccessoryItem.Kind kind : LegacyAccessoryItem.Kind.values())
            ITEMS.put(kind, ModItems.ITEMS.register(kind.id, () -> kind == LegacyAccessoryItem.Kind.GIRDLE
                    ? new com.bewitchment.item.DryadGirdleItem() : new LegacyAccessoryItem(kind)));
    }
    public static void init() {}
    public static void slots(InterModEnqueueEvent event) {
        for (SlotTypePreset slot : new SlotTypePreset[]{SlotTypePreset.HEAD, SlotTypePreset.NECKLACE, SlotTypePreset.HANDS, SlotTypePreset.RING, SlotTypePreset.BELT, SlotTypePreset.CHARM, SlotTypePreset.CURIO})
            InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> slot.getMessageBuilder().size(1).build());
    }
    private ModAccessories() {}
}
