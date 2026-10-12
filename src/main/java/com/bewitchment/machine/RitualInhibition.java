package com.bewitchment.machine;

import com.bewitchment.Bewitchment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-owned Failure state. Player persistent data saves it without per-tick packets. */
@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class RitualInhibition {
    public static final int DURATION=24000;
    static final String KEY="bewitchment:ritual_inhibition";
    static int remaining(CompoundNBT state){return Math.max(0,Math.min(DURATION,state.getInt("remaining")));}
    static boolean blocked(CompoundNBT state){return remaining(state)>0 || state.getBoolean("blocked");}
    static void reset(CompoundNBT data){
        CompoundNBT state=new CompoundNBT();state.putInt("remaining",DURATION);state.putBoolean("blocked",true);data.put(KEY,state);
    }
    static void advance(CompoundNBT data){
        if(!data.contains(KEY))return;
        CompoundNBT state=data.getCompound(KEY);int time=remaining(state);
        // Legacy clears canRitual on the tick after its countdown reaches zero.
        if(time==0){data.remove(KEY);return;}
        state.putInt("remaining",time-1);state.putBoolean("blocked",true);
    }
    static void copy(CompoundNBT original,CompoundNBT replacement){
        replacement.remove(KEY);
        if(original.contains(KEY,10))replacement.put(KEY,original.getCompound(KEY).copy());
    }
    public static boolean blocked(PlayerEntity player){return blocked(player.getPersistentData().getCompound(KEY));}
    public static void apply(LivingEntity target){
        if(!(target instanceof PlayerEntity) || !(target.level instanceof ServerWorld) || !target.isAlive() || target.isSpectator())return;
        reset(target.getPersistentData());
        ((ServerWorld)target.level).sendParticles(ParticleTypes.LARGE_SMOKE,target.getX(),target.getY(),target.getZ(),100,.5,1,.5,.05);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase==TickEvent.Phase.END && !event.player.level.isClientSide)advance(event.player.getPersistentData());
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){
        if(!event.getPlayer().level.isClientSide)copy(event.getOriginal().getPersistentData(),event.getPlayer().getPersistentData());
    }
    private RitualInhibition(){}
}
