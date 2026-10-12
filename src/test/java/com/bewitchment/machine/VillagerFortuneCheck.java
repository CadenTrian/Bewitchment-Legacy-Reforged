package com.bewitchment.machine;

import com.bewitchment.combat.VillagerFortunes;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class VillagerFortuneCheck {
    private static int checks;
    private static void check(boolean result, String reason) {
        checks++;
        if (!result) throw new AssertionError(reason);
    }
    private static final class Store implements FortuneDispatch.Store {
        CompoundNBT saved = new CompoundNBT();
        public FortuneData read() { FortuneData data = new FortuneData(); data.load(saved.copy()); return data; }
        public void write(FortuneData data) { saved = data.save(); }
        void assign(FortuneData.Kind kind) { FortuneData data = new FortuneData(); data.assign(kind, 0); write(data); }
    }
    private static final class Access implements FortuneExecution.Access {
        int calls; boolean child; Runnable callback = () -> {};
        public boolean hasCreditedAttacker() { throw new AssertionError("Encounter queried attacker"); }
        public boolean rainingAtPlayer() { throw new AssertionError("Encounter queried rain"); }
        public int difficulty() { throw new AssertionError("Encounter queried effect difficulty"); }
        public void effect(EffectInstance effect) { throw new AssertionError("Encounter applied effect"); }
        public void visualLightning() { throw new AssertionError("Encounter created lightning"); }
        public void lightningDamage(float damage) { throw new AssertionError("Encounter damaged player"); }
        public void special(FortuneData.Kind kind) { throw new AssertionError("Unexpected special fortune"); }
        public void hostile(FortuneData.Kind kind) { throw new AssertionError("Unexpected hostile encounter"); }
        public void villager(boolean child) { this.child = child; calls++; callback.run(); }
    }
    private static boolean execute(UUID player, Store store, Access access) {
        return FortuneDispatch.execute(player, store, kind -> FortuneExecution.apply(kind, access));
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        check(VillagerFortunePlacement.ATTEMPTS == 16 && VillagerFortunePlacement.RADIUS == 16, "Fixed spatial and work budgets");
        check(VillagerFortunePlacement.CHILD_AGE == -2400, "Original two-minute baby age");
        BlockPos origin = new BlockPos(100, 64, -100);
        double[] rolls = {-Double.MAX_VALUE, -4, -1, -.01, 0, .01, 1, 4, Double.MAX_VALUE};
        int[] offsets = {-16, -16, -4, -1, 0, 0, 4, 16, 16};
        for (int x = 0; x < rolls.length; x++) for (int z = 0; z < rolls.length; z++) {
            BlockPos result = VillagerFortunePlacement.candidate(origin, rolls[x], rolls[z]);
            check(result.equals(origin.offset(offsets[x], 0, offsets[z])), "Gaussian offsets floor, clamp and preserve elevation");
            check(origin.equals(new BlockPos(100, 64, -100)), "Candidate generation does not mutate origin");
        }
        for (double invalid : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            check(VillagerFortunePlacement.candidate(origin, invalid, 0) == null, "Nonfinite X rejected");
            check(VillagerFortunePlacement.candidate(origin, 0, invalid) == null, "Nonfinite Z rejected");
        }
        for (int y : new int[]{Integer.MIN_VALUE, -1, 0, 1, 64, 254, 255, 256, Integer.MAX_VALUE})
            check(VillagerFortunePlacement.height(y, 256) == (y >= 1 && y <= 254), "Support and two-block air column within build height");

        for (int success = -1; success <= 16; success++) {
            final int hit = success; int[] generated = {0}, tested = {0};
            BlockPos result = VillagerFortunePlacement.find(() -> new BlockPos(generated[0]++, 64, 0), pos -> { tested[0]++; return pos.getX() == hit; });
            boolean found = success >= 0 && success < 16;
            check((result != null) == found, "Find only within sixteen attempts");
            check(generated[0] == (found ? success + 1 : 16) && tested[0] == generated[0], "Stop immediately after first fit");
            if (found) check(result.getX() == success, "Return first valid landing unchanged");
        }
        int[] invalidCalls = {0};
        check(VillagerFortunePlacement.find(() -> { invalidCalls[0]++; return null; }, pos -> { throw new AssertionError("Null position tested"); }) == null,
                "Invalid generated positions cannot enter world checks");
        check(invalidCalls[0] == 16, "Invalid candidates still consume search budget");
        BlockPos.Mutable mutable = new BlockPos.Mutable(2, 64, 3);
        BlockPos chosen = VillagerFortunePlacement.find(() -> mutable, pos -> true); mutable.set(9, 9, 9);
        check(chosen.equals(new BlockPos(2, 64, 3)), "Returned landing cannot change through mutable candidate alias");

        Set<VillagerProfession> professions = new HashSet<>();
        for (int i = 0; i < 13; i++) professions.add(VillagerFortunes.profession(i));
        check(professions.size() == 13 && !professions.contains(VillagerProfession.NONE) && !professions.contains(VillagerProfession.NITWIT),
                "Thirteen initial vanilla working professions, no unemployed or nitwit");
        for (int roll : new int[]{Integer.MIN_VALUE, -14, -1, 0, 12, 13, Integer.MAX_VALUE})
            check(professions.contains(VillagerFortunes.profession(roll)), "Profession lookup handles every integer");

        UUID player = UUID.randomUUID();
        for (FortuneData.Kind kind : new FortuneData.Kind[]{FortuneData.Kind.MEET_MERCHANT, FortuneData.Kind.ORPHAN}) {
            Store store = new Store(); store.assign(kind); Access access = new Access();
            check(execute(player, store, access), "Encounter dispatch accepted");
            check(access.calls == 1 && access.child == (kind == FortuneData.Kind.ORPHAN), "Correct adult/baby request exactly once");
            check(store.read().kind() == null, "Even unsuccessful placement consumes fortune");
            check(!execute(player, store, access) && access.calls == 1, "No future replay after unsuccessful placement");
            store.assign(kind); access.callback = () -> {
                check(store.read().kind() == null, "Saved consumption precedes encounter callbacks");
                store.assign(FortuneData.Kind.DEATH);
                check(!execute(player, store, access), "Same-player recursive encounter cannot execute replacement");
            };
            check(execute(player, store, access), "Reentrant callback completes");
            check(store.read().kind() == FortuneData.Kind.DEATH, "Callback replacement survives encounter");
            store.assign(kind); access.callback = () -> { throw new IllegalStateException("insertion"); };
            try { execute(player, store, access); throw new AssertionError("Failure swallowed"); }
            catch (IllegalStateException expected) { check("insertion".equals(expected.getMessage()), "Insertion failure remains visible"); }
            check(store.read().kind() == null, "Exception cannot replay encounter");
            store.assign(kind); access.callback = () -> {};
            check(execute(player, store, access), "Guard released after exception");
        }
        System.out.println("PASS: " + checks + " villager fortune bounds, search budgets, professions and dispatch lifecycle checks");
    }
}
