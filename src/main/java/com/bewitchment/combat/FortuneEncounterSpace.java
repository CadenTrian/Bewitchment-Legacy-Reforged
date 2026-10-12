package com.bewitchment.combat;

import com.bewitchment.machine.VillagerFortunePlacement;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** Loaded, dry two-block landing shared by villager and small hostile encounters. */
final class FortuneEncounterSpace {
    static boolean fits(ServerWorld world, MobEntity entity, BlockPos pos) {
        if (!VillagerFortunePlacement.height(pos.getY(), world.getMaxBuildHeight())) return false;
        entity.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        AxisAlignedBB box = entity.getBoundingBox();
        if (box.minY < 0 || box.maxY > world.getMaxBuildHeight()
                || box.minX < world.getWorldBorder().getMinX() || box.maxX > world.getWorldBorder().getMaxX()
                || box.minZ < world.getWorldBorder().getMinZ() || box.maxZ > world.getWorldBorder().getMaxZ()) return false;
        // Guard every block touched by collision scans, including their neighboring cells.
        if (!world.hasChunksAt(new BlockPos(box.minX - 1, pos.getY() - 1, box.minZ - 1),
                new BlockPos(box.maxX + 1, Math.max(pos.getY() + 2, box.maxY + 1), box.maxZ + 1))) return false;
        return world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above())
                && world.getBlockState(pos.below()).isValidSpawn(world, pos.below(), entity.getType())
                && world.noCollision(entity) && !world.containsAnyLiquid(box);
    }

    private FortuneEncounterSpace() {}
}
