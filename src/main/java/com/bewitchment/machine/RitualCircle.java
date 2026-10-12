package com.bewitchment.machine;

import net.minecraft.util.math.BlockPos;
import java.util.function.Predicate;

/** Exact legacy concentric circles, with loaded-world/type validation supplied by the caller. */
public final class RitualCircle {
    private static final String[] SMALL = {"..###..", ".#...#.", "#.....#", "#.....#", "#.....#", ".#...#.", "..###.."};
    private static final String[] MEDIUM = {"...#####...", "..#.....#..", ".#.......#.", "#.........#", "#.........#", "#.........#", "#.........#", "#.........#", ".#.......#.", "..#.....#..", "...#####..."};
    private static final String[] LARGE = {"....#######....", "...#.......#...", "..#.........#..", ".#...........#.", "#.............#", "#.............#", "#.............#", "#.............#", "#.............#", "#.............#", "#.............#", ".#...........#.", "..#.........#..", "...#.......#...", "....#######...."};
    public static boolean matches(BlockPos center, Predicate<BlockPos> normalGlyph) {
        return matches(0, center, normalGlyph);
    }
    public static boolean matches(int ring, BlockPos center, Predicate<BlockPos> glyph) {
        String[] pattern = ring == 0 ? SMALL : ring == 1 ? MEDIUM : ring == 2 ? LARGE : null;
        if (pattern == null) throw new IllegalArgumentException("Unknown circle size");
        int radius = pattern.length / 2;
        for (int x = 0; x < pattern.length; x++) for (int z = 0; z < pattern.length; z++)
            if (pattern[x].charAt(z) == '#' && !glyph.test(center.offset(x - radius, 0, z - radius))) return false;
        return true;
    }
    public static boolean accepts(int required, int actual) { return actual >= 1 && actual <= 3 && (required == 4 || actual == required); }
    public static java.util.List<BlockPos> positions(int ring, BlockPos center) {
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        matches(ring, center, pos -> { positions.add(pos); return true; });
        return java.util.Collections.unmodifiableList(positions);
    }
    private RitualCircle() {}
}
