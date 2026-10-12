package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.*;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

public final class PlacedSigilTile extends TileEntity implements ITickableTileEntity {
    public final PlacedSigilData data=new PlacedSigilData();
    private Kind visibleKind;
    public PlacedSigilTile(){this(ModMachines.PLACED_SIGIL.get());}
    PlacedSigilTile(TileEntityType<?> type){super(type);}
    public Kind kind(){return level!=null && level.isClientSide?visibleKind:data.targets.kind();}
    public void install(Kind kind,UUID owner){
        if(data.install(kind,owner)){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    }
    @Override public void tick(){
        if(level==null || level.isClientSide)return;
        PlacedSigilData.Step step=data.advance();setChanged();
        if(step==PlacedSigilData.Step.EXPIRE){
            ((ServerWorld)level).sendParticles(RedstoneParticleData.REDSTONE,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,100,.3,0,.3,0);
            level.removeBlock(worldPosition,false);return;
        }
        if(step==PlacedSigilData.Step.PULSE)for(LivingEntity entity:level.getEntitiesOfClass(LivingEntity.class,PlacedSigilData.area(worldPosition),
                entity->entity.isAlive() && !entity.isSpectator() && data.targets.targets(entity.getUUID())))data.targets.kind().apply(entity);
    }
    @Override public CompoundNBT save(CompoundNBT tag){super.save(tag);tag.put("sigilData",data.save());return tag;}
    @Override public void load(BlockState state,CompoundNBT tag){super.load(state,tag);data.load(tag.getCompound("sigilData"));visibleKind=data.targets.kind();}
    @Override public CompoundNBT getUpdateTag(){CompoundNBT tag=super.getUpdateTag();tag.putString("sigil",kind()==null?"":kind().id());return tag;}
    @Override public SUpdateTileEntityPacket getUpdatePacket(){return new SUpdateTileEntityPacket(worldPosition,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SUpdateTileEntityPacket packet){visibleKind=Kind.fromId(packet.getTag().getString("sigil"));}
    @Override public void handleUpdateTag(BlockState state,CompoundNBT tag){visibleKind=Kind.fromId(tag.getString("sigil"));}
}
