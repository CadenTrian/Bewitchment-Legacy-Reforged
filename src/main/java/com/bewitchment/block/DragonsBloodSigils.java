package com.bewitchment.block;

import com.bewitchment.machine.DragonsBloodSigilTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class DragonsBloodSigils {
    public static BlockPos base(BlockState state, BlockPos pos) {
        return state.getBlock() instanceof DragonsBloodDoor && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }
    public static DragonsBloodSigilTile tile(BlockState state, IBlockReader world, BlockPos pos) {
        BlockPos base = base(state, pos);
        if (world.getBlockState(base).getBlock() != state.getBlock()) return null;
        TileEntity tile = world.getBlockEntity(base);
        return tile instanceof DragonsBloodSigilTile ? (DragonsBloodSigilTile) tile : null;
    }
    static void activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) return;
        DragonsBloodSigilTile tile = tile(state, world, pos);
        if (tile != null) tile.activate(player, hand, hit);
    }
    static void particles(BlockState state, World world, BlockPos pos) {
        DragonsBloodSigilTile tile = tile(state, world, pos);
        if (tile != null && tile.hasSigil()) world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX(), pos.getY(), pos.getZ(), 1, 0, 0);
    }
    private DragonsBloodSigils() {}
}
