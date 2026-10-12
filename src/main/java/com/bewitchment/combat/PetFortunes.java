package com.bewitchment.combat;

import com.bewitchment.machine.VillagerFortunePlacement;
import com.bewitchment.registry.ModCreatures;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** Legacy eleven-member pet pool; modern cats replace the old tameable ocelot. */
public final class PetFortunes {
    public static final int POOL_SIZE = 11;
    public static EntityType<? extends MobEntity> type(int roll) {
        switch (Math.floorMod(roll, POOL_SIZE)) {
            case 0: return EntityType.CAT;
            case 1: return EntityType.WOLF;
            case 2: return EntityType.HORSE;
            case 3: return EntityType.DONKEY;
            case 4: return EntityType.MULE;
            case 5: return EntityType.LLAMA;
            case 6: return EntityType.PARROT;
            case 7: return ModCreatures.OWL.get();
            case 8: return ModCreatures.RAVEN.get();
            case 9: return ModCreatures.SNAKE.get();
            default: return ModCreatures.TOAD.get();
        }
    }
    public static boolean spawn(PlayerEntity player) {
        if (!(player.level instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return false;
        ServerWorld world = (ServerWorld) player.level;
        MobEntity pet = type(player.getRandom().nextInt(POOL_SIZE)).create(world);
        if (pet == null) return false;
        BlockPos origin = player.blockPosition();
        BlockPos pos = VillagerFortunePlacement.find(
                () -> VillagerFortunePlacement.candidate(origin, world.random.nextGaussian(), world.random.nextGaussian()),
                candidate -> FortuneEncounterSpace.fits(world, pet, candidate));
        if (pos == null) return false;
        pet.moveTo(pos.getX()+.5, pos.getY(), pos.getZ()+.5, world.random.nextInt(360), 0);
        pet.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), SpawnReason.EVENT, null, null);
        // Set ownership before insertion without awarding tame advancements for rejected entities.
        if (pet instanceof TameableEntity) {
            ((TameableEntity)pet).setOwnerUUID(player.getUUID());
            ((TameableEntity)pet).setTame(true);
        } else if (pet instanceof AbstractHorseEntity) {
            ((AbstractHorseEntity)pet).setOwnerUUID(player.getUUID());
            ((AbstractHorseEntity)pet).setTamed(true);
        }
        if (!FortuneEncounterSpace.fits(world, pet, pos) || !world.addFreshEntity(pet)) return false;
        if (player instanceof net.minecraft.entity.player.ServerPlayerEntity
                && (pet instanceof TameableEntity || pet instanceof AbstractHorseEntity))
            net.minecraft.advancements.CriteriaTriggers.TAME_ANIMAL.trigger((net.minecraft.entity.player.ServerPlayerEntity)player,
                    (net.minecraft.entity.passive.AnimalEntity)pet);
        return true;
    }
    private PetFortunes() {}
}
