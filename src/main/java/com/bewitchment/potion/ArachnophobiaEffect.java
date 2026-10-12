package com.bewitchment.potion;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import javax.annotation.Nullable;
import java.util.*;

public final class ArachnophobiaEffect extends TerrainBrewEffect {
    private static final Map<ServerWorld,ArachnophobiaRules.Budget> BUDGETS=new WeakHashMap<>();
    public ArachnophobiaEffect(){super(EffectType.HARMFUL,0xc0c0c0);}
    private static boolean targetAllowed(ServerWorld world,LivingEntity target){
        return target.level==world&&target.isAlive()&&world.getDifficulty()!=Difficulty.PEACEFUL
            &&!(target instanceof PlayerEntity&&(((PlayerEntity)target).isCreative()||target.isSpectator()));
    }
    private static boolean safe(ServerWorld world,SpiderEntity spider){
        AxisAlignedBB box=spider.getBoundingBox();
        return box.minY>=0&&box.maxY<=world.getMaxBuildHeight()
            &&world.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ))
            &&world.getWorldBorder().isWithinBounds(box)&&world.noCollision(spider)&&!world.containsAnyLiquid(box);
    }
    private static boolean locate(ServerWorld world,SpiderEntity spider,LivingEntity target){
        int x=MathHelper.floor(target.getX())+world.random.nextInt(24)-12;
        int z=MathHelper.floor(target.getZ())+world.random.nextInt(24)-12;
        int y=Math.min(254,MathHelper.floor(target.getY()));
        for(int step=0;step<=ArachnophobiaRules.DESCENT&&y-step>0;step++){
            BlockPos feet=new BlockPos(x,y-step,z),floor=feet.below();
            if(!world.hasChunkAt(floor)||!world.getWorldBorder().isWithinBounds(feet))return false;
            if(world.getBlockState(floor).isFaceSturdy(world,floor,Direction.UP)){
                spider.moveTo(x+.5,y-step,z+.5,world.random.nextFloat()*360F,0);
                return safe(world,spider);
            }
        }
        return false;
    }
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        ServerWorld world=(ServerWorld)target.level;if(!targetAllowed(world,target))return;
        ArachnophobiaRules.guarded(target.getUUID(),()->{
            ArachnophobiaRules.Budget budget=BUDGETS.computeIfAbsent(world,w->new ArachnophobiaRules.Budget());
            for(int i=0;i<ArachnophobiaRules.count(amplifier)&&targetAllowed(world,target)&&budget.mob(world.getGameTime());i++){
                SpiderEntity spider=world.random.nextBoolean()?EntityType.SPIDER.create(world):EntityType.CAVE_SPIDER.create(world);
                if(spider==null)continue;
                for(int attempt=0;attempt<ArachnophobiaRules.ATTEMPTS&&budget.position(world.getGameTime());attempt++){
                    if(!locate(world,spider,target))continue;
                    ArachnophobiaRules.spawn(new ArachnophobiaRules.SpawnSteps(){
                        public boolean valid(){return targetAllowed(world,target)&&spider.isAlive()&&spider.level==world&&safe(world,spider);}
                        public boolean permitted(){return ForgeEventFactory.canEntitySpawn(spider,world,spider.getX(),spider.getY(),spider.getZ(),null,SpawnReason.MOB_SUMMONED)!=Event.Result.DENY;}
                        public boolean special(){return ForgeEventFactory.doSpecialSpawn(spider,world,(float)spider.getX(),(float)spider.getY(),(float)spider.getZ(),null,SpawnReason.MOB_SUMMONED);}
                        public void initialize(){spider.finalizeSpawn(world,world.getCurrentDifficultyAt(spider.blockPosition()),SpawnReason.MOB_SUMMONED,null,null);}
                        public boolean insert(){
                            spider.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,ArachnophobiaRules.BUFF_TICKS,2));
                            spider.addEffect(new EffectInstance(Effects.REGENERATION,ArachnophobiaRules.BUFF_TICKS,1));
                            spider.addEffect(new EffectInstance(Effects.DAMAGE_BOOST,ArachnophobiaRules.BUFF_TICKS));
                            spider.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,ArachnophobiaRules.BUFF_TICKS));
                            spider.setTarget(target);
                            return valid()&&world.addFreshEntity(spider);
                        }
                    });
                    // Denied insertion is terminal; do not repeat callbacks at another position.
                    break;
                }
            }
        });
    }
    private static boolean actorAllowed(ServerWorld world,PlayerEntity actor){return BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        if(!actorAllowed(world,actor))return 0;
        return BrewTerrain.clear(center,amplifier,new BrewTerrain.Terrain(){
            public boolean loaded(BlockPos p){return world.hasChunkAt(p);}
            public boolean insideBorder(BlockPos p){return world.getWorldBorder().isWithinBounds(p);}
            public boolean eligible(BlockPos p){return ArachnophobiaRules.webCandidate(world.getBlockState(p));}
            public boolean mayEdit(BlockPos p){return actorAllowed(world,actor)&&world.mayInteract(actor,p)&&actor.mayUseItemAt(p,Direction.UP,ItemStack.EMPTY);}
            public boolean clear(BlockPos p){
                BlockState before=world.getBlockState(p);
                if(!ArachnophobiaRules.webCandidate(before)||MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,p,before,actor)))return false;
                if(!loaded(p)||!insideBorder(p)||!mayEdit(p)||!world.getBlockState(p).equals(before))return false;
                return world.setBlock(p,Blocks.COBWEB.defaultBlockState(),3);
            }
        });
    }
}