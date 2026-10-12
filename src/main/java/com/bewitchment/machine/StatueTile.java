package com.bewitchment.machine;

import com.bewitchment.block.StatueBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/** Identity and facing come from the block state, so no duplicate NBT needs syncing. */
public final class StatueTile extends TileEntity {
    public StatueTile() { super(ModMachines.STATUE.get()); }
    @Override public AxisAlignedBB getRenderBoundingBox() {
        int height = getBlockState().getBlock() instanceof StatueBlock ? ((StatueBlock)getBlockState().getBlock()).height : 4;
        return new AxisAlignedBB(worldPosition).expandTowards(0, height - 1, 0).inflate(1);
    }
}
