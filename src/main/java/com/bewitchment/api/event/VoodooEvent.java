package com.bewitchment.api.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import javax.annotation.Nullable;

/** Fired on the logical server before an eligible needle or lava attempt. */
@Cancelable
public class VoodooEvent extends Event {
    public enum Action { NEEDLE, LAVA }
    @Nullable private final PlayerEntity caster;
    private final LivingEntity target;
    private final Action action;
    public VoodooEvent(@Nullable PlayerEntity caster, LivingEntity target) { this(caster, target, Action.NEEDLE); }
    public VoodooEvent(@Nullable PlayerEntity caster, LivingEntity target, Action action) {
        this.caster = caster; this.target = target; this.action = action;
    }
    @Nullable public PlayerEntity getCaster() { return caster; }
    public LivingEntity getTarget() { return target; }
    public Action getAction() { return action; }
}
