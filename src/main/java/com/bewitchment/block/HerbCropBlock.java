package com.bewitchment.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.util.IItemProvider;
import java.util.function.Supplier;

public final class HerbCropBlock extends CropsBlock {
    private final Supplier<? extends IItemProvider> seeds;
    public HerbCropBlock(Supplier<? extends IItemProvider> seeds) {
        super(Properties.copy(Blocks.WHEAT));
        this.seeds = seeds;
    }
    @Override protected IItemProvider getBaseSeedId() { return seeds.get(); }
}
