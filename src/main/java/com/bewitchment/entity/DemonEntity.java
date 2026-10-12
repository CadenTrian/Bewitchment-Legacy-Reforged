package com.bewitchment.entity;

import com.bewitchment.combat.AccessoryEffects;
import com.bewitchment.item.LegacyAccessoryItem;
import com.bewitchment.registry.ModClothing;
import com.bewitchment.trade.DemonMerchantState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import java.util.EnumSet;

public class DemonEntity extends MonsterEntity implements IMerchant {
    private static final DataParameter<Integer> SKIN=EntityDataManager.defineId(DemonEntity.class,DataSerializers.INT);
    private DemonMerchantState trades;
    private PlayerEntity buyer;
    private Container tradingMenu;
    private MerchantOffers clientOffers=new MerchantOffers();
    private boolean opening,invalidTrades;
    private int attackTimer;
    public DemonEntity(EntityType<? extends DemonEntity> type,World world){
        super(type,world);xpReward=75;setPersistenceRequired();
        setPathfindingMalus(PathNodeType.WATER,-1);setPathfindingMalus(PathNodeType.LAVA,8);
        setPathfindingMalus(PathNodeType.DANGER_FIRE,0);setPathfindingMalus(PathNodeType.DAMAGE_FIRE,0);
        trades=DemonMerchantState.random(random);
    }
    public static AttributeModifierMap.MutableAttribute attributes(){return DemonRules.attributes(MonsterEntity.createMonsterAttributes());}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(SKIN,0);}
    public int skin(){return DemonRules.skin(entityData.get(SKIN));}
    public int attackTimer(){return attackTimer;}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        entityData.set(SKIN,random.nextInt(4));
        IFormattableTextComponent name=new StringTextComponent("");
        if(random.nextInt(3)==0)name.append(new TranslationTextComponent("entity.bewitchment.prefix."+random.nextInt(102))).append(" ");
        name.append(new TranslationTextComponent("entity.bewitchment.given_name."+random.nextInt(518)));setCustomName(name);
        return super.finalizeSpawn(world,difficulty,reason,data,tag);
    }
    private boolean prey(LivingEntity target){return buyer==null && target!=this && target.isAlive() && !(target instanceof PlayerEntity && ((PlayerEntity)target).isCreative()) && !target.isSpectator()
        && DemonRules.prey(target.fireImmune(),AccessoryEffects.wearing(target,LegacyAccessoryItem.Kind.HELLISH),ModClothing.hasSet(target,"besmirched"),random.nextInt(4));}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));
        goalSelector.addGoal(0,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));} @Override public boolean canUse(){return buyer!=null;} @Override public void tick(){getNavigation().stop();if(buyer!=null)getLookControl().setLookAt(buyer,10,40);} });
        goalSelector.addGoal(1,new MeleeAttackGoal(this,.5,false));goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,5,1));
        goalSelector.addGoal(3,new LookRandomlyGoal(this));goalSelector.addGoal(3,new RandomWalkingGoal(this,.8*2/3));
        targetSelector.addGoal(0,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,false,false,this::prey));
    }
    private boolean validCustomer(){return buyer!=null && DemonRules.customer(isAlive() && !removed,buyer.isAlive(),buyer.isSpectator(),buyer.level==level,isInWater(),isOnGround(),hurtMarked,getTarget()!=null,distanceToSqr(buyer))
        && (!(buyer instanceof ServerPlayerEntity) || !((ServerPlayerEntity)buyer).hasDisconnected()) && (opening || buyer.containerMenu==tradingMenu);}
    private void closeTrade(){PlayerEntity old=buyer;Container menu=tradingMenu;buyer=null;tradingMenu=null;opening=false;if(old!=null && menu!=null && old.containerMenu==menu)old.closeContainer();}
    @Override protected ActionResultType mobInteract(PlayerEntity player,Hand hand){
        if(player.getItemInHand(hand).getItem()==Items.NAME_TAG || player.getItemInHand(hand).getItem()==Items.LEAD)return super.mobInteract(player,hand);
        if(invalidTrades || buyer!=null || player.isShiftKeyDown() || !DemonRules.customer(isAlive(),player.isAlive(),player.isSpectator(),player.level==level,isInWater(),isOnGround(),hurtMarked,getTarget()!=null,distanceToSqr(player)))return ActionResultType.PASS;
        if(!level.isClientSide){
            buyer=player;opening=true;getNavigation().stop();
            try{trades.open(random,ModClothing.hasSet(player,"besmirched"));openTradingScreen(player,getDisplayName(),trades.level());tradingMenu=player.containerMenu;if(hand==Hand.MAIN_HAND)player.awardStat(net.minecraft.stats.Stats.TALKED_TO_VILLAGER);}
            finally{opening=false;}
            if(tradingMenu==player.inventoryMenu)closeTrade();
        }
        return ActionResultType.sidedSuccess(level.isClientSide);
    }
    @Override public void setTradingPlayer(PlayerEntity player){buyer=player;if(player==null){tradingMenu=null;opening=false;}}
    @Override public PlayerEntity getTradingPlayer(){return level.isClientSide?buyer:validCustomer()?buyer:null;}
    @Override public MerchantOffers getOffers(){
        if(level.isClientSide)return clientOffers;
        MerchantOffers offers=new MerchantOffers();if(!invalidTrades)offers.addAll(trades.offers());return offers;
    }
    @Override public void overrideOffers(MerchantOffers offers){if(level.isClientSide)clientOffers=offers==null?new MerchantOffers():offers;}
    @Override public void notifyTrade(MerchantOffer offer){
        if(level.isClientSide || !validCustomer() || invalidTrades)return;
        DemonMerchantState.TradeResult result=trades.completeTrade(offer,buyer.getUUID(),random);
        if(result.accepted && result.xp>0)level.addFreshEntity(new ExperienceOrbEntity(level,getX()+.5,getY(),getZ(),result.xp));
    }
    @Override public void notifyTradeUpdated(ItemStack stack){}
    @Override public World getLevel(){return level;}
    @Override public int getVillagerXp(){return 0;}
    @Override public void overrideXp(int xp){}
    @Override public boolean showProgressBar(){return false;}
    @Override public boolean canRestock(){return true;}
    @Override public SoundEvent getNotifyTradeSound(){return SoundEvents.VILLAGER_YES;}
    @Override public void setTarget(LivingEntity target){if(target!=null && buyer!=null)closeTrade();super.setTarget(target);}
    @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit && !level.isClientSide)closeTrade();return hit;}
    @Override public void die(DamageSource source){closeTrade();super.die(source);}
    @Override public void remove(){closeTrade();super.remove();}
    @Override public boolean doHurtTarget(Entity target){
        boolean hit=super.doHurtTarget(target);
        if(hit && !level.isClientSide){attackTimer=10;level.broadcastEntityEvent(this,(byte)4);
            if(target instanceof LivingEntity){((LivingEntity)target).addEffect(new EffectInstance(Effects.CONFUSION,200,1,false,false));target.setSecondsOnFire(25);target.setDeltaMovement(target.getDeltaMovement().add(0,.6,0));
                if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new SEntityVelocityPacket(target));}}
        return hit;
    }
    @Override public void handleEntityEvent(byte id){if(id==4)attackTimer=10;else super.handleEntityEvent(id);}
    @Override public boolean canBeAffected(EffectInstance effect){return effect.getEffect()!=Effects.POISON && effect.getEffect()!=Effects.WITHER && super.canBeAffected(effect);}
    @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("bewitchment","entities/demon"+skin());}
    @Override public void aiStep(){
        if(!level.isClientSide && buyer!=null && !validCustomer())closeTrade();
        super.aiStep();if(attackTimer>0)attackTimer--;if(level.isClientSide)return;
        if(tickCount%20==0 && isInLava())heal(6);
        if(isAlive() && !invalidTrades){PlayerEntity last=trades.lastBuyer()==null?null:level.getServer().getPlayerList().getPlayer(trades.lastBuyer());
            if(trades.tick(buyer!=null,last!=null && ModClothing.hasSet(last,"besmirched"),random))addEffect(new EffectInstance(Effects.REGENERATION,200,0));}
        LivingEntity target=getTarget();
        if(isAlive() && buyer==null && target!=null && target.isAlive() && DemonRules.fires(tickCount,distanceToSqr(target))){
            double spread=DemonRules.spread(distanceToSqr(target));SmallFireballEntity ball=new SmallFireballEntity(level,this,target.getX()-getX()+random.nextGaussian()*spread,target.getY()+target.getBbHeight()/2-(getY()+getBbHeight()/2),target.getZ()-getZ()+random.nextGaussian()*spread);
            ball.setPos(getX(),getY()+getBbHeight()/2+.5,getZ());swing(Hand.MAIN_HAND);if(level.addFreshEntity(ball))level.levelEvent(null,1018,blockPosition(),0);
        }
    }
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("skin",skin());if(!invalidTrades)tag.put("demonTrades",trades.save());tag.putBoolean("invalidTrades",invalidTrades);}
    @Override public void readAdditionalSaveData(CompoundNBT tag){
        super.readAdditionalSaveData(tag);entityData.set(SKIN,DemonRules.skin(tag.getInt("skin")));invalidTrades=tag.getBoolean("invalidTrades");buyer=null;tradingMenu=null;
        if(tag.contains("demonTrades")){try{trades=DemonMerchantState.load(tag.getCompound("demonTrades"));}catch(IllegalArgumentException ex){invalidTrades=true;org.apache.logging.log4j.LogManager.getLogger().warn("Disabled malformed demon merchant state for {}",getUUID());}}
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.BLAZE_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.ENDER_DRAGON_GROWL;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.BLAZE_DEATH;}
}