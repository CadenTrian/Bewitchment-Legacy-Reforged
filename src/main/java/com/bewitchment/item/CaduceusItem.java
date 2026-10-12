package com.bewitchment.item;

import com.bewitchment.Bewitchment;
import com.bewitchment.entity.FeuerwurmEntity;
import com.bewitchment.registry.ModCreatures;
import com.bewitchment.registry.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class CaduceusItem extends Item {
    public CaduceusItem(){super(new Item.Properties().tab(ModItems.TAB).durability(CaduceusRules.DURABILITY).setNoRepair());}
    @Override public boolean isEnchantable(ItemStack stack){return false;}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack repair){return false;}
    @Override public boolean isBookEnchantable(ItemStack stack,ItemStack book){return false;}
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack,Enchantment enchantment){return false;}
    private static boolean fits(ServerWorld world,FeuerwurmEntity entity){
        AxisAlignedBB box=entity.getBoundingBox();
        return CaduceusRules.fits(box,world.getWorldBorder().getMinX(),world.getWorldBorder().getMaxX(),world.getWorldBorder().getMinZ(),world.getWorldBorder().getMaxZ())
            && world.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ))
            && world.noCollision(entity) && !world.containsAnyLiquid(box);
    }
    @Override public ActionResultType useOn(ItemUseContext context){
        PlayerEntity player=context.getPlayer();if(player==null)return ActionResultType.PASS;
        Hand hand=context.getHand();ItemStack held=context.getItemInHand();
        if(player.getCooldowns().isOnCooldown(this))return ActionResultType.FAIL;
        if(context.getLevel().isClientSide)return ActionResultType.SUCCESS;
        ServerWorld world=(ServerWorld)context.getLevel();BlockPos pos=context.getClickedPos();
        if(!player.abilities.mayBuild || !world.mayInteract(player,pos) || !player.mayUseItemAt(pos.relative(context.getClickedFace()),context.getClickedFace(),held))return ActionResultType.FAIL;
        if(world.getDifficulty()==Difficulty.PEACEFUL){player.displayClientMessage(new TranslationTextComponent("item.bewitchment.caduceus.peaceful"),true);return ActionResultType.FAIL;}
        LivingEntity target=player.getLastHurtByMob()!=null?player.getLastHurtByMob():player.getLastHurtMob();
        int spawned=CaduceusRules.spawn(world.random.nextInt(3),()->{
            FeuerwurmEntity entity=ModCreatures.FEUERWURM.get().create(world);if(entity==null)return null;
            entity.summon(player.getUUID(),CaduceusRules.LIFETIME,world.random.nextInt(9));
            if(target!=null && target.isAlive())entity.setTarget(target);
            entity.moveTo(pos.getX()+world.random.nextGaussian()*.8,pos.getY()+1,pos.getZ()+world.random.nextGaussian()*.8,0,0);
            return entity;
        },entity->fits(world,entity) && world.addFreshEntity(entity));
        if(spawned==0){player.displayClientMessage(new TranslationTextComponent("item.bewitchment.caduceus.blocked"),true);return ActionResultType.FAIL;}
        player.getCooldowns().addCooldown(this,CaduceusRules.COOLDOWN);
        held.hurtAndBreak(1,player,e->e.broadcastBreakEvent(hand));
        return ActionResultType.CONSUME;
    }
    @SubscribeEvent(priority=EventPriority.NORMAL)
    public static void rescue(LivingDeathEvent event){
        if(event.isCanceled() || !(event.getEntityLiving() instanceof PlayerEntity) || event.getEntityLiving().level.isClientSide)return;
        PlayerEntity player=(PlayerEntity)event.getEntityLiving();
        Hand hand=CaduceusRules.rescueHand(player.getMainHandItem().getItem() instanceof CaduceusItem,player.getOffhandItem().getItem() instanceof CaduceusItem);
        if(hand==null)return;
        event.setCanceled(true);player.setHealth(CaduceusRules.rescueHealth(player.getMaxHealth()));
        player.addEffect(new EffectInstance(Effects.REGENERATION,200,0));
        player.getItemInHand(hand).hurtAndBreak(CaduceusRules.RESCUE_COST,player,e->e.broadcastBreakEvent(hand));
    }
}
