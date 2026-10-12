package com.bewitchment.registry;

import com.bewitchment.entity.MetalArrowEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;

public final class ModProjectiles {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, "bewitchment");
    public static final RegistryObject<EntityType<MetalArrowEntity>> METAL_ARROW = ENTITIES.register("silver_arrow", () ->
            EntityType.Builder.<MetalArrowEntity>of(MetalArrowEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).updateInterval(20).build("bewitchment:silver_arrow"));
    public static void setup() {
        DispenserBlock.registerBehavior(ModItems.SILVER_ARROW.get(), dispenser(false));
        DispenserBlock.registerBehavior(ModItems.COLD_IRON_ARROW.get(), dispenser(true));
    }
    private static ProjectileDispenseBehavior dispenser(boolean coldIron) {
        return new ProjectileDispenseBehavior() {
            @Override protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack stack) {
                MetalArrowEntity arrow = new MetalArrowEntity(METAL_ARROW.get(), world);
                arrow.setPos(pos.x(), pos.y(), pos.z()); arrow.setColdIron(coldIron);
                arrow.pickup = AbstractArrowEntity.PickupStatus.ALLOWED; return arrow;
            }
        };
    }
    private ModProjectiles() {}
}
