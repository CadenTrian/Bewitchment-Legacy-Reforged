package com.bewitchment.combat;

import com.bewitchment.machine.FortuneData;
import com.bewitchment.machine.HostileFortuneEncounter;
import com.bewitchment.machine.VillagerFortunePlacement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import com.bewitchment.registry.ModCreatures;

public final class HostileFortunes {
    public static EntityType<? extends MobEntity> type(FortuneData.Kind kind) {
        if (kind == FortuneData.Kind.MEET_ZOMBIE) return EntityType.ZOMBIE;
        if (kind == FortuneData.Kind.MEET_WITCH) return EntityType.WITCH;
        if (kind == FortuneData.Kind.MEET_SILVERFISH) return EntityType.SILVERFISH;
        if (kind == FortuneData.Kind.MEET_BLAZE) return EntityType.BLAZE;
        if (kind == FortuneData.Kind.MEET_CAMBION) return ModCreatures.CAMBION.get();
        if (kind == FortuneData.Kind.MEET_DRUDEN) return ModCreatures.DRUDEN.get();
        if (kind == FortuneData.Kind.MEET_SHADOW_PERSON) return ModCreatures.SHADOW_PERSON.get();
        if (kind == FortuneData.Kind.MEET_FEUERWURM) return ModCreatures.FEUERWURM.get();
        if (kind == FortuneData.Kind.MEET_BAFOMETYR) return ModCreatures.BAFOMETYR.get();
        if (kind == FortuneData.Kind.MEET_CLEAVER) return ModCreatures.CLEAVER.get();
        if (kind == FortuneData.Kind.MEET_DEMON) return ModCreatures.DEMON.get();
        if (kind == FortuneData.Kind.MEET_DIRE_WOLF) return EntityType.WOLF;
        return null;
    }

    public static ZombieEntity.GroupData zombieData(boolean baby) {
        // Initialization must not spawn or take over a chicken before the encounter is accepted.
        return new ZombieEntity.GroupData(baby, false);
    }

    public static boolean spawn(PlayerEntity player, FortuneData.Kind kind) {
        if (!(player.level instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return false;
        ServerWorld world = (ServerWorld) player.level;
        BlockPos origin = player.blockPosition();
        return HostileFortuneEncounter.execute(kind, world.getDifficulty().getId(), new HostileFortuneEncounter.Access() {
            MobEntity entity;
            BlockPos landing;
            public boolean prepare() {
                EntityType<? extends MobEntity> selected = kind == FortuneData.Kind.MEET_DEMON && world.random.nextBoolean()
                        ? ModCreatures.DEMONESS.get() : type(kind);
                entity = selected.create(world);
                if (entity == null) return false;
                landing = VillagerFortunePlacement.find(
                        () -> VillagerFortunePlacement.candidate(origin, player.getRandom().nextGaussian(), player.getRandom().nextGaussian()),
                        pos -> FortuneEncounterSpace.fits(world, entity, pos));
                if (landing == null) return false;
                entity.moveTo(landing.getX() + .5, landing.getY(), landing.getZ() + .5, player.getRandom().nextInt(360), 0);
                ILivingEntityData data = entity instanceof ZombieEntity
                        ? zombieData(ZombieEntity.getSpawnAsBabyOdds(world.random)) : null;
                entity.finalizeSpawn(world, world.getCurrentDifficultyAt(landing), SpawnReason.EVENT, data, null);
                if (kind == FortuneData.Kind.MEET_DIRE_WOLF) entity.setTarget(player);
                return FortuneEncounterSpace.fits(world, entity, landing);
            }
            public boolean insert() { return world.addFreshEntity(entity); }
            public int roll() { return player.getRandom().nextInt(10); }
            public void effect(EffectInstance effect) { entity.addEffect(effect); }
        });
    }

    private HostileFortunes() {}
}
