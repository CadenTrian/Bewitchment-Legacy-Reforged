package com.bewitchment.machine;

import net.minecraft.potion.EffectInstance;

/** Executes an already-consumed fortune against current player conditions. */
public final class FortuneExecution {
    public static final float LIGHTNING_DAMAGE = 1.0F;

    public interface Access {
        boolean hasCreditedAttacker();
        boolean rainingAtPlayer();
        int difficulty();
        void effect(EffectInstance effect);
        void visualLightning();
        void lightningDamage(float amount);
        void villager(boolean child);
        void hostile(FortuneData.Kind kind);
        void special(FortuneData.Kind kind);
    }

    public static void apply(FortuneData.Kind kind, Access access) {
        if (kind == null) return;
        if (kind.digging()) return; // Triggered by a qualifying block break, not timer expiry.
        if (kind == FortuneData.Kind.DROP_ITEM || kind == FortuneData.Kind.MEET_PET
                || kind == FortuneData.Kind.CATS_AND_DOGS || kind == FortuneData.Kind.CORNUCOPIA) {
            access.special(kind);
            return;
        }
        if (HostileFortuneEncounter.supported(kind)) {
            access.hostile(kind);
            return;
        }
        if (kind == FortuneData.Kind.MEET_MERCHANT || kind == FortuneData.Kind.ORPHAN) {
            access.villager(kind == FortuneData.Kind.ORPHAN);
            return;
        }
        if (kind == FortuneData.Kind.COURAGE && !access.hasCreditedAttacker()) return;
        if (kind == FortuneData.Kind.THUNDERBOLT) {
            if (access.rainingAtPlayer()) {
                access.visualLightning();
                access.lightningDamage(LIGHTNING_DAMAGE);
            }
            return;
        }
        for (EffectInstance effect : kind.effects(access.difficulty())) access.effect(effect);
    }

    private FortuneExecution() {}
}
