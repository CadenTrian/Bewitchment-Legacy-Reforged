package com.bewitchment.machine;

import com.bewitchment.registry.*;
import net.minecraft.entity.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.*;

/** Optional legacy toad shower. Final elevated bodies are checked before insertion. */
public final class WednesdayRitual {
    static <T> List<T> plan(Random random,Supplier<T> create,BiPredicate<T,List<T>> place){
        List<T> planned=new ArrayList<>();
        for(int i=0;i<random.nextInt(128)+64;i++){
            T candidate=create.get();if(candidate==null)continue;
            for(int attempt=0;attempt<16;attempt++)if(place.test(candidate,Collections.unmodifiableList(planned))){planned.add(candidate);break;}
        }
        return planned;
    }
    private static MobEntity create(ServerWorld world,BlockPos center){
        MobEntity entity=ModCreatures.TOAD.get().create(world);if(entity==null)return null;
        entity.moveTo(center.getX()+.5,center.getY(),center.getZ()+.5,0,0);
        entity.finalizeSpawn(world,world.getCurrentDifficultyAt(center),SpawnReason.MOB_SUMMONED,null,null);
        return entity;
    }
    private static boolean place(ServerWorld world,BlockPos center,MobEntity entity,List<MobEntity> planned){
        BlockPos landing=center.offset(LesserHellmouth.offset(world.random),0,LesserHellmouth.offset(world.random));
        if(!world.hasChunkAt(landing))return false;
        while(landing.getY()>0 && !world.getBlockState(landing.below()).getMaterial().blocksMotion())landing=landing.below();
        if(landing.getY()<=0)return false;
        entity.moveTo(landing.getX()+.5,landing.getY(),landing.getZ()+.5,0,0);
        if(!LesserHellmouth.fits(world,entity,Collections.emptyList()))return false;
        entity.moveTo(entity.getX(),entity.getY()+WednesdayRules.height(world.random),entity.getZ(),world.random.nextInt(360),0);
        return LesserHellmouth.fits(world,entity,planned);
    }
    static List<MobEntity> prepare(ServerWorld world,BlockPos center){return plan(world.random,()->create(world,center),(entity,planned)->place(world,center,entity,planned));}
    private static void decorate(ServerWorld world,MobEntity entity){
        entity.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,Integer.MAX_VALUE,9));
        entity.addEffect(new EffectInstance(ModEffects.WEDNESDAY.get(),WednesdayRules.lifetime(world.random)));
        entity.setDeltaMovement(entity.getDeltaMovement().add(new Vector3d(WednesdayRules.velocity(world.random),WednesdayRules.velocity(world.random),WednesdayRules.velocity(world.random))));
    }
    static int spawn(ServerWorld world,List<MobEntity> planned){
        // Install expiry before insertion, so even an insertion hook sees a temporary toad.
        for(MobEntity entity:planned)decorate(world,entity);
        return CallOfTheWild.spawn(world,planned);
    }
    static void tick(ServerWorld world,BlockPos center){
        for(int i=0;i<10;i++){
            double x=world.random.nextGaussian()*.5,y=world.random.nextGaussian()*.5,z=world.random.nextGaussian()*.5;
            world.sendParticles(ParticleTypes.ITEM_SLIME,center.getX()+.5+x,center.getY()+.5+y,center.getZ()+.5+z,0,.6*x,.6*y,.6*z,1);
        }
        if(!WednesdayRules.extra(world.getGameTime(),world.random))return;
        MobEntity entity=create(world,center);if(entity==null)return;
        for(int attempt=0;attempt<16;attempt++)if(place(world,center,entity,Collections.emptyList())){spawn(world,Collections.singletonList(entity));break;}
    }
    private WednesdayRitual(){}
}
