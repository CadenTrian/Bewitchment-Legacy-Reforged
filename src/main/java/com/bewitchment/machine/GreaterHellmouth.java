package com.bewitchment.machine;

import com.bewitchment.registry.ModCreatures;
import net.minecraft.entity.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.*;

/** Original Greater Hellmouth pool, with bounded loaded-space wave placement. */
public final class GreaterHellmouth {
    static String id(int choice) {
        switch(choice) {
            case 0:return "wither_skeleton";case 1:return "hellhound";
            case 2:return "bafometyr";case 3:return "cleaver";
            case 4:return "shadow_person";case 5:return "ghast";
            default:throw new IllegalArgumentException("Invalid Greater Hellmouth choice");
        }
    }
    static EntityType<?> type(int choice) {
        switch(choice) {
            case 0:return EntityType.WITHER_SKELETON;case 1:return ModCreatures.HELLHOUND.get();
            case 2:return ModCreatures.BAFOMETYR.get();case 3:return ModCreatures.CLEAVER.get();
            case 4:return ModCreatures.SHADOW_PERSON.get();case 5:return EntityType.GHAST;
            default:throw new IllegalArgumentException("Invalid Greater Hellmouth choice");
        }
    }
    static <T> List<T> plan(Random random, IntFunction<T> create, BiPredicate<T,List<T>> place) {
        List<T> planned = new ArrayList<>();
        // Preserve the rerolled bound, including after failed candidates.
        for (int i = 0; i < random.nextInt(6) + 1; i++) {
            T candidate = create.apply(random.nextInt(6));
            if (candidate == null) continue;
            for (int attempt = 0; attempt < 16; attempt++) {
                if (place.test(candidate, Collections.unmodifiableList(planned))) {
                    planned.add(candidate);
                    break;
                }
            }
        }
        return planned;
    }
    static List<MobEntity> prepare(ServerWorld world, BlockPos center) {
        if (LesserHellmouth.failure(world) != null) return Collections.emptyList();
        return plan(world.random, choice -> {
            MobEntity entity = (MobEntity)type(choice).create(world);
            if (entity == null) return null;
            entity.moveTo(center.getX()+.5, center.getY(), center.getZ()+.5, 0, 0);
            entity.finalizeSpawn(world, world.getCurrentDifficultyAt(center), SpawnReason.MOB_SUMMONED, null, null);
            return entity;
        }, (entity, planned) -> {
            BlockPos landing = center.offset(LesserHellmouth.offset(world.random), 0, LesserHellmouth.offset(world.random));
            if (!world.hasChunkAt(landing)) return false;
            while (landing.getY()>0 && !world.getBlockState(landing.below()).getMaterial().blocksMotion()) landing=landing.below();
            if (landing.getY()<=0) return false;
            entity.moveTo(landing.getX()+.5, landing.getY(), landing.getZ()+.5, world.random.nextInt(360), 0);
            return LesserHellmouth.fits(world, entity, planned);
        });
    }
    static int spawn(ServerWorld world, List<MobEntity> planned) {
        return LesserHellmouth.failure(world) == null ? CallOfTheWild.spawn(world, planned) : 0;
    }
    public static void verifyPool() {
        for (int i=0; i<6; i++) {
            EntityType<?> type=type(i);
            if (type.getRegistryName()==null || !type.getRegistryName().getPath().equals(id(i))
                    || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(type))
                throw new IllegalStateException("Invalid Greater Hellmouth creature slot " + i);
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Greater Hellmouth: all six creature slots and attributes verified");
    }
    static void particles(ServerWorld world, BlockPos pos) {
        world.sendParticles(ParticleTypes.FLAME, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, 20, .5, .5, .5, .1);
    }
    private GreaterHellmouth() {}
}
