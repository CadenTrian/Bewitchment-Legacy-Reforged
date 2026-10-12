package com.bewitchment.machine;

import com.bewitchment.block.GlyphBlock;
import com.bewitchment.item.ChalkItem;
import com.bewitchment.registry.ModRituals;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

/** Repaints an existing legacy ring, validating every position before any mutation. */
public final class RitualDrawing {
    interface Access {
        int type(BlockPos pos);
        boolean allowed(BlockPos pos);
        boolean setType(BlockPos pos, int type);
    }
    public static boolean enoughChalk(int type, int damage, int maximum, int needed, boolean creative) {
        return type >= 1 && type <= 3 && needed > 0 && maximum > 0 && damage >= 0
                && (creative || (long)maximum - damage >= needed);
    }
    static final class Plan {
        private final Map<BlockPos, Integer> before;
        private final int type;
        private final Access access;
        Plan(Map<BlockPos, Integer> before, int type, Access access) { this.before = before; this.type = type; this.access = access; }
        int size() { return before.size(); }
        boolean apply() {
            for (Map.Entry<BlockPos, Integer> entry : before.entrySet())
                if (access.type(entry.getKey()) != entry.getValue() || !access.allowed(entry.getKey())) return false;
            List<BlockPos> changed = new ArrayList<>();
            for (Map.Entry<BlockPos, Integer> entry : before.entrySet()) {
                BlockPos pos = entry.getKey();
                if (entry.getValue() == type) continue;
                changed.add(pos);
                if (!access.setType(pos, type) || access.type(pos) != type) {
                    Collections.reverse(changed);
                    // Do not overwrite unrelated blocks changed by a callback during placement.
                    for (BlockPos undo : changed) if (access.type(undo) == type) access.setType(undo, before.get(undo));
                    return false;
                }
            }
            return true;
        }
    }
    static Plan plan(int ring, BlockPos center, int type, Access access) {
        if (type < 1 || type > 3) return null;
        Map<BlockPos, Integer> before = new LinkedHashMap<>();
        for (BlockPos pos : RitualCircle.positions(ring, center)) {
            int old = access.type(pos);
            if (old < 1 || old > 3 || !access.allowed(pos)) return null;
            before.put(pos, old);
        }
        return new Plan(before, type, access);
    }
    public static final class Prepared {
        private final Plan plan;
        private final ItemStack chalk;
        private final PlayerEntity player;
        Prepared(Plan plan, ItemStack chalk, PlayerEntity player) { this.plan = plan; this.chalk = chalk; this.player = player; }
        public boolean apply() {
            if (player.getOffhandItem() != chalk || chalk.isEmpty() || !(chalk.getItem() instanceof ChalkItem)
                    || !enoughChalk(((ChalkItem)chalk.getItem()).glyphType(),
                    chalk.getDamageValue(), chalk.getMaxDamage(), plan.size(), player.isCreative()) || !plan.apply()) return false;
            if (!player.isCreative()) chalk.hurtAndBreak(plan.size(), player, p -> p.broadcastBreakEvent(Hand.OFF_HAND));
            return true;
        }
    }
    public static Prepared prepare(ServerWorld world, BlockPos center, PlayerEntity player, int ring) {
        ItemStack chalk = player.getOffhandItem();
        if (!(chalk.getItem() instanceof ChalkItem)) return null;
        int type = ((ChalkItem)chalk.getItem()).glyphType();
        int count = RitualCircle.positions(ring, center).size();
        if (!enoughChalk(type, chalk.getDamageValue(), chalk.getMaxDamage(), count, player.isCreative())) return null;
        Access access = new Access() {
            public int type(BlockPos pos) {
                if (!world.hasChunkAt(pos)) return -1;
                BlockState state = world.getBlockState(pos);
                return state.is(ModRituals.GLYPH.get()) ? state.getValue(GlyphBlock.TYPE) : -1;
            }
            public boolean allowed(BlockPos pos) {
                return world.hasChunkAt(pos) && player.abilities.mayBuild && world.mayInteract(player, pos)
                        && player.mayUseItemAt(pos, Direction.UP, chalk) && world.getFluidState(pos).isEmpty()
                        && world.getBlockState(pos).canSurvive(world, pos);
            }
            public boolean setType(BlockPos pos, int target) {
                if (type(pos) < 1) return false;
                BlockState state = world.getBlockState(pos).setValue(GlyphBlock.TYPE, target);
                return world.getBlockState(pos).equals(state) || world.setBlock(pos, state, 3);
            }
        };
        Plan plan = plan(ring, center, type, access);
        return plan == null ? null : new Prepared(plan, chalk, player);
    }
    private RitualDrawing() {}
}
