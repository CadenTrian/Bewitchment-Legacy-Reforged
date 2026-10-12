package com.bewitchment.world;

import java.util.*;

/** Legacy MiscHandler structure pools. Weights are additive to biome/vanilla pools. */
public final class StructureSpawnRules {
    public enum Site { FORTRESS, STRONGHOLD, VILLAGE, MINESHAFT }
    public enum Creature { CLEAVER, BAFOMETYR, FEUERWURM, HELLHOUND, CAMBION, SHADOW_PERSON, GHOST, BLACK_DOG }
    public static final class Entry {
        public final Creature creature;
        public final int weight;
        public final int minCount = 0, maxCount = 1;
        private Entry(Creature creature, int weight) { this.creature = creature; this.weight = weight; }
    }
    private static Entry entry(Creature creature, int weight) { return new Entry(creature, weight); }
    public static List<Entry> pool(Site site) {
        switch (site) {
            case FORTRESS: return FORTRESS;
            case STRONGHOLD: return STRONGHOLD;
            case VILLAGE: return VILLAGE;
            case MINESHAFT: return MINESHAFT;
            default: throw new AssertionError(site);
        }
    }
    private static final List<Entry> FORTRESS = Collections.unmodifiableList(Arrays.asList(
        entry(Creature.CLEAVER, 2), entry(Creature.BAFOMETYR, 2), entry(Creature.FEUERWURM, 3),
        entry(Creature.HELLHOUND, 3), entry(Creature.CAMBION, 1), entry(Creature.SHADOW_PERSON, 1)));
    private static final List<Entry> STRONGHOLD = Collections.unmodifiableList(Arrays.asList(
        entry(Creature.SHADOW_PERSON, 1), entry(Creature.GHOST, 1), entry(Creature.BLACK_DOG, 1)));
    private static final List<Entry> VILLAGE = Collections.singletonList(entry(Creature.CAMBION, 1));
    private static final List<Entry> MINESHAFT = Collections.unmodifiableList(Arrays.asList(
        entry(Creature.SHADOW_PERSON, 1), entry(Creature.GHOST, 1)));

    public static boolean eligible(Site site, boolean overworld, boolean nether, boolean day,
                                   boolean raining, int moonPhase) {
        if (site == Site.FORTRESS) return nether;
        if (!overworld || day) return false;
        switch (site) {
            case STRONGHOLD: return moonPhase == 4;
            case VILLAGE: return moonPhase == 0 && !raining;
            case MINESHAFT: return moonPhase == 2 || moonPhase == 6;
            default: return false;
        }
    }
    public static boolean passesRoll(int roll) { return roll >= 5 && roll < 100; }
    private StructureSpawnRules() {}
}
