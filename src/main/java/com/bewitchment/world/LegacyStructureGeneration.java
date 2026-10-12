package com.bewitchment.world;

import com.bewitchment.config.StructureGenerationConfig;
import com.bewitchment.world.LegacyStructureRules.Kind;
import net.minecraft.entity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.template.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;
import java.util.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "bewitchment", bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class LegacyStructureGeneration {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "bewitchment");
    private static final RegistryObject<Feature<NoFeatureConfig>> FEATURE = FEATURES.register("legacy_surface_structures",
            () -> new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
                @Override public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos origin, NoFeatureConfig config) {
                    if (!StructureGenerationConfig.ENABLED.get() || world.getLevel().dimension() != World.OVERWORLD) return false;
                    int x = (origin.getX() >> 4) << 4, z = (origin.getZ() >> 4) << 4;
                    boolean placed = false;
                    for (Kind kind : Kind.values()) {
                        if (random.nextDouble() >= StructureGenerationConfig.CHANCES.get(kind).get()) continue;
                        int y = ground(world, x, z);
                        if (y <= 31) continue;
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!eligible(world, pos, kind) || !eligible(world, pos.offset(7, 0, 7), kind)) continue;
                        placed |= placeTemplate(world, random, pos, kind);
                    }
                    return placed;
                }
            });
    private static ConfiguredFeature<?, ?> configured;
    public static void register() {
        configured = FEATURE.get().configured(NoFeatureConfig.INSTANCE);
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("bewitchment", "legacy_surface_structures"), configured);
    }
    public static void biomes(BiomeLoadingEvent event) {
        // One ordered feature in all biomes; runtime checks preserve modded biome tags and exact dimension scope.
        // Legacy placed these after trees and moss, so templates clear their space after vegetation.
        if (configured != null) event.getGeneration().addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, configured);
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void verify(net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            if (configured == null || Registry.FEATURE.get(new ResourceLocation("bewitchment", "legacy_surface_structures")) != FEATURE.get()
                    || WorldGenRegistries.CONFIGURED_FEATURE.get(new ResourceLocation("bewitchment", "legacy_surface_structures")) != configured)
                throw new IllegalStateException("Missing legacy structure feature registration");
            org.apache.logging.log4j.LogManager.getLogger().info("Verified legacy surface structure feature and five configurable generation families");
        });
    }
    private static RegistryKey<Biome> biome(ISeedReader world, BlockPos pos) {
        return world.getLevel().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
                .getResourceKey(world.getBiome(pos)).orElse(null);
    }
    private static boolean eligible(ISeedReader world, BlockPos pos, Kind kind) {
        if (!world.hasChunkAt(pos)) return false;
        RegistryKey<Biome> key = biome(world, pos);
        return key != null && LegacyStructureRules.eligible(kind, BiomeDictionary.hasType(key, BiomeDictionary.Type.PLAINS),
                BiomeDictionary.hasType(key, BiomeDictionary.Type.FOREST), BiomeDictionary.hasType(key, BiomeDictionary.Type.BEACH), key.location().toString());
    }
    static int ground(ISeedReader world, int x, int z) {
        if (!world.hasChunk(x >> 4, z >> 4)) return -1;
        return LegacyStructureRules.groundY(world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, x, z),
                y -> world.getBlockState(new BlockPos(x, y, z)));
    }
    private static boolean placeTemplate(ISeedReader world, Random random, BlockPos ground, Kind kind) {
        boolean burned = kind == Kind.WICKERMAN && random.nextBoolean();
        String name = LegacyStructureRules.template(kind, biome(world, ground).location().toString(), burned);
        Template template = world.getLevel().getStructureManager().get(new ResourceLocation("bewitchment", name));
        if (template == null || template.getSize().getX() <= 0) return false;
        Rotation rotation = kind == Kind.WICKERMAN ? Rotation.values()[random.nextInt(4)] : Rotation.NONE;
        PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(Mirror.NONE).setIgnoreEntities(false).setRandom(random);
        BlockPos base = ground.offset(0, LegacyStructureRules.offset(kind, burned), 0);
        MutableBoundingBox bounds = template.getBoundingBox(settings, base);
        // Include a one-block halo needed by template neighbor updates, with no forced chunk loads.
        MutableBoundingBox halo = new MutableBoundingBox(bounds.x0 - 1, bounds.y0, bounds.z0 - 1, bounds.x1 + 1, bounds.y1, bounds.z1 + 1);
        if (!LegacyStructureRules.available(halo, world::hasChunk)
                || !world.getLevel().getWorldBorder().isWithinBounds(new BlockPos(bounds.x0, bounds.y0, bounds.z0))
                || !world.getLevel().getWorldBorder().isWithinBounds(new BlockPos(bounds.x1, bounds.y1, bounds.z1))) return false;
        // Check all rotated corners, fixing the legacy two-unrotated-corner test for Wickermen.
        for (int x : new int[]{bounds.x0, bounds.x1}) for (int z : new int[]{bounds.z0, bounds.z1})
            if (!LegacyStructureRules.levelCorner(ground.getY(), ground(world, x, z))) return false;
        if (!template.placeInWorld(world, base, base, settings, random, 2)) return false;
        if (kind == Kind.WICKERMAN && !burned) {
            inhabitant(world, base, settings, EntityType.VILLAGER, new Vector3d(6, 10, 3));
            int count = random.nextInt(5);
            for (int i = 0; i < count; i++) inhabitant(world, base, settings, animal(random.nextInt(8)), new Vector3d(6, 6, 3));
        }
        return true;
    }
    public static EntityType<?> animal(int index) {
        switch (index) {
            case 0: return EntityType.COW; case 1: return EntityType.PIG; case 2: return EntityType.SHEEP;
            case 3: return EntityType.CHICKEN; case 4: return EntityType.HORSE; case 5: return EntityType.MULE;
            case 6: return EntityType.DONKEY; case 7: return EntityType.RABBIT;
            default: throw new IllegalArgumentException("Animal choice outside 0..7");
        }
    }
    private static void inhabitant(ISeedReader world, BlockPos base, PlacementSettings settings, EntityType<?> type, Vector3d local) {
        Vector3d pos = Template.transformedVec3d(settings, local).add(base.getX(), base.getY(), base.getZ());
        if (!world.hasChunkAt(new BlockPos(pos)) || pos.y < 0 || pos.y >= 256) return;
        Entity entity = type.create(world.getLevel());
        if (entity == null) return;
        entity.moveTo(pos.x, pos.y, pos.z, 0, 0);
        world.addFreshEntity(entity);
    }
    private LegacyStructureGeneration() {}
}
