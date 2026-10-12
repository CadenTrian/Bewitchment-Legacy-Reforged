package com.bewitchment.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Legacy demoness variant; combat, merchant state and skin persistence are shared with demons. */
public final class DemonessEntity extends DemonEntity {
    public DemonessEntity(EntityType<? extends DemonessEntity> type, World world) {
        super(type, world);
    }
}