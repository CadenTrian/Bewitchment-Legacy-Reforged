package com.bewitchment.entity;

import com.bewitchment.registry.ModClothing;
import com.bewitchment.combat.AccessoryEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.MonsterEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

public final class DrudenEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(DrudenEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(DrudenEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public DrudenEntity(EntityType<? extends DrudenEntity> type,World world){
        super(type,world);xpReward=45;
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,24)
        .add(Attributes.MOVEMENT_SPEED,.8).add(Attributes.ARMOR,4.14).add(Attributes.ARMOR_TOUGHNESS,2);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return CreatureRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        entityData.set(SKIN,random.nextInt(4));data=super.finalizeSpawn(world,difficulty,reason,data,tag);
        int roll=random.nextInt(12);
        if(roll<2)setItemSlot(net.minecraft.inventory.EquipmentSlotType.MAINHAND,new net.minecraft.item.ItemStack(com.bewitchment.registry.ModItems.THYRSUS.get()));
        else if(roll>10)populateDefaultEquipmentSlots(difficulty);
        setCanPickUpLoot(random.nextFloat()<.35F*difficulty.getSpecialMultiplier());
        populateDefaultEquipmentEnchantments(difficulty);return data;
    }
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,CreatureRules.skin(tag.getInt("skin")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target){
        boolean explicit=target instanceof net.minecraft.entity.merchant.villager.VillagerEntity || target instanceof net.minecraft.entity.monster.EndermanEntity || target instanceof net.minecraft.entity.passive.GolemEntity;
        return target!=this && !target.getUUID().equals(summoner) && DrudenRules.prey(target instanceof PlayerEntity,explicit,target.fireImmune(),target.getType().is(AccessoryEffects.DEMONS),target.getMobType()==CreatureAttribute.UNDEAD,distanceToSqr(target),ModClothing.hasSet(target,"besmirched") || ModClothing.hasSet(target,"green_witch"));
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.8*2/3));
        goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,10,false,false,this::prey));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());

        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->!(e instanceof PlayerEntity) && prey(e)));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity){
            playSound(SoundEvents.GHAST_SCREAM,1.3F,1);
            ((LivingEntity)target).addEffect(new EffectInstance(com.bewitchment.registry.ModEffects.FEAR.get(),500,0,false,false));
        }
        return hit;
    }
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}

    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}

    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        int healing=DrudenRules.waterHealing(tickCount,isInWater(),isInWaterRainOrBubble());
        if(healing>0)heal(healing);
        if(DrudenRules.recovery(tickCount,getHealth()<getMaxHealth())){
            heal(2);addEffect(new EffectInstance(Effects.REGENERATION,80,2));
            playSound(SoundEvents.GRASS_PLACE,3,1);
        }
        if(isOnFire() && hurt(DamageSource.ON_FIRE,6.66F)){
            ((ServerWorld)level).sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+getBbHeight()/2,getZ(),20,getBbWidth()/2,getBbHeight()/2,getBbWidth()/2,0);
            playSound(SoundEvents.GHAST_HURT,1.3F,1);
        }
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize(){return 2;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState state){}
    @Override public boolean hurt(DamageSource source,float damage){
        Entity direct=source.getDirectEntity();
        return super.hurt(source,DrudenRules.axeDamage(damage,direct instanceof LivingEntity && ((LivingEntity)direct).getMainHandItem().getItem() instanceof net.minecraft.item.AxeItem));
    }
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.GHAST_SCREAM;}

    @Override protected SoundEvent getDeathSound(){return SoundEvents.GHAST_DEATH;}
}
