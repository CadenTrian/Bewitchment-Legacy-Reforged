package com.bewitchment.combat;

import com.bewitchment.machine.FortuneData;
import com.bewitchment.machine.FortuneExecution;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

/** Server adapter for conditional fortunes; does not own or replace pending state. */
public final class FortuneEffects {
    public static void apply(PlayerEntity player, FortuneData.Kind kind) {
        if (!(player.level instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return;
        ServerWorld world = (ServerWorld) player.level;
        FortuneExecution.apply(kind, new FortuneExecution.Access() {
            public boolean hasCreditedAttacker() { return player.getKillCredit() != null; }
            public boolean rainingAtPlayer() { return world.isRainingAt(player.blockPosition()); }
            public int difficulty() { return world.getDifficulty().getId(); }
            public void effect(EffectInstance effect) { player.addEffect(effect); }
            public void visualLightning() {
                LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                if (bolt != null) {
                    bolt.moveTo(player.getX(), player.getY(), player.getZ());
                    bolt.setVisualOnly(true);
                    world.addFreshEntity(bolt);
                }
            }
            // Legacy damage is independent of whether the visual entity is inserted.
            public void lightningDamage(float amount) { player.hurt(DamageSource.LIGHTNING_BOLT, amount); }
            public void villager(boolean child) { VillagerFortunes.spawn(player, child); }
            public void hostile(FortuneData.Kind kind) { HostileFortunes.spawn(player, kind); }
            public void special(FortuneData.Kind kind) {
                if (kind == FortuneData.Kind.DROP_ITEM) player.drop(true);
                else if (kind == FortuneData.Kind.MEET_PET) PetFortunes.spawn(player);
                else AnimalShowerFortunes.spawn(player, kind == FortuneData.Kind.CATS_AND_DOGS);
            }
        });
    }

    private FortuneEffects() {}
}
