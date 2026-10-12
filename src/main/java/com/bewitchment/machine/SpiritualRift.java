package com.bewitchment.machine;

import com.bewitchment.registry.ModCreatures;
import net.minecraft.entity.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.*;

/** Original Spiritual Rift pool, with bounded loaded-space wave placement. */
public final class SpiritualRift {
    static String id(int choice) {
        switch(choice) {
            case 0:return "vex";case 1:return "black_dog";case 2:return "ghost";
            case 3:return "druden";case 4:return "skeleton";case 5:return "husk";
            case 6:return "stray";case 7:return "shadow_person";case 8:return "enderman";
            case 9:return "endermite";case 10:return "cambion";case 11:return "zombie";
            default:throw new IllegalArgumentException("Invalid Spiritual Rift choice");
        }
    }
    static EntityType<?> type(int choice) {
        switch(choice) {
            case 0:return EntityType.VEX;case 1:return ModCreatures.BLACK_DOG.get();case 2:return ModCreatures.GHOST.get();
            case 3:return ModCreatures.DRUDEN.get();case 4:return EntityType.SKELETON;case 5:return EntityType.HUSK;
            case 6:return EntityType.STRAY;case 7:return ModCreatures.SHADOW_PERSON.get();case 8:return EntityType.ENDERMAN;
            case 9:return EntityType.ENDERMITE;case 10:return ModCreatures.CAMBION.get();case 11:return EntityType.ZOMBIE;
            default:throw new IllegalArgumentException("Invalid Spiritual Rift choice");
        }
    }
    static <T> List<T> plan(Random random, IntFunction<T> create, BiPredicate<T,List<T>> place) {
        List<T> planned = new ArrayList<>();
        // Preserve the rerolled bound, including after failed candidates.
        for (int i = 0; i < random.nextInt(12) + 2; i++) {
            T candidate = create.apply(random.nextInt(12));
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
        if (world.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return Collections.emptyList();
        return plan(world.random, choice -> {
            MobEntity entity = (MobEntity)type(choice).create(world);
            if (entity == null) return null;
            entity.moveTo(center.getX()+.5, center.getY(), center.getZ()+.5, 0, 0);
            // Prevent unplanned chicken jockey insertion during the planning phase.
            ILivingEntityData group = entity instanceof net.minecraft.entity.monster.ZombieEntity
                    ? new net.minecraft.entity.monster.ZombieEntity.GroupData(net.minecraft.entity.monster.ZombieEntity.getSpawnAsBabyOdds(world.random), false) : null;
            entity.finalizeSpawn(world, world.getCurrentDifficultyAt(center), SpawnReason.MOB_SUMMONED, group, null);
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
        return CallOfTheWild.spawn(world, planned);
    }
    public static void verifyPool() {
        for (int i=0; i<12; i++) {
            EntityType<?> type=type(i);
            if (type.getRegistryName()==null || !type.getRegistryName().getPath().equals(id(i))
                    || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(type))
                throw new IllegalStateException("Invalid Spiritual Rift creature slot " + i);
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Spiritual Rift: all twelve creature slots and attributes verified");
    }
    static void particles(ServerWorld world, BlockPos pos) {
        for (int i=0; i<5; i++) {
            double x=world.random.nextGaussian()*.5, y=world.random.nextGaussian()*.5, z=world.random.nextGaussian()*.5;
            world.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX()+.5+x, pos.getY()+.5+y, pos.getZ()+.5+z, 0, .6*x, .6*y, .6*z, 1);
        }
    }
    private SpiritualRift() {}
}
