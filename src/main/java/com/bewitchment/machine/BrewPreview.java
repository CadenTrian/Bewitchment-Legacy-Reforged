package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

/** Read-only calculator using a held potion as supplied base effects, not a brew recipe. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class BrewPreview {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSource,Integer> glow=Commands.argument("glowstone",IntegerArgumentType.integer(0,10));
        for(BrewPlan.Form form:BrewPlan.Form.values())glow.then(Commands.literal(form.name().toLowerCase(java.util.Locale.ROOT))
            .executes(ctx->run(ctx.getSource(),IntegerArgumentType.getInteger(ctx,"redstone"),IntegerArgumentType.getInteger(ctx,"glowstone"),form,false))
            .then(Commands.argument("hideParticles",BoolArgumentType.bool()).executes(ctx->run(ctx.getSource(),IntegerArgumentType.getInteger(ctx,"redstone"),IntegerArgumentType.getInteger(ctx,"glowstone"),form,BoolArgumentType.getBool(ctx,"hideParticles")))));
        event.getDispatcher().register(Commands.literal("brewpreview").requires(source->source.hasPermission(2)).then(Commands.argument("redstone",IntegerArgumentType.integer(0,10)).then(glow)));
    }
    private static int run(CommandSource source,int redstone,int glowstone,BrewPlan.Form form,boolean hide) throws CommandSyntaxException {
        ServerPlayerEntity player=source.getPlayerOrException();ItemStack held=player.getMainHandItem();
        if(!(held.getItem() instanceof PotionItem)){source.sendFailure(new TranslationTextComponent("commands.bewitchment.brewpreview.potion"));return 0;}
        List<EffectInstance> effects=PotionUtils.getMobEffects(held);
        if(effects.isEmpty()){source.sendFailure(new TranslationTextComponent("commands.bewitchment.brewpreview.empty"));return 0;}
        BrewBonuses bonuses=BrewBonuses.forPlayer(player);
        BrewPlan plan;
        try{plan=new BrewPlan(effects,redstone,glowstone,bonuses.alchemist,bonuses.leonards,form!=BrewPlan.Form.DRINK,form==BrewPlan.Form.LINGERING,hide);}
        catch(IllegalArgumentException ex){source.sendFailure(new TranslationTextComponent("commands.bewitchment.brewpreview.invalid"));return 0;}
        source.sendSuccess(new TranslationTextComponent("commands.bewitchment.brewpreview.header",plan.potency,plan.durationMultiplier,plan.bottles,bonuses.leonards,plan.potion().getDisplayName()),false);
        for(EffectInstance effect:plan.effects())source.sendSuccess(new TranslationTextComponent("commands.bewitchment.brewpreview.effect",effect.getEffect().getDisplayName(),effect.getAmplifier()+1,effect.getDuration()),false);
        return plan.effects().size();
    }
    private BrewPreview(){}
}