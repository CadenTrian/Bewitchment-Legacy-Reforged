package com.bewitchment.machine;

import com.bewitchment.item.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

/** Local teleportation to loaded destinations; the entire group is checked before movement. */
public final class RitualTeleport {
    public static AxisAlignedBB bounds(BlockPos center) { return new AxisAlignedBB(center).inflate(3); }
    public static BlockPos destination(ServerWorld world, ItemStack offering) {
        if(offering.getItem() instanceof WaystoneItem) {
            BlockPos pos=WaystoneItem.location(offering);
            return pos!=null && offering.getDamageValue()<3 && world.dimension().location().equals(WaystoneItem.dimension(offering)) ? pos : null;
        }
        if(offering.getItem() instanceof TaglockItem) {
            UUID id=PoppetItem.binding(offering);
            Entity target=id==null ? null : world.getEntity(id);
            return target!=null && target.isAlive() ? target.blockPosition() : null;
        }
        return null;
    }
    public static final class Plan {
        private final ServerWorld world;
        private final BlockPos destination;
        private final List<Entity> travelers;
        Plan(ServerWorld world, BlockPos destination, List<Entity> travelers) {this.world=world;this.destination=destination;this.travelers=travelers;}
        public void move() {
            for(Entity entity:travelers) {
                world.playSound(null,entity.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundCategory.PLAYERS,1,1);
                double x=destination.getX()+.5, y=destination.getY(), z=destination.getZ()+.5;
                if(entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player=(ServerPlayerEntity)entity;
                    player.connection.teleport(x,y,z,player.yRot,player.xRot);
                } else entity.teleportTo(x,y,z);
                entity.fallDistance=0;
                world.playSound(null,destination,SoundEvents.ENDERMAN_TELEPORT,SoundCategory.PLAYERS,1,1);
            }
        }
    }
    static ItemStack soleOffering(Inventory inputs) {
        ItemStack found=ItemStack.EMPTY;
        for(int i=0;i<inputs.getContainerSize();i++) if(!inputs.getItem(i).isEmpty()) {
            if(!found.isEmpty() || inputs.getItem(i).getCount()!=1) return ItemStack.EMPTY;
            found=inputs.getItem(i);
        }
        return found;
    }
    public static Plan prepare(ServerWorld world, BlockPos center, Inventory inputs) {
        BlockPos destination=destination(world,soleOffering(inputs));
        if(destination==null || !WaystoneItem.validPosition(destination) || !world.hasChunkAt(destination)
                || !world.getBlockState(destination.below()).isFaceSturdy(world,destination.below(),Direction.UP)) return null;
        List<Entity> travelers=world.getEntitiesOfClass(Entity.class,bounds(center),Entity::isAlive);
        if(travelers.isEmpty()) return null;
        for(Entity entity:travelers) {
            if(entity.isPassenger() || entity.isVehicle()) return null;
            AxisAlignedBB box=entity.getBoundingBox().move(destination.getX()+.5-entity.getX(),destination.getY()-entity.getY(),destination.getZ()+.5-entity.getZ());
            if(!RitualConjuration.fits(box,world.getWorldBorder().getMinX(),world.getWorldBorder().getMaxX(),world.getWorldBorder().getMinZ(),world.getWorldBorder().getMaxZ())
                    || !world.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ))
                    || world.getBlockCollisions(entity,box).findAny().isPresent() || world.containsAnyLiquid(box)) return null;
        }
        return new Plan(world,destination,travelers);
    }
    public static ItemStack returnedOffering(Inventory inputs, boolean creative) {
        ItemStack offering=soleOffering(inputs);
        return offering.getItem() instanceof WaystoneItem ? WaystoneItem.afterUse(offering,creative) : ItemStack.EMPTY;
    }
    private RitualTeleport() {}
}
