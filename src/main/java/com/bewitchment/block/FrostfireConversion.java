package com.bewitchment.block;

import net.minecraft.item.ItemStack;
import java.util.function.Predicate;

/** Reserve one ore before insertion callbacks; refused or throwing insertion restores it. */
public final class FrostfireConversion {
    public static boolean convert(ItemStack input, ItemStack output, Predicate<ItemStack> insert) {
        if (input.isEmpty() || output.isEmpty()) return false;
        input.shrink(1);
        boolean accepted = false;
        try {
            accepted = insert.test(output.copy());
            return accepted;
        } finally {
            if (!accepted) input.grow(1);
        }
    }
    private FrostfireConversion() {}
}