package com.bewitchment.world;

import com.bewitchment.config.StructureGenerationConfig;
import com.bewitchment.world.LegacyStructureRules.Kind;
import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.gen.feature.template.*;
import java.io.*;
import java.util.*;

public final class LegacyStructureCheck {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        Bootstrap.bootStrap();
        CommentedConfig config = CommentedConfig.inMemory();
        StructureGenerationConfig.SPEC.correct(config); StructureGenerationConfig.SPEC.setConfig(config);
        check(StructureGenerationConfig.ENABLED.get(), "Natural structures enabled by default");
        check(StructureGenerationConfig.CHANCES.size() == 5, "Five independent family chances");
        for (Kind kind : Kind.values()) {
            check(StructureGenerationConfig.CHANCES.get(kind).get() == .001, "Legacy one-in-thousand default");
            StructureGenerationConfig.CHANCES.get(kind).set(0.0);
            check(StructureGenerationConfig.CHANCES.get(kind).get() == 0, "Family can be disabled independently");
        }
        for (int mask = 0; mask < 8; mask++) for (String biome : new String[]{"minecraft:plains", "minecraft:taiga",
                "minecraft:giant_tree_taiga", "minecraft:snowy_taiga", "example:taiga"}) {
            boolean plains = (mask & 1) != 0, forest = (mask & 2) != 0, beach = (mask & 4) != 0;
            check(LegacyStructureRules.eligible(Kind.WICKERMAN, plains, forest, beach, biome) == (plains || forest || beach), "Wickerman biomes");
            check(LegacyStructureRules.eligible(Kind.STONE_CIRCLE, plains, forest, beach, biome) == (plains || forest), "Stone-circle biomes");
            check(LegacyStructureRules.eligible(Kind.MENHIR, plains, forest, beach, biome)
                    == (plains || forest || biome.startsWith("minecraft:") && !biome.equals("minecraft:plains")), "Menhir biome exceptions");
            check(LegacyStructureRules.eligible(Kind.CAMBION_HOME, plains, forest, beach, biome) == plains, "Small Cambion home biomes");
            check(LegacyStructureRules.eligible(Kind.CAMBION_HOME_MEDIUM, plains, forest, beach, biome) == plains, "Medium Cambion home biomes");
        }
        String[] biomes = {"minecraft:plains", "minecraft:taiga", "minecraft:giant_tree_taiga", "minecraft:snowy_taiga", "example:taiga"};
        String[] variants = {"menhir1", "menhir3", "menhir4", "menhir5", "menhir1"};
        for (int i = 0; i < biomes.length; i++) check(LegacyStructureRules.template(Kind.MENHIR, biomes[i], false).equals(variants[i]), "Exact biome variant");
        check(LegacyStructureRules.template(Kind.STONE_CIRCLE, "minecraft:plains", false).equals("stonecircle1"), "Legacy second circle remains unselected");
        check(LegacyStructureRules.template(Kind.WICKERMAN, "minecraft:plains", true).equals("burnedwickerman"), "Burned template");
        check(LegacyStructureRules.offset(Kind.WICKERMAN, false) == 1 && LegacyStructureRules.offset(Kind.WICKERMAN, true) == 0, "Wickerman ground offsets");
        check(LegacyStructureRules.offset(Kind.STONE_CIRCLE, false) == -1 && LegacyStructureRules.offset(Kind.MENHIR, false) == -1, "Stone foundations buried one block");
        check(LegacyStructureRules.offset(Kind.CAMBION_HOME, false) == 0 && LegacyStructureRules.offset(Kind.CAMBION_HOME_MEDIUM, false) == 0, "House offsets");
        Set<EntityType<?>> animals = new HashSet<>();
        for (int i = 0; i < 8; i++) animals.add(LegacyStructureGeneration.animal(i));
        check(animals.equals(new HashSet<>(Arrays.asList(EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN,
                EntityType.HORSE, EntityType.MULE, EntityType.DONKEY, EntityType.RABBIT))), "Original eight Wickerman animals");
        terrain(); templates();
        System.out.println("Legacy structure checks passed: " + checks);
    }
    private static void terrain() {
        for (Block block : new Block[]{Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.GLASS, Blocks.MYCELIUM})
            check(LegacyStructureRules.groundY(70, y -> (y == 64 ? block : Blocks.AIR).defaultBlockState()) == 64, "Legacy ground substrate");
        check(LegacyStructureRules.groundY(70, y -> (y == 65 ? Blocks.WATER : y == 64 ? Blocks.GRASS_BLOCK : Blocks.AIR).defaultBlockState()) == -1, "Water above ground rejects site");
        check(LegacyStructureRules.groundY(70, y -> Blocks.STONE.defaultBlockState()) == -1, "No permitted ground");
        check(LegacyStructureRules.groundY(400, y -> { check(y <= 255, "Height bounded"); return Blocks.SAND.defaultBlockState(); }) == 255, "Top height clamp");
        for (int y : new int[]{30,31,32,64,250}) for (int delta = -3; delta <= 4; delta++)
            check(LegacyStructureRules.levelCorner(y, y + delta) == (y > 31 && delta >= 0 && delta <= 2), "Terrain height boundaries");
        MutableBoundingBox box = new MutableBoundingBox(-17, 32, -1, 16, 80, 16);
        Set<ChunkPos> visited = new HashSet<>();
        check(LegacyStructureRules.available(box, (x,z) -> { visited.add(new ChunkPos(x,z)); return true; }), "Loaded negative-coordinate footprint");
        check(visited.size() == 12 && visited.contains(new ChunkPos(-2,-1)) && visited.contains(new ChunkPos(1,1)), "Every intersected chunk checked");
        for (ChunkPos missing : visited)
            check(!LegacyStructureRules.available(box, (x,z) -> x != missing.x || z != missing.z), "Any missing chunk rejects entire footprint");
        check(!LegacyStructureRules.available(new MutableBoundingBox(0,-1,0,10,20,10), (x,z) -> {throw new AssertionError("Chunk access");}), "Below-world rejected before access");
        check(!LegacyStructureRules.available(new MutableBoundingBox(0,250,0,10,256,10), (x,z) -> {throw new AssertionError("Chunk access");}), "Above-world rejected before access");
    }
    private static void templates() throws Exception {
        for (String name : com.bewitchment.machine.BundledBroomAudit.STRUCTURES) {
            CompoundNBT nbt;
            try (InputStream in = new FileInputStream("src/main/resources/data/bewitchment/structures/" + name + ".nbt")) {
                nbt = CompressedStreamTools.readCompressed(in);
            }
            Template template = new Template(); template.load(nbt);
            check(template.getSize().getX() > 0 && template.getSize().getY() > 0 && template.getSize().getZ() > 0, "Nonempty template");
            System.out.println(name + " size=" + template.getSize() + " entities=" + nbt.getList("entities", 10).size());
            for (Rotation rotation : Rotation.values()) {
                PlacementSettings settings = new PlacementSettings().setRotation(rotation);
                BlockPos base = new BlockPos(-16,64,-16);
                MutableBoundingBox box = template.getBoundingBox(settings, base);
                for (int x : new int[]{0,template.getSize().getX()-1}) for (int z : new int[]{0,template.getSize().getZ()-1})
                    check(box.isInside(Template.calculateRelativePosition(settings, new BlockPos(x,0,z)).offset(base)), "Rotated corners stay inside native bounds");
                if (name.equals("wickerman")) for (int y : new int[]{6,10}) {
                    Vector3d point = Template.transformedVec3d(settings, new Vector3d(6,y,3)).add(base.getX(),base.getY(),base.getZ());
                    check(box.isInside(new BlockPos(point)), "Rotated occupants remain within Wickerman");
                }
            }
            for (net.minecraft.nbt.INBT raw : nbt.getList("entities", 10)) {
                CompoundNBT entity = ((CompoundNBT)raw).getCompound("nbt");
                check(!entity.contains("UUID") && !entity.contains("UUIDMost") && !entity.contains("UUIDLeast"), "Fresh identity for each generated inhabitant");
            }
        }
    }
}
