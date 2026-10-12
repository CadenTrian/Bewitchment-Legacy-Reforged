package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.registry.ModClothing;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class WitchArmorEffects {
    @SubscribeEvent public static void damage(LivingDamageEvent event) {
        if (!event.getEntityLiving().level.isClientSide && event.getEntityLiving() instanceof PlayerEntity
                && "indirectMagic".equals(event.getSource().getMsgId()) && ModClothing.hasSet(event.getEntityLiving(), "alchemist"))
            event.setAmount(event.getAmount() * 0.5F);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide || player.level.getGameTime() % 40 != 0) return;
        if (ModClothing.hasSet(player, "alchemist")) {
            player.removeEffect(Effects.WITHER); player.removeEffect(Effects.POISON);
        }
        if (ModClothing.hasSet(player, "green_witch") && player.level.isDay()
                && player.level.getBlockState(player.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
            net.minecraft.util.ResourceLocation biome = player.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(player.level.getBiome(player.blockPosition()));
            if (biome != null && BiomeDictionary.hasType(RegistryKey.create(Registry.BIOME_REGISTRY, biome), BiomeDictionary.Type.FOREST))
                player.addEffect(new EffectInstance(Effects.REGENERATION, 80));
        }
    }
    private WitchArmorEffects() {}
}
