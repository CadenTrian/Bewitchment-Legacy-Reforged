package com.bewitchment.machine;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;
import java.util.function.Consumer;

/** Saved, bounded completion work for Nature's Blessing, owned by its focal glyph. */
public final class NatureBlessing {
    static final int BUDGET = 4096;
    private final BlockPos center;
    private final UUID owner;
    private int cursor;
    private final boolean salt;

    NatureBlessing(BlockPos center, UUID owner, int cursor) {
        this(center, owner, cursor, false);
    }
    NatureBlessing(BlockPos center, UUID owner, int cursor, boolean salt) {
        this.center = center.immutable(); this.owner = owner; this.cursor = cursor; this.salt = salt;
    }
    static int minY(int centerY) { return Math.max(0, centerY - 64); }
    static int maxY(int centerY) { return Math.min(255, centerY + 255); }
    static boolean inCircle(int x, int z) { return x > -32 && x < 32 && z > -32 && z < 32 && x*x + z*z < 32*32; }
    static boolean converts(net.minecraft.block.Block block) { return block == Blocks.COARSE_DIRT || block == Blocks.PODZOL; }
    boolean salted() { return salt; }
    private int lowerY() { return salt ? Math.max(0, center.getY()-60) : minY(center.getY()); }
    static boolean salts(net.minecraft.block.Block block) {
        return block == Blocks.DIRT || block == Blocks.PODZOL || block == Blocks.GRASS_BLOCK || block == Blocks.GRASS_PATH || block == Blocks.MYCELIUM || block == Blocks.FARMLAND;
    }
    int total() { return 64 * 64 * (maxY(center.getY()) - lowerY() + 1); }
    int cursor() { return cursor; }
    UUID owner() { return owner; }
    boolean finished() { return cursor >= total(); }
    void stop() { cursor = total(); }
    BlockPos position(int index) {
        int x = index % 64 - 32, z = (index / 64) % 64 - 32;
        return center.offset(x, lowerY() + index / 4096 - center.getY(), z);
    }
    /** Advance before invoking the callback; saved progress never starts the scan again. */
    int advance(int budget, Consumer<BlockPos> visit) {
        int examined = 0;
        while (examined < Math.max(0, budget) && !finished()) {
            BlockPos pos = position(cursor++); examined++;
            if (inCircle(pos.getX() - center.getX(), pos.getZ() - center.getZ())) visit.accept(pos);
        }
        return examined;
    }
    interface Terrain {
        boolean loaded(BlockPos pos);
        boolean insideBorder(BlockPos pos);
        boolean convertible(BlockPos pos);
        boolean mayEdit(BlockPos pos);
        void restore(BlockPos pos);
    }
    void tick(Terrain terrain) {
        advance(BUDGET, pos -> {
            if (terrain.loaded(pos) && terrain.insideBorder(pos) && terrain.convertible(pos) && terrain.mayEdit(pos)) terrain.restore(pos);
        });
    }
    void tick(ServerWorld world, PlayerEntity caster) {
        if (!caster.isAlive() || !caster.abilities.mayBuild) return;
        tick(new Terrain() {
            public boolean loaded(BlockPos pos) { return world.hasChunkAt(pos); }
            public boolean insideBorder(BlockPos pos) { return world.getWorldBorder().isWithinBounds(pos); }
            public boolean convertible(BlockPos pos) { return salt ? salts(world.getBlockState(pos).getBlock()) : converts(world.getBlockState(pos).getBlock()); }
            public boolean mayEdit(BlockPos pos) { return world.mayInteract(caster, pos) && caster.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY); }
            public void restore(BlockPos pos) { world.setBlock(pos, (salt ? Blocks.COARSE_DIRT : Blocks.DIRT).defaultBlockState(), 3); }
        });
    }
    CompoundNBT save() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("Version", 1); tag.putBoolean("Salt", salt); tag.putUUID("Owner", owner); tag.putInt("Cursor", cursor);
        return tag;
    }
    static NatureBlessing read(BlockPos center, CompoundNBT tag) {
        if (center.getY() < 0 || center.getY() > 255 || tag.getInt("Version") != 1
                || !tag.hasUUID("Owner") || !tag.contains("Cursor", 3)) return null;
        NatureBlessing job = new NatureBlessing(center, tag.getUUID("Owner"), tag.getInt("Cursor"), tag.getBoolean("Salt"));
        return job.cursor < 0 || job.finished() ? null : job;
    }
}
