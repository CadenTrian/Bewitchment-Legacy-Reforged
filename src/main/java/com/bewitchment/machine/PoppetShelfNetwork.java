package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.PoppetItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Rebuilt by tile onLoad; never loads chunks merely to look for protection. */
@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class PoppetShelfNetwork {
    private static final Map<World, Set<BlockPos>> LOADED = new WeakHashMap<>();
    public static void add(World world, BlockPos pos) {
        if (world != null && !world.isClientSide) LOADED.computeIfAbsent(world, key -> new LinkedHashSet<>()).add(pos.immutable());
    }
    public static void remove(World world, BlockPos pos) {
        if (world == null || world.isClientSide) return;
        Set<BlockPos> shelves = LOADED.get(world);
        if (shelves != null) { shelves.remove(pos); if (shelves.isEmpty()) LOADED.remove(world); }
    }
    public static boolean spend(LivingEntity target, PoppetItem.Kind kind) {
        World world = target.level;
        if (world.isClientSide) return false;
        Set<BlockPos> shelves = LOADED.get(world);
        if (shelves == null) return false;
        for (BlockPos pos : new ArrayList<>(shelves)) {
            if (!world.hasChunkAt(pos)) { remove(world, pos); continue; }
            TileEntity tile = world.getBlockEntity(pos);
            if (!(tile instanceof PoppetShelfTile) || tile.isRemoved()) { remove(world, pos); continue; }
            if (((PoppetShelfTile)tile).inventory.spend(kind, target.getUUID())) return true;
        }
        return false;
    }
    @SubscribeEvent public static void unload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof World && !((World)event.getWorld()).isClientSide) LOADED.remove(event.getWorld());
    }
    private PoppetShelfNetwork() {}
}
