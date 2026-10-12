package com.bewitchment.machine;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public final class DiggingFortuneRules {
    public static boolean eligible(FortuneData.Kind kind, Block block, boolean nether) {
        if (kind == FortuneData.Kind.TREASURE)
            return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.SAND
                    || block == Blocks.MYCELIUM || block == Blocks.GRAVEL || block == Blocks.SOUL_SAND;
        if (kind == FortuneData.Kind.HELLISH_FORTUNE && nether)
            return block == Blocks.SOUL_SAND || block == Blocks.NETHERRACK || block == Blocks.MAGMA_BLOCK
                    || block == Blocks.GRAVEL || block == Blocks.NETHER_BRICKS || block == Blocks.NETHER_QUARTZ_ORE;
        return false;
    }
    private DiggingFortuneRules() {}
}
