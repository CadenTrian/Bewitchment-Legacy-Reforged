package com.bewitchment.item;

import com.bewitchment.block.GlyphBlock;
import com.bewitchment.registry.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ChalkItem extends Item {
    private final int type;
    public ChalkItem(int type) { super(new Properties().durability(128).tab(ModItems.TAB)); this.type = type; }
    public int glyphType() { return type; }
    @Override public boolean isRepairable(ItemStack stack) { return false; }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return false; }
    @Override public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel(); PlayerEntity player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.abilities.mayBuild || context.getClickedFace() != Direction.UP) return ActionResultType.PASS;
        BlockPos clicked = context.getClickedPos();
        BlockPos pos = world.getBlockState(clicked).canBeReplaced(new BlockItemUseContext(context)) ? clicked : clicked.above();
        if (!ChalkPlacement.insideHeight(pos.getY(), world.getMaxBuildHeight()) || !world.hasChunkAt(pos)
                || !world.getWorldBorder().isWithinBounds(pos)) return ActionResultType.FAIL;
        BlockState previous = world.getBlockState(pos);
        if (!ChalkPlacement.canReplace(previous, !world.getFluidState(pos).isEmpty())
                || !world.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, context.getItemInHand())) return ActionResultType.FAIL;
        BlockState state = ModRituals.GLYPH.get().defaultBlockState().setValue(GlyphBlock.TYPE, type)
                .setValue(GlyphBlock.LETTER, GlyphBlock.letter(pos)).setValue(GlyphBlock.FACING, Direction.from2DDataValue(world.random.nextInt(4)));
        if (!state.canSurvive(world, pos)) return ActionResultType.FAIL;
        return com.bewitchment.block.BlockInteractionCommit.apply(true, world.isClientSide,
                () -> world.setBlock(pos, state, 3), () -> {
                    world.playSound(null, pos, ModRituals.CHALK_SOUND.get(), SoundCategory.BLOCKS, .5F, 1 + world.random.nextFloat() * .5F);
                    if (!player.isCreative()) context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                });
    }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) { return false; }
}
