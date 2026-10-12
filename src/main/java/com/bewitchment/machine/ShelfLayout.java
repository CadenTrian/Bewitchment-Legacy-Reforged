package com.bewitchment.machine;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

/** Legacy nine-cell hit layout, shared with the visible stack positions. */
public final class ShelfLayout {
    public static int slot(Direction facing, double x, double y, double z) {
        double horizontal = facing == Direction.SOUTH ? x : facing == Direction.NORTH ? 1 - x : facing == Direction.WEST ? z : 1 - z;
        int column = Math.max(0, Math.min(2, (int)Math.floor(horizontal * 3)));
        int row = Math.max(0, Math.min(2, (int)Math.floor(y * 3)));
        return (2 - row) * 3 + column;
    }
    public static Vector3d center(Direction facing, int slot) {
        if (slot < 0 || slot >= 9) throw new IllegalArgumentException("Shelf slot must be 0..8");
        double u = (3 + (slot % 3) * 5) / 16.0, y = (13 - (slot / 3) * 5) / 16.0;
        if (facing == Direction.SOUTH) return new Vector3d(u, y, .2);
        if (facing == Direction.NORTH) return new Vector3d(1 - u, y, .8);
        if (facing == Direction.WEST) return new Vector3d(.8, y, u);
        return new Vector3d(.2, y, 1 - u);
    }
    private ShelfLayout() {}
}
