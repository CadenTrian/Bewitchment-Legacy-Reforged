package com.bewitchment.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
public final class OwlEntity extends LegacyBirdEntity {
    public OwlEntity(EntityType<? extends OwlEntity> type,World world){super(type,world);}
    @Override protected boolean owl(){return true;}
}
