package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import javax.annotation.Nullable;

public final class LeonardsWandItem extends Item {
    public LeonardsWandItem(){super(new Item.Properties().tab(ModItems.TAB).durability(128).setNoRepair());}
    public static int charges(ItemStack stack){return stack.hasTag()?Math.max(0,Math.min(8,stack.getTag().getInt("uses"))):0;}
    private static void clearPotion(CompoundNBT tag){tag.remove("uses");tag.remove("Potion");tag.remove("CustomPotionEffects");tag.remove("CustomPotionColor");}
    public static ItemStack charge(ItemStack wand,ItemStack potion){
        ItemStack result=wand.copy();result.setCount(1);clearPotion(result.getOrCreateTag());
        PotionUtils.setCustomEffects(result,PotionUtils.getMobEffects(potion));result.getOrCreateTag().putInt("uses",8);return result;
    }
    public static ItemStack projectile(ItemStack wand){
        ItemStack potion=new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setCustomEffects(potion,PotionUtils.getMobEffects(wand));
        potion.getOrCreateTag().putInt("CustomPotionColor",PotionUtils.getColor(PotionUtils.getMobEffects(wand)));return potion;
    }
    public static void spend(ItemStack wand){
        int remaining=charges(wand)-1;
        if(remaining>0)wand.getOrCreateTag().putInt("uses",remaining);
        else if(wand.hasTag()){clearPotion(wand.getTag());if(wand.getTag().isEmpty())wand.setTag(null);}
    }
    @Override public ActionResult<ItemStack> use(World world,PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);
        if(charges(held)==0)return ActionResult.pass(held);
        if(player.getCooldowns().isOnCooldown(this))return ActionResult.fail(held);
        if(!world.isClientSide){
            PotionEntity potion=new PotionEntity(world,player);potion.setItem(projectile(held));
            potion.shootFromRotation(player,player.xRot,player.yRot,-20,1,1);
            if(!world.addFreshEntity(potion))return ActionResult.fail(held);
            world.playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.SPLASH_POTION_THROW,SoundCategory.PLAYERS,.5F,.4F/(world.random.nextFloat()*.4F+.8F));
            player.awardStat(Stats.ITEM_USED.get(this));spend(held);
            held.hurtAndBreak(1,player,e->e.broadcastBreakEvent(hand));player.getCooldowns().addCooldown(this,10);
        }
        return ActionResult.sidedSuccess(held,world.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable World world,List<ITextComponent> text,ITooltipFlag flag){
        text.add(new TranslationTextComponent("tooltip.bewitchment.leonards_wand.uses",charges(stack)));super.appendHoverText(stack,world,text,flag);
    }
    @Override public boolean isFoil(ItemStack stack){return charges(stack)>0;}
    @Override public boolean isEnchantable(ItemStack stack){return false;}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack repair){return false;}
    @Override public boolean isBookEnchantable(ItemStack stack,ItemStack book){return false;}
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack,Enchantment enchantment){return false;}
}
