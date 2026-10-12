package com.bewitchment.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
public final class RavenEntity extends LegacyBirdEntity {
    public RavenEntity(EntityType<? extends RavenEntity> type,World world){super(type,world);}
    @Override protected boolean owl(){return false;}
}
