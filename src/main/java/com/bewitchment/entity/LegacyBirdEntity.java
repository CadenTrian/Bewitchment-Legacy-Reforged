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

public abstract class LegacyBirdEntity extends TameableEntity implements net.minecraft.entity.passive.IFlyingAnimal {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(LegacyBirdEntity.class,DataSerializers.INT);
    private int shearTimer, shedTime;
    private static final DataParameter<Integer> PECK=EntityDataManager.defineId(LegacyBirdEntity.class,DataSerializers.INT);
    protected abstract boolean owl();
    protected LegacyBirdEntity(EntityType<? extends LegacyBirdEntity> type,World world){
        super(type,world);xpReward=5;
        moveControl=new net.minecraft.entity.ai.controller.FlyingMovementController(this,10,false);
        shedTime=BirdRules.shedDelay(random);entityData.set(PECK,random.nextInt(600)+30);
    }
    public static AttributeModifierMap.MutableAttribute attributes(boolean owl){return AnimalEntity.createMobAttributes()
        .add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,.1).add(Attributes.FOLLOW_RANGE,10)
        .add(Attributes.ATTACK_DAMAGE,owl?2.5:1.5).add(Attributes.FLYING_SPEED,owl?.8:1);}
    @Override protected net.minecraft.pathfinding.PathNavigator createNavigation(World world){
        net.minecraft.pathfinding.FlyingPathNavigator path=new net.minecraft.pathfinding.FlyingPathNavigator(this,world);
        path.setCanPassDoors(true);path.setCanFloat(true);path.setCanOpenDoors(false);return path;
    }
    @Override protected void registerGoals(){
        double flight=owl()?.8:1;
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new SitGoal(this));
        if(owl())goalSelector.addGoal(1,new OwlShadeGoal(this));
        goalSelector.addGoal(2,new BreedGoal(this,.05));goalSelector.addGoal(2,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(3,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(3,new FollowParentGoal(this,flight));
        goalSelector.addGoal(3,new WaterAvoidingRandomFlyingGoal(this,flight));
        goalSelector.addGoal(4,new FollowOwnerGoal(this,flight,10,2,true));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(0,new OwnerHurtByTargetGoal(this));targetSelector.addGoal(1,new OwnerHurtTargetGoal(this));
        if(owl())targetSelector.addGoal(2,new NonTamedTargetGoal<>(this,LivingEntity.class,false,e->e instanceof BatEntity || e instanceof ChickenEntity || e instanceof LizardEntity || e instanceof RabbitEntity || e instanceof ParrotEntity || e.getClass().getName().endsWith("Rat")));
    }
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);entityData.define(PECK,80);}
    public int skin(){return owl()?CreatureRules.skin(entityData.get(SKIN)):0;}
    public void skin(int value){entityData.set(SKIN,CreatureRules.skin(value));}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());tag.putInt("shearTimer",shearTimer);tag.putInt("shedTime",shedTime);}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);skin(tag.getInt("skin"));shearTimer=BirdRules.shearTimer(tag.getInt("shearTimer"));if(tag.contains("shedTime",3))shedTime=BirdRules.shedTimer(tag.getInt("shedTime"));}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        skin(random.nextInt(owl()?4:1));return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    @Override public void setTame(boolean tame){super.setTame(tame);if(getAttribute(Attributes.MAX_HEALTH)!=null)getAttribute(Attributes.MAX_HEALTH).setBaseValue(CreatureRules.health(tame));}
    @Override public LegacyBirdEntity getBreedOffspring(ServerWorld world,AgeableEntity other){
        LegacyBirdEntity child=(LegacyBirdEntity)getType().create(world);
        if(child!=null)child.skin(random.nextBoolean()?skin():other instanceof LegacyBirdEntity?((LegacyBirdEntity)other).skin():skin());
        return child; // Legacy offspring inherit a skin, not ownership.
    }
    @Override public boolean canMate(AnimalEntity other){
        return other!=this && other instanceof LegacyBirdEntity && other.getType()==getType() && isTame() && ((LegacyBirdEntity)other).isTame()
                && isInLove() && other.isInLove() && !isOrderedToSit() && !((LegacyBirdEntity)other).isOrderedToSit();
    }
    @Override public boolean isFood(ItemStack stack){return owl()?stack.getItem()==Items.RABBIT:stack.getItem().is(net.minecraftforge.common.Tags.Items.SEEDS);}
    @Override public ActionResultType mobInteract(PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);
        if(held.getItem() instanceof com.bewitchment.item.BolineItem){
            if(!level.isClientSide && shearTimer==0){
                spawnAtLocation(new ItemStack(feather(),BirdRules.featherCount(random)));
                shearTimer=12000;
            }
            return ActionResultType.sidedSuccess(level.isClientSide);
        }
        if(!isTame() && tamingFood(held.getItem())){
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
    private Item feather(){return owl()?Items.FEATHER:com.bewitchment.registry.ModItems.RAVENS_FEATHER.get();}
    private boolean tamingFood(Item item){
        if(owl())return SnakeRules.tamingFood(item);
        return item==Items.GOLD_NUGGET || item==Items.IRON_NUGGET || item==com.bewitchment.registry.ModItems.COLD_IRON_NUGGET.get() || item==com.bewitchment.registry.ModItems.SILVER_NUGGET.get();
    }
    @Override public boolean doHurtTarget(Entity target){return target.hurt(DamageSource.mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));}
    @Override public int getMaxSpawnClusterSize(){return 6;}
    public int peckTime(){return entityData.get(PECK);}
    public boolean isFlying(){return !isOnGround();}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override protected void checkFallDamage(double y,boolean grounded,net.minecraft.block.BlockState state,net.minecraft.util.math.BlockPos pos){}
    @Override public void aiStep(){
        super.aiStep();
        if(!isOnGround() && getDeltaMovement().y<=0)setDeltaMovement(getDeltaMovement().multiply(1,.6,1));
        if(!level.isClientSide){
            if(CreatureRules.lizardHeals(tickCount))heal(2);
            if(shearTimer>0)shearTimer--;
            if(!isBaby() && --shedTime<=0){spawnAtLocation(feather());shedTime=BirdRules.shedDelay(random);}
            if(!owl()){
                int peck=peckTime();if((!isOnGround() || getMoveControl().hasWanted()) && peck<=61)peck=80;
                entityData.set(PECK,--peck<=0?random.nextInt(600)+30:peck);
            }
        }
    }
    @Override protected SoundEvent getAmbientSound(){return owl()?ModCreatures.OWL_HOOT.get():ModCreatures.RAVEN_CRY.get();}
    @Override protected float getSoundVolume(){return owl()?.5F:super.getSoundVolume();}
}
