package com.bewitchment.trade;

import com.bewitchment.Bewitchment;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.*;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.text.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Random;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class DemonTradePreview {
    @SubscribeEvent public static void commands(RegisterCommandsEvent event){
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> root=Commands.literal("demontrades").requires(s->s.hasPermission(2));
        for(String career:DemonTrades.careers())root.then(Commands.literal(career).then(Commands.argument("level",IntegerArgumentType.integer(1,5))
            .executes(ctx->preview(ctx.getSource(),career,IntegerArgumentType.getInteger(ctx,"level"),false))
            .then(Commands.argument("discounted",BoolArgumentType.bool()).executes(ctx->preview(ctx.getSource(),career,IntegerArgumentType.getInteger(ctx,"level"),BoolArgumentType.getBool(ctx,"discounted"))))));
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> progression=Commands.literal("progression");
        for(String career:DemonTrades.careers())progression.then(Commands.literal(career)
            .executes(ctx->progression(ctx.getSource(),career,false))
            .then(Commands.argument("discounted",BoolArgumentType.bool()).executes(ctx->progression(ctx.getSource(),career,BoolArgumentType.getBool(ctx,"discounted")))));
        root.then(progression);
        event.getDispatcher().register(root);
    }
    private static int preview(CommandSource source,String career,int level,boolean discounted){
        Random random=new Random();int count=0;
        source.sendSuccess(new TranslationTextComponent("commands.bewitchment.demontrades.header",career,level,discounted),false);
        for(DemonTrades.Entry entry:DemonTrades.select(career,level,random)){
            MerchantOffer offer=DemonTrades.generate(entry,random,discounted);
            source.sendSuccess(new TranslationTextComponent("commands.bewitchment.demontrades.offer",offer.getCostA().getCount(),offer.getCostA().getDisplayName(),offer.getResult().getCount(),offer.getResult().getDisplayName(),offer.getMaxUses()),false);count++;
        }
        return count;
    }
    private static int progression(CommandSource source,String career,boolean discounted){
        Random random=new Random();DemonMerchantState state=new DemonMerchantState(career);state.open(random,discounted);
        source.sendSuccess(new TranslationTextComponent("commands.bewitchment.demontrades.progression",career,1,state.offers().size(),0),false);
        for(int next=2;next<=5;next++){
            DemonMerchantState.TradeResult trade=state.completeTrade(state.offers().get(state.offers().size()-1),null,random);
            for(int tick=0;tick<40;tick++)state.tick(false,discounted,random);
            state=DemonMerchantState.load(state.save());
            source.sendSuccess(new TranslationTextComponent("commands.bewitchment.demontrades.progression",career,state.level(),state.offers().size(),trade.xp),false);
        }
        return state.offers().size();
    }
    private DemonTradePreview(){}
}