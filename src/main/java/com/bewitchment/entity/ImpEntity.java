package com.bewitchment.entity;

import com.bewitchment.registry.ModClothing;
import com.bewitchment.combat.AccessoryEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

public final class ImpEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(ImpEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(ImpEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public ImpEntity(EntityType<? extends ImpEntity> type,World world){
        super(type,world);xpReward=75;
        setPathfindingMalus(PathNodeType.WATER,-1);setPathfindingMalus(PathNodeType.LAVA,8);
        setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,85).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,16)
        .add(Attributes.MOVEMENT_SPEED,.6).add(Attributes.ARMOR,5).add(Attributes.ARMOR_TOUGHNESS,2.5).add(Attributes.KNOCKBACK_RESISTANCE,.5);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return ImpRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){entityData.set(SKIN,random.nextInt(7));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,ImpRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private int attackTimer;
    public int attackTimer(){return attackTimer;}
    private boolean prey(LivingEntity target){
        return target!=this && !target.getUUID().equals(summoner) && ImpRules.prey(target instanceof PlayerEntity,
            target instanceof HellhoundEntity || target instanceof FeuerwurmEntity,distanceToSqr(target),ModClothing.hasSet(target,"besmirched"));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.4));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,10,false,false,this::prey));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());

        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->!(e instanceof PlayerEntity) && prey(e)));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity){
            attackTimer=10;level.broadcastEntityEvent(this,(byte)4);
            ((LivingEntity)target).addEffect(new EffectInstance(Effects.CONFUSION,100,1,false,false));target.setSecondsOnFire(20);
            target.setDeltaMovement(target.getDeltaMovement().add(0,.2,0));target.hasImpulse=true;
            if(target instanceof net.minecraft.entity.player.ServerPlayerEntity)((net.minecraft.entity.player.ServerPlayerEntity)target).connection.send(new net.minecraft.network.play.server.SEntityVelocityPacket(target));
        }
        return hit;
    }
    @Override public void handleEntityEvent(byte event){if(event==4)attackTimer=10;else super.handleEntityEvent(event);}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}

    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}

    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public void aiStep(){
        super.aiStep();attackTimer=ImpRules.nextAttackTimer(attackTimer);if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(ImpRules.lavaHeals(tickCount,isInLava()))heal(4);
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.BLAZE_AMBIENT;}

    @Override protected SoundEvent getDeathSound(){return SoundEvents.BLAZE_DEATH;}
}
