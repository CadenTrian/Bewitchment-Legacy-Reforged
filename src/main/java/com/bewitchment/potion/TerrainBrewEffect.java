package com.bewitchment.potion;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.potion.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import javax.annotation.Nullable;

public abstract class TerrainBrewEffect extends InstantEffect {
    protected TerrainBrewEffect(EffectType type,int color){super(type,color);}
    public abstract int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier);
    protected abstract void affectLiving(LivingEntity target,int amplifier);
    @Override public void applyEffectTick(LivingEntity target,int amplifier){applyInstantenousEffect(null,null,target,amplifier,1);}
    @Override public void applyInstantenousEffect(@Nullable Entity source,@Nullable Entity owner,LivingEntity target,int amplifier,double intensity){
        if(!(target.level instanceof ServerWorld))return;
        ServerWorld world=(ServerWorld)target.level;
        if(source instanceof AreaEffectCloudEntity){
            String key="bewitchment:terrain_"+getRegistryName();
            if(BrewTerrain.cloudPulse(source.getPersistentData(),key,world.getGameTime()))terrain(world,source.blockPosition(),owner,amplifier);
        }else if(!(source instanceof PotionEntity))terrain(world,target.blockPosition(),owner,amplifier);
        // Thrown potions already perform one terrain pass at their impact position.
        if(target.isAlive())affectLiving(target,amplifier);
    }
}
