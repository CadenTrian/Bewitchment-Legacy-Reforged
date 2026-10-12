package com.bewitchment.potion;

import net.minecraft.potion.*;

/** Legacy magic vulnerability is added at LivingDamageEvent, after mitigation. */
public final class MagicWeaknessEffect extends Effect {
    public MagicWeaknessEffect(){super(EffectType.HARMFUL,0x7f00c0);}
    public static float damage(float amount,int amplifier,boolean magic){return magic?amount+3F*(Math.max(0,amplifier)+1F):amount;}
}
