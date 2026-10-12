package com.bewitchment.entity;

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

public final class CambionEntity extends MonsterEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(CambionEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> SPECTRAL=EntityDataManager.defineId(CambionEntity.class,DataSerializers.BOOLEAN);
    private int lifeTime;
    private UUID summoner;
    public CambionEntity(EntityType<? extends CambionEntity> type,World world){
        super(type,world);xpReward=25;
        ((net.minecraft.pathfinding.GroundPathNavigator)getNavigation()).setCanOpenDoors(true);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return MonsterEntity.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH,35).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32)
        .add(Attributes.MOVEMENT_SPEED,.6).add(Attributes.ARMOR,1).add(Attributes.ARMOR_TOUGHNESS,1).add(Attributes.KNOCKBACK_RESISTANCE,.2);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(SPECTRAL,false);}
    public int skin(){return CreatureRules.skin(entityData.get(SKIN));}
    public boolean spectral(){return entityData.get(SPECTRAL);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        data=super.finalizeSpawn(world,difficulty,reason,data,tag);
        int variant=random.nextInt(4),a=random.nextInt(6),b=random.nextInt(8),c=random.nextInt(4),d=random.nextInt(8);
        if(data instanceof GroupData)variant=((GroupData)data).variant;else data=new GroupData(variant);
        entityData.set(SKIN,CreatureRules.skin(variant));
        net.minecraft.item.Item chest=CambionRules.chest(a),weapon=CambionRules.weapon(b,d);
        if(chest!=net.minecraft.item.Items.AIR)setItemSlot(net.minecraft.inventory.EquipmentSlotType.CHEST,new net.minecraft.item.ItemStack(chest));
        if(weapon!=net.minecraft.item.Items.AIR)setItemSlot(net.minecraft.inventory.EquipmentSlotType.MAINHAND,new net.minecraft.item.ItemStack(weapon));
        if(c<2)setItemSlot(net.minecraft.inventory.EquipmentSlotType.OFFHAND,new net.minecraft.item.ItemStack(net.minecraft.item.Items.SHIELD));
        else if(c>2)populateDefaultEquipmentSlots(difficulty);
        setCanPickUpLoot(random.nextFloat()<.55F*difficulty.getSpecialMultiplier());
        populateDefaultEquipmentEnchantments(difficulty);
        return data;
    }
    public static final class GroupData implements ILivingEntityData {
        final int variant;
        public GroupData(int variant){this.variant=CreatureRules.skin(variant);}
    }
    @Override protected void pickUpItem(net.minecraft.entity.item.ItemEntity item){
        if(CambionRules.canPickUp(item.getItem().getItem()))super.pickUpItem(item);
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("minecraft","entities/witch");}
    @Override public void addAdditionalSaveData(CompoundNBT tag){
        super.addAdditionalSaveData(tag);tag.putInt("CambionType",skin());tag.putBoolean("spectral",spectral());
        tag.putInt("lifeTimeTick",lifeTime);tag.putString("summoner",summoner==null?"":summoner.toString());
    }
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,CreatureRules.skin(tag.getInt("CambionType")));
        entityData.set(SPECTRAL,tag.getBoolean("spectral"));lifeTime=Math.max(0,tag.getInt("lifeTimeTick"));summoner=FeuerwurmRules.summoner(tag.getString("summoner"));
    }
    private int attackTimer;
    public int attackTimer(){return attackTimer;}
    private boolean prey(LivingEntity target){
        return target!=this && !target.getUUID().equals(summoner) && CambionRules.prey(target);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.35,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.4));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));

        goalSelector.addGoal(3,new OpenDoorGoal(this,true));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));

        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,true,false,this::prey));
    }
    @Override public void setTarget(LivingEntity target){super.setTarget(target!=null && target.getUUID().equals(summoner)?null:target);}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity){
            attackTimer=10;level.broadcastEntityEvent(this,(byte)4);
            int effect=CambionRules.attackEffect(random.nextInt(100),random.nextInt(100));
            if(effect==1)target.setSecondsOnFire(25);
            else if(effect==2)((LivingEntity)target).addEffect(new EffectInstance(Effects.POISON,500,0,false,true));
        }
        return hit;
    }
    @Override public void handleEntityEvent(byte event){if(event==4)attackTimer=10;else super.handleEntityEvent(event);}

    @Override protected boolean shouldDropLoot(){return !spectral() && super.shouldDropLoot();}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){
        if(!spectral())super.dropCustomDeathLoot(source,looting,recentlyHit);
    }
    @Override protected int getExperienceReward(PlayerEntity player){return spectral()?0:super.getExperienceReward(player);}

    @Override public void aiStep(){
        super.aiStep();attackTimer=ImpRules.nextAttackTimer(attackTimer);if(level.isClientSide)return;
        if(spectral() && lifeTime--<=0){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.3,.3,.3,0);remove();return;}
        if(summoner!=null && tickCount%60==0){
            PlayerEntity owner=level.getPlayerByUUID(summoner);
            if(owner!=null){LivingEntity target=owner.getLastHurtByMob()!=null?owner.getLastHurtByMob():owner.getLastHurtMob();if(target!=this && target!=null && target.isAlive())setTarget(target);}
        }
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.EVOKER_AMBIENT;}

    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.EVOKER_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.EVOKER_DEATH;}
}
