package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.DonationContractData;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class DonationContractTasks {
    public static boolean pickupAllowed(UUID owner,UUID player,boolean delayed){return !delayed && player!=null && (owner==null || owner.equals(player));}
    public static int donateFirst(net.minecraft.inventory.IInventory inventory,UUID owner,ItemStack offered){
        for(int i=0;i<inventory.getContainerSize();i++){ItemStack contract=inventory.getItem(i);if(KillContractItem.donationData(contract)==null)continue;int consumed=DonationContractData.donate(contract.getTag(),owner,offered);if(consumed>0)return consumed;}return 0;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void pickup(EntityItemPickupEvent event){
        PlayerEntity player=event.getPlayer();ItemEntity entity=event.getItem();
        if(event.isCanceled() || player.level.isClientSide || !player.isAlive() || player.isSpectator() || !pickupAllowed(entity.getOwner(),player.getUUID(),entity.hasPickUpDelay()))return;
        // Mutate the existing stack: vanilla playerTouch retains this same reference across the hook.
        if(donateFirst(player.inventory,player.getUUID(),entity.getItem())>0){player.inventory.setChanged();if(entity.getItem().isEmpty()){event.setCanceled(true);entity.remove();}}
    }
    private DonationContractTasks(){}
}
