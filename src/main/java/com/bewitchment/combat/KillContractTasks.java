package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.*;
import com.bewitchment.registry.ModItems;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class KillContractTasks {
    public static boolean qualifies(CurseData.Kind kind,boolean villager,boolean player,boolean animal){return kind==CurseData.Kind.BERSERKER?(villager || player):kind==CurseData.Kind.DEVOURING_RAGE && animal;}
    public static boolean qualifies(CurseData.Kind kind,LivingEntity victim){return qualifies(kind,victim instanceof VillagerEntity,victim instanceof PlayerEntity,victim instanceof AnimalEntity);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent event){
        if(event.isCanceled() || event.getEntityLiving().level.isClientSide || !(event.getSource().getEntity() instanceof ServerPlayerEntity))return;
        ServerPlayerEntity player=(ServerPlayerEntity)event.getSource().getEntity();if(!player.isAlive() || player.isSpectator())return;
        boolean changed=false;
        for(int i=0;i<player.inventory.getContainerSize();i++){ItemStack stack=player.inventory.getItem(i);KillContractData data=KillContractItem.data(stack);if(data!=null && KillContractData.advance(stack.getTag(),player.getUUID(),qualifies(data.kind,event.getEntityLiving())))changed=true;}
        if(changed)player.inventory.setChanged();
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event){
        com.mojang.brigadier.builder.RequiredArgumentBuilder<net.minecraft.command.CommandSource,net.minecraft.command.arguments.EntitySelector> target=Commands.argument("player",EntityArgument.player());
        for(CurseData.Kind kind:ContractTasks.kinds())target.then(Commands.literal(kind.id).executes(ctx->{
            ServerPlayerEntity player=EntityArgument.getPlayer(ctx,"player");
            return give(player,ContractTasks.generate(ModItems.CONTRACT.get(),kind,player.getRandom()));
        }));
        target.then(Commands.literal("random").executes(ctx->{
            ServerPlayerEntity player=EntityArgument.getPlayer(ctx,"player");
            return give(player,ContractTasks.random(ModItems.CONTRACT.get(),player.getRandom()));
        }));
        event.getDispatcher().register(Commands.literal("contract").requires(source->source.hasPermission(2)).then(target));
    }
    private static int give(ServerPlayerEntity player,ItemStack task){
        if(!player.inventory.add(task))player.drop(task,false);
        player.inventory.setChanged();
        return 1;
    }
    private KillContractTasks(){}
}
