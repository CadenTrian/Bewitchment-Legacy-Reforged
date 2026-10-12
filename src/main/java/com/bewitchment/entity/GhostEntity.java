package com.bewitchment.entity;

import com.bewitchment.block.CandleBlock;
import com.bewitchment.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

/** Legacy Ghost by Joseph: phasing flight, charge attacks and periodic local ward checks. */
public final class GhostEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(GhostEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(GhostEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    private boolean charging;
    public GhostEntity(EntityType<? extends GhostEntity> type,World world){super(type,world);xpReward=35;moveControl=new GhostMoveControl();setNoGravity(true);noPhysics=true;}
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,10).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,16)
        .add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FLYING_SPEED,.25).add(Attributes.KNOCKBACK_RESISTANCE,1);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return GhostRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){entityData.set(SKIN,random.nextInt(5));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);entityData.set(SKIN,GhostRules.skin(tag.getInt("skin")));entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));setNoGravity(true);noPhysics=true;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new ChargeGoal());goalSelector.addGoal(3,new WanderGoal());
        goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,3,1));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,10,true,false,e->!e.getUUID().equals(summoner)));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->!e.getUUID().equals(summoner) && (e instanceof VillagerEntity || e instanceof AbstractIllagerEntity || e instanceof WitchEntity || e instanceof IronGolemEntity)));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    /** At most 17 cubed loaded block reads per second, with early exit on a ward. */
    private boolean warded(){
        BlockPos center=blockPosition();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-8,-8,-8),center.offset(8,8,8))){
            if(pos.getY()<0 || pos.getY()>=256 || !level.hasChunkAt(pos))continue;
            BlockState state=level.getBlockState(pos);
            boolean candle=state.getBlock() instanceof CandleBlock && ((CandleBlock)state.getBlock()).isCandelabra() && state.getValue(CandleBlock.LIT);
            if(GhostRules.ward(pos.getX()-center.getX(),pos.getY()-center.getY(),pos.getZ()-center.getZ(),state.is(ModBlocks.SALT_BARRIER.get()),candle))return true;
        }
        return false;
    }
    private boolean loaded(AxisAlignedBB box){return level.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ));}
    @Override public void travel(Vector3d input){
        setNoGravity(true);noPhysics=true;
        if(!level.isClientSide && !loaded(getBoundingBox().move(getDeltaMovement())))setDeltaMovement(Vector3d.ZERO);
        super.travel(input);
    }
    @Override public boolean onClimbable(){return false;}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(BlackDogRules.vanishes(level.isDay(),level.isRaining(),hasCustomName())){remove();return;}
        if(summoner!=null && tickCount%60==0){PlayerEntity owner=level.getPlayerByUUID(summoner);if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=null && target!=this && target.isAlive())setTarget(target);}}
        if(tickCount%20==0 && warded())setTarget(null);
    }
    private final class GhostMoveControl extends MovementController {
        GhostMoveControl(){super(GhostEntity.this);}
        @Override public void tick(){
            if(operation!=Action.MOVE_TO)return;
            Vector3d delta=new Vector3d(wantedX-getX(),wantedY-getY(),wantedZ-getZ());double distance=delta.length();
            if(!level.hasChunkAt(new BlockPos(wantedX,wantedY,wantedZ))){operation=Action.WAIT;setDeltaMovement(Vector3d.ZERO);return;}
            if(distance<getBoundingBox().getSize()){operation=Action.WAIT;setDeltaMovement(getDeltaMovement().scale(.5));}
            else {
                setDeltaMovement(getDeltaMovement().add(delta.scale(.05*speedModifier/distance)));
                LivingEntity target=getTarget();double x=target==null?getDeltaMovement().x:target.getX()-getX(),z=target==null?getDeltaMovement().z:target.getZ()-getZ();
                yRot=-(float)MathHelper.atan2(x,z)*57.295776F;yBodyRot=yRot;
            }
        }
    }
    private final class WanderGoal extends Goal {
        WanderGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){return !moveControl.hasWanted() && random.nextInt(7)==0;}
        @Override public boolean canContinueToUse(){return false;}
        @Override public void tick(){
            for(int i=0;i<3;i++){
                BlockPos pos=blockPosition().offset(random.nextInt(15)-7,random.nextInt(7)-3,random.nextInt(15)-7);
                if(level.hasChunkAt(pos) && level.isEmptyBlock(pos)){
                    moveControl.setWantedPosition(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,.25);
                    if(getTarget()==null)getLookControl().setLookAt(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,180,20);break;
                }
            }
        }
    }
    private final class ChargeGoal extends Goal {
        ChargeGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){LivingEntity target=getTarget();return target!=null && !moveControl.hasWanted() && random.nextInt(7)==0 && GhostRules.charge(distanceToSqr(target));}
        @Override public boolean canContinueToUse(){return moveControl.hasWanted() && charging && getTarget()!=null && getTarget().isAlive();}
        @Override public void start(){LivingEntity target=getTarget();if(target!=null){Vector3d eyes=target.getEyePosition(1);moveControl.setWantedPosition(eyes.x,eyes.y,eyes.z,1);charging=true;}}
        @Override public void stop(){charging=false;}
        @Override public void tick(){
            LivingEntity target=getTarget();if(target==null)return;
            double distance=distanceToSqr(target);
            if(distance<=GhostRules.reach(getBbWidth(),target.getBbWidth())){doHurtTarget(target);charging=false;}
            else if(GhostRules.retarget(distance)){Vector3d eyes=target.getEyePosition(1);moveControl.setWantedPosition(eyes.x,eyes.y,eyes.z,1);}
        }
    }
}
