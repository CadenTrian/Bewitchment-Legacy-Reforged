package com.bewitchment.machine;

import com.bewitchment.block.*;
import com.bewitchment.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class PlacedItemInteraction {
    public static void register() {
        AltarItemUpgrades.init();
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, PlacedItemInteraction::place);
    }
    private static void place(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer(); World world = event.getWorld(); ItemStack stack = event.getItemStack();
        if (!player.isShiftKeyDown() || !player.abilities.mayBuild || event.getFace() != Direction.UP || AltarItemUpgrades.get(stack) == null) return;
        BlockState below = world.getBlockState(event.getPos());
        if (!(below.getBlock() instanceof WitchesAltarBlock) || below.getValue(WitchesAltarBlock.TYPE) == 0) return;
        BlockPos pos = event.getPos().above();
        BlockState placed = ModBlocks.PLACED_ITEM.get().defaultBlockState().setValue(PlacedItemBlock.FACING, player.getDirection().getOpposite());
        if (!world.isEmptyBlock(pos) || !placed.canSurvive(world, pos) || !world.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) return;
        event.setCanceled(true); event.setCancellationResult(ActionResultType.sidedSuccess(world.isClientSide));
        if (!world.isClientSide && world.setBlock(pos, placed, 3)) {
            ((PlacedItemTile) world.getBlockEntity(pos)).setItem(stack);
            if (!player.abilities.instabuild) stack.shrink(1);
        }
    }
    private PlacedItemInteraction() {}
}
