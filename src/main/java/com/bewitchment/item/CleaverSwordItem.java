package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;

public final class CleaverSwordItem extends SwordItem {
    public static final IItemTier TIER=new IItemTier(){
        public int getUses(){return 333;}public float getSpeed(){return 16;}
        public float getAttackDamageBonus(){return 3;}public int getLevel(){return 2;}
        public int getEnchantmentValue(){return 26;}public Ingredient getRepairIngredient(){return Ingredient.EMPTY;}
    };
    public CleaverSwordItem(){super(TIER,3,-2.4F,new Item.Properties().tab(ModItems.TAB).setNoRepair());}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack material){return false;}
    @Override public boolean isEnchantable(ItemStack stack){return false;}
    public static boolean corrodes(boolean playerTarget,boolean playerAttacker,int roll){return !(playerTarget && playerAttacker) && roll>=0 && roll<5;}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
        if(!target.level.isClientSide && corrodes(target instanceof PlayerEntity,attacker instanceof PlayerEntity,attacker.getRandom().nextInt(100))){
            target.setDeltaMovement(target.getDeltaMovement().add(0,.6,0));target.hasImpulse=true;
            target.addEffect(new EffectInstance(ModEffects.CORROSION.get(),450,1,false,false));
            stack.hurtAndBreak(9,attacker,e->e.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
            if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new net.minecraft.network.play.server.SEntityVelocityPacket(target));
        }
        return super.hurtEnemy(stack,target,attacker);
    }
}
