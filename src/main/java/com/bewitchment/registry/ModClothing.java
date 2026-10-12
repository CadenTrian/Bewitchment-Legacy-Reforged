package com.bewitchment.registry;

import com.bewitchment.item.WitchClothingItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModClothing {
    public static final Map<String, RegistryObject<WitchClothingItem>> ITEMS = new LinkedHashMap<>();
    static {
        family("witches", "witches"); family("alchemist", "alchemists_attire");
        family("besmirched", "besmirched_cloak"); family("green_witch", "green_witch_robes");
    }
    private static void family(String family, String texture) {
        for (String part : Arrays.asList("hat", family.equals("witches") ? "cowl" : "hood", "robes", "pants")) {
            EquipmentSlotType slot = part.equals("robes") ? EquipmentSlotType.CHEST : part.equals("pants") ? EquipmentSlotType.LEGS : EquipmentSlotType.HEAD;
            ITEMS.put(family + "_" + part, ModItems.ITEMS.register(family + "_" + part, () -> new WitchClothingItem(family, texture, slot, part.equals("hat"))));
        }
    }
    public static boolean hasSet(LivingEntity entity, String family) {
        return matches(entity.getItemBySlot(EquipmentSlotType.HEAD), family, EquipmentSlotType.HEAD)
                && matches(entity.getItemBySlot(EquipmentSlotType.CHEST), family, EquipmentSlotType.CHEST)
                && matches(entity.getItemBySlot(EquipmentSlotType.LEGS), family, EquipmentSlotType.LEGS);
    }
    public static boolean matches(ItemStack stack, String family, EquipmentSlotType slot) {
        return stack.getItem() instanceof WitchClothingItem && ((WitchClothingItem)stack.getItem()).family.equals(family)
                && ((WitchClothingItem)stack.getItem()).getSlot() == slot;
    }
    public static void init() {}
    private ModClothing() {}
}
