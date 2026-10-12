package com.bewitchment.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

/** Legacy temporary demon. Its four skin values intentionally share two textures. */
public final class ShadowPersonEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(ShadowPersonEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(ShadowPersonEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public ShadowPersonEntity(EntityType<? extends ShadowPersonEntity> type,World world) {
        super(type,world);xpReward=45;
        lifeTime=ShadowPersonRules.lifetime(random.nextInt(31));
        // Enable the legacy door-breaking goal's required ground navigation capability.
        ((net.minecraft.pathfinding.GroundPathNavigator)getNavigation()).setCanOpenDoors(true);
    }
    public static AttributeModifierMap.MutableAttribute attributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH,45).add(Attributes.ATTACK_DAMAGE,4)
            .add(Attributes.FOLLOW_RANGE,16).add(Attributes.MOVEMENT_SPEED,.65).add(Attributes.ARMOR,2.22)
            .add(Attributes.ARMOR_TOUGHNESS,.5).add(Attributes.KNOCKBACK_RESISTANCE,.9);
    }
    @Override protected void defineSynchedData() {super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,true);}
    public int skin() {return ShadowPersonRules.skin(entityData.get(SKIN));}
    public boolean spectral() {return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag) {
        entityData.set(SKIN,random.nextInt(4));return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    @Override public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);entityData.set(SKIN,ShadowPersonRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,ShadowPersonRules.savedSpectral(tag));
        lifeTime=ShadowPersonRules.savedLifetime(tag,lifeTime);
        summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target) {
        return target!=this && !target.getUUID().equals(summoner) && ShadowPersonRules.prey(target instanceof PlayerEntity,
            target instanceof VillagerEntity,target instanceof AbstractIllagerEntity,target instanceof WitchEntity,target instanceof IronGolemEntity);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new SwimGoal(this));
        goalSelector.addGoal(0,new BreakDoorGoal(this,difficulty->difficulty==Difficulty.HARD) {
            @Override public boolean canUse() {return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && super.canUse();}
            @Override public boolean canContinueToUse() {return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && net.minecraftforge.common.ForgeHooks.canEntityDestroy(level,doorPos,ShadowPersonEntity.this) && super.canContinueToUse();}
        });
        goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(3,new RandomWalkingGoal(this,.65*2/3));
        goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,10,false,false,this::prey));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->!(e instanceof PlayerEntity) && prey(e)));
    }
    @Override public void setTarget(LivingEntity target) {super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean causeFallDamage(float distance,float multiplier) {return false;}
    @Override public boolean canBreatheUnderwater() {return true;}
    @Override public boolean removeWhenFarAway(double distance) {return !hasCustomName();}
    @Override protected boolean shouldDropLoot() {return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player) {return spectral()?0:super.getExperienceReward(player);}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit) {if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override public void aiStep() {
        super.aiStep();if(level.isClientSide)return;
        if(ShadowPersonRules.expired(spectral(),lifeTime)) {
            ((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;
        }
        if(spectral())lifeTime--;
        if(summoner!=null && tickCount%60==0) {
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null) {
                LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();
                if(target!=null && target!=this && target.isAlive())setTarget(target);
            }
        }
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) {return SoundEvents.ENDERMAN_SCREAM;}
    @Override protected SoundEvent getDeathSound() {return SoundEvents.ENDERMAN_DEATH;}
    @Override protected void playStepSound(BlockPos pos,BlockState state) {}
}
