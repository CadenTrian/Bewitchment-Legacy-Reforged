package com.bewitchment.enchantment;

import net.minecraft.enchantment.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;

public final class MagicProtectionEnchantment extends Enchantment {
    public MagicProtectionEnchantment(){super(Rarity.VERY_RARE,EnchantmentType.ARMOR,new EquipmentSlotType[]{EquipmentSlotType.HEAD,EquipmentSlotType.CHEST,EquipmentSlotType.LEGS,EquipmentSlotType.FEET});}
    @Override public int getMaxLevel(){return 4;}
    @Override public int getDamageProtection(int level,DamageSource source){return !source.isBypassInvul()&&source.isMagic()?Math.max(0,level):0;}
    @Override public boolean checkCompatibility(Enchantment other){
        return other instanceof ProtectionEnchantment?other==Enchantments.FALL_PROTECTION:super.checkCompatibility(other);
    }
    public static float reduce(float damage,int total,boolean magic,boolean bypass){
        return Float.isFinite(damage)&&damage>0&&magic&&!bypass?damage*(1-Math.max(0,Math.min(20,total))/75F):damage;
    }
}
