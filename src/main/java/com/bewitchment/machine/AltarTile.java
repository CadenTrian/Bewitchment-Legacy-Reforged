package com.bewitchment.machine;

import com.bewitchment.block.WitchesAltarBlock;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.IPlantable;
import java.util.*;

/** Legacy base altar: diverse nearby vegetation supplies capacity, with eight power/second. */
public final class AltarTile extends TileEntity implements ITickableTileEntity, MagicPowerSource {
    private BlockPos origin;
    private int width, depth, power, capacity, baseCapacity, cursor, scannedCapacity;
    private int regeneration = 8;
    private boolean dismantling, initialScan = true;
    private ItemStack carpet = ItemStack.EMPTY;
    private final Map<Block, Integer> plants = new HashMap<>();
    public AltarTile() { super(ModMachines.ALTAR.get()); }
    public void initialize(BlockPos pos, int w, int d, ItemStack cloth) {
        origin = pos.immutable(); width = w; depth = d; carpet = cloth.copy(); carpet.setCount(1); setChanged();
    }
    public int getPower() { return power; }
    public int getCapacity() { return capacity; }
    public int getRegeneration() { return regeneration; }

    public boolean recolor(ItemStack replacement) {
        if (!intact() || !(replacement.getItem() instanceof net.minecraft.item.BlockItem)) return false;
        Block block = ((net.minecraft.item.BlockItem) replacement.getItem()).getBlock();
        if (!(block instanceof CarpetBlock)) return false;
        int color = ((CarpetBlock) block).getColor().getId() + 1;
        if (getBlockState().getValue(WitchesAltarBlock.COLOR) == color) return false;
        ItemStack previous = carpet;
        carpet = replacement.copy(); carpet.setCount(1);
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1)))
            level.setBlock(p, level.getBlockState(p).setValue(WitchesAltarBlock.COLOR, color), 3);
        Block.popResource(level, worldPosition, previous);
        setChanged(); return true;
    }

    /** One placed block per legacy category; traverse the footprint in a stable order. */
    private void refreshUpgrades() {
        boolean cup = false, pentacle = false, wand = false, sword = false;
        int gain = 1, bonus = 0;
        double multiplier = 1;
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1))) {
            Block block = level.getBlockState(p.above()).getBlock();
            TileEntity placed = level.getBlockEntity(p.above());
            if (placed instanceof PlacedItemTile) {
                AltarItemUpgrades.Upgrade upgrade = AltarItemUpgrades.get(((PlacedItemTile) placed).getItem());
                if (upgrade != null) switch (upgrade.kind) {
                    case CUP: if (!cup) { multiplier *= upgrade.multiplier; gain += upgrade.gain; cup = true; } break;
                    case PENTACLE: if (!pentacle) { gain += upgrade.gain; pentacle = true; } break;
                    case SWORD: if (!sword) { multiplier *= upgrade.multiplier; sword = true; } break;
                    case WAND: if (!wand) { bonus = upgrade.bonus; wand = true; } break;
                }
            }
            if (!cup && block instanceof FlowerPotBlock) {
                boolean filled = ((FlowerPotBlock) block).getContent() != Blocks.AIR;
                gain += filled ? 1 : 0;
                multiplier *= filled ? 1.1 : 1.05;
                cup = true;
            }
            if (!cup && block instanceof com.bewitchment.block.GobletBlock) {
                boolean filled = ((com.bewitchment.block.GobletBlock) block).isFilled();
                gain += filled ? 1 : 0;
                multiplier *= filled ? 1.2 : 1.075;
                cup = true;
            }
            if (!pentacle && block instanceof AbstractSkullBlock) {
                SkullBlock.ISkullType type = ((AbstractSkullBlock) block).getType();
                gain += type == SkullBlock.Types.DRAGON ? 6 : type == SkullBlock.Types.WITHER_SKELETON ? 2 : 1;
                pentacle = true;
            }
            if (!wand) {
                if (block instanceof com.bewitchment.block.CandleBlock) {
                    bonus = ((com.bewitchment.block.CandleBlock) block).altarBonus(); wand = true;
                }
                // Check redstone before ordinary torches: it is a TorchBlock subclass.
                if (block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_WALL_TORCH) {
                    bonus = (int) (64 * 1.1); wand = true;
                } else if (block == Blocks.TORCH || block == Blocks.WALL_TORCH) {
                    bonus = (int) (64 * 1.05); wand = true;
                } else if (block == Blocks.END_ROD) {
                    bonus = (int) (64 * 1.325); wand = true;
                }
            }
        }
        int nextCapacity = (int) ((baseCapacity + bonus) * multiplier);
        int nextRegeneration = gain * 8;
        if (capacity != nextCapacity || regeneration != nextRegeneration) {
            capacity = nextCapacity; regeneration = nextRegeneration;
            power = Math.min(power, capacity); setChanged();
        }
    }
    public boolean contains(BlockPos pos) {
        return origin != null && pos.getY() == origin.getY() && pos.getX() >= origin.getX() && pos.getX() < origin.getX() + width
                && pos.getZ() >= origin.getZ() && pos.getZ() < origin.getZ() + depth;
    }
    public boolean intact() {
        if (level == null || origin == null || dismantling || !((width == 3 && depth == 2) || (width == 2 && depth == 3))) return false;
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1))) {
            if (!level.hasChunkAt(p)) return false;
            BlockState state = level.getBlockState(p);
            if (!state.is(getBlockState().getBlock()) || !(state.getBlock() instanceof WitchesAltarBlock)
                    || state.getValue(WitchesAltarBlock.TYPE) != (p.equals(worldPosition) ? 2 : 1)) return false;
        }
        return true;
    }
    public void dismantle(BlockPos removed) {
        if (dismantling || origin == null || level == null) return;
        dismantling = true; power = 0;
        Block.popResource(level, worldPosition, carpet); carpet = ItemStack.EMPTY;
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1))) {
            if (p.equals(removed) || !level.hasChunkAt(p)) continue;
            BlockState state = level.getBlockState(p);
            if (state.getBlock() instanceof WitchesAltarBlock)
                level.setBlock(p, state.setValue(WitchesAltarBlock.TYPE, 0).setValue(WitchesAltarBlock.COLOR, 0), 3);
        }
        setChanged();
    }
    @Override public boolean tryDrainMagic(int amount) {
        if (amount < 0 || level == null || level.isClientSide || !intact() || power < amount) return false;
        power -= amount; setChanged(); return true;
    }
    private void scan(int count) {
        for (int i = 0; i < count; i++) {
            BlockPos p = worldPosition.offset((cursor & 31) - 15, ((cursor >> 5) & 31) - 15, ((cursor >> 10) & 31) - 15);
            if (level.hasChunkAt(p)) {
                BlockState state = level.getBlockState(p); Block block = state.getBlock();
                if (block != Blocks.GRASS_BLOCK && (block instanceof IGrowable || block instanceof IPlantable || state.is(BlockTags.LOGS)
                        || state.is(BlockTags.LEAVES) || block == Blocks.MELON || block == Blocks.PUMPKIN)) {
                    int seen = plants.getOrDefault(block, 0);
                    if (plants.isEmpty() || seen < 4 * plants.size()) { plants.put(block, seen + 1); scannedCapacity++; }
                }
            }
            if (++cursor == 32768) {
                baseCapacity = scannedCapacity; refreshUpgrades();
                chargePlacedGrimoires();
                cursor = 0; scannedCapacity = 0; plants.clear(); setChanged();
            }
        }
    }
    @Override public void tick() {
        if (level == null || level.isClientSide || origin == null) return;
        // Never load a missing structure chunk just to validate it.
        if (!level.hasChunkAt(origin) || !level.hasChunkAt(origin.offset(width - 1, 0, depth - 1))) return;
        if (!intact()) { dismantle(null); return; }
        scan(initialScan ? 32768 : 64); initialScan = false;
        if (level.getGameTime() % 100 == 0) chargeCarriedGrimoires();
        if (level.getGameTime() % 20 == 0) {
            refreshUpgrades();
            if (power < capacity) { power = Math.min(capacity, power + regeneration); setChanged(); }
        }
    }
    public static AltarTile nearest(ServerWorld world, BlockPos pos) {
        AltarTile best = null; double distance = Double.MAX_VALUE;
        for (int x = (pos.getX() - 24) >> 4; x <= (pos.getX() + 24) >> 4; x++)
            for (int z = (pos.getZ() - 24) >> 4; z <= (pos.getZ() + 24) >> 4; z++) {
                Chunk chunk = world.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (TileEntity tile : chunk.getBlockEntities().values()) if (tile instanceof AltarTile) {
                    BlockPos p = tile.getBlockPos();
                    double d = p.distSqr(pos);
                    if (inRange(p, pos) && d < distance && ((AltarTile) tile).intact()) { best = (AltarTile) tile; distance = d; }
                }
            }
        return best;
    }
    private void chargeCarriedGrimoires() {
        for (net.minecraft.entity.player.PlayerEntity player : level.getEntitiesOfClass(net.minecraft.entity.player.PlayerEntity.class,
                new net.minecraft.util.math.AxisAlignedBB(worldPosition).inflate(25))) {
            for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
                if (com.bewitchment.item.GrimoireMagiaItem.charge(player.inventory.getItem(slot), this, 50, 25)) {
                    player.inventory.setChanged(); break;
                }
            }
        }
    }
    private void chargePlacedGrimoires() {
        for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(width - 1, 0, depth - 1))) {
            TileEntity tile = level.getBlockEntity(pos.above());
            if (tile instanceof PlacedItemTile) {
                ItemStack stack = ((PlacedItemTile)tile).getItem();
                if (com.bewitchment.item.GrimoireMagiaItem.charge(stack, this, 100, 25)) ((PlacedItemTile)tile).setItem(stack);
            }
        }
    }
    public static boolean inRange(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) <= 24 && Math.abs(a.getY() - b.getY()) <= 24 && Math.abs(a.getZ() - b.getZ()) <= 24;
    }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); if (origin != null) tag.putLong("Origin", origin.asLong());
        tag.putInt("Width", width); tag.putInt("Depth", depth); tag.putInt("Power", power); tag.putInt("Capacity", capacity);
        tag.putInt("BaseCapacity", baseCapacity);
        tag.put("Carpet", carpet.save(new CompoundNBT())); return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); origin = tag.contains("Origin") ? BlockPos.of(tag.getLong("Origin")) : null;
        width = tag.getInt("Width"); depth = tag.getInt("Depth"); capacity = Math.max(0, tag.getInt("Capacity"));
        baseCapacity = Math.max(0, tag.getInt("BaseCapacity"));
        power = Math.max(0, Math.min(capacity, tag.getInt("Power"))); carpet = ItemStack.of(tag.getCompound("Carpet"));
        initialScan = true; cursor = 0; scannedCapacity = 0; plants.clear();
    }
}
