package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.machine.CurseData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.*;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Legacy operator command, limited to implemented curses and bounded positive durations. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class CurseCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSource,net.minecraft.command.arguments.EntitySelector> player=Commands.argument("player",EntityArgument.player());
        player.then(Commands.literal("get").executes(ctx->{
            ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");CurseData data=PlayerCurses.read(target);
            StringBuilder text=new StringBuilder();for(CurseData.Kind kind:data.active()){if(text.length()>0)text.append(", ");text.append(kind.id).append(" (").append(data.remaining(kind)).append(" ticks)");}
            ctx.getSource().sendSuccess(new TranslationTextComponent("commands.bewitchment.curse.list",target.getDisplayName(),text.length()==0?"[]":text.toString()),false);return data.active().size();
        }));
        player.then(Commands.literal("clear").executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");CurseData data=PlayerCurses.read(target);int count=data.clear();PlayerCurses.write(target,data);feedback(ctx.getSource(),target,count);return count;}));
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> add=Commands.literal("add"),remove=Commands.literal("remove"),cast=Commands.literal("cast");
        for(CurseData.Kind kind:CurseData.Kind.values()){
            add.then(Commands.literal(kind.id).then(Commands.argument("days",IntegerArgumentType.integer(1,CurseData.MAX_DAYS)).executes(ctx->{
                ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");CurseData data=PlayerCurses.read(target);int count=data.add(kind,IntegerArgumentType.getInteger(ctx,"days"))?1:0;PlayerCurses.write(target,data);feedback(ctx.getSource(),target,count);return count;
            })));
            cast.then(Commands.literal(kind.id).then(Commands.argument("days",IntegerArgumentType.integer(1,CurseData.MAX_DAYS)).executes(ctx->{
                ServerPlayerEntity caster=ctx.getSource().getPlayerOrException(),target=EntityArgument.getPlayer(ctx,"player");
                CurseCasting.Outcome result=CurseCasting.cast(caster,target,kind,IntegerArgumentType.getInteger(ctx,"days"));
                String status=result==null?"rejected":result.reflected?(result.added?"reflected":"reflected_duplicate"):(result.added?"applied":"duplicate");
                ctx.getSource().sendSuccess(new TranslationTextComponent("commands.bewitchment.curse.cast."+status),true);
                return result!=null && (result.added || result.reflected)?1:0;
            })));
            remove.then(Commands.literal(kind.id).executes(ctx->{ServerPlayerEntity target=EntityArgument.getPlayer(ctx,"player");CurseData data=PlayerCurses.read(target);int count=data.remove(kind)?1:0;PlayerCurses.write(target,data);feedback(ctx.getSource(),target,count);return count;}));
        }
        player.then(add).then(remove).then(cast);event.getDispatcher().register(Commands.literal("curse").requires(source->source.hasPermission(2)).then(player));
    }
    private static void feedback(CommandSource source,ServerPlayerEntity target,int count){source.sendSuccess(new TranslationTextComponent("commands.bewitchment.curse.changed",count,target.getDisplayName()),true);}
    private CurseCommands(){}
}
