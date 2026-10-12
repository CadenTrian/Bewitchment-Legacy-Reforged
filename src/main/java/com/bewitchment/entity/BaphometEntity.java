package com.bewitchment.entity;

import com.bewitchment.registry.*;
import com.bewitchment.world.DemonPledges;
import com.bewitchment.combat.PlayerCurses;
import com.bewitchment.machine.CurseData;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;

public final class BaphometEntity extends GreaterDemonEntity {
    private int mobSpawnTicks,pullCooldown;
    public BaphometEntity(EntityType<? extends BaphometEntity> type,World world){super(type,world);xpReward=185;setPersistenceRequired();setDropChance(EquipmentSlotType.MAINHAND,0);}
    public static AttributeModifierMap.MutableAttribute attributes(){return BaphometRules.attributes(MonsterEntity.createMonsterAttributes());}
    @Override public DemonPledges.Patron patron(){return DemonPledges.Patron.BAPHOMET;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));
        goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));goalSelector.addGoal(3,new LookRandomlyGoal(this));
        goalSelector.addGoal(3,new RandomWalkingGoal(this,.7*2/3));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.8));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4F));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,false));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,e->e instanceof VillagerEntity || e instanceof AbstractIllagerEntity || e instanceof WitchEntity || e instanceof IronGolemEntity));
    }
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(ModItems.CADUCEUS.get()));return super.finalizeSpawn(world,difficulty,reason,data,tag);}
    @Override protected ActionResultType mobInteract(PlayerEntity player,Hand hand){
        if(hand!=Hand.MAIN_HAND)return super.mobInteract(player,hand);
        if(level.isClientSide)return ActionResultType.SUCCESS;
        if(!isAlive() || !player.isAlive() || player.isSpectator())return ActionResultType.PASS;
        CurseData data=PlayerCurses.read(player);
        String failure=BaphometRules.contractFailure(DemonPledges.get((ServerWorld)level).has(player.getUUID(),patron()),player.experienceLevel,data);
        if(failure!=null){player.displayClientMessage(new TranslationTextComponent("baphomet.bewitchment."+failure),true);return ActionResultType.CONSUME;}
        CurseData.Kind kind=BaphometRules.grant(data,player.getRandom());
        if(kind!=null){
            PlayerCurses.write(player,data);player.giveExperienceLevels(-6);
            player.displayClientMessage(new TranslationTextComponent("baphomet.bewitchment.getcontract",new TranslationTextComponent(kind.id.replace(':','.'))),true);
            level.playSound(null,player.blockPosition(),SoundEvents.EVOKER_CAST_SPELL,SoundCategory.PLAYERS,5,1);
        }
        return ActionResultType.CONSUME;
    }
    @Override public void aiStep(){
        super.aiStep();if(level.isClientSide || !isAlive() || !(getTarget() instanceof PlayerEntity) || !getTarget().isAlive())return;
        PlayerEntity target=(PlayerEntity)getTarget();ServerWorld world=(ServerWorld)level;
        if(BaphometRules.pulse(tickCount,600)){
            world.sendParticles(ParticleTypes.FLAME,getX(),getY(),getZ(),100,getBbWidth(),getBbHeight()+1,getBbWidth(),.1);
            level.playSound(null,blockPosition(),SoundEvents.FIRECHARGE_USE,SoundCategory.HOSTILE,5,1);
            for(Effect effect:new Effect[]{Effects.DAMAGE_BOOST,Effects.DAMAGE_RESISTANCE,Effects.MOVEMENT_SPEED})addEffect(new EffectInstance(effect,300,1));return;
        }
        BaphometRules.Summons summons=BaphometRules.summons(getHealth()/getMaxHealth(),mobSpawnTicks);mobSpawnTicks=summons.timer;
        if(summons.kind!=0)summonMinions(world,target,summons.kind);
        if(BaphometRules.pull(distanceToSqr(target),pullCooldown)){
            target.setDeltaMovement(target.getDeltaMovement().add((getX()-target.getX())/10,0,(getZ()-target.getZ())/10));
            target.hurtMarked=true;
            if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new SEntityVelocityPacket(target));
            pullCooldown=40;
        }else if(pullCooldown>0)pullCooldown--;
        if(BaphometRules.fireball(tickCount,distanceToSqr(target))){
            double spread=DemonRules.spread(distanceToSqr(target));
            SmallFireballEntity ball=new SmallFireballEntity(level,this,target.getX()-getX()+random.nextGaussian()*spread,target.getY()+target.getBbHeight()/2-(getY()+getBbHeight()/2),target.getZ()-getZ()+random.nextGaussian()*spread);
            ball.setPos(getX(),getY()+getBbHeight()/2+.5,getZ());if(level.addFreshEntity(ball))level.levelEvent(null,1018,blockPosition(),0);
        }
    }
    private void summonMinions(ServerWorld world,PlayerEntity target,int kind){
        int amount=1+random.nextInt(3);
        for(int i=0;i<amount;i++){
            MobEntity mob=kind==1?ModCreatures.FEUERWURM.get().create(world):ModCreatures.BAFOMETYR.get().create(world);
            if(mob==null)continue;
            mob.moveTo(getX()+random.nextGaussian()*.8,getY()+.5,getZ()+random.nextGaussian()*.8,0,0);
            if(!world.hasChunkAt(mob.blockPosition()) || !world.getWorldBorder().isWithinBounds(mob.blockPosition()))continue;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(mob.blockPosition()),SpawnReason.MOB_SUMMONED,null,null);
            mob.setTarget(target);mob.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,600,2));
            if(world.addFreshEntity(mob))world.sendParticles(ParticleTypes.FLAME,getX(),getY(),getZ(),16,mob.getBbWidth(),mob.getBbHeight()+1,mob.getBbWidth(),.1);
        }
        swing(Hand.MAIN_HAND);
    }
    @Override public boolean doHurtTarget(Entity entity){if(!level.isClientSide && entity instanceof PlayerEntity){((PlayerEntity)entity).addEffect(new EffectInstance(Effects.BLINDNESS,40));heal(2);}return super.doHurtTarget(entity);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    // The original resets its air supply every underwater tick.
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("bewitchment:entities/baphomet");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModCreatures.BAFOMETYR_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModCreatures.BAFOMETYR_DEATH.get();}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("mobSpawnTicks",mobSpawnTicks);tag.putInt("pullCooldown",pullCooldown);}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);mobSpawnTicks=BaphometRules.timer(tag.getInt("mobSpawnTicks"),180);pullCooldown=BaphometRules.timer(tag.getInt("pullCooldown"),40);}
}
