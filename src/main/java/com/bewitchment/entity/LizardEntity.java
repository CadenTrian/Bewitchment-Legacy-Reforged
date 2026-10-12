package com.bewitchment.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import com.bewitchment.registry.ModCreatures;

public final class LizardEntity extends AnimalEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(LizardEntity.class,DataSerializers.INT);
    public LizardEntity(EntityType<? extends LizardEntity> type,World world){super(type,world);xpReward=5;}
    public static AttributeModifierMap.MutableAttribute attributes(){return AnimalEntity.createMobAttributes().add(Attributes.MAX_HEALTH,10).add(Attributes.MOVEMENT_SPEED,.6).add(Attributes.FOLLOW_RANGE,6);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new PanicGoal(this,.6));
        goalSelector.addGoal(0,new SwimGoal(this));
        goalSelector.addGoal(1,new BreedGoal(this,.3));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(2,new FollowParentGoal(this,.6));
        goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(4,new RandomWalkingGoal(this,.4));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.5));
    }
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);}
    public int skin(){return CreatureRules.skin(entityData.get(SKIN));}
    public void skin(int value){entityData.set(SKIN,CreatureRules.skin(value));}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);skin(tag.getInt("skin"));}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        skin(random.nextInt(4));return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    @Override public LizardEntity getBreedOffspring(ServerWorld world,AgeableEntity other){
        LizardEntity child=ModCreatures.LIZARD.get().create(world);
        if(child!=null)child.skin(random.nextBoolean()?skin():other instanceof LizardEntity?((LizardEntity)other).skin():skin());
        return child;
    }
    @Override public boolean isFood(ItemStack stack){return stack.getItem()==Items.SPIDER_EYE;}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override public void aiStep(){super.aiStep();if(!level.isClientSide && getHealth()<getMaxHealth() && CreatureRules.lizardHeals(tickCount))heal(2);}
}
