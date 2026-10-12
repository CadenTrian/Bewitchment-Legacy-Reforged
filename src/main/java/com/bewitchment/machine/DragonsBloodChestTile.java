package com.bewitchment.machine;

import com.bewitchment.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;

/** 27 accessible legacy slots, with ordinary automation and optional opening effects. */
public final class DragonsBloodChestTile extends ChestTileEntity {
    public final ChestSigilData sigil=new ChestSigilData();
    private int ticks;
    public DragonsBloodChestTile(){this(ModMachines.DRAGONS_BLOOD_CHEST.get());}
    DragonsBloodChestTile(TileEntityType<?> type){super(type);}
    @Override protected ITextComponent getDefaultName(){return new TranslationTextComponent("block.bewitchment.dragons_blood_chest");}
    public void activate(PlayerEntity player,Hand hand){
        if(level==null || level.isClientSide || player.isSpectator() || !stillValid(player))return;
        ItemStack held=player.getItemInHand(hand);
        if(sigil.kind()==null && held.getItem() instanceof ChestSigilItem){
            if(sigil.install(((ChestSigilItem)held.getItem()).kind,player.getUUID())){held.shrink(1);setChanged();player.displayClientMessage(new TranslationTextComponent("message.bewitchment.chest_sigil_installed"),true);}
            return;
        }
        if(sigil.kind()!=null && held.getItem() instanceof TaglockItem && TaglockItem.bound(held)){
            boolean changed=sigil.toggle(held.getTag().getString("boundId"));
            if(changed)setChanged();
            player.displayClientMessage(new TranslationTextComponent(changed?"message.bewitchment.chest_sigil_toggled":"message.bewitchment.chest_sigil_invalid"),true);return;
        }
        player.openMenu(this);
    }
    @Override public boolean canOpen(PlayerEntity player){return level!=null && !level.isClientSide && !player.isSpectator() && stillValid(player) && super.canOpen(player);}
    @Override protected Container createMenu(int id,PlayerInventory inventory){
        if(sigil.trigger(inventory.player.getUUID())){sigil.kind().apply(inventory.player);setChanged();}
        return super.createMenu(id,inventory);
    }
    @Override public CompoundNBT save(CompoundNBT tag){super.save(tag);tag.put("ChestSigil",sigil.save());return tag;}
    @Override public void load(BlockState state,CompoundNBT tag){super.load(state,tag);sigil.load(tag.getCompound("ChestSigil"));}
    @Override protected void signalOpenCount(){openCount=Math.max(0,openCount);if(level!=null && !level.isClientSide)level.blockEvent(worldPosition,getBlockState().getBlock(),1,openCount);}
    @Override public void tick(){
        if(level==null)return;
        int previous=openCount;
        openCount=ChestTileEntity.getOpenCount(level,this,++ticks,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),openCount);
        if(previous!=openCount)signalOpenCount();
        oOpenness=openness;
        if(openCount>0 && openness==0)sound(SoundEvents.CHEST_OPEN);
        openness=JuniperChestTile.advanceLid(openness,openCount);
        if(openness<.5F && oOpenness>=.5F)sound(SoundEvents.CHEST_CLOSE);
        if(!level.isClientSide && sigil.cooldown()>0){sigil.tick();setChanged();}
    }
    private void sound(SoundEvent sound){if(!level.isClientSide)level.playSound(null,worldPosition,sound,SoundCategory.BLOCKS,.5F,.9F+level.random.nextFloat()*.1F);}
}
