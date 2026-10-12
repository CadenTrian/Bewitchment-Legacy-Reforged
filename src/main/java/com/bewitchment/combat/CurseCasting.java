package com.bewitchment.combat;

import com.bewitchment.api.event.PlayerCurseEvent;
import com.bewitchment.machine.CurseData;
import com.bewitchment.machine.CurseData.Kind;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

public final class CurseCasting {
    public static final class Outcome {
        public final boolean reflected,added;
        Outcome(boolean reflected,boolean added){this.reflected=reflected;this.added=added;}
    }
    public static boolean valid(Kind kind,int days){return kind!=null && days>=1 && days<=CurseData.MAX_DAYS;}
    /** Same-player casts must pass the same state object for caster and target. */
    public static Outcome apply(CurseData caster,CurseData target,Kind kind,int days){
        if(!valid(kind,days))return new Outcome(false,false);
        boolean reflect=kind.lesser() && target.remaining(Kind.RETURN_TO_SENDER)>0;
        if(reflect){target.remove(Kind.RETURN_TO_SENDER);caster.remove(Kind.RETURN_TO_SENDER);}
        return new Outcome(reflect,(reflect?caster:target).add(kind,days));
    }
    private static boolean online(ServerPlayerEntity player){return player!=null && player.getServer()!=null && player.getServer().getPlayerList().getPlayer(player.getUUID())==player;}
    /** Null means canceled or rejected, without consuming either ward. */
    public static Outcome cast(ServerPlayerEntity caster,ServerPlayerEntity target,Kind kind,int days){
        if(!online(caster) || !online(target) || caster.getServer()!=target.getServer() || !valid(kind,days))return null;
        PlayerCurseEvent event=new PlayerCurseEvent(caster,target,kind,days);
        if(MinecraftForge.EVENT_BUS.post(event) || !online(event.getTarget()) || event.getTarget().getServer()!=caster.getServer() || !valid(event.getCurse(),event.getDays()))return null;
        target=event.getTarget();CurseData from=PlayerCurses.read(caster),to=target==caster?from:PlayerCurses.read(target);
        Outcome result=apply(from,to,event.getCurse(),event.getDays());
        PlayerCurses.write(caster,from);if(target!=caster)PlayerCurses.write(target,to);
        return result;
    }
    public static void verify(){
        PlayerCurseEvent event=new PlayerCurseEvent(null,null,Kind.DARKNESS,7);
        net.minecraftforge.eventbus.api.IEventBus bus=net.minecraftforge.eventbus.api.BusBuilder.builder().build();
        bus.addListener((PlayerCurseEvent cast)->cast.setCanceled(true));
        if(!bus.post(event))throw new IllegalStateException("Curse event cancellation must propagate through the bus");
        if(!event.isCanceled())throw new IllegalStateException("Curse event must support cancellation");
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment curse event cancellation verified");
    }
    private CurseCasting(){}
}
