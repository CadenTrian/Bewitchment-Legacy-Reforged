package com.bewitchment.machine;

import com.bewitchment.registry.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** Converts only the five vanilla saplings supported by the legacy ritual. */
public final class FrenziedGrowth {
    enum Target { NONE, ELDER, CYPRESS, JUNIPER }
    static Target target(Block block) {
        if (block == Blocks.OAK_SAPLING || block == Blocks.BIRCH_SAPLING) return Target.ELDER;
        if (block == Blocks.SPRUCE_SAPLING || block == Blocks.JUNGLE_SAPLING) return Target.CYPRESS;
        return block == Blocks.ACACIA_SAPLING ? Target.JUNIPER : Target.NONE;
    }
    static Iterable<BlockPos> positions(BlockPos center) { return BlockPos.betweenClosed(center.offset(-16,-16,-16), center.offset(16,16,16)); }
    public static void complete(ServerWorld world, BlockPos center, PlayerEntity caster) {
        if (!caster.abilities.mayBuild) return;
        for (BlockPos pos : positions(center)) {
            if (pos.getY() < 0 || pos.getY() >= 256 || !world.hasChunkAt(pos)) continue;
            Target target = target(world.getBlockState(pos).getBlock());
            if (target == Target.NONE || !world.mayInteract(caster, pos) || !caster.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) continue;
            Block block = target == Target.ELDER ? ModBlocks.ELDER_SAPLING.get()
                    : target == Target.CYPRESS ? ModTreeBlocks.CYPRESS.sapling.get() : ModTreeBlocks.JUNIPER.sapling.get();
            BlockState replacement = block.defaultBlockState();
            if (replacement.canSurvive(world, pos)) world.setBlock(pos, replacement, 3);
        }
    }
    private FrenziedGrowth() {}
}
