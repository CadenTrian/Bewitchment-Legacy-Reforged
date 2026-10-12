package com.bewitchment.combat;
import com.bewitchment.Bewitchment;
import com.bewitchment.potion.TerrainBrewEffect;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.potion.*;
import net.minecraft.util.math.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class TerrainBrewImpacts {
    public static BlockPos center(RayTraceResult hit){
        if(hit instanceof BlockRayTraceResult&&hit.getType()==RayTraceResult.Type.BLOCK)return ((BlockRayTraceResult)hit).getBlockPos();
        if(hit instanceof EntityRayTraceResult)return ((EntityRayTraceResult)hit).getEntity().blockPosition();
        return null;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void impact(ProjectileImpactEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof PotionEntity)||!(event.getEntity().level instanceof ServerWorld))return;
        BlockPos pos=center(event.getRayTraceResult());if(pos==null)return;
        PotionEntity potion=(PotionEntity)event.getEntity();
        for(EffectInstance effect:PotionUtils.getMobEffects(potion.getItem()))if(effect.getEffect() instanceof TerrainBrewEffect)
            ((TerrainBrewEffect)effect.getEffect()).terrain((ServerWorld)potion.level,pos,potion.getOwner(),effect.getAmplifier());
    }
    private TerrainBrewImpacts(){}
}
