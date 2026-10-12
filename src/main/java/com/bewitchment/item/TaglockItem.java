package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import java.util.*;

public final class TaglockItem extends Item {
    private static final ITag.INamedTag<EntityType<?>> EXCLUDED = EntityTypeTags.createOptional(new ResourceLocation("bewitchment", "taglock_immune"));
    public TaglockItem() { super(new Properties().tab(ModItems.TAB)); }
    public static boolean eligible(LivingEntity entity) { return entity.isAlive() && !EXCLUDED.contains(entity.getType()); }
    public static boolean bound(ItemStack stack) { return stack.hasTag() && !stack.getTag().getString("boundId").isEmpty(); }
    public static boolean empty(ItemStack stack) { return stack.getItem() == ModItems.TAGLOCK.get() && !bound(stack); }
    @Override public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer(); World world = context.getLevel();
        net.minecraft.block.BlockState state = world.getBlockState(context.getClickedPos());
        if (player == null || !player.isShiftKeyDown() || !empty(context.getItemInHand()) || !(state.getBlock() instanceof net.minecraft.block.BedBlock)) return ActionResultType.PASS;
        if (world.isClientSide) return ActionResultType.SUCCESS;
        net.minecraft.util.math.BlockPos head = state.getValue(net.minecraft.block.BedBlock.PART) == net.minecraft.state.properties.BedPart.HEAD
                ? context.getClickedPos() : context.getClickedPos().relative(state.getValue(net.minecraft.block.BedBlock.FACING));
        net.minecraft.util.math.BlockPos foot = head.relative(state.getValue(net.minecraft.block.BedBlock.FACING).getOpposite());
        for (net.minecraft.entity.player.ServerPlayerEntity target : world.getServer().getPlayerList().getPlayers()) {
            if (target.getRespawnDimension().equals(world.dimension()) && (head.equals(target.getRespawnPosition()) || foot.equals(target.getRespawnPosition()))) {
                giveBound(player, context.getHand(), target.getUUID(), target.getName().getString());
                world.playSound(null, context.getClickedPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
                return ActionResultType.CONSUME;
            }
        }
        return ActionResultType.PASS;
    }
    public static void giveBound(PlayerEntity player, Hand hand, UUID id, String name) {
        ItemStack held = player.getItemInHand(hand);
        if (!empty(held)) return;
        ItemStack result = new ItemStack(ModItems.TAGLOCK.get());
        result.getOrCreateTag().putString("boundId", id.toString());
        result.getOrCreateTag().putString("boundName", name);
        if (!player.abilities.instabuild) held.shrink(1);
        if (held.isEmpty()) player.setItemInHand(hand, result);
        else if (!player.inventory.add(result)) player.drop(result, false);
    }
    @Override public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (!empty(stack) || !eligible(target)) return ActionResultType.PASS;
        if (!player.level.isClientSide) giveBound(player, hand, target.getUUID(), target.getName().getString());
        return ActionResultType.sidedSuccess(player.level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (bound(stack)) tooltip.add(new TranslationTextComponent("tooltip.bewitchment.taglock", stack.getTag().getString("boundName")));
    }
}
