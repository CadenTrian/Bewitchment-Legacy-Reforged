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

public final class SnakeEntity extends TameableEntity {
    private static final DataParameter<Integer> HISS=EntityDataManager.defineId(SnakeEntity.class,DataSerializers.INT);
    private int milkTimer;
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(SnakeEntity.class,DataSerializers.INT);
    public SnakeEntity(EntityType<? extends SnakeEntity> type,World world){super(type,world);xpReward=5;entityData.set(HISS,random.nextInt(600)+30);}
    public static AttributeModifierMap.MutableAttribute attributes(){return AnimalEntity.createMobAttributes().add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.FOLLOW_RANGE,15).add(Attributes.ATTACK_DAMAGE,1.5);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new SitGoal(this));
        goalSelector.addGoal(2,new BreedGoal(this,.15));goalSelector.addGoal(2,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(3,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.3));
        goalSelector.addGoal(3,new FollowParentGoal(this,.3));goalSelector.addGoal(3,new RandomWalkingGoal(this,.3));
        goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(4,new FollowOwnerGoal(this,.3,10,2,false));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(0,new OwnerHurtByTargetGoal(this));targetSelector.addGoal(1,new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2,new NonTamedTargetGoal<>(this,PlayerEntity.class,true,e->distanceToSqr(e)<1));
        targetSelector.addGoal(3,new NonTamedTargetGoal<>(this,LivingEntity.class,false,e->e instanceof ChickenEntity || e instanceof LizardEntity || e instanceof RabbitEntity || e instanceof ParrotEntity || e.getClass().getName().endsWith("Rat")));
    }
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(HISS,80);}
    public int hissTime(){return entityData.get(HISS);}
    public int skin(){return SnakeRules.skin(entityData.get(SKIN));}
    public void skin(int value){entityData.set(SKIN,SnakeRules.skin(value));}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putInt("milkTimer",milkTimer);}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);skin(tag.getInt("skin"));milkTimer=SnakeRules.cooldown(tag.getInt("milkTimer"));}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        skin(random.nextInt(6));return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    @Override public void setTame(boolean tame){super.setTame(tame);if(getAttribute(Attributes.MAX_HEALTH)!=null)getAttribute(Attributes.MAX_HEALTH).setBaseValue(CreatureRules.health(tame));}
    @Override public SnakeEntity getBreedOffspring(ServerWorld world,AgeableEntity other){
        SnakeEntity child=ModCreatures.SNAKE.get().create(world);
        if(child!=null)child.skin(random.nextBoolean()?skin():other instanceof SnakeEntity?((SnakeEntity)other).skin():skin());
        return child; // Legacy offspring inherit a skin, not ownership.
    }
    @Override public boolean canMate(AnimalEntity other){
        return other!=this && other instanceof SnakeEntity && isTame() && ((SnakeEntity)other).isTame()
                && isInLove() && other.isInLove() && !isOrderedToSit() && !((SnakeEntity)other).isOrderedToSit();
    }
    @Override public boolean isFood(ItemStack stack){return stack.getItem()==Items.RABBIT;}
    @Override public ActionResultType mobInteract(PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);
        if(held.getItem()==Items.GLASS_BOTTLE){
            if(!level.isClientSide){
                boolean roll=random.nextBoolean();
                if(SnakeRules.canMilk(isBaby(),milkTimer,roll)){
                    ItemStack venom=new ItemStack(com.bewitchment.registry.ModItems.SNAKE_VENOM.get());
                    player.setItemInHand(hand,DrinkHelper.createFilledResult(held,player,venom));
                    milkTimer=3600;playSound(SoundEvents.BOTTLE_FILL,1,1);
                }else if(SnakeRules.milkAngers(milkTimer,roll)){
                    setOrderedToSit(false);setTarget(player);setLastHurtByMob(player);
                }
            }
            return ActionResultType.sidedSuccess(level.isClientSide);
        }
        if(!isTame() && SnakeRules.tamingFood(held.getItem())){
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
        if(hit && target instanceof LivingEntity)((LivingEntity)target).addEffect(new EffectInstance(Effects.POISON,100,0,false,false));
        return hit;
    }
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && super.canBeAffected(effect);}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    @Override public void aiStep(){
        super.aiStep();
        if(!isOnGround() && getDeltaMovement().y<=0)setDeltaMovement(getDeltaMovement().multiply(1,.6,1));
        if(!level.isClientSide){
            if(CreatureRules.lizardHeals(tickCount))heal(2);
            if(milkTimer>0)milkTimer--;
            int hiss=hissTime();
            if((!isOnGround() || getMoveControl().hasWanted()) && hiss<=61)hiss=80;
            entityData.set(HISS,--hiss<=0?random.nextInt(600)+30:hiss);
        }
    }
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState state){}
    @Override public void thunderHit(ServerWorld world,net.minecraft.entity.effect.LightningBoltEntity bolt){
        if(!isAlive() || world.getDifficulty()==Difficulty.PEACEFUL)return;
        FeuerwurmEntity converted=ModCreatures.FEUERWURM.get().create(world);
        if(converted==null)return;
        converted.moveTo(getX(),getY(),getZ(),yRot,xRot);converted.setNoAi(isNoAi());
        if(hasCustomName()){converted.setCustomName(getCustomName());converted.setCustomNameVisible(isCustomNameVisible());}
        if(isPersistenceRequired())converted.setPersistenceRequired();
        net.minecraft.util.math.AxisAlignedBB box=converted.getBoundingBox();
        if(box.minY<0 || box.maxY>256 || !world.getWorldBorder().isWithinBounds(box)
                || !world.hasChunksAt(new net.minecraft.util.math.BlockPos(box.minX,box.minY,box.minZ),new net.minecraft.util.math.BlockPos(box.maxX,box.maxY,box.maxZ))
                || !world.noCollision(this,box) || world.containsAnyLiquid(box))return;
        // Legacy conversion starts with skin zero; ownership and baby state are not inherited.
        if(world.addFreshEntity(converted))remove();
    }
}
