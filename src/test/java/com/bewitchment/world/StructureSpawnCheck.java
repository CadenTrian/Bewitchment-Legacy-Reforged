package com.bewitchment.world;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.event.world.WorldEvent;
import java.util.*;

public final class StructureSpawnCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        String[] expected = {"CLEAVER:2,BAFOMETYR:2,FEUERWURM:3,HELLHOUND:3,CAMBION:1,SHADOW_PERSON:1",
                "SHADOW_PERSON:1,GHOST:1,BLACK_DOG:1", "CAMBION:1", "SHADOW_PERSON:1,GHOST:1"};
        for (StructureSpawnRules.Site site : StructureSpawnRules.Site.values()) {
            StringJoiner actual = new StringJoiner(",");
            for (StructureSpawnRules.Entry entry : StructureSpawnRules.pool(site)) {
                actual.add(entry.creature + ":" + entry.weight);
                check(entry.minCount == 0 && entry.maxCount == 1, "Legacy zero-to-one groups");
            }
            check(actual.toString().equals(expected[site.ordinal()]), "Exact original pool: " + site);
            try { StructureSpawnRules.pool(site).clear(); throw new AssertionError("Mutable pool"); }
            catch (UnsupportedOperationException correct) { checks++; }
            for (int dimension = 0; dimension < 3; dimension++)
                for (int moon = 0; moon < 8; moon++)
                    for (boolean day : new boolean[]{false, true})
                        for (boolean rain : new boolean[]{false, true}) {
                            boolean eligible;
                            switch (site) {
                                case FORTRESS: eligible = dimension == 1; break;
                                case STRONGHOLD: eligible = dimension == 0 && !day && moon == 4; break;
                                case VILLAGE: eligible = dimension == 0 && !day && moon == 0 && !rain; break;
                                default: eligible = dimension == 0 && !day && (moon == 2 || moon == 6);
                            }
                            check(StructureSpawnRules.eligible(site, dimension == 0, dimension == 1, day, rain, moon) == eligible,
                                    "Dimension/moon/weather gate: " + site);
                        }
        }
        int accepted = 0;
        for (int roll = 0; roll < 100; roll++) if (StructureSpawnRules.passesRoll(roll)) accepted++;
        check(accepted == 95, "Legacy roll is a 95 percent pool gate");
        check(!StructureSpawnRules.passesRoll(-1) && !StructureSpawnRules.passesRoll(100), "Invalid rolls rejected");
        MobSpawnInfo.Spawners vanilla = new MobSpawnInfo.Spawners(EntityType.BLAZE, 10, 2, 3);
        List<MobSpawnInfo.Spawners> original = Collections.singletonList(vanilla);
        WorldEvent.PotentialSpawns event = new WorldEvent.PotentialSpawns(null, EntityClassification.MONSTER, BlockPos.ZERO, original);
        event.getList().add(new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 1, 0, 1));
        check(original.size() == 1 && event.getList().size() == 2 && event.getList().get(0) == vanilla,
                "Forge event owns an additive copy of original spawn pool");
        StructureSpawns.potentialSpawns(event);
        check(event.getList().size() == 2, "Non-server worlds ignored");
        WorldEvent.PotentialSpawns animals = new WorldEvent.PotentialSpawns(null, EntityClassification.CREATURE, BlockPos.ZERO, null);
        StructureSpawns.potentialSpawns(animals);
        check(animals.getList().isEmpty(), "Non-monster category ignored");
        checkLoadedStructures();
        checkConfiguration();
        System.out.println("Structure spawn checks passed: " + checks);
    }

    private static void checkConfiguration() {
        com.electronwill.nightconfig.core.CommentedConfig config = com.electronwill.nightconfig.core.CommentedConfig.inMemory();
        com.bewitchment.registry.ModCreatures.SPEC.setConfig(config);
        check(com.bewitchment.registry.ModCreatures.STRUCTURE_SPAWNS.get(), "Structure pools enabled by default");
        String[] keys = {"cleaverWeight", "bafometyrWeight", "feuerwurmWeight", "hellhoundWeight",
                "cambionStructureSpawns", "shadowPersonStructureSpawns", "ghostWeight", "blackDogWeight"};
        for (StructureSpawnRules.Creature creature : StructureSpawnRules.Creature.values()) {
            String key = keys[creature.ordinal()];
            check(com.bewitchment.registry.ModCreatures.structureCreatureEnabled(creature), "Default enabled: " + creature);
            config.set(key, key.endsWith("Weight") ? (Object) 0 : false);
            com.bewitchment.registry.ModCreatures.SPEC.setConfig(config);
            check(!com.bewitchment.registry.ModCreatures.structureCreatureEnabled(creature), "Zero/false disables: " + creature);
            config.set(key, key.endsWith("Weight") ? (Object) 1000 : true);
            com.bewitchment.registry.ModCreatures.SPEC.setConfig(config);
            check(com.bewitchment.registry.ModCreatures.structureCreatureEnabled(creature), "Positive/true enables: " + creature);
        }
        com.bewitchment.registry.ModCreatures.STRUCTURE_SPAWNS.set(false);
        check(!com.bewitchment.registry.ModCreatures.STRUCTURE_SPAWNS.get(), "Master toggle can disable pools");
    }

    private static void checkLoadedStructures() {
        BlockPos pos = new BlockPos(-15, 51, -15);
        Map<ChunkPos, Chunk> loaded = new HashMap<>();
        List<ChunkPos> requested = new ArrayList<>();
        java.util.function.BiFunction<Integer, Integer, Chunk> lookup = (x,z) -> {
            ChunkPos key = new ChunkPos(x,z); requested.add(key); return loaded.get(key);
        };
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup) && requested.size() == 1,
                "Unloaded candidate chunk returns without looking up starts");
        Chunk candidate = new Chunk(null, new ChunkPos(-1,-1), null);
        loaded.put(candidate.getPos(), candidate);
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup) && candidate.getAllReferences().isEmpty(),
                "Missing references return without mutating chunk");
        candidate.addReferenceForFeature(Structure.MINESHAFT, new ChunkPos(-2,-1).toLong());
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup), "Unloaded referenced start skipped");
        Chunk origin = new Chunk(null, new ChunkPos(-2,-1), null);
        loaded.put(origin.getPos(), origin);
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup), "Absent structure start skipped");
        origin.setStartForFeature(Structure.MINESHAFT, StructureStart.INVALID_START);
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup), "Invalid structure start skipped");
        StructureStart<MineshaftConfig> start = new StructureStart<MineshaftConfig>(Structure.MINESHAFT, -2,-1,
                new MutableBoundingBox(-32, 0, -16, -1, 255, -1), 0, 1L) {
            @Override public void generatePieces(DynamicRegistries registries, ChunkGenerator generator,
                    TemplateManager templates, int x, int z, Biome biome, MineshaftConfig config) {}
        };
        start.getPieces().add(new MineshaftPieces.Room(0, new Random(1), -16,-16, MineshaftStructure.Type.NORMAL));
        origin.setStartForFeature(Structure.MINESHAFT, start);
        check(StructureSpawns.inside(pos, Structure.MINESHAFT, lookup), "Loaded piece in referenced negative chunk detected");
        check(!StructureSpawns.inside(new BlockPos(-15, 100,-15), Structure.MINESHAFT, lookup),
                "Overall bounding box alone does not count as a structure piece");
        check(!StructureSpawns.inside(pos, Structure.STRONGHOLD, lookup), "Other structure type ignored");
        loaded.remove(origin.getPos());
        check(!StructureSpawns.inside(pos, Structure.MINESHAFT, lookup), "Unloaded starts are not cached across checks");
    }
}
