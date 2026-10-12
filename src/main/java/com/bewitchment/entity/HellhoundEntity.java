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

public final class HellhoundEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(HellhoundEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(HellhoundEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public HellhoundEntity(EntityType<? extends HellhoundEntity> type,World world){
        super(type,world);xpReward=20;
        setPathfindingMalus(PathNodeType.WATER,-1);setPathfindingMalus(PathNodeType.LAVA,8);
        setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32)
        .add(Attributes.MOVEMENT_SPEED,.95).add(Attributes.ARMOR,2.25).add(Attributes.ARMOR_TOUGHNESS,1);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return CreatureRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){entityData.set(SKIN,random.nextInt(4));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,CreatureRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private int attackTimer;
    public int attackTimer(){return attackTimer;}
    private boolean prey(LivingEntity target){
        boolean special=target instanceof PlayerEntity || target instanceof FeuerwurmEntity
            || target instanceof net.minecraft.entity.passive.SheepEntity || target instanceof net.minecraft.entity.passive.CowEntity
            || target instanceof net.minecraft.entity.passive.ChickenEntity || target instanceof net.minecraft.entity.passive.horse.LlamaEntity
            || target instanceof net.minecraft.entity.passive.PigEntity || target instanceof net.minecraft.entity.passive.RabbitEntity
            || target instanceof net.minecraft.entity.passive.horse.AbstractHorseEntity;
        return target!=this && !target.getUUID().equals(summoner) && HellhoundRules.prey(special,target.fireImmune(),
            AccessoryEffects.DEMONS.contains(target.getType()),target.getMobType()==CreatureAttribute.UNDEAD,ModClothing.hasSet(target,"besmirched"));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.95*2/3));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        goalSelector.addGoal(7,new WaterAvoidingRandomWalkingGoal(this,1,0));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity){
            attackTimer=5;level.broadcastEntityEvent(this,(byte)4);
            ((LivingEntity)target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,100,1,false,false));target.setSecondsOnFire(3);
        }
        return hit;
    }
    @Override public void handleEntityEvent(byte event){if(event==4)attackTimer=5;else super.handleEntityEvent(event);}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}

    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override public void aiStep(){
        super.aiStep();attackTimer=HellhoundRules.nextAttackTimer(attackTimer);if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(isInWaterRainOrBubble()){
            if(hurt(DamageSource.MAGIC,2.5F)){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.5,.4,.5,0);playSound(SoundEvents.FIRE_EXTINGUISH,1,1);}
        }else if(FeuerwurmRules.lavaHeals(tickCount,false,isInLava()))heal(2);
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.WOLF_GROWL;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.WOLF_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.WOLF_DEATH;}
}
