package com.bewitchment.entity;

import com.bewitchment.registry.ModClothing;
import com.bewitchment.registry.ModCreatures;
import com.bewitchment.combat.AccessoryEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

/** Port of the original Bafometyr by Joseph, including its six-tick fireball burst. */
public final class BafometyrEntity extends MonsterEntity {
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(BafometyrEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime,flameTimer,attackTimer;
    private UUID summoner;
    public BafometyrEntity(EntityType<? extends BafometyrEntity> type,World world) {
        super(type,world);xpReward=25;
        setPathfindingMalus(PathNodeType.LAVA,8);setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
    }
    public static AttributeModifierMap.MutableAttribute attributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,2)
            .add(Attributes.FOLLOW_RANGE,32).add(Attributes.MOVEMENT_SPEED,.66).add(Attributes.ARMOR,1)
            .add(Attributes.ARMOR_TOUGHNESS,1).add(Attributes.KNOCKBACK_RESISTANCE,.3);
    }
    @Override protected void defineSynchedData() {super.defineSynchedData();entityData.define(SPECTRAL,false);}
    public boolean spectral() {return entityData.get(SPECTRAL);}
    public int flameTimer() {return flameTimer;}
    public int attackTimer() {return attackTimer;}
    @Override public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);tag.putInt("skin",0);tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);entityData.set(SPECTRAL,tag.getBoolean("spectral"));
        lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target) {
        return target!=this && !target.getUUID().equals(summoner) && BafometyrRules.prey(target instanceof PlayerEntity,
            target instanceof HellhoundEntity || new ResourceLocation("bewitchment","cleaver").equals(target.getType().getRegistryName()),
            target.fireImmune(),AccessoryEffects.DEMONS.contains(target.getType()),target.getMobType()==CreatureAttribute.UNDEAD,ModClothing.hasSet(target,"besmirched"));
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.75,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.66*2/3));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target) {super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target) {
        // The original ignites before damage is attempted, even when that damage is rejected.
        if(!level.isClientSide && target instanceof LivingEntity) {swing(Hand.MAIN_HAND);target.setSecondsOnFire(25);}
        return super.doHurtTarget(target);
    }
    @Override public void handleEntityEvent(byte event) {if(event==4)attackTimer=40;else super.handleEntityEvent(event);}
    @Override public boolean causeFallDamage(float distance,float multiplier) {return false;}
    @Override public boolean canBreatheUnderwater() {return true;}
    @Override public boolean removeWhenFarAway(double distance) {return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize() {return 6;}
    @Override protected boolean shouldDropLoot() {return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player) {return spectral()?0:super.getExperienceReward(player);}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit) {if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override public void aiStep() {
        super.aiStep();flameTimer=BafometyrRules.flame(flameTimer+1);attackTimer=BafometyrRules.nextAttackTimer(attackTimer);
        if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0) {((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(summoner!=null && tickCount%60==0) {
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null) {LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=null && target!=this && target.isAlive())setTarget(target);}
        }
        LivingEntity target=getTarget();
        if(isAlive() && target!=null && target.isAlive() && BafometyrRules.fires(tickCount,distanceToSqr(target))) {
            double spread=BafometyrRules.spread(distanceToSqr(target));
            SmallFireballEntity fireball=new SmallFireballEntity(level,this,target.getX()-getX()+random.nextGaussian()*spread,
                target.getY()+target.getBbHeight()/2.0-(getY()+getBbHeight()/2.0),target.getZ()-getZ()+random.nextGaussian()*spread);
            fireball.setPos(getX(),getY()+getBbHeight()/2.0+.5,getZ());
            if(level.addFreshEntity(fireball)) {
                level.levelEvent(null,1018,blockPosition(),0);attackTimer=40;level.broadcastEntityEvent(this,(byte)4);
            }
        }
    }
    @Override protected SoundEvent getAmbientSound() {return ModCreatures.BAFOMETYR_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return ModCreatures.BAFOMETYR_HURT.get();}
    @Override protected SoundEvent getDeathSound() {return ModCreatures.BAFOMETYR_DEATH.get();}
}
