package com.bewitchment.entity;

import com.bewitchment.combat.MaterialEffects;
import com.bewitchment.registry.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public final class MetalArrowEntity extends AbstractArrowEntity {
    private static final DataParameter<Boolean> COLD_IRON = EntityDataManager.defineId(MetalArrowEntity.class, DataSerializers.BOOLEAN);
    public MetalArrowEntity(EntityType<? extends MetalArrowEntity> type, World world) { super(type, world); }
    public MetalArrowEntity(World world, LivingEntity shooter, boolean coldIron) {
        super(ModProjectiles.METAL_ARROW.get(), shooter, world); setColdIron(coldIron);
    }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(COLD_IRON, false); }
    public boolean isColdIron() { return entityData.get(COLD_IRON); }
    public void setColdIron(boolean value) { entityData.set(COLD_IRON, value); }
    @Override protected ItemStack getPickupItem() { return new ItemStack(isColdIron() ? ModItems.COLD_IRON_ARROW.get() : ModItems.SILVER_ARROW.get()); }
    @Override protected void doPostHurtEffects(LivingEntity target) {
        if (!level.isClientSide && (isColdIron() ? MaterialEffects.COLD_IRON_WEAK : MaterialEffects.SILVER_WEAK).contains(target.getType()))
            target.hurt(DamageSource.MAGIC, com.bewitchment.combat.MaterialDamageRules.arrow(true, target instanceof net.minecraft.entity.player.PlayerEntity));
        super.doPostHurtEffects(target);
    }
    @Override public void addAdditionalSaveData(CompoundNBT tag) { super.addAdditionalSaveData(tag); tag.putBoolean("isColdIron", isColdIron()); }
    @Override public void readAdditionalSaveData(CompoundNBT tag) { super.readAdditionalSaveData(tag); setColdIron(tag.getBoolean("isColdIron")); }
    @Override public IPacket<?> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
