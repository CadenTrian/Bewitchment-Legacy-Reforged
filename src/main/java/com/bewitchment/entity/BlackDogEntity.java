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

public final class BlackDogEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(BlackDogEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(BlackDogEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public BlackDogEntity(EntityType<? extends BlackDogEntity> type,World world){
        super(type,world);xpReward=35;


    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,15).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,24)
        .add(Attributes.MOVEMENT_SPEED,.5).add(Attributes.ARMOR,2.25).add(Attributes.KNOCKBACK_RESISTANCE,1);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return BlackDogRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){entityData.set(SKIN,random.nextInt(5));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,BlackDogRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private int attackTimer;
    public int attackTimer(){return attackTimer;}
    private boolean prey(LivingEntity target){
        return target!=this && !target.getUUID().equals(summoner) && (target instanceof net.minecraft.entity.merchant.villager.VillagerEntity
            || target instanceof net.minecraft.entity.monster.AbstractIllagerEntity || target instanceof net.minecraft.entity.monster.WitchEntity
            || target instanceof net.minecraft.entity.passive.IronGolemEntity || target instanceof net.minecraft.entity.passive.SheepEntity
            || target instanceof net.minecraft.entity.passive.CowEntity || target instanceof net.minecraft.entity.passive.ChickenEntity
            || target instanceof net.minecraft.entity.passive.horse.LlamaEntity || target instanceof net.minecraft.entity.passive.PigEntity
            || target instanceof net.minecraft.entity.passive.RabbitEntity || target instanceof net.minecraft.entity.passive.horse.AbstractHorseEntity);
    }
    @Override protected net.minecraft.pathfinding.PathNavigator createNavigation(World world){
        net.minecraft.pathfinding.GroundPathNavigator path=new net.minecraft.pathfinding.GroundPathNavigator(this,world);
        path.setCanOpenDoors(true);return path;
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.5*2/3));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,false));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());

        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity){
            attackTimer=5;level.broadcastEntityEvent(this,(byte)4);

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
        if(BlackDogRules.vanishes(level.isDay(),level.isRaining(),hasCustomName())){remove();return;}
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState state){}
    @Override public boolean hurt(DamageSource source,float damage){
        boolean hit=super.hurt(source,damage);
        if(hit && !level.isClientSide && isAlive()){
            addEffect(new EffectInstance(Effects.INVISIBILITY,50,1,false,false));
            addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,50,1,false,false));
        }
        return hit;
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.WOLF_GROWL;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.WOLF_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.WOLF_DEATH;}
}
