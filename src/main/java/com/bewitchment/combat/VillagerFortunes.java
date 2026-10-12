package com.bewitchment.combat;

import com.bewitchment.machine.VillagerFortunePlacement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/** One ordinary villager encounter, without chunk loading or persistent retry work. */
public final class VillagerFortunes {
    private static final VillagerProfession[] PROFESSIONS = {
        VillagerProfession.ARMORER, VillagerProfession.BUTCHER, VillagerProfession.CARTOGRAPHER,
        VillagerProfession.CLERIC, VillagerProfession.FARMER, VillagerProfession.FISHERMAN,
        VillagerProfession.FLETCHER, VillagerProfession.LEATHERWORKER, VillagerProfession.LIBRARIAN,
        VillagerProfession.MASON, VillagerProfession.SHEPHERD, VillagerProfession.TOOLSMITH,
        VillagerProfession.WEAPONSMITH
    };

    public static VillagerProfession profession(int roll) {
        return PROFESSIONS[Math.floorMod(roll, PROFESSIONS.length)];
    }

    public static boolean spawn(PlayerEntity player, boolean child) {
        if (!(player.level instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return false;
        ServerWorld world = (ServerWorld) player.level;
        BlockPos origin = player.blockPosition();
        VillagerEntity villager = EntityType.VILLAGER.create(world);
        if (villager == null) return false;
        BlockPos landing = VillagerFortunePlacement.find(
                () -> VillagerFortunePlacement.candidate(origin, player.getRandom().nextGaussian(), player.getRandom().nextGaussian()),
                pos -> FortuneEncounterSpace.fits(world, villager, pos));
        if (landing == null) return false;
        villager.moveTo(landing.getX() + .5, landing.getY(), landing.getZ() + .5, player.getRandom().nextInt(360), 0);
        villager.finalizeSpawn(world, world.getCurrentDifficultyAt(landing), SpawnReason.EVENT, null, null);
        villager.setAge(child ? VillagerFortunePlacement.CHILD_AGE : 0);
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.byBiome(world.getBiomeName(landing)))
                .setProfession(profession(player.getRandom().nextInt(PROFESSIONS.length))));
        // Recheck after initialization; do not repeatedly invoke insertion callbacks.
        return FortuneEncounterSpace.fits(world, villager, landing) && world.addFreshEntity(villager);
    }

    private VillagerFortunes() {}
}
