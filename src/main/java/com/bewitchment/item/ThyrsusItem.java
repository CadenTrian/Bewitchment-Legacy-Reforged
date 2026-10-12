package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.*;
import net.minecraft.util.*;

/** Legacy Thyrsus interaction, executed on the authoritative server. */
public final class ThyrsusItem extends SwordItem {
    public static final IItemTier TIER=new IItemTier(){
        public int getUses(){return 72;} public float getSpeed(){return 12;}
        public float getAttackDamageBonus(){return 3;} public int getLevel(){return 1;}
        public int getEnchantmentValue(){return 20;}
        public Ingredient getRepairIngredient(){return Ingredient.of(Items.STICK);}
    };
    public ThyrsusItem(){super(TIER,3,-2.4F,new Item.Properties().tab(ModItems.TAB));}
    @Override public ActionResultType interactLivingEntity(ItemStack stack,PlayerEntity player,LivingEntity target,Hand hand){
        if(!player.level.isClientSide){
            if(target instanceof AnimalEntity)((AnimalEntity)target).setInLove(null);
            if(target instanceof TameableEntity)((TameableEntity)target).tame(player);
            stack.hurtAndBreak(6,player,e->e.broadcastBreakEvent(hand));
        }
        return ActionResultType.sidedSuccess(player.level.isClientSide);
    }
    public static boolean nauseates(boolean playerTarget,boolean playerAttacker,int roll){return !(playerTarget && playerAttacker) && roll>=0 && roll<10;}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
        if(!target.level.isClientSide && nauseates(target instanceof PlayerEntity,attacker instanceof PlayerEntity,attacker.getRandom().nextInt(100))){
            target.addEffect(new EffectInstance(Effects.CONFUSION,100,0,false,false));
            stack.hurtAndBreak(6,attacker,e->e.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
        }
        return super.hurtEnemy(stack,target,attacker);
    }
}
