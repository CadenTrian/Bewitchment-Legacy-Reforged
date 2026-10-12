package com.bewitchment.entity;

import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.*;
import net.minecraft.world.server.*;
import java.util.*;

/** Shared legacy boss bar; concrete greater demons supply their patron and behavior. */
public abstract class GreaterDemonEntity extends MonsterEntity implements Pledgeable {
    private final Set<ServerPlayerEntity> tracking=new HashSet<>();
    private final ServerBossInfo boss=new ServerBossInfo(getDisplayName(),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
    protected GreaterDemonEntity(EntityType<? extends GreaterDemonEntity> type,World world){super(type,world);boss.setDarkenScreen(false);}
    public static float healthFraction(float health,float maximum){
        if(!Float.isFinite(health) || !Float.isFinite(maximum) || maximum<=0)return 0;
        return Math.max(0,Math.min(1,health/maximum));
    }
    public static boolean showBar(boolean tracked,boolean alive,boolean pledged){return tracked && alive && !pledged;}
    private void refresh(ServerPlayerEntity player){
        if(!(level instanceof ServerWorld))return;
        boolean pledged=DemonPledges.get((ServerWorld)level).has(player.getUUID(),patron());
        if(showBar(tracking.contains(player),isAlive() && !removed,pledged))boss.addPlayer(player);else boss.removePlayer(player);
    }
    @Override public void startSeenByPlayer(ServerPlayerEntity player){super.startSeenByPlayer(player);tracking.add(player);refresh(player);}
    @Override public void stopSeenByPlayer(ServerPlayerEntity player){super.stopSeenByPlayer(player);tracking.remove(player);boss.removePlayer(player);}
    @Override public void pledgeChanged(PlayerEntity player,boolean pledged){if(player instanceof ServerPlayerEntity)refresh((ServerPlayerEntity)player);}
    @Override public void tick(){
        super.tick();if(level.isClientSide)return;
        boss.setName(getDisplayName());boss.setPercent(healthFraction(getHealth(),getMaxHealth()));
        // Includes operator changes and revocations affecting other instances of this patron.
        for(ServerPlayerEntity player:new ArrayList<>(tracking)){
            if(player.hasDisconnected() || player.level!=level){tracking.remove(player);boss.removePlayer(player);}else refresh(player);
        }
    }
    @Override public void die(DamageSource source){boss.removeAllPlayers();tracking.clear();super.die(source);}
    @Override public void remove(){boss.removeAllPlayers();tracking.clear();super.remove();}
}