package com.bewitchment.registry;

import com.bewitchment.Bewitchment;
import net.minecraft.potion.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Bewitchment.MOD_ID);
    public static final RegistryObject<Effect> HELLWORLD = EFFECTS.register("hellworld", com.bewitchment.potion.HellworldEffect::new);
    public static final RegistryObject<Effect> ICEWORLD = EFFECTS.register("iceworld", com.bewitchment.potion.IceworldEffect::new);
    public static final RegistryObject<Effect> CITRINITAS = EFFECTS.register("citrinitas", com.bewitchment.potion.CitrinitasEffect::new);
    public static final RegistryObject<Effect> RUBEDO = EFFECTS.register("rubedo", com.bewitchment.potion.RubedoEffect::new);
    public static final RegistryObject<Effect> ARACHNOPHOBIA = EFFECTS.register("arachnophobia", com.bewitchment.potion.ArachnophobiaEffect::new);
    public static final RegistryObject<Effect> DESERTIFICATION = EFFECTS.register("desertification", com.bewitchment.potion.DesertificationEffect::new);
    public static final RegistryObject<Effect> SPORE_CLOUD = EFFECTS.register("spore_cloud", com.bewitchment.potion.SporeCloudEffect::new);
    public static final RegistryObject<Effect> BLIGHT = EFFECTS.register("blight", com.bewitchment.potion.BlightEffect::new);
    public static final RegistryObject<Effect> MENDING = EFFECTS.register("mending", com.bewitchment.potion.MendingEffect::new);
    public static final RegistryObject<Effect> FERTILITY = EFFECTS.register("fertility", com.bewitchment.potion.FertilityEffect::new);
    public static final RegistryObject<Effect> HARVEST = EFFECTS.register("harvest", com.bewitchment.potion.HarvestEffect::new);
    public static final RegistryObject<Effect> RUIN = EFFECTS.register("ruin", com.bewitchment.potion.RuinEffect::new);
    public static final RegistryObject<Effect> PESTICIDE = EFFECTS.register("pesticide", com.bewitchment.potion.PesticideEffect::new);
    public static final RegistryObject<Effect> WOLFSBANE = EFFECTS.register("wolfsbane", () -> new com.bewitchment.potion.TargetedBrewEffect(true));
    public static final RegistryObject<Effect> DEVIANTS_DECOMPOSURE = EFFECTS.register("deviants_decomposure", () -> new com.bewitchment.potion.TargetedBrewEffect(false));
    public static final RegistryObject<Effect> DEFLECTION = EFFECTS.register("deflection", com.bewitchment.potion.CombatBrewRules::deflection);
    public static final RegistryObject<Effect> VOLATILITY = EFFECTS.register("volatility", com.bewitchment.potion.CombatBrewRules::volatility);
    public static final RegistryObject<Effect> RAGE = EFFECTS.register("rage", () -> new com.bewitchment.potion.InfluenceBrewEffect(false));
    public static final RegistryObject<Effect> FRIENDSHIP = EFFECTS.register("friendship", () -> new com.bewitchment.potion.InfluenceBrewEffect(true));
    public static final RegistryObject<Effect> ABSENCE = EFFECTS.register("absence", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.ABSENCE));
    public static final RegistryObject<Effect> PURIFICATION = EFFECTS.register("purification", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.PURIFICATION));
    public static final RegistryObject<Effect> CORRUPTION = EFFECTS.register("corruption", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.CORRUPTION));
    public static final RegistryObject<Effect> SLEEPING = EFFECTS.register("sleeping", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.SLEEPING));
    public static final RegistryObject<Effect> CURSED_LEAPS = EFFECTS.register("cursed_leaps", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.CURSED_LEAPS));
    public static final RegistryObject<Effect> BUTTERFINGERS = EFFECTS.register("butterfingers", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.BUTTERFINGERS));
    public static final RegistryObject<Effect> DISROBING = EFFECTS.register("disrobing", () -> new com.bewitchment.potion.UtilityBrewEffect(com.bewitchment.potion.UtilityBrewEffect.Kind.DISROBING));
    public static final RegistryObject<Effect> MAGIC_RESISTANCE = EFFECTS.register("magic_resistance", com.bewitchment.potion.DefensiveBrewEffects::magicResistance);
    public static final RegistryObject<Effect> GRACE = EFFECTS.register("grace", com.bewitchment.potion.DefensiveBrewEffects::grace);
    public static final RegistryObject<Effect> SHELL_ARMOR = EFFECTS.register("shell_armor", com.bewitchment.potion.DefensiveBrewEffects::shellArmor);
    public static final RegistryObject<Effect> SINKING = EFFECTS.register("sinking", com.bewitchment.potion.DefensiveBrewEffects::sinking);
    public static final RegistryObject<Effect> BULK = EFFECTS.register("bulk", com.bewitchment.potion.DefensiveBrewEffects::bulk);
    public static final RegistryObject<Effect> PAPER_SKIN = EFFECTS.register("paper_skin", com.bewitchment.potion.DefensiveBrewEffects::paperSkin);
    public static final RegistryObject<Effect> HOLY_WATER = EFFECTS.register("holy_water", () -> new com.bewitchment.potion.InstantBrewEffect(true));
    public static final RegistryObject<Effect> REVEALING = EFFECTS.register("revealing", () -> new com.bewitchment.potion.InstantBrewEffect(false));
    public static final RegistryObject<Effect> MORTAL_COIL = EFFECTS.register("mortal_coil", com.bewitchment.potion.MortalCoilEffect::new);
    public static final RegistryObject<Effect> MAGIC_WEAKNESS = EFFECTS.register("magic_weakness", com.bewitchment.potion.MagicWeaknessEffect::new);
    public static final RegistryObject<Effect> RHINO_HIDE = EFFECTS.register("rhino_hide", com.bewitchment.potion.RhinoHideEffect::new);
    public static final RegistryObject<Effect> HELLFIRE = EFFECTS.register("hellfire", () -> new Effect(EffectType.HARMFUL, 0) {});
    public static Effect corrosion(){return new Effect(EffectType.HARMFUL, 0x0BDA51) {}.addAttributeModifier(net.minecraft.entity.ai.attributes.Attributes.ARMOR,"e8506ffe-e2b4-4f19-8669-becb8e3eb666",-2,net.minecraft.entity.ai.attributes.AttributeModifier.Operation.ADDITION);}
    public static final RegistryObject<Effect> WEDNESDAY = EFFECTS.register("wednesday", com.bewitchment.potion.WednesdayEffect::new);
    public static final RegistryObject<Effect> CORROSION = EFFECTS.register("corrosion", ModEffects::corrosion);
    public static final RegistryObject<Effect> FEAR = EFFECTS.register("fear", () -> new Effect(EffectType.HARMFUL, 0x10c440) {});
    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if (event.getEntityLiving().level.isClientSide || !event.getSource().isFire()) return;
        EffectInstance effect = event.getEntityLiving().getEffect(HELLFIRE.get());
        if (effect != null) {
            float amplifier = effect.getAmplifier();
            event.setAmount(event.getAmount() * (1.5F + amplifier * amplifier * 0.5F));
        }
    }
    private ModEffects() {}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGH) public static void magicDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event){
        if(event.isCanceled() || event.getEntityLiving().level.isClientSide || !event.getSource().isMagic())return;
        EffectInstance effect=event.getEntityLiving().getEffect(MAGIC_WEAKNESS.get());
        if(effect!=null)event.setAmount(com.bewitchment.potion.MagicWeaknessEffect.damage(event.getAmount(),effect.getAmplifier(),true));
    }
}
