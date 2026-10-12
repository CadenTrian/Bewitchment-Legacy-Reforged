package com.bewitchment.entity;
import com.bewitchment.registry.*;
import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
public final class LeonardEntity extends GreaterDemonEntity {
    private int stewTimer;
    public LeonardEntity(EntityType<? extends LeonardEntity> type,World world){super(type,world);xpReward=165;setPersistenceRequired();setDropChance(EquipmentSlotType.MAINHAND,0);}
    public static AttributeModifierMap.MutableAttribute attributes(){return LeonardRules.attributes(MonsterEntity.createMonsterAttributes());}
    @Override public DemonPledges.Patron patron(){return DemonPledges.Patron.LEONARD;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(3,new RandomWalkingGoal(this,.5*2/3));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,false));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->e instanceof VillagerEntity || e instanceof AbstractIllagerEntity || e instanceof WitchEntity || e instanceof IronGolemEntity));
    }
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(ModItems.LEONARDS_WAND.get()));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override protected ActionResultType mobInteract(PlayerEntity player,Hand hand){
        if(!level.isClientSide && isAlive() && LeonardRules.stew(DemonPledges.get((ServerWorld)level).has(player.getUUID(),patron()),hand==Hand.MAIN_HAND,player.getItemInHand(hand).getItem()==Items.BOWL,player.experienceLevel,stewTimer)){
            ItemStack bowls=player.getItemInHand(hand);bowls.shrink(1);ItemStack stew=new ItemStack(ModItems.STEW_OF_THE_GROTESQUE.get());
            if(bowls.isEmpty())player.setItemInHand(hand,stew);else if(!player.inventory.add(stew))player.drop(stew,false);
            stewTimer=24000;level.playSound(null,player.blockPosition(),SoundEvents.BOTTLE_FILL,SoundCategory.PLAYERS,1,1);return ActionResultType.SUCCESS;
        }
        return super.mobInteract(player,hand);
    }
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide || !isAlive())return;if(stewTimer>0)stewTimer--;
        if(LeonardRules.pulse(tickCount,200)){
            if(getHealth()<getMaxHealth())buff(Effects.REGENERATION,100,1,6);
            if(getHealth()/getMaxHealth()<.4F)buff(Effects.DAMAGE_RESISTANCE,100,0,6);
        }
        LivingEntity target=getTarget();if(getHealth()/getMaxHealth()<.25F && target!=null && target.isAlive() && !target.hasEffect(ModEffects.MORTAL_COIL.get()))target.addEffect(new EffectInstance(ModEffects.MORTAL_COIL.get(),6000));
        if(LeonardRules.pulse(tickCount,450)){
            ((ServerWorld)level).sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),128,getBbWidth(),getBbHeight()+1,getBbWidth(),.1);
            buff(Effects.DAMAGE_BOOST,300,1,5);addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,300,1));addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,300,1));return;
        }
        if(target instanceof PlayerEntity && target.isAlive() && LeonardRules.pulse(tickCount,100) && distanceToSqr(target)>16){
            double spread=DemonRules.spread(distanceToSqr(target));SmallFireballEntity ball=new SmallFireballEntity(level,this,target.getX()-getX()+random.nextGaussian()*spread,target.getY()+target.getBbHeight()/2-(getY()+getBbHeight()/2),target.getZ()-getZ()+random.nextGaussian()*spread);
            ball.setPos(getX(),getY()+getBbHeight()/2+.5,getZ());if(level.addFreshEntity(ball))level.levelEvent(null,1018,blockPosition(),0);swing(Hand.MAIN_HAND);
        }
    }
    private void buff(Effect effect,int ticks,int amp,float volume){addEffect(new EffectInstance(effect,ticks,amp));level.playSound(null,blockPosition(),SoundEvents.ENCHANTMENT_TABLE_USE,SoundCategory.HOSTILE,volume,1);swing(Hand.MAIN_HAND);}
    @Override public boolean doHurtTarget(Entity entity){if(!level.isClientSide && entity instanceof PlayerEntity)((PlayerEntity)entity).addEffect(new EffectInstance(ModEffects.MAGIC_WEAKNESS.get(),60));return super.doHurtTarget(entity);}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect().getCategory()!=EffectType.HARMFUL && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void die(DamageSource source){if(!level.isClientSide)for(PlayerEntity player:level.getEntitiesOfClass(PlayerEntity.class,new net.minecraft.util.math.AxisAlignedBB(getX()-16,getY()-16,getZ()-16,getX()+16,getY()+16,getZ()+16)))player.removeEffect(ModEffects.MORTAL_COIL.get());super.die(source);}
    @Override protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("bewitchment:entities/leonard");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModCreatures.BAFOMETYR_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModCreatures.BAFOMETYR_DEATH.get();}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("timer",stewTimer);}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);stewTimer=LeonardRules.timer(tag.getInt("timer"));}
}