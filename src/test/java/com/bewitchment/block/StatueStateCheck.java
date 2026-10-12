package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;

public final class StatueStateCheck {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        for (String deity : new String[]{"baphomet", "herne", "leonard", "lilith", "moloch"}) {
            for (String material : new String[]{"stone", "gold", "nether_brick", "scorned_brick"}) {
                for (boolean idol : new boolean[]{false, true}) {
                    int height = idol ? (deity.equals("baphomet") || deity.equals("moloch") ? 1 : 2)
                            : deity.equals("baphomet") ? 2 : deity.equals("herne") || deity.equals("lilith") ? 4 : 3;
                    StatueBlock block = new StatueBlock(AbstractBlock.Properties.copy(Blocks.STONE), deity, material, idol, height);
                    StatueBlock neighbor = new StatueBlock(AbstractBlock.Properties.copy(Blocks.STONE), deity, material, idol, height);
                    check(block.getStateDefinition().getPossibleStates().size() == 16, "Four facings and four part indices");
                    for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                        int part = state.getValue(StatueBlock.PART);
                        check(block.hasTileEntity(state) == (part == 0), "Only origin renders");
                        check(block.getEnchantPowerBonus(state, null, BlockPos.ZERO) == (part == 0 ? idol ? 1.5F : 3.5F : 0), "No multiplied enchantment bonus");
                        check(state.getPistonPushReaction() == PushReaction.DESTROY, "Pistons cannot move column parts");
                        for (Rotation rotation : Rotation.values()) {
                            BlockState result = block.rotate(state, rotation);
                            check(result.getValue(StatueBlock.PART) == part, "Rotation keeps column index");
                            check(result.getValue(StatueBlock.FACING) == rotation.rotate(state.getValue(StatueBlock.FACING)), "Rotation maps facing");
                        }
                        for (Mirror mirror : Mirror.values()) {
                            BlockState result = block.mirror(state, mirror);
                            check(result.getValue(StatueBlock.FACING) == mirror.mirror(state.getValue(StatueBlock.FACING)), "Mirror maps facing");
                            check(block.mirror(result, mirror) == state, "Mirror round trip");
                        }
                        for (int i = -1; i <= 4; i++) {
                            check(block.isMatchingPart(state, state, i) == (i == part && i < height), "Ownership rejects out-of-height parts");
                            check(!block.isMatchingPart(state, neighbor.defaultBlockState(), i), "Different statue is never owned");
                            check(!block.isMatchingPart(state, Blocks.AIR.defaultBlockState(), i), "Air is never owned");
                        }
                        BlockState opposite = state.setValue(StatueBlock.FACING, state.getValue(StatueBlock.FACING).getOpposite());
                        check(!block.isMatchingPart(state, opposite, part), "Opposite-facing neighbor protected");
                    }
                }
            }
        }
        for (int height : new int[]{-1, 0, 5, Integer.MAX_VALUE}) {
            boolean rejected = false;
            try { new StatueBlock(AbstractBlock.Properties.copy(Blocks.STONE), "herne", "stone", false, height); }
            catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "Invalid height rejected");
        }
        System.out.println("PASS: " + checks + " statue state and ownership checks");
    }
}