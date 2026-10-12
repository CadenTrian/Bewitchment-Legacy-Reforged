package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.world.DemonPledges;
import com.bewitchment.world.DemonPledges.Patron;
import net.minecraft.command.*;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Operator-only inspection and testing until greater-demon acquisition is ported. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class PledgeCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSource,net.minecraft.command.arguments.EntitySelector> player=Commands.argument("player",EntityArgument.player());
        player.then(Commands.literal("get").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");DemonPledges data=DemonPledges.get(target.getLevel());
            String names=data.patrons(target.getUUID()).stream().map(p->p.id).collect(java.util.stream.Collectors.joining(", "));
            ctx.getSource().sendSuccess(new TranslationTextComponent("commands.bewitchment.pledge.list",target.getDisplayName(),names.isEmpty()?"[]":names),false);return data.patrons(target.getUUID()).size();}));
        player.then(Commands.literal("clear").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");int count=DemonPledges.get(target.getLevel()).clear(target.getUUID());feedback(ctx.getSource(),target,count);return count;}));
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> add=Commands.literal("add"),remove=Commands.literal("remove");
        for(Patron patron:Patron.values()){
            add.then(Commands.literal(patron.id).executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");int count=DemonPledges.get(target.getLevel()).add(target.getUUID(),patron)?1:0;feedback(ctx.getSource(),target,count);return count;}));
            remove.then(Commands.literal(patron.id).executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");int count=DemonPledges.get(target.getLevel()).remove(target.getUUID(),patron)?1:0;feedback(ctx.getSource(),target,count);return count;}));
        }
        event.getDispatcher().register(Commands.literal("pledge").requires(source->source.hasPermission(2)).then(player.then(add).then(remove)));
    }
    private static void feedback(CommandSource source,ServerPlayerEntity target,int count){source.sendSuccess(new TranslationTextComponent("commands.bewitchment.pledge.changed",count,target.getDisplayName()),true);}
    private PledgeCommands(){}
}