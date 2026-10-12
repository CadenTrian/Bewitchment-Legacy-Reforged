package com.bewitchment.combat;

import com.bewitchment.machine.CurseData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraftforge.registries.ForgeRegistries;

public final class ParanoiaCurse {
    private static final String[] SOUNDS={
        "minecraft:entity.enderman.scream","minecraft:entity.blaze.ambient","minecraft:entity.wolf.growl","bewitchment:werewolf_howl",
        "minecraft:entity.slime.squish","minecraft:entity.creeper.primed","minecraft:entity.spider.ambient","minecraft:entity.ender_dragon.growl",
        "minecraft:block.chest.open","minecraft:block.wooden_door.open","minecraft:entity.player.hurt","minecraft:item.flintandsteel.use",
        "minecraft:entity.zombie.ambient","minecraft:block.lava.pop","minecraft:block.glass.break","minecraft:block.stone.break",
        "minecraft:entity.ghast.ambient","minecraft:block.fence_gate.open","minecraft:block.portal.ambient","minecraft:entity.tnt.primed",
        "minecraft:entity.generic.explode","minecraft:block.grass.step","minecraft:entity.arrow.shoot","bewitchment:chalk_scribble",
        "bewitchment:cleaver_living","bewitchment:bafometyr_living","minecraft:entity.silverfish.hurt","minecraft:entity.bat.takeoff",
        "minecraft:entity.egg.throw","minecraft:entity.ender_dragon.ambient","minecraft:entity.evoker.ambient","minecraft:ambient.cave",
        "minecraft:block.glass.break","minecraft:block.wooden_pressure_plate.click_on","minecraft:entity.enderman.stare"
    };
    public static int soundCount(){return SOUNDS.length;}
    public static ResourceLocation sound(int index){return new ResourceLocation(SOUNDS[Math.floorMod(index,SOUNDS.length)]);}
    public static EffectInstance blindness(){return new EffectInstance(Effects.BLINDNESS,256,3);}
    public static void apply(PlayerEntity player,CurseData data){
        player.addEffect(blindness());
        if(data.paranoiaTick(player.getRandom().nextInt(100))){
            SoundEvent event=ForgeRegistries.SOUND_EVENTS.getValue(sound(player.getRandom().nextInt(soundCount())));
            if(event!=null)player.level.playSound(null,player.blockPosition(),event,SoundCategory.HOSTILE,1,1);
        }
    }
    public static void verify(){
        for(int i=0;i<soundCount();i++)if(!ForgeRegistries.SOUND_EVENTS.containsKey(sound(i)))throw new IllegalStateException("Missing Paranoia sound "+sound(i));
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment 35 Paranoia sound choices verified");
    }
    private ParanoiaCurse(){}
}
