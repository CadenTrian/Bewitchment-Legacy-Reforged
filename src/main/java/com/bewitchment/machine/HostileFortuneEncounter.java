package com.bewitchment.machine;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/** One bounded encounter attempt; buffs are rolled only after accepted insertion. */
public final class HostileFortuneEncounter {
    public interface Access {
        boolean prepare();
        boolean insert();
        int roll();
        void effect(EffectInstance effect);
        default Effect magicResistance() { return com.bewitchment.registry.ModEffects.MAGIC_RESISTANCE.get(); }
    }

    public static boolean supported(FortuneData.Kind kind) {
        if (kind == null) return false;
        switch (kind) {
            case MEET_ZOMBIE: case MEET_WITCH: case MEET_SILVERFISH: case MEET_BLAZE:
            case MEET_CAMBION: case MEET_DRUDEN: case MEET_SHADOW_PERSON: case MEET_FEUERWURM:
            case MEET_BAFOMETYR: case MEET_CLEAVER: case MEET_DEMON: case MEET_DIRE_WOLF: return true;
            default: return false;
        }
    }

    public static boolean magicProtection(FortuneData.Kind kind) {
        return kind == FortuneData.Kind.MEET_BLAZE || kind == FortuneData.Kind.MEET_FEUERWURM
                || kind == FortuneData.Kind.MEET_BAFOMETYR || kind == FortuneData.Kind.MEET_CLEAVER
                || kind == FortuneData.Kind.MEET_DEMON;
    }

    public static boolean buff(int difficulty, int roll) {
        return roll >= 0 && roll < Math.max(0, Math.min(3, difficulty));
    }

    public static List<EffectInstance> buffs(int difficulty, IntSupplier rolls) {
        return buffs(difficulty, rolls, Effects.FIRE_RESISTANCE);
    }

    public static List<EffectInstance> buffs(int difficulty, IntSupplier rolls, Effect protection) {
        List<EffectInstance> effects = new ArrayList<>();
        for (Effect effect : new Effect[]{Effects.DAMAGE_RESISTANCE, Effects.DAMAGE_BOOST, Effects.MOVEMENT_SPEED, protection})
            if (buff(difficulty, rolls.getAsInt())) effects.add(new EffectInstance(effect, 900, 1));
        return effects;
    }

    public static boolean execute(FortuneData.Kind kind, int difficulty, Access access) {
        if (!supported(kind) || difficulty <= 0 || !access.prepare() || !access.insert()) return false;
        if (kind == FortuneData.Kind.MEET_DIRE_WOLF) {
            for (Effect effect : new Effect[]{Effects.DAMAGE_RESISTANCE, Effects.DAMAGE_BOOST, Effects.REGENERATION})
                if (buff(difficulty, access.roll())) access.effect(new EffectInstance(effect, 900, 1));
        } else {
            Effect protection = magicProtection(kind) ? access.magicResistance() : Effects.FIRE_RESISTANCE;
            for (EffectInstance effect : buffs(difficulty, access::roll, protection)) access.effect(effect);
        }
        return true;
    }

    private HostileFortuneEncounter() {}
}
