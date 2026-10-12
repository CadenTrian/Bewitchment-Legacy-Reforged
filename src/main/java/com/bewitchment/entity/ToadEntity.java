package com.bewitchment.entity;

import com.bewitchment.registry.ModCreatures;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public final class ToadEntity extends TameableEntity {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(ToadEntity.class,DataSerializers.INT);
    public ToadEntity(EntityType<? extends ToadEntity> type,World world){super(type,world);xpReward=5;}
    public static AttributeModifierMap.MutableAttribute attributes(){return AnimalEntity.createMobAttributes().add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,.8).add(Attributes.FOLLOW_RANGE,10).add(Attributes.ATTACK_DAMAGE,.5);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new SitGoal(this));
        goalSelector.addGoal(2,new BreedGoal(this,.8));goalSelector.addGoal(2,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(3,new FollowParentGoal(this,.8));goalSelector.addGoal(3,new RandomWalkingGoal(this,.8));
        goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(4,new FollowOwnerGoal(this,.8,10,2,false));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(0,new OwnerHurtByTargetGoal(this));targetSelector.addGoal(1,new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2,new NonTamedTargetGoal<>(this,PlayerEntity.class,true,e->distanceToSqr(e)<1));
        targetSelector.addGoal(3,new NonTamedTargetGoal<>(this,LivingEntity.class,false,e->e instanceof EndermiteEntity || e instanceof SilverfishEntity));
    }
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);}
    public int skin(){return CreatureRules.skin(entityData.get(SKIN));}
    public void skin(int value){entityData.set(SKIN,CreatureRules.skin(value));}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);skin(tag.getInt("skin"));}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        skin(random.nextInt(4));return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    @Override public void setTame(boolean tame){super.setTame(tame);if(getAttribute(Attributes.MAX_HEALTH)!=null)getAttribute(Attributes.MAX_HEALTH).setBaseValue(CreatureRules.health(tame));}
    @Override public ToadEntity getBreedOffspring(ServerWorld world,AgeableEntity other){
        ToadEntity child=ModCreatures.TOAD.get().create(world);
        if(child!=null)child.skin(random.nextBoolean()?skin():other instanceof ToadEntity?((ToadEntity)other).skin():skin());
        return child; // Legacy offspring inherit a skin, not ownership.
    }
    @Override public boolean canMate(AnimalEntity other){
        return other!=this && other instanceof ToadEntity && isTame() && ((ToadEntity)other).isTame()
                && isInLove() && other.isInLove() && !isOrderedToSit() && !((ToadEntity)other).isOrderedToSit();
    }
    @Override public boolean isFood(ItemStack stack){return stack.getItem()==Items.FERMENTED_SPIDER_EYE;}
    @Override public ActionResultType mobInteract(PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);
        if(!isTame() && held.getItem()==Items.SPIDER_EYE){
            if(!level.isClientSide){
                if(!player.isCreative())held.shrink(1);
                if(!isSilent())playSound(SoundEvents.PARROT_EAT,1,1+(random.nextFloat()-random.nextFloat())*.2F);
                if(CreatureRules.tameRoll(random.nextInt(5)) && !ForgeEventFactory.onAnimalTame(this,player)){
                    tame(player);heal(getMaxHealth());setTarget(null);level.broadcastEntityEvent(this,(byte)7);
                }else level.broadcastEntityEvent(this,(byte)6);
            }
            return ActionResultType.sidedSuccess(level.isClientSide);
        }
        if(isTame() && isOwnedBy(player) && !isFood(held)){
            if(!level.isClientSide){setOrderedToSit(!isOrderedToSit());setJumping(false);getNavigation().stop();setTarget(null);}
            return ActionResultType.sidedSuccess(level.isClientSide);
        }
        return super.mobInteract(player,hand);
    }
    @Override public boolean hurt(DamageSource source,float amount){
        Entity attacker=source.getEntity();
        boolean hit=super.hurt(source,CreatureRules.reducedDamage(amount,attacker!=null && !(attacker instanceof PlayerEntity) && !(attacker instanceof AbstractArrowEntity)));
        if(hit && !level.isClientSide)setOrderedToSit(false);
        return hit;
    }
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && target instanceof LivingEntity)((LivingEntity)target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,100,1,false,false));
        return hit;
    }
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.MOVEMENT_SLOWDOWN && super.canBeAffected(effect);}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override public void aiStep(){super.aiStep();if(!level.isClientSide && CreatureRules.toadHeals(tickCount,isInWaterRainOrBubble()))heal(1);}
    @Override protected SoundEvent getAmbientSound(){return ModCreatures.TOAD_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModCreatures.TOAD_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModCreatures.TOAD_DEATH.get();}
}
