package com.bewitchment.potion;

import net.minecraft.item.ItemStack;
import java.util.function.Predicate;

/** Remove before publishing; restore the original stack if the world rejects the drop. */
public final class BrewEquipment {
    public interface Slot { ItemStack get(); void set(ItemStack stack); }
    public static boolean consumingPotion(boolean using,ItemStack active,ItemStack held){return using&&active==held&&held.getItem() instanceof net.minecraft.item.PotionItem;}
    public static boolean drop(Slot slot,int count,Predicate<ItemStack> publish){
        ItemStack original=slot.get().copy();
        if(original.isEmpty()||count<=0)return false;
        ItemStack rest=original.copy(),removed=rest.split(Math.min(count,rest.getCount()));
        slot.set(rest);
        boolean accepted=false;
        try {accepted=publish.test(removed);return accepted;}
        finally {if(!accepted)slot.set(original);}
    }
    private BrewEquipment(){}
}
