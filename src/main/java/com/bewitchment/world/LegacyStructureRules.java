package com.bewitchment.world;

import net.minecraft.block.*;
import net.minecraft.util.math.*;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;

public final class LegacyStructureRules {
    public enum Kind { WICKERMAN, STONE_CIRCLE, MENHIR, CAMBION_HOME, CAMBION_HOME_MEDIUM }
    public static boolean eligible(Kind kind, boolean plains, boolean forest, boolean beach, String biome) {
        switch (kind) {
            case WICKERMAN: return plains || forest || beach;
            case STONE_CIRCLE: return plains || forest;
            case MENHIR: return plains || forest || biome.equals("minecraft:taiga")
                    || biome.equals("minecraft:giant_tree_taiga") || biome.equals("minecraft:snowy_taiga");
            default: return plains;
        }
    }
    public static String template(Kind kind, String biome, boolean burned) {
        switch (kind) {
            case WICKERMAN: return burned ? "burnedwickerman" : "wickerman";
            case STONE_CIRCLE: return "stonecircle1"; // Legacy nextInt(1) never selected variant 2.
            case MENHIR:
                if (biome.equals("minecraft:taiga")) return "menhir3";
                if (biome.equals("minecraft:giant_tree_taiga")) return "menhir4";
                if (biome.equals("minecraft:snowy_taiga")) return "menhir5";
                return "menhir1";
            case CAMBION_HOME: return "cambion1";
            case CAMBION_HOME_MEDIUM: return "cambionmedium1";
            default: throw new AssertionError(kind);
        }
    }
    public static int offset(Kind kind, boolean burned) {
        return kind == Kind.WICKERMAN ? (burned ? 0 : 1) : kind == Kind.MENHIR || kind == Kind.STONE_CIRCLE ? -1 : 0;
    }
    public static boolean ground(Block block) {
        return block == Blocks.GRASS_BLOCK || block == Blocks.SAND || block == Blocks.SNOW_BLOCK
                || block == Blocks.SNOW || block == Blocks.GLASS || block == Blocks.MYCELIUM;
    }
    public static boolean levelCorner(int originY, int cornerY) {
        return originY > 31 && cornerY >= originY && cornerY <= originY + 2;
    }
    public static int groundY(int height, IntFunction<BlockState> stateAt) {
        for (int y = Math.min(255, height); y >= 30; y--) {
            BlockState state = stateAt.apply(y);
            net.minecraft.fluid.Fluid fluid = state.getFluidState().getType();
            if (fluid == net.minecraft.fluid.Fluids.WATER || fluid == net.minecraft.fluid.Fluids.FLOWING_WATER) return -1;
            if (ground(state.getBlock())) return y;
        }
        return -1;
    }
    /** Validate every intersected chunk before reading terrain or placing any template blocks/entities. */
    public static boolean available(MutableBoundingBox box, BiPredicate<Integer, Integer> hasChunk) {
        if (box.y0 < 0 || box.y1 >= 256) return false;
        for (int x = box.x0 >> 4; x <= box.x1 >> 4; x++)
            for (int z = box.z0 >> 4; z <= box.z1 >> 4; z++) if (!hasChunk.test(x, z)) return false;
        return true;
    }
    private LegacyStructureRules() {}
}
