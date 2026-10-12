package com.bewitchment.block;

import com.bewitchment.item.TaglockItem;
import com.bewitchment.machine.SiphoningFlowerTile;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import java.util.List;

public final class SiphoningFlowerBlock extends BushBlock {
    public SiphoningFlowerBlock() { super(Properties.copy(Blocks.POPPY)); }
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    @Override public void animateTick(BlockState state, World world, BlockPos pos, java.util.Random random) {
        PlayerEntity player = net.minecraft.client.Minecraft.getInstance().player;
        TileEntity found = world.getBlockEntity(pos);
        if (player != null && found instanceof SiphoningFlowerTile) {
            SiphoningFlowerTile tile = (SiphoningFlowerTile) found;
            if (tile.boundId != null && player.getUUID().equals(tile.owner))
                world.addParticle(net.minecraft.particles.RedstoneParticleData.REDSTONE, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 0, 0, 0);
        }
    }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new SiphoningFlowerTile(); }
    @Override public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClientSide && placer != null && world.getBlockEntity(pos) instanceof SiphoningFlowerTile) {
            SiphoningFlowerTile tile = (SiphoningFlowerTile) world.getBlockEntity(pos);
            tile.owner = placer.getUUID(); tile.changed();
        }
    }
    @Override public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClientSide || !(entity instanceof LivingEntity) || !TaglockItem.eligible((LivingEntity) entity)) return;
        TileEntity found = world.getBlockEntity(pos);
        if (found instanceof SiphoningFlowerTile) {
            SiphoningFlowerTile tile = (SiphoningFlowerTile) found;
            if (tile.boundId == null) {
                tile.boundId = entity.getUUID(); tile.boundName = entity.getName().getString(); tile.changed();
                entity.hurt(DamageSource.CACTUS, 2);
            }
        }
    }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (hand != Hand.MAIN_HAND || !(world.getBlockEntity(pos) instanceof SiphoningFlowerTile)) return ActionResultType.PASS;
        SiphoningFlowerTile tile = (SiphoningFlowerTile) world.getBlockEntity(pos);
        if (tile.boundId == null) return ActionResultType.PASS;
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.getUUID().equals(tile.owner)) {
            if (!world.isClientSide) tile.clear();
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        if (!player.isShiftKeyDown() && player.getMainHandItem().getItem() == ModItems.BOLINE.get() && TaglockItem.empty(player.getOffhandItem())) {
            if (!world.isClientSide) {
                TaglockItem.giveBound(player, Hand.OFF_HAND, tile.boundId, tile.boundName);
                tile.clear();
                player.getMainHandItem().hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(Hand.MAIN_HAND));
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }
    @Override public void appendHoverText(ItemStack stack, IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.bewitchment.siphoning_flower").withStyle(TextFormatting.RED));
    }
}
