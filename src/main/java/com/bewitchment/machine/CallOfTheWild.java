package com.bewitchment.machine;

import com.bewitchment.registry.ModCreatures;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import java.util.*;
import java.util.function.*;

/** Original eighteen-choice ritual pool, with bounded loaded-space placement. */
public final class CallOfTheWild {
    private static final String[] IDS={"lizard","lizard","owl","raven","snake","ocelot","wolf","parrot","druden","werewolf","horse","llama","donkey","sheep","cow","pig","chicken","toad"};
    static String id(int choice){if(choice<0 || choice>=IDS.length)throw new IllegalArgumentException("Invalid wild creature choice");return IDS[choice];}
    static boolean hostile(int choice){return choice==8 || choice==9;}
    static boolean allowed(int choice,boolean peaceful){id(choice);return !peaceful || !hostile(choice);}
    static EntityType<?> type(int choice){
        switch(choice){
            case 0:case 1:return ModCreatures.LIZARD.get();
            case 2:return ModCreatures.OWL.get();case 3:return ModCreatures.RAVEN.get();
            case 4:return ModCreatures.SNAKE.get();case 5:return EntityType.OCELOT;
            case 6:return EntityType.WOLF;case 7:return EntityType.PARROT;
            case 8:return ModCreatures.DRUDEN.get();case 9:return ModCreatures.WEREWOLF.get();
            case 10:return EntityType.HORSE;case 11:return EntityType.LLAMA;case 12:return EntityType.DONKEY;
            case 13:return EntityType.SHEEP;case 14:return EntityType.COW;case 15:return EntityType.PIG;
            case 16:return EntityType.CHICKEN;case 17:return ModCreatures.TOAD.get();
            default:throw new IllegalArgumentException("Invalid wild creature choice");
        }
    }
    /** Reroll the original loop bound after each candidate, including failed candidates. */
    static <T> List<T> plan(Random random,IntFunction<T> create,BiPredicate<T,List<T>> place){
        List<T> planned=new ArrayList<>();
        for(int i=0;i<random.nextInt(3)+1;i++){
            T candidate=create.apply(random.nextInt(18));if(candidate==null)continue;
            for(int attempt=0;attempt<16;attempt++)if(place.test(candidate,Collections.unmodifiableList(planned))){planned.add(candidate);break;}
        }
        return planned;
    }
    static List<MobEntity> prepare(ServerWorld world,BlockPos center){
        return plan(world.random,choice->{
            if(!allowed(choice,world.getDifficulty()==Difficulty.PEACEFUL))return null;
            MobEntity entity=(MobEntity)type(choice).create(world);if(entity==null)return null;
            entity.moveTo(center.getX()+.5,center.getY(),center.getZ()+.5,0,0);
            entity.finalizeSpawn(world,world.getCurrentDifficultyAt(center),SpawnReason.MOB_SUMMONED,null,null);
            return entity;
        },(entity,planned)->{
            BlockPos landing=center.offset(LesserHellmouth.offset(world.random),0,LesserHellmouth.offset(world.random));
            if(!world.hasChunkAt(landing))return false;
            while(landing.getY()>0 && !world.getBlockState(landing.below()).getMaterial().blocksMotion())landing=landing.below();
            if(landing.getY()<=0)return false;
            entity.moveTo(landing.getX()+.5,landing.getY(),landing.getZ()+.5,world.random.nextInt(360),0);
            return LesserHellmouth.fits(world,entity,planned);
        });
    }
    static <T> int insert(List<T> planned,Predicate<T> spawn,Consumer<T> onSpawn){
        int count=0;
        for(T entity:planned)if(spawn.test(entity)){count++;onSpawn.accept(entity);}
        return count;
    }
    static int spawn(ServerWorld world,List<MobEntity> planned){
        return insert(planned,entity->
            !(world.getDifficulty()==Difficulty.PEACEFUL && entity.getType().getCategory()==EntityClassification.MONSTER)
                && LesserHellmouth.fits(world,entity,Collections.emptyList()) && world.addFreshEntity(entity),
            entity->{for(ServerPlayerEntity player:world.getEntitiesOfClass(ServerPlayerEntity.class,entity.getBoundingBox().inflate(50)))CriteriaTriggers.SUMMONED_ENTITY.trigger(player,entity);});
    }
    public static void verifyPool(){
        for(int i=0;i<18;i++){
            EntityType<?> type=type(i);
            if(type==null || type.getRegistryName()==null || !type.getRegistryName().getPath().equals(id(i)) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(type))
                throw new IllegalStateException("Invalid Call of the Wild creature slot "+i);
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Call of the Wild: all eighteen creature slots and attributes verified");
    }
    static void particles(ServerWorld world,BlockPos center){
        world.sendParticles(ParticleTypes.CLOUD,center.getX()+.5,center.getY()+.5,center.getZ()+.5,10,.5,.5,.5,.1);
    }
    private CallOfTheWild(){}
}
