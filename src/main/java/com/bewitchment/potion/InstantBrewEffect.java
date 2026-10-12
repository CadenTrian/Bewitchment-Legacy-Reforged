package com.bewitchment.potion;

import com.bewitchment.combat.MaterialEffects;
import net.minecraft.entity.*;
import net.minecraft.potion.*;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.DamageSource;
import javax.annotation.Nullable;

/** Both drinking and splash/cloud dispatch reach the same server-only action. */
public final class InstantBrewEffect extends InstantEffect {
    private final boolean holy;
    public InstantBrewEffect(boolean holy){super(holy?EffectType.BENEFICIAL:EffectType.HARMFUL,holy?0x7f7fff:0x00c4ff);this.holy=holy;}
    @Override public void applyEffectTick(LivingEntity target,int amplifier){applyInstantenousEffect(null,null,target,amplifier,1);}
    @Override public void applyInstantenousEffect(@Nullable Entity source,@Nullable Entity owner,LivingEntity target,int amplifier,double intensity){
        if(target.level.isClientSide)return;
        if(!holy){target.removeEffect(Effects.INVISIBILITY);return;}
        boolean weak=EntityTypeTags.getAllTags().getTagOrEmpty(MaterialEffects.COLD_IRON_WEAK.getName()).contains(target.getType());
        if(DefensiveBrewEffects.holyTarget(target.getMobType()==CreatureAttribute.UNDEAD,target.fireImmune(),weak))
            target.hurt(source==null?DamageSource.MAGIC:DamageSource.indirectMagic(source,owner),DefensiveBrewEffects.holyDamage(amplifier));
        // The original Holy Water ignores splash-distance intensity.
    }
}
