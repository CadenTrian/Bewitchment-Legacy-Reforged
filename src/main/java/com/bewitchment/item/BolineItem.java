package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.IForgeShearable;
import java.util.List;

public final class BolineItem extends ShearsItem {
    private final Multimap<Attribute, AttributeModifier> attributes = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1.5, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
    public BolineItem() { super(new Item.Properties().tab(ModItems.TAB).durability(131)); }
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            ResourceLocation id = world.registryAccess().registryOrThrow(net.minecraft.util.registry.Registry.BIOME_REGISTRY).getKey(world.getBiome(player.blockPosition()));
            if (id != null) stack.getOrCreateTag().putString("Biome", id.toString());
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
    public static ResourceLocation recordedBiome(ItemStack stack) {
        if (!(stack.getItem() instanceof BolineItem) || !stack.hasTag() || !stack.getTag().contains("Biome", 8)) return null;
        String id = stack.getTag().getString("Biome");
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains("Biome", 8) && !stack.getTag().getString("Biome").isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(stack.getTag().getString("Biome"));
            if (id != null) tooltip.add(new TranslationTextComponent("tooltip.bewitchment.boline_biome", id.toString()).withStyle(TextFormatting.GRAY));
        }
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
        return true;
    }
    @Override public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (!(entity instanceof IForgeShearable)) return ActionResultType.PASS;
        IForgeShearable shearable = (IForgeShearable) entity;
        if (!shearable.isShearable(stack, entity.level, entity.blockPosition())) return ActionResultType.PASS;
        if (!entity.level.isClientSide) {
            for (ItemStack drop : shearable.onSheared(player, stack, entity.level, entity.blockPosition(),
                    EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack))) {
                ItemStack result = entity instanceof SheepEntity && drop.getItem().is(ItemTags.WOOL) ? new ItemStack(Items.STRING, 4) : drop;
                ItemEntity spawned = entity.spawnAtLocation(result, 1);
                if (spawned != null) spawned.setDeltaMovement(spawned.getDeltaMovement().add(
                        (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1,
                        entity.getRandom().nextFloat() * 0.05, (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1));
            }
            stack.hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(hand));
        }
        return ActionResultType.sidedSuccess(entity.level.isClientSide);
    }
}
