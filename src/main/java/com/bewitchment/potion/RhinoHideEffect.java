package com.bewitchment.potion;

import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.*;

/** Original UUID retained; the vanilla effect system scales and removes this modifier. */
public final class RhinoHideEffect extends Effect {
    public static final String MODIFIER="1845f14c-5411-4380-8be7-85e81317523a";
    public RhinoHideEffect(){super(EffectType.BENEFICIAL,0xBEBEBE);addAttributeModifier(Attributes.ARMOR_TOUGHNESS,MODIFIER,1,AttributeModifier.Operation.ADDITION);}
}
