package com.bewitchment.api.event;

import com.bewitchment.machine.CurseData.Kind;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.*;

/** Posted before a cast consumes reflection wards or changes curse state. Duration is in days. */
@Cancelable
public final class PlayerCurseEvent extends Event {
    private final ServerPlayerEntity caster;
    private ServerPlayerEntity target;
    private Kind curse;
    private int days;
    public PlayerCurseEvent(ServerPlayerEntity caster,ServerPlayerEntity target,Kind curse,int days){this.caster=caster;this.target=target;this.curse=curse;this.days=days;}
    public ServerPlayerEntity getCaster(){return caster;}
    public ServerPlayerEntity getTarget(){return target;}
    public void setTarget(ServerPlayerEntity target){this.target=target;}
    public Kind getCurse(){return curse;}
    public void setCurse(Kind curse){this.curse=curse;}
    public int getDays(){return days;}
    public void setDays(int days){this.days=days;}
}
