package com.bewitchment.block;

import com.bewitchment.machine.DragonsBloodSigilTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;

public final class DragonsBloodDoor extends DoorBlock {
    public DragonsBloodDoor() { super(Properties.copy(Blocks.OAK_DOOR)); }
    @Override public boolean hasTileEntity(BlockState state) { return state.getValue(HALF) == DoubleBlockHalf.LOWER; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return hasTileEntity(state) ? new DragonsBloodSigilTile() : null; }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (player.isSpectator()) return ActionResultType.PASS;
        DragonsBloodSigils.activate(state, world, pos, player, hand, hit);
        return super.use(state, world, pos, player, hand, hit);
    }
    @Override public void animateTick(BlockState state, World world, BlockPos pos, Random random) { DragonsBloodSigils.particles(state, world, pos); }
}
