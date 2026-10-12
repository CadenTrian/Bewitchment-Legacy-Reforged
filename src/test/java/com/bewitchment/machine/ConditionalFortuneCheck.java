package com.bewitchment.machine;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class ConditionalFortuneCheck {
    private static int checks;
    private static void check(boolean result, String reason) {
        checks++;
        if (!result) throw new AssertionError(reason);
    }

    private static class Access implements FortuneExecution.Access {
        boolean attacker, rain;
        int difficulty, attackerReads, rainReads, difficultyReads, visuals, hits;
        float damage;
        List<EffectInstance> effects = new ArrayList<>();
        List<String> calls = new ArrayList<>();
        Runnable onVisual = () -> {}, onHit = () -> {}, onEffect = () -> {};
        public boolean hasCreditedAttacker() { attackerReads++; calls.add("attacker"); return attacker; }
        public boolean rainingAtPlayer() { rainReads++; calls.add("rain"); return rain; }
        public int difficulty() { difficultyReads++; calls.add("difficulty"); return difficulty; }
        public void effect(EffectInstance effect) { effects.add(effect); calls.add("effect"); onEffect.run(); }
        public void visualLightning() { visuals++; calls.add("visual"); onVisual.run(); }
        public void special(FortuneData.Kind kind) { throw new AssertionError("Unexpected special fortune"); }
        public void hostile(FortuneData.Kind kind) { throw new AssertionError("Unexpected hostile encounter"); }
        public void villager(boolean child) { throw new AssertionError("Unexpected encounter"); }
        public void lightningDamage(float amount) { hits++; damage = amount; calls.add("hit"); onHit.run(); }
    }

    private static final class Store implements FortuneDispatch.Store {
        CompoundNBT saved = new CompoundNBT();
        public FortuneData read() { FortuneData data = new FortuneData(); data.load(saved.copy()); return data; }
        public void write(FortuneData data) { saved = data.save(); }
        void assign(FortuneData.Kind kind) { FortuneData data = new FortuneData(); data.assign(kind, 0); write(data); }
    }

    private static boolean execute(UUID player, Store store, Access access) {
        return FortuneDispatch.execute(player, store, kind -> FortuneExecution.apply(kind, access));
    }

    public static void main(String[] args) {
        Bootstrap.bootStrap();
        for (int difficulty : new int[]{Integer.MIN_VALUE, 0, 1, 2, 3, Integer.MAX_VALUE}) {
            for (int mask = 0; mask < 4; mask++) {
                Access a = new Access(); a.difficulty = difficulty; a.attacker = (mask & 1) != 0; a.rain = (mask & 2) != 0;
                FortuneExecution.apply(FortuneData.Kind.COURAGE, a);
                check(a.attackerReads == 1 && a.rainReads == 0, "Courage only checks credited attacker");
                check(a.effects.size() == (a.attacker ? 1 : 0), "Courage has no payload without attacker");
                check(a.difficultyReads == (a.attacker ? 1 : 0), "Difficulty read at actual activation only");
                check(a.visuals == 0 && a.hits == 0, "Courage never invokes weather or damage");
                if (a.attacker) {
                    EffectInstance effect = a.effects.get(0);
                    check(effect.getEffect() == Effects.DAMAGE_BOOST && effect.getDuration() == 500,
                            "Courage applies exactly 25 seconds of Strength");
                    check(effect.getAmplifier() == 1 + Math.max(0, Math.min(3, difficulty)), "Strength II-V by current difficulty");
                    check(!effect.isAmbient() && !effect.isVisible(), "Legacy nonambient hidden particles");
                    check(a.calls.equals(Arrays.asList("attacker", "difficulty", "effect")), "Check conditions before effect callbacks");
                }

                Access b = new Access(); b.difficulty = difficulty; b.attacker = a.attacker; b.rain = a.rain;
                FortuneExecution.apply(FortuneData.Kind.THUNDERBOLT, b);
                check(b.rainReads == 1 && b.attackerReads == 0 && b.difficultyReads == 0, "Thunderbolt only checks rain at player");
                check(b.effects.isEmpty(), "Thunderbolt applies no potion effects");
                check(b.visuals == (b.rain ? 1 : 0) && b.hits == (b.rain ? 1 : 0), "One visual and hit only under rain");
                check(b.damage == (b.rain ? 1F : 0F), "One point of damage independent of difficulty");
                check(b.calls.equals(b.rain ? Arrays.asList("rain", "visual", "hit") : Collections.singletonList("rain")),
                        "Visual precedes direct hit and failed condition has no callbacks");
            }
        }

        for (FortuneData.Kind kind : FortuneData.Kind.values()) {
            if (kind == FortuneData.Kind.COURAGE || kind == FortuneData.Kind.THUNDERBOLT || kind == FortuneData.Kind.MEET_MERCHANT || kind == FortuneData.Kind.ORPHAN || HostileFortuneEncounter.supported(kind) || kind.ordinal() >= FortuneData.Kind.DROP_ITEM.ordinal()) continue;
            Access a = new Access(); a.difficulty = 2;
            FortuneExecution.apply(kind, a);
            check(a.attackerReads == 0 && a.rainReads == 0 && a.visuals == 0 && a.hits == 0,
                    "Existing unconditional fortunes never query conditional context");
            check(a.effects.equals(kind.effects(2)), "Existing effect payloads preserved");
        }
        Access absent = new Access(); FortuneExecution.apply(null, absent);
        check(absent.calls.isEmpty(), "No fortune cannot query or mutate context");

        UUID player = UUID.randomUUID();
        for (FortuneData.Kind kind : new FortuneData.Kind[]{FortuneData.Kind.COURAGE, FortuneData.Kind.THUNDERBOLT}) {
            Store store = new Store(); store.assign(kind); Access inactive = new Access();
            check(execute(player, store, inactive), "Conditional fortune is consumed even when inactive");
            check(store.read().kind() == null && inactive.effects.isEmpty() && inactive.hits == 0, "No deferred retry after unmet condition");
            inactive.attacker = true; inactive.rain = true;
            check(!execute(player, store, inactive), "Later combat or rain does not replay consumed fortune");
            check(inactive.effects.isEmpty() && inactive.hits == 0, "No callback after condition changes on empty state");

            store.assign(kind); Access active = new Access(); active.attacker = true; active.rain = true;
            Runnable reenter = () -> {
                check(store.read().kind() == null, "Pending fortune removed before callback");
                store.assign(FortuneData.Kind.VITALITY);
                check(!execute(player, store, active), "Recursive callback cannot execute replacement fortune");
            };
            if (kind == FortuneData.Kind.COURAGE) active.onEffect = reenter; else active.onHit = reenter;
            check(execute(player, store, active), "Active conditional fortune dispatches once");
            check(store.read().kind() == FortuneData.Kind.VITALITY, "Callback replacement survives dispatch");
            check(active.effects.size() == (kind == FortuneData.Kind.COURAGE ? 1 : 0)
                    && active.hits == (kind == FortuneData.Kind.THUNDERBOLT ? 1 : 0), "Exactly one payload under reentry");

            store.assign(kind); Access failed = new Access(); failed.attacker = true; failed.rain = true;
            Runnable fail = () -> { throw new IllegalStateException("callback"); };
            if (kind == FortuneData.Kind.COURAGE) failed.onEffect = fail; else failed.onHit = fail;
            try { execute(player, store, failed); throw new AssertionError("Failure swallowed"); }
            catch (IllegalStateException expected) { check("callback".equals(expected.getMessage()), "Callback failure remains visible"); }
            check(store.read().kind() == null, "Failure does not restore consumed fortune");
            store.assign(kind); Access retry = new Access(); retry.attacker = true; retry.rain = true;
            check(execute(player, store, retry), "Dispatch guard released after failed conditional callback");
        }

        Store visualStore = new Store(); visualStore.assign(FortuneData.Kind.THUNDERBOLT);
        Access visualFailure = new Access(); visualFailure.rain = true;
        visualFailure.onVisual = () -> { throw new IllegalStateException("visual"); };
        try { execute(player, visualStore, visualFailure); throw new AssertionError("Visual failure swallowed"); }
        catch (IllegalStateException expected) { check("visual".equals(expected.getMessage()), "Visual exception propagated"); }
        check(visualFailure.hits == 0 && visualStore.read().kind() == null, "Visual exception can leave partial result but cannot replay");

        Store replaced = new Store(); replaced.assign(FortuneData.Kind.THUNDERBOLT);
        Access visualReplace = new Access(); visualReplace.rain = true;
        visualReplace.onVisual = () -> {
            check(replaced.read().kind() == null, "Consumed before visual insertion too");
            replaced.assign(FortuneData.Kind.COURAGE);
            check(!execute(player, replaced, visualReplace), "Visual callback reentry blocked");
        };
        check(execute(player, replaced, visualReplace) && visualReplace.hits == 1, "Replacement does not cancel original direct hit");
        check(replaced.read().kind() == FortuneData.Kind.COURAGE, "Visual callback replacement preserved");
        System.out.println("PASS: " + checks + " conditional fortune gates, effect payloads, damage order and dispatch lifecycle checks");
    }
}
