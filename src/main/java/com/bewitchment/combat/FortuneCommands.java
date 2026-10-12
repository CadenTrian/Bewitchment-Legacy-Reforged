package com.bewitchment.combat;
import com.bewitchment.Bewitchment;
import com.bewitchment.machine.FortuneData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.*;
import net.minecraft.command.*;
import net.minecraft.command.arguments.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class FortuneCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){register(event.getDispatcher());}
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        RequiredArgumentBuilder<CommandSource,EntitySelector> player=Commands.argument("player",EntityArgument.player());
        player.then(Commands.literal("get").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");FortuneData data=PlayerFortunes.read(target);
            ctx.getSource().sendSuccess(new TranslationTextComponent("commands.bewitchment.fortune.get",target.getDisplayName(),data.kind()==null?"[]":data.kind().key(),data.remaining()),false);return data.kind()==null?0:1;}));
        player.then(Commands.literal("remove").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");FortuneData data=PlayerFortunes.read(target);boolean changed=data.take()!=null;PlayerFortunes.write(target,data);feedback(ctx.getSource(),target,changed?"removed":"none");return changed?1:0;}));
        player.then(Commands.literal("execute").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");boolean changed=PlayerFortunes.execute(target);feedback(ctx.getSource(),target,changed?"executed":"unavailable");return changed?1:0;}));
        LiteralArgumentBuilder<CommandSource> set=Commands.literal("set");
        for(FortuneData.Kind kind:FortuneData.Kind.values())for(String id:new String[]{kind.id,kind.key()})set.then(Commands.literal(id).executes(ctx->{
            ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");PlayerFortunes.assign(target,kind);
            ctx.getSource().sendSuccess(new TranslationTextComponent("commands.bewitchment.fortune.set",target.getDisplayName(),kind.key(),PlayerFortunes.read(target).remaining()),true);return 1;
        }));
        dispatcher.register(Commands.literal("fortune").requires(source->source.hasPermission(2)).then(player.then(set)));
    }
    private static void feedback(CommandSource source,ServerPlayerEntity target,String outcome){source.sendSuccess(new TranslationTextComponent("commands.bewitchment.fortune."+outcome,target.getDisplayName()),true);}
    private FortuneCommands(){}
}