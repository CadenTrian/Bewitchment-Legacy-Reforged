package com.bewitchment.entity;

import com.bewitchment.item.*;
import com.bewitchment.registry.ModBrooms;
import com.bewitchment.machine.AltarTile;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class BroomEntity extends Entity {
    private static final DataParameter<ItemStack> ITEM=EntityDataManager.defineId(BroomEntity.class,DataSerializers.ITEM_STACK);
    private static final java.lang.reflect.Field JUMP=ObfuscationReflectionHelper.findField(LivingEntity.class,"field_70703_bu");
    public final BroomRules.Kind kind;
    private int paidTicks,lerpSteps; private java.util.UUID riderId;
    private double targetX,targetY,targetZ; private float targetYaw,targetPitch;
    public BroomEntity(EntityType<? extends BroomEntity> type,World world,BroomRules.Kind kind){super(type,world);this.kind=kind;setNoGravity(true);}
    @Override protected void defineSynchedData(){entityData.define(ITEM,ItemStack.EMPTY);}
    public ItemStack item(){ItemStack item=entityData.get(ITEM);return item.isEmpty()?new ItemStack(ModBrooms.ITEMS.get(kind).get()):item.copy();}
    public void setItem(ItemStack stack){ItemStack copy=stack.copy();copy.setCount(1);entityData.set(ITEM,copy);}
    @Override public IPacket<?> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
    @Override protected void addAdditionalSaveData(CompoundNBT tag){tag.put("item",item().save(new CompoundNBT()));}
    @Override protected void readAdditionalSaveData(CompoundNBT tag){
        ItemStack stack=ItemStack.of(tag.getCompound("item"));
        if(stack.getItem()!=ModBrooms.ITEMS.get(kind).get())stack=new ItemStack(ModBrooms.ITEMS.get(kind).get());
        setItem(stack);paidTicks=0;riderId=null;setNoGravity(true);
    }
    @Override public boolean isControlledByLocalInstance(){return !level.isClientSide;}
    @Override public Entity getControllingPassenger(){return getPassengers().isEmpty()?null:getPassengers().get(0);}
    @Override protected boolean canAddPassenger(Entity passenger){return passenger instanceof PlayerEntity && getPassengers().isEmpty();}
    @Override public double getPassengersRidingOffset(){return .4;}
    @Override public boolean isPickable(){return !removed;}
    @Override public boolean isPushable(){return !removed;}
    @Override public boolean canRiderInteract(){return true;}
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){targetX=x;targetY=y;targetZ=z;targetYaw=yaw;targetPitch=pitch;lerpSteps=Math.max(1,steps);}
    @Override public void tick(){
        super.tick();
        if(level.isClientSide){
            if(lerpSteps>0){setPos(getX()+(targetX-getX())/lerpSteps,getY()+(targetY-getY())/lerpSteps,getZ()+(targetZ-getZ())/lerpSteps);
                yRot+=MathHelper.wrapDegrees(targetYaw-yRot)/lerpSteps;xRot+=(targetPitch-xRot)/lerpSteps;lerpSteps--;}
            return;
        }
        Entity passenger=getControllingPassenger(); PlayerEntity player=passenger instanceof PlayerEntity?(PlayerEntity)passenger:null;
        boolean jump=false,powered=kind.cost==0;float strafe=0,forward=0;
        if(player!=null){
            if(!player.getUUID().equals(riderId)){riderId=player.getUUID();paidTicks=0;}
            yRot=player.yRot;strafe=player.xxa;forward=player.zza;
            try{jump=JUMP.getBoolean(player);}catch(IllegalAccessException error){throw new IllegalStateException("Cannot read vanilla rider input",error);}
            if(kind.cost>0 && (!onGround||jump)){
                if(paidTicks<=0){AltarTile altar=AltarTile.nearest((ServerWorld)level,blockPosition());
                    if((altar!=null&&altar.tryDrainMagic(kind.cost))||GrimoireMagiaItem.drain(player,kind.cost))paidTicks=20;}
                powered=paidTicks>0;if(paidTicks>0)paidTicks--;
            }
            if(kind==BroomRules.Kind.CYPRESS&&!player.hasEffect(Effects.DAMAGE_RESISTANCE))player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,100,1));
            if(kind==BroomRules.Kind.DRAGONS_BLOOD&&level.getGameTime()%100==0){
                ItemStack stack=item();ChestSigilItem.Kind sigil=stack.hasTag()?BroomRules.sigil(stack.getTag().getString("sigil")):null;
                if(sigil!=null)sigil.apply(player);
            }
        }else{riderId=null;paidTicks=0;}
        Vector3d motion=BroomRules.motion(kind,getDeltaMovement(),yRot,strafe,forward,jump,powered,onGround,horizontalCollision);
        AxisAlignedBB destination=getBoundingBox().move(motion);
        if(!loadedSpace((ServerWorld)level,destination))motion=Vector3d.ZERO;
        setDeltaMovement(motion);move(MoverType.SELF,motion);hasImpulse=true;fallDistance=0;
    }
    public static boolean loadedSpace(ServerWorld world,AxisAlignedBB box){
        return box.minY>=0&&box.maxY<=world.getMaxBuildHeight()
                &&box.minX>=world.getWorldBorder().getMinX()&&box.maxX<=world.getWorldBorder().getMaxX()
                &&box.minZ>=world.getWorldBorder().getMinZ()&&box.maxZ<=world.getWorldBorder().getMaxZ()
                &&world.hasChunksAt(new BlockPos(box.minX-1,box.minY-1,box.minZ-1),new BlockPos(box.maxX+1,box.maxY+1,box.maxZ+1));
    }
    @Override public ActionResultType interact(PlayerEntity player,Hand hand){
        if(player.isSpectator()||!player.isAlive())return ActionResultType.PASS;
        if(level.isClientSide)return ActionResultType.SUCCESS;
        ItemStack held=player.getItemInHand(hand),stored=item();
        if(kind==BroomRules.Kind.DRAGONS_BLOOD && held.getItem() instanceof ChestSigilItem
                &&(!stored.hasTag()||BroomRules.sigil(stored.getTag().getString("sigil"))==null)){
            stored.getOrCreateTag().putString("sigil",((ChestSigilItem)held.getItem()).kind.id());setItem(stored);held.shrink(1);return ActionResultType.CONSUME;
        }
        return !isVehicle()&&player.startRiding(this)?ActionResultType.CONSUME:ActionResultType.PASS;
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(level.isClientSide||removed||isVehicle()||source.getDirectEntity()==null||!Float.isFinite(amount)||amount<=0||isInvulnerableTo(source))return false;
        ItemStack drop=item();remove();if(level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))spawnAtLocation(drop);return true;
    }
}
