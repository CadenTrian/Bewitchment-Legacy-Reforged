package com.bewitchment.combat;
import com.bewitchment.Bewitchment;
import com.bewitchment.machine.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class PlayerFortunes {
    public static final String KEY="bewitchment:fortune";
    public static FortuneData read(PlayerEntity player){FortuneData data=new FortuneData();data.load(player.getPersistentData().getCompound(KEY));return data;}
    public static void write(PlayerEntity player,FortuneData data){if(data.kind()==null)player.getPersistentData().remove(KEY);else player.getPersistentData().put(KEY,data.save());}
    public static boolean eligible(PlayerEntity player){return !player.level.isClientSide&&player.isAlive()&&!player.isSpectator();}
    public static void assign(PlayerEntity player,FortuneData.Kind kind){FortuneData data=new FortuneData();data.assign(kind,player.getRandom().nextInt(kind.maxSeconds-kind.minSeconds));write(player,data);}
    public static boolean execute(PlayerEntity player){
        if(!eligible(player))return false;
        return FortuneDispatch.executeIf(player.getUUID(),new FortuneDispatch.Store(){public FortuneData read(){return PlayerFortunes.read(player);}public void write(FortuneData data){PlayerFortunes.write(player,data);}},kind->!kind.digging(),kind->FortuneEffects.apply(player,kind));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!eligible(event.player)||!event.player.getPersistentData().contains(KEY,10))return;
        FortuneData data=read(event.player);boolean due=data.tick();write(event.player,data);if(due && !data.kind().digging())execute(event.player);
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){if(!event.getPlayer().level.isClientSide)write(event.getPlayer(),read(event.getOriginal()));}
    private PlayerFortunes(){}
}
