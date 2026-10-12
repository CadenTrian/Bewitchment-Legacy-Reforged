package com.bewitchment.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** Legacy material effects; data-pack tags replace class-name checks and integration lists. */
public final class MaterialEffects {
    public static final ITag.INamedTag<EntityType<?>> SILVER_WEAK = EntityTypeTags.createOptional(id("silver_weakness"));
    public static final ITag.INamedTag<EntityType<?>> COLD_IRON_WEAK = EntityTypeTags.createOptional(id("cold_iron_weakness"));
    public static final ITag.INamedTag<Item> SILVER_TOOLS = ItemTags.createOptional(id("silver_tools"));
    public static final ITag.INamedTag<Item> COLD_IRON_TOOLS = ItemTags.createOptional(id("cold_iron_tools"));
    public static final ITag.INamedTag<Item> SILVER_ARMOR = ItemTags.createOptional(id("silver_armor"));
    public static final ITag.INamedTag<Item> COLD_IRON_ARMOR = ItemTags.createOptional(id("cold_iron_armor"));

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(MaterialEffects::onHurt);
        MinecraftForge.EVENT_BUS.addListener(MaterialEffects::onLivingTick);
    }

    private static ResourceLocation id(String path) { return new ResourceLocation("bewitchment", path); }

    private static void onHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntityLiving();
        // Retaliation must not trigger another retaliation between two armored entities.
        if (target.level.isClientSide || "thorns".equals(event.getSource().getMsgId())
                || !(event.getSource().getDirectEntity() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity) event.getSource().getDirectEntity();
        float damage = apply(event.getAmount(), target, attacker, SILVER_WEAK, SILVER_TOOLS, SILVER_ARMOR);
        event.setAmount(apply(damage, target, attacker, COLD_IRON_WEAK, COLD_IRON_TOOLS, COLD_IRON_ARMOR));
    }

    private static float apply(float damage, LivingEntity target, LivingEntity attacker,
                               ITag<EntityType<?>> weakness, ITag<Item> tools, ITag<Item> armor) {
        return MaterialDamageRules.melee(damage, weakness.contains(target.getType()), target instanceof PlayerEntity,
                tools.contains(attacker.getMainHandItem().getItem()), weakness.contains(attacker.getType()),
                armorPieces(target, armor), pieces -> attacker.hurt(DamageSource.thorns(target), pieces));
    }
    private static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide || entity.tickCount % 20 != 0) return;
        harmWearer(entity, SILVER_WEAK, SILVER_ARMOR);
        harmWearer(entity, COLD_IRON_WEAK, COLD_IRON_ARMOR);
    }

    private static void harmWearer(LivingEntity entity, ITag<EntityType<?>> weakness, ITag<Item> armor) {
        if (weakness.contains(entity.getType())) {
            int pieces = armorPieces(entity, armor);
            if (pieces > 0) entity.hurt(DamageSource.MAGIC, pieces);
        }
    }

    private static int armorPieces(LivingEntity entity, ITag<Item> armor) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!stack.isEmpty() && armor.contains(stack.getItem())) count++;
        }
        return count;
    }

    private MaterialEffects() {}
}
