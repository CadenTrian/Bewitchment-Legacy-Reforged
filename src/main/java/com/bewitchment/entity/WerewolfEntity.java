package com.bewitchment.entity;



import net.minecraft.entity.*;
import net.minecraft.entity.monster.MonsterEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;

import net.minecraft.particles.ParticleTypes;

import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

public final class WerewolfEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(WerewolfEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(WerewolfEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public WerewolfEntity(EntityType<? extends WerewolfEntity> type,World world){
        super(type,world);xpReward=35;


    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,55).add(Attributes.ATTACK_DAMAGE,2.5).add(Attributes.FOLLOW_RANGE,32)
        .add(Attributes.MOVEMENT_SPEED,.8).add(Attributes.ARMOR,3).add(Attributes.ARMOR_TOUGHNESS,1).add(Attributes.KNOCKBACK_RESISTANCE,.5);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return WerewolfRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        data=super.finalizeSpawn(world,difficulty,reason,data,tag);
        net.minecraft.util.RegistryKey<net.minecraft.world.biome.Biome> key=world.getLevel().registryAccess().registryOrThrow(net.minecraft.util.registry.Registry.BIOME_REGISTRY).getResourceKey(world.getBiome(blockPosition())).orElse(null);
        int variant=WerewolfRules.variant(hasBiome(key,net.minecraftforge.common.BiomeDictionary.Type.SNOWY),hasBiome(key,net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS),hasBiome(key,net.minecraftforge.common.BiomeDictionary.Type.SWAMP),random.nextBoolean());
        if(data instanceof GroupData)variant=((GroupData)data).variant;else data=new GroupData(variant);
        entityData.set(SKIN,WerewolfRules.skin(variant));return data;
    }
    private static boolean hasBiome(net.minecraft.util.RegistryKey<net.minecraft.world.biome.Biome> key,net.minecraftforge.common.BiomeDictionary.Type type){return key!=null && net.minecraftforge.common.BiomeDictionary.hasType(key,type);}
    public static final class GroupData implements ILivingEntityData {
        final int variant;
        public GroupData(int variant){this.variant=WerewolfRules.skin(variant);}
    }
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("WerewolfType",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,WerewolfRules.skin(tag.getInt("WerewolfType")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target){
        return target!=this && !target.getUUID().equals(summoner) && (target instanceof net.minecraft.entity.merchant.villager.VillagerEntity
            || target instanceof net.minecraft.entity.monster.AbstractIllagerEntity || target instanceof net.minecraft.entity.monster.WitchEntity
            || target instanceof net.minecraft.entity.passive.IronGolemEntity || target instanceof net.minecraft.entity.passive.SheepEntity
            || target instanceof net.minecraft.entity.passive.CowEntity || target instanceof net.minecraft.entity.passive.ChickenEntity
            || target instanceof net.minecraft.entity.passive.horse.LlamaEntity || target instanceof net.minecraft.entity.passive.PigEntity
            || target instanceof CambionEntity || target instanceof net.minecraft.entity.passive.RabbitEntity || target instanceof net.minecraft.entity.passive.horse.AbstractHorseEntity);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.8*2/3));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,false));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());

        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=target.hurt(DamageSource.mobAttack(this),WerewolfRules.damage(getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if(hit){doEnchantDamageEffects(this,target);setLastHurtMob(target);}return hit;
    }
    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(WerewolfRules.vanishes(level.isDay(),hasCustomName())){remove();return;}
        if(WerewolfRules.heals(tickCount,getHealth()<getMaxHealth()))heal(4);
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override protected float getSoundVolume(){return 5;}
    @Override public int getAmbientSoundInterval(){return 2000;}
    @Override protected SoundEvent getAmbientSound(){return com.bewitchment.registry.ModCreatures.WEREWOLF_HOWL.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.WOLF_GROWL;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.WOLF_DEATH;}
}
