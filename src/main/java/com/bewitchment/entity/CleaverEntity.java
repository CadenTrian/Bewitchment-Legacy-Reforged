package com.bewitchment.entity;

import com.bewitchment.registry.*;
import com.bewitchment.combat.AccessoryEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

/** Port of Joseph's legacy Cleaver, with server-authoritative combat and safe player packets. */
public final class CleaverEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(CleaverEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(CleaverEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime,attackTimer;
    private UUID summoner;
    public CleaverEntity(EntityType<? extends CleaverEntity> type,World world){
        super(type,world);xpReward=25;setPathfindingMalus(PathNodeType.LAVA,8);
        setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,40).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32)
        .add(Attributes.MOVEMENT_SPEED,.55).add(Attributes.ARMOR,3.5).add(Attributes.ARMOR_TOUGHNESS,2.5).add(Attributes.KNOCKBACK_RESISTANCE,.5);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return CleaverRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    public int attackTimer(){return attackTimer;}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        data=super.finalizeSpawn(world,difficulty,reason,data,tag);entityData.set(SKIN,random.nextInt(2));
        populateDefaultEquipmentSlots(difficulty);populateDefaultEquipmentEnchantments(difficulty);return data;
    }
    @Override protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty){
        super.populateDefaultEquipmentSlots(difficulty);
        setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(ModItems.CLEAVER_SWORD.get()));
        setItemSlot(EquipmentSlotType.HEAD,new ItemStack(Items.GOLDEN_HELMET));
        setItemSlot(EquipmentSlotType.CHEST,new ItemStack(Items.GOLDEN_CHESTPLATE));
    }
    @Override protected void pickUpItem(net.minecraft.entity.item.ItemEntity item){if(CambionRules.canPickUp(item.getItem().getItem()))super.pickUpItem(item);}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,CleaverRules.skin(tag.getInt("skin")));entityData.set(SPECTRAL,tag.getBoolean("spectral"));
        lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private boolean prey(LivingEntity target){return target!=this && !target.getUUID().equals(summoner) && BafometyrRules.prey(target instanceof PlayerEntity,
        target instanceof FeuerwurmEntity || target instanceof HellhoundEntity || target instanceof BafometyrEntity,target.fireImmune(),
        AccessoryEffects.DEMONS.contains(target.getType()),target.getMobType()==CreatureAttribute.UNDEAD,ModClothing.hasSet(target,"besmirched"));}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.75,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.55*2/3));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        if(level.isClientSide)return false;
        boolean hit=target.hurt(DamageSource.mobAttack(this),CleaverRules.damage(getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if(hit){setLastHurtMob(target);if(getTarget()!=null){
            doEnchantDamageEffects(this,target);int roll=random.nextInt(100);
            if(target instanceof LivingEntity){attackTimer=20;level.broadcastEntityEvent(this,(byte)4);
                if(CleaverRules.cripples(roll)){
                    ((LivingEntity)target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,500,3,false,false));
                    target.setDeltaMovement(target.getDeltaMovement().add(0,0,-1.5));target.hasImpulse=true;
                    if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new net.minecraft.network.play.server.SEntityVelocityPacket(target));
                }
            }
        }}return hit;
    }
    @Override public void handleEntityEvent(byte event){if(event==4)attackTimer=20;else if(event==5)attackTimer=Integer.MAX_VALUE-1;else super.handleEntityEvent(event);}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean removeWhenFarAway(double distance){return !hasCustomName();}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);}
    @Override public void aiStep(){
        super.aiStep();attackTimer=BafometyrRules.nextAttackTimer(attackTimer);if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(isAlive() && CleaverRules.enrages(tickCount,getHealth(),getMaxHealth())){
            addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,2000,0));addEffect(new EffectInstance(Effects.DAMAGE_BOOST,2000,0));
            level.playSound(null,blockPosition(),ModCreatures.CLEAVER_IDLE.get(),SoundCategory.HOSTILE,1,1);
            attackTimer=Integer.MAX_VALUE-1;level.broadcastEntityEvent(this,(byte)5);
        }
        if(summoner!=null && tickCount%60==0){PlayerEntity owner=level.getPlayerByUUID(summoner);if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=null && target!=this && target.isAlive())setTarget(target);}}
    }
    @Override protected SoundEvent getAmbientSound(){return ModCreatures.CLEAVER_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModCreatures.CLEAVER_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModCreatures.CLEAVER_DEATH.get();}
}
