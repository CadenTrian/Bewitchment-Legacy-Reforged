package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import java.lang.reflect.Proxy;
import java.util.*;

/** Exercises support removal without loading a client or generating a world. */
public final class SpanishMossCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        IWorld world = (IWorld) Proxy.newProxyInstance(IWorld.class.getClassLoader(), new Class<?>[]{IWorld.class}, (proxy, method, arguments) -> {
            if (method.getName().equals("getBlockState")) return blocks.getOrDefault(arguments[0], Blocks.AIR.defaultBlockState());
            throw new AssertionError("Unexpected world access: " + method.getName());
        });
        SpanishMossBlock body = new SpanishMossBlock(false), tip = new SpanishMossBlock(true);
        BlockPos pos = new BlockPos(8, 70, 8);
        BlockState north = body.defaultBlockState().setValue(VineBlock.NORTH, true);
        check(!body.canSurvive(north, world, pos), "Floating moss must not survive");
        blocks.put(pos.north(), Blocks.STONE.defaultBlockState());
        check(body.canSurvive(north, world, pos), "Solid neighbor supports matching face");
        blocks.clear(); blocks.put(pos.above(), north);
        check(tip.canSurvive(tip.defaultBlockState().setValue(VineBlock.NORTH, true), world, pos), "Body supports a terminal tip");
        blocks.put(pos.above(), tip.defaultBlockState().setValue(VineBlock.NORTH, true));
        check(body.canSurvive(north, world, pos), "Both moss variants carry support");
        BlockState twoFaces = north.setValue(VineBlock.SOUTH, true);
        BlockState pruned = body.updateShape(twoFaces, Direction.UP, blocks.get(pos.above()), world, pos, pos.above());
        check(pruned.getValue(VineBlock.NORTH) && !pruned.getValue(VineBlock.SOUTH), "Prune unsupported faces individually");
        blocks.clear();
        check(body.updateShape(north, Direction.UP, Blocks.AIR.defaultBlockState(), world, pos, pos.above()).isAir(), "Last support removal clears moss");
        blocks.put(pos.above(), Blocks.STONE.defaultBlockState());
        check(body.canSurvive(body.defaultBlockState().setValue(VineBlock.UP, true), world, pos), "Ceiling supports upward face");
        blocks.put(pos.above(), Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
        check(!body.canSurvive(north, world, pos), "Vanilla vines do not carry Spanish Moss");
        check(!body.isLadder(north, world, pos, null), "Moss cannot be climbed");
        System.out.println("PASS: " + checks + " Spanish Moss support checks");
    }
}
