package com.bewitchment.registry;

import com.bewitchment.enchantment.MagicProtectionEnchantment;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

@Mod.EventBusSubscriber(modid="bewitchment")
public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS=DeferredRegister.create(ForgeRegistries.ENCHANTMENTS,"bewitchment");
    public static final RegistryObject<Enchantment> MAGIC_PROTECTION=ENCHANTMENTS.register("magic_protection",MagicProtectionEnchantment::new);
    @SubscribeEvent public static void hurt(LivingHurtEvent event){
        if(event.isCanceled()||event.getEntityLiving().level.isClientSide)return;
        int total=0;for(ItemStack stack:event.getEntityLiving().getArmorSlots())total+=EnchantmentHelper.getItemEnchantmentLevel(MAGIC_PROTECTION.get(),stack);
        event.setAmount(MagicProtectionEnchantment.reduce(event.getAmount(),total,event.getSource().isMagic(),event.getSource().isBypassInvul()));
    }
    private ModEnchantments(){}
}
