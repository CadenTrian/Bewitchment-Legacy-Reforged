package com.bewitchment.item;

import com.bewitchment.config.ToolConfig;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

/** A bypass is paid only after the final lock event accepts it. */
public final class SkeletonKeyItem extends Item {
    public static final int LEVEL_COST=10;
    public SkeletonKeyItem(){super(new Properties().tab(ModItems.TAB).durability(10).setNoRepair());}
    @Override public int getMaxDamage(ItemStack stack){return ToolConfig.SKELETON_KEY_USES.get();}
    @Override public boolean isEnchantable(ItemStack stack){return false;}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack repair){return false;}
    @Override public boolean isBookEnchantable(ItemStack stack,ItemStack book){return false;}
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack,Enchantment enchantment){return false;}
    public static int cost(Block block){
        if(block instanceof ContainerBlock)return 1000;
        if(block instanceof TrapDoorBlock)return 750;
        if(block instanceof DoorBlock)return 500;
        return 250;
    }
    public static ItemStack carried(PlayerEntity player){
        for(int i=0;i<player.inventory.getContainerSize();i++){
            ItemStack stack=player.inventory.getItem(i);
            if(!stack.isEmpty() && stack.getItem() instanceof SkeletonKeyItem)return stack;
        }
        return ItemStack.EMPTY;
    }
    public static ItemStack powerSource(PlayerEntity player,int cost){
        for(int i=0;i<player.inventory.getContainerSize();i++){
            ItemStack stack=player.inventory.getItem(i);
            if(canPower(stack,cost))return stack;
        }
        return ItemStack.EMPTY;
    }
    public static boolean canPower(ItemStack book,int cost){return cost>0 && !book.isEmpty() && book.getItem() instanceof GrimoireMagiaItem && GrimoireMagiaItem.power(book)>=cost;}
    public static boolean canPay(int levels,ItemStack key,ItemStack book,int cost){
        return levels>=LEVEL_COST && !key.isEmpty() && key.getItem() instanceof SkeletonKeyItem && canPower(book,cost);
    }
    public static boolean pay(PlayerEntity player,ItemStack key,int cost){
        if(player.level.isClientSide)return false;
        ItemStack book=powerSource(player,cost);
        if(!canPay(player.experienceLevel,key,book,cost))return false;
        if(!GrimoireMagiaItem.drain(book,cost))return false;
        player.giveExperienceLevels(-LEVEL_COST);
        key.hurtAndBreak(1,player,e->{});
        player.inventory.setChanged();
        return true;
    }
}
