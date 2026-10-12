package com.bewitchment.world;

import com.bewitchment.registry.ModCreatures;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "bewitchment")
public final class StructureSpawns {
    @SubscribeEvent
    public static void potentialSpawns(WorldEvent.PotentialSpawns event) {
        if (event.getType() != EntityClassification.MONSTER || !(event.getWorld() instanceof ServerWorld)
                || !ModCreatures.STRUCTURE_SPAWNS.get()) return;
        ServerWorld world = (ServerWorld) event.getWorld();
        for (StructureSpawnRules.Site site : StructureSpawnRules.Site.values()) {
            if (!StructureSpawnRules.eligible(site, world.dimension() == World.OVERWORLD,
                    world.dimension() == World.NETHER, world.isDay(), world.isRaining(), world.getMoonPhase())) continue;
            if (!StructureSpawnRules.passesRoll(world.random.nextInt(100)) || !inside(world, event.getPos(), structure(site))) continue;
            for (StructureSpawnRules.Entry entry : StructureSpawnRules.pool(site)) {
                if (ModCreatures.structureCreatureEnabled(entry.creature))
                    event.getList().add(new MobSpawnInfo.Spawners(ModCreatures.structureCreature(entry.creature),
                            entry.weight, entry.minCount, entry.maxCount));
            }
        }
    }

    private static Structure<?> structure(StructureSpawnRules.Site site) {
        switch (site) {
            case FORTRESS: return Structure.NETHER_BRIDGE;
            case STRONGHOLD: return Structure.STRONGHOLD;
            case VILLAGE: return Structure.VILLAGE;
            case MINESHAFT: return Structure.MINESHAFT;
            default: throw new AssertionError(site);
        }
    }

    // StructureManager#getStructureAt may load referenced chunks. Natural spawn checks must not do so.
    static boolean inside(ServerWorld world, BlockPos pos, Structure<?> structure) {
        return inside(pos, structure, world.getChunkSource()::getChunkNow);
    }

    static boolean inside(BlockPos pos, Structure<?> structure, java.util.function.BiFunction<Integer, Integer, Chunk> loadedChunk) {
        Chunk chunk = loadedChunk.apply(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return false;
        LongSet references = chunk.getAllReferences().get(structure);
        if (references == null) return false;
        for (long packed : references) {
            ChunkPos origin = new ChunkPos(packed);
            Chunk startChunk = loadedChunk.apply(origin.x, origin.z);
            if (startChunk == null) continue;
            StructureStart<?> start = startChunk.getStartForFeature(structure);
            if (start != null && start.isValid() && start.getBoundingBox().isInside(pos)
                    && start.getPieces().stream().anyMatch(piece -> piece.getBoundingBox().isInside(pos))) return true;
        }
        return false;
    }
    private StructureSpawns() {}
}
