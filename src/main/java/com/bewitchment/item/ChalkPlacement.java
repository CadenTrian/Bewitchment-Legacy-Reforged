package com.bewitchment.item;

import com.bewitchment.block.GlyphBlock;
import net.minecraft.block.BlockState;

/** Focal glyph storage is never replaced by drawing over it. */
public final class ChalkPlacement {
    public static boolean canReplace(BlockState previous, boolean fluidPresent) {
        if (fluidPresent) return false;
        if (previous.getBlock() instanceof GlyphBlock) return previous.getValue(GlyphBlock.TYPE) != 0;
        return previous.getMaterial().isReplaceable();
    }
    public static boolean insideHeight(int y, int maximum) { return y > 0 && y < maximum; }
    private ChalkPlacement() {}
}