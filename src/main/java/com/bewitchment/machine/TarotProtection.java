package com.bewitchment.machine;

import com.bewitchment.item.PoppetItem;
import net.minecraft.item.*;
import java.util.UUID;

/** Inspect only protection locations supported by this reading; never consume charges. */
public final class TarotProtection {
    public static boolean heldTotem(ItemStack main,ItemStack off){return (!main.isEmpty()&&main.getItem()==Items.TOTEM_OF_UNDYING)||(!off.isEmpty()&&off.getItem()==Items.TOTEM_OF_UNDYING);}
    public static boolean poppet(ItemStack stack,UUID target){
        if(!(stack.getItem() instanceof PoppetItem))return false;
        PoppetItem.Kind kind=((PoppetItem)stack.getItem()).kind();
        return kind!=PoppetItem.Kind.VOODOO&&PoppetItem.usable(stack,kind,target);
    }
    private TarotProtection(){}
}
