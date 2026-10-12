package com.bewitchment.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;

public final class BottledFrostfireItem extends BlockItem {
    public BottledFrostfireItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public String getDescriptionId() { return "item.bewitchment.bottled_frostfire"; }

    @Override
    public ActionResultType place(BlockItemUseContext context) {
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
