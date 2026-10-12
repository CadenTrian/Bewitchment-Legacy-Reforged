package com.bewitchment.potion;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraftforge.event.ForgeEventFactory;
import javax.annotation.Nullable;

/** Instant influence requires a real player owner; command/dispenser effects do not invent one. */
public final class InfluenceBrewEffect extends InstantEffect {
    private final boolean friendship;
    public InfluenceBrewEffect(boolean friendship){super(friendship?EffectType.BENEFICIAL:EffectType.HARMFUL,friendship?0xF4C2C2:0x660000);this.friendship=friendship;}
    @Override public void applyInstantenousEffect(@Nullable Entity source,@Nullable Entity owner,LivingEntity target,int amplifier,double intensity){
        if(target.level.isClientSide||!(owner instanceof PlayerEntity)||!target.isAlive())return;
        PlayerEntity player=(PlayerEntity)owner;
        if(!CombatBrewRules.owner(true,player.isAlive(),player.isSpectator(),player.level==target.level))return;
        if(friendship){
            if(CombatBrewRules.friendship(target instanceof TameableEntity,target.getClass().getName())){
                TameableEntity animal=(TameableEntity)target;
                if(!ForgeEventFactory.onAnimalTame(animal,player))animal.tame(player);
            }
        }else if(target!=player)target.setLastHurtByMob(player);
    }
}
