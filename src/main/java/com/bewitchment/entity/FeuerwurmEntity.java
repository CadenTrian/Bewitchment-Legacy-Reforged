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

public final class FeuerwurmEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(FeuerwurmEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(FeuerwurmEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public FeuerwurmEntity(EntityType<? extends FeuerwurmEntity> type,World world){
        super(type,world);xpReward=20;
        setPathfindingMalus(PathNodeType.WATER,-1);setPathfindingMalus(PathNodeType.LAVA,8);
        setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,15).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32)
        .add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ARMOR,1.5).add(Attributes.ARMOR_TOUGHNESS,.25);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return FeuerwurmRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    public void summon(UUID owner,int ticks,int variant){
        summoner=owner;lifeTime=Math.max(0,ticks);entityData.set(SPECTRAL,true);entityData.set(SKIN,FeuerwurmRules.skin(variant));
        setTarget(null);
    }
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){entityData.set(SKIN,random.nextInt(9));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,FeuerwurmRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target){
        ResourceLocation id=target.getType().getRegistryName();
        return target!=this && !target.getUUID().equals(summoner) && FeuerwurmRules.prey(target instanceof AnimalEntity,
            new ResourceLocation("bewitchment","hellhound").equals(id),target.fireImmune(),AccessoryEffects.DEMONS.contains(target.getType()),
            target.getMobType()==CreatureAttribute.UNDEAD,ModClothing.hasSet(target,"besmirched"));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.2));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,10,false,false,e->distanceToSqr(e)<2 && prey(e)));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit && target instanceof LivingEntity)((LivingEntity)target).addEffect(new EffectInstance(Effects.WITHER,100,0,false,false));return hit;}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState state){}
    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(isInWaterRainOrBubble()){
            if(hurt(DamageSource.MAGIC,2.5F)){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.5,.4,.5,0);playSound(SoundEvents.FIRE_EXTINGUISH,1,1);}
        }else if(FeuerwurmRules.lavaHeals(tickCount,false,isInLava()))heal(2);
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.BLAZE_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.ENDER_DRAGON_GROWL;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.BLAZE_DEATH;}
}
