package com.bewitchment.machine;

import com.bewitchment.combat.HostileFortunes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class HostileFortuneCheck {
    private static int checks;
    private static final Effect MAGIC = com.bewitchment.potion.DefensiveBrewEffects.magicResistance();
    private static void check(boolean result, String reason) { checks++; if (!result) throw new AssertionError(reason); }
    private static final FortuneData.Kind[] KINDS = {FortuneData.Kind.MEET_ZOMBIE, FortuneData.Kind.MEET_WITCH, FortuneData.Kind.MEET_SILVERFISH, FortuneData.Kind.MEET_BLAZE, FortuneData.Kind.MEET_CAMBION, FortuneData.Kind.MEET_DRUDEN, FortuneData.Kind.MEET_SHADOW_PERSON, FortuneData.Kind.MEET_FEUERWURM, FortuneData.Kind.MEET_BAFOMETYR, FortuneData.Kind.MEET_CLEAVER, FortuneData.Kind.MEET_DEMON, FortuneData.Kind.MEET_DIRE_WOLF};
    private static class Access implements HostileFortuneEncounter.Access {
        boolean ready = true, accepted = true;
        int prepared, inserted, rolls; List<EffectInstance> effects = new ArrayList<>();
        Runnable onInsert = () -> {}, onEffect = () -> {};
        public Effect magicResistance() { return MAGIC; }
        public boolean prepare() { prepared++; return ready; }
        public boolean insert() { inserted++; onInsert.run(); return accepted; }
        public int roll() { rolls++; return 0; }
        public void effect(EffectInstance effect) { effects.add(effect); onEffect.run(); }
    }
    private static class Store implements FortuneDispatch.Store {
        CompoundNBT saved = new CompoundNBT();
        public FortuneData read() { FortuneData data = new FortuneData(); data.load(saved.copy()); return data; }
        public void write(FortuneData data) { saved = data.save(); }
        void assign(FortuneData.Kind kind) { FortuneData data = new FortuneData(); data.assign(kind, 0); write(data); }
    }
    private static boolean dispatch(UUID player, Store store, Access access) {
        return FortuneDispatch.execute(player, store, kind -> HostileFortuneEncounter.execute(kind, 3, access));
    }
    private static final class Routing implements FortuneExecution.Access {
        FortuneData.Kind selected; int calls;
        public boolean hasCreditedAttacker() { throw new AssertionError("Attacker queried"); }
        public boolean rainingAtPlayer() { throw new AssertionError("Weather queried"); }
        public int difficulty() { throw new AssertionError("Player effect difficulty queried"); }
        public void effect(EffectInstance effect) { throw new AssertionError("Player effect applied"); }
        public void visualLightning() { throw new AssertionError("Lightning invoked"); }
        public void lightningDamage(float damage) { throw new AssertionError("Player hit"); }
        public void villager(boolean child) { throw new AssertionError("Villager invoked"); }
        public void special(FortuneData.Kind kind) { throw new AssertionError("Unexpected special fortune"); }
        public void hostile(FortuneData.Kind kind) { selected = kind; calls++; }
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        EntityType<?>[] types = {EntityType.ZOMBIE, EntityType.WITCH, EntityType.SILVERFISH, EntityType.BLAZE};
        for (int i = 0; i < KINDS.length; i++) {
            if (i < types.length) check(HostileFortunes.type(KINDS[i]) == types[i], "Exact vanilla encounter type");
            Routing route = new Routing(); FortuneExecution.apply(KINDS[i], route);
            check(route.selected == KINDS[i] && route.calls == 1, "Routes once without unrelated facts or player effects");
        }
        for (FortuneData.Kind kind : FortuneData.Kind.values()) {
            boolean supported = Arrays.asList(KINDS).contains(kind);
            check(HostileFortuneEncounter.supported(kind) == supported,
                    "Supported identities and entity mapping agree");
        }
        check(!HostileFortuneEncounter.supported(null) && HostileFortunes.type(null) == null, "Null identity unsupported");
        for (boolean baby : new boolean[]{false, true}) {
            ZombieEntity.GroupData data = HostileFortunes.zombieData(baby);
            check(data.isBaby == baby && !data.canSpawnJockey, "Preserve chosen age but suppress extra chicken or riding during initialization");
        }

        for (int difficulty : new int[]{Integer.MIN_VALUE, 0, 1, 2, 3, Integer.MAX_VALUE}) {
            int wins = 0;
            for (int roll = 0; roll < 10; roll++) if (HostileFortuneEncounter.buff(difficulty, roll)) wins++;
            check(wins == Math.max(0, Math.min(3, difficulty)), "Exact independent 0/10/20/30 percent chance");
            check(!HostileFortuneEncounter.buff(difficulty, -1) && !HostileFortuneEncounter.buff(difficulty, 10), "Out-of-range rolls cannot grant buffs");
            for (FortuneData.Kind kind : KINDS) for (int mask = 0; mask < 4; mask++) {
                Access access = new Access(); access.ready = (mask & 1) != 0; access.accepted = (mask & 2) != 0;
                boolean expected = difficulty > 0 && access.ready && access.accepted;
                check(HostileFortuneEncounter.execute(kind, difficulty, access) == expected, "Preparation, insertion and Peaceful outcome");
                check(access.prepared == (difficulty > 0 ? 1 : 0), "Peaceful does not prepare or query terrain");
                check(access.inserted == (difficulty > 0 && access.ready ? 1 : 0), "Insert at most once after preparation");
                check(access.rolls == (expected ? (kind == FortuneData.Kind.MEET_DIRE_WOLF ? 3 : 4) : 0) && access.effects.size() == (expected ? (kind == FortuneData.Kind.MEET_DIRE_WOLF ? 3 : 4) : 0), "Only accepted insertion rolls buffs");
            }
        }
        for (FortuneData.Kind kind : KINDS) {
            Access granted = new Access(); HostileFortuneEncounter.execute(kind, 3, granted);
            if (kind == FortuneData.Kind.MEET_DIRE_WOLF) check(granted.effects.size() == 3 && granted.effects.get(2).getEffect() == Effects.REGENERATION, "Dire wolf has three rolls and regeneration");
            else check(granted.effects.get(3).getEffect() == (HostileFortuneEncounter.magicProtection(kind) ? MAGIC : Effects.FIRE_RESISTANCE), "Creature-specific fourth buff");
        }
        Access blaze = new Access();
        HostileFortuneEncounter.execute(FortuneData.Kind.MEET_BLAZE, 3, blaze);
        check(blaze.effects.get(3).getEffect() == MAGIC, "Blaze receives magic resistance instead of fire resistance");
        Access unsupported = new Access();
        check(!HostileFortuneEncounter.execute(FortuneData.Kind.VITALITY, 3, unsupported)
                && !HostileFortuneEncounter.execute(null, 3, unsupported) && unsupported.prepared == 0, "Unsupported requests cannot prepare entities");

        Effect[] expectedTypes = {Effects.DAMAGE_RESISTANCE, Effects.DAMAGE_BOOST, Effects.MOVEMENT_SPEED, Effects.FIRE_RESISTANCE};
        for (int mask = 0; mask < 16; mask++) {
            final int selected = mask; int[] index = {0};
            List<EffectInstance> effects = HostileFortuneEncounter.buffs(1, () -> ((selected & (1 << index[0]++)) != 0) ? 0 : 9);
            check(index[0] == 4 && effects.size() == Integer.bitCount(mask), "Four independent rolls, not one shared roll");
            int found = 0;
            for (int i = 0; i < 4; i++) if ((mask & (1 << i)) != 0) {
                EffectInstance effect = effects.get(found++);
                check(effect.getEffect() == expectedTypes[i] && effect.getDuration() == 900 && effect.getAmplifier() == 1,
                        "Legacy 45-second level-II payload in original order");
                check(!effect.isAmbient() && effect.isVisible(), "Encounter buffs retain normal visible particles");
            }
        }
        check(HostileFortuneEncounter.buffs(3, () -> 0).get(0) != HostileFortuneEncounter.buffs(3, () -> 0).get(0), "Fresh effects per encounter");

        UUID player = UUID.randomUUID();
        for (FortuneData.Kind kind : KINDS) {
            Store store = new Store(); store.assign(kind); Access failed = new Access(); failed.ready = false;
            check(dispatch(player, store, failed) && store.read().kind() == null, "Placement failure consumes pending fortune");
            check(!dispatch(player, store, failed) && failed.prepared == 1, "No persistent placement retry");
            store.assign(kind); Access rejected = new Access(); rejected.accepted = false;
            check(dispatch(player, store, rejected) && store.read().kind() == null && rejected.rolls == 0, "Rejected insertion consumes without buffs");
            store.assign(kind); Access active = new Access(); active.onInsert = () -> {
                check(store.read().kind() == null, "Consumption saved before insertion callback");
                store.assign(FortuneData.Kind.VITALITY);
                check(!dispatch(player, store, active), "Reentrant insertion cannot dispatch replacement");
            };
            check(dispatch(player, store, active) && active.inserted == 1 && active.effects.size() == (kind == FortuneData.Kind.MEET_DIRE_WOLF ? 3 : 4), "Single insertion and original buffs despite replacement");
            check(store.read().kind() == FortuneData.Kind.VITALITY, "Callback-assigned replacement preserved");
            store.assign(kind); Access throwing = new Access(); throwing.onEffect = () -> { throw new IllegalStateException("effect"); };
            try { dispatch(player, store, throwing); throw new AssertionError("Failure swallowed"); }
            catch (IllegalStateException expected) { check("effect".equals(expected.getMessage()), "Buff callback exception visible"); }
            check(throwing.inserted == 1 && throwing.effects.size() == 1 && store.read().kind() == null, "Partial buff result never replays encounter");
            store.assign(kind); check(dispatch(player, store, new Access()), "Guard released after callback failure");
        }
        System.out.println("PASS: " + checks + " hostile fortune mapping, Peaceful gating, buffs, insertion and callback lifecycle checks");
    }
}
