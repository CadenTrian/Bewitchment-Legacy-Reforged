package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;

public final class BottledHellfireItem extends BlockItem {
    public BottledHellfireItem(Block block) { super(block, new Properties().tab(ModItems.TAB).craftRemainder(Items.GLASS_BOTTLE)); }
    @Override public String getDescriptionId() { return "item.bewitchment.bottled_hellfire"; }
    @Override public ActionResultType place(BlockItemUseContext context) {
        ActionResultType result = super.place(context);
        PlayerEntity player = context.getPlayer();
        if (result.consumesAction() && !context.getLevel().isClientSide && player != null && !player.isCreative()) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (context.getItemInHand().isEmpty()) player.setItemInHand(context.getHand(), bottle);
            else if (!player.inventory.add(bottle)) player.drop(bottle, false);
        }
        return result;
    }
}
