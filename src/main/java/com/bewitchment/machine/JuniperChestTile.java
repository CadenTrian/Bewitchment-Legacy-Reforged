package com.bewitchment.machine;

import com.bewitchment.block.JuniperLocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

/** Uses the legacy GUI's 27 accessible slots, without its nine hidden slots. */
public final class JuniperChestTile extends ChestTileEntity implements ISidedInventory {
    private int ticks;
    public JuniperChestTile(){this(ModMachines.JUNIPER_CHEST.get());}
    JuniperChestTile(TileEntityType<?> type){super(type);}
    /** Verify capabilities and transformed lock events under the real Forge loader. */
    public static void verify(){
        JuniperChestTile chest=new JuniperChestTile();
        if(chest.getContainerSize()!=27 || chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null).isPresent())throw new IllegalStateException("Juniper Chest exposes an invalid inventory");
        for(Direction side:Direction.values())if(chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,side).isPresent())throw new IllegalStateException("Juniper Chest exposes automation on "+side);
        com.bewitchment.api.event.LockCheckEvent[] events={
            new com.bewitchment.api.event.LockCheckEvent(null,net.minecraft.util.math.BlockPos.ZERO,true),
            new com.bewitchment.api.event.LockCheckEvent.KeyCheckEvent(null,net.minecraft.util.math.BlockPos.ZERO,true,ItemStack.EMPTY),
            new com.bewitchment.api.event.LockCheckEvent.LockCheckedEvent(null,net.minecraft.util.math.BlockPos.ZERO,true,false,false)};
        for(com.bewitchment.api.event.LockCheckEvent event:events){if(!event.isCancelable())throw new IllegalStateException("Lock event cancellation was not transformed");event.setCanceled(true);}
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment Juniper Chest inventory, automation restrictions and lock event cancellation verified");
    }
    @Override protected ITextComponent getDefaultName(){return new TranslationTextComponent("block.bewitchment.juniper_chest");}
    @Override public boolean canOpen(PlayerEntity player){
        return level!=null && !level.isClientSide && !player.isSpectator() && stillValid(player)
            && !level.getBlockState(worldPosition.above()).isRedstoneConductor(level,worldPosition.above())
            && super.canOpen(player) && JuniperLocks.access(player,level,worldPosition,true);
    }
    @Override protected void signalOpenCount(){
        openCount=Math.max(0,openCount);
        if(level!=null && !level.isClientSide)level.blockEvent(worldPosition,getBlockState().getBlock(),1,openCount);
    }
    public static float advanceLid(float openness,int viewers){return Math.max(0,Math.min(1,openness+(viewers>0?.1F:-.1F)));}
    @Override public void tick(){
        if(level==null)return;
        int previous=openCount;
        openCount=ChestTileEntity.getOpenCount(level,this,++ticks,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),openCount);
        if(previous!=openCount)signalOpenCount();
        oOpenness=openness;
        if(openCount>0 && openness==0)sound(SoundEvents.CHEST_OPEN);
        openness=advanceLid(openness,openCount);
        if(openness<.5F && oOpenness>=.5F)sound(SoundEvents.CHEST_CLOSE);
    }
    private void sound(SoundEvent sound){if(!level.isClientSide)level.playSound(null,worldPosition,sound,SoundCategory.BLOCKS,.5F,.9F+level.random.nextFloat()*.1F);}
    // No side, including the unsided Forge capability, may bypass the key gate.
    @Override public int[] getSlotsForFace(Direction side){return new int[0];}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return false;}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return false;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){return capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY?LazyOptional.empty():super.getCapability(capability,side);}
}
