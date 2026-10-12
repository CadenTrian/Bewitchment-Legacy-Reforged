package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import java.util.List;

/** Three-use, one-time-bound location item. Dimension IDs use 1.16 registry names. */
public final class WaystoneItem extends Item {
    public WaystoneItem() { super(new Properties().durability(3).tab(ModItems.TAB)); }
    public static boolean validPosition(BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < 256 && pos.getX() >= -30000000 && pos.getX() < 30000000
                && pos.getZ() >= -30000000 && pos.getZ() < 30000000;
    }
    public static ResourceLocation dimension(ItemStack stack) {
        if (!(stack.getItem() instanceof WaystoneItem) || !stack.hasTag() || !stack.getTag().contains("dimension",8)) return null;
        String id = stack.getTag().getString("dimension");
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }
    public static BlockPos location(ItemStack stack) {
        if (dimension(stack) == null || !stack.getTag().contains("location",4)) return null;
        BlockPos pos = BlockPos.of(stack.getTag().getLong("location"));
        return validPosition(pos) ? pos : null;
    }
    public static boolean bind(ItemStack stack, BlockPos pos, ResourceLocation dimension) {
        if (!(stack.getItem() instanceof WaystoneItem) || !validPosition(pos) || dimension == null
                || stack.hasTag() && stack.getTag().contains("location")) return false;
        stack.getOrCreateTag().putLong("location",pos.asLong());
        stack.getTag().putString("dimension",dimension.toString());
        return true;
    }
    public static ItemStack afterUse(ItemStack stack, boolean creative) {
        ItemStack result = stack.copy();
        if (location(result) == null || result.getDamageValue() >= 3) return ItemStack.EMPTY;
        if (!creative) { int damage=result.getDamageValue()+1; if(damage>=3) return ItemStack.EMPTY; result.setDamageValue(damage); }
        return result;
    }
    @Override public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player=context.getPlayer(); World world=context.getLevel();
        BlockPos pos=context.getClickedPos().relative(context.getClickedFace()); ItemStack stack=context.getItemInHand();
        if(player==null || !player.abilities.mayBuild || !world.mayInteract(player,context.getClickedPos())
                || !player.mayUseItemAt(pos,context.getClickedFace(),stack) || !validPosition(pos)
                || stack.hasTag() && stack.getTag().contains("location")) return ActionResultType.PASS;
        if(!world.isClientSide) bind(stack,pos,world.dimension().location());
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
    @Override public boolean isFoil(ItemStack stack) { return location(stack)!=null || super.isFoil(stack); }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        BlockPos pos=location(stack);
        tooltip.add(pos==null ? new TranslationTextComponent("tooltip.bewitchment.waystone.unbound")
                : new TranslationTextComponent("tooltip.bewitchment.waystone",pos.getX(),pos.getY(),pos.getZ(),dimension(stack).toString()));
    }
}
