package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.machine.CurseData;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class PlayerCurses {
    public static final String KEY="bewitchment:curses";
    public static CurseData read(PlayerEntity player){CurseData data=new CurseData();data.load(player.getPersistentData().getCompound(KEY));return data;}
    public static void write(PlayerEntity player,CurseData data){if(data.active().isEmpty())player.getPersistentData().remove(KEY);else player.getPersistentData().put(KEY,data.save());}
    public static boolean proc(double roll){return roll>=0 && roll<.0005;}
    public static boolean eligible(Effect effect,ResourceLocation id){return effect.getCategory()==EffectType.HARMFUL && !effect.isInstantenous()
        && effect!=Effects.WITHER && effect!=Effects.POISON && !new ResourceLocation("bewitchment","mortal_coil").equals(id)
        && !new ResourceLocation("bewitchment","wednesday").equals(id);}
    public static int misfortuneDuration(int roll){return (Math.max(0,Math.min(19,roll))+10)*20;}
    private static void apply(PlayerEntity player,CurseData.Kind kind){
        if(kind==CurseData.Kind.EVERFLAME){EverflameCurse.apply(player);return;}
        if(kind==CurseData.Kind.DARKNESS){
            player.addEffect(new EffectInstance(Effects.BLINDNESS,100));
            AxisAlignedBB area=new AxisAlignedBB(player.getX()-4,player.getY()-4,player.getZ()-4,player.getX()+4,player.getY()+4,player.getZ()+4);
            for(MobEntity mob:player.level.getEntitiesOfClass(MobEntity.class,area,e->e.isAlive()))mob.addEffect(new EffectInstance(Effects.BLINDNESS,200));
        }else if(kind==CurseData.Kind.CONFLAGRATION){
            if(EnvironmentalCurseRules.conflagration(player.tickCount))player.addEffect(new EffectInstance(com.bewitchment.registry.ModEffects.HELLFIRE.get(),200,0,false,false));
        }else if(kind==CurseData.Kind.OVERHEATING){
            ResourceLocation biome=player.level.registryAccess().registryOrThrow(net.minecraft.util.registry.Registry.BIOME_REGISTRY).getKey(player.level.getBiome(player.blockPosition()));
            if(biome!=null && net.minecraftforge.common.BiomeDictionary.hasType(net.minecraft.util.RegistryKey.create(net.minecraft.util.registry.Registry.BIOME_REGISTRY,biome),net.minecraftforge.common.BiomeDictionary.Type.HOT)){
                player.setSecondsOnFire(5);player.addEffect(new EffectInstance(com.bewitchment.registry.ModEffects.HELLFIRE.get(),200));
            }
        }else if(kind==CurseData.Kind.HYDROPHOBIA){
            if(player.isInWaterOrRain())player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,120,1));
        }else if(kind==CurseData.Kind.SOLAR_HATRED){
            if(EnvironmentalCurseRules.sunlight(player.level.dimensionType().hasSkyLight(),player.level.getBrightness(net.minecraft.world.LightType.SKY,player.blockPosition()),player.level.getSkyDarken())){
                player.setSecondsOnFire(2);player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,100,1));
                player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN,100,1));player.addEffect(new EffectInstance(Effects.WEAKNESS,100,1));
            }        }else if(kind==CurseData.Kind.LIGHTNING_ROD){
            GreaterCurseEffects.strike(player);
        }else if(kind==CurseData.Kind.PALOR_PESTILENCE){
            for(EffectInstance effect:GreaterCurseEffects.pestilence())player.addEffect(effect);
        }else if(kind==CurseData.Kind.MISFORTUNE){
            List<Effect> effects=new ArrayList<>();for(Effect effect:ForgeRegistries.POTIONS.getValues())if(eligible(effect,effect.getRegistryName()))effects.add(effect);
            effects.sort(Comparator.comparing(e->e.getRegistryName().toString()));
            if(!effects.isEmpty())player.addEffect(new EffectInstance(effects.get(player.getRandom().nextInt(effects.size())),misfortuneDuration(player.getRandom().nextInt(20)),0));
        }
    }
    public static void cleanse(PlayerEntity player,boolean lesser){
        if(player.level.isClientSide || !player.isAlive() || player.isSpectator())return;
        CurseData data=read(player);
        if(data.cleanse(lesser,()->player.getRandom().nextDouble())>0)write(player,data);
    }
    public static void purify(LivingEntity target){
        if(!(target instanceof PlayerEntity) || target.level.isClientSide || !target.isAlive() || target.isSpectator())return;
        PlayerEntity player=(PlayerEntity)target;CurseData data=read(player);if(data.purify(()->player.getRandom().nextDouble())>0)write(player,data);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        PlayerEntity player=event.player;
        if(event.phase!=TickEvent.Phase.END || player.level.isClientSide || !player.getPersistentData().contains(KEY,10))return;
        CurseData data=read(player);
        if(MahapadmaCurse.consume(data,player.isAlive(),player.isSpectator())){
            write(player,data); // Remove before damage can trigger death or cloning.
            MahapadmaCurse.apply(player);
            data=read(player);
        }
        if(player.isAlive() && !player.isSpectator())for(CurseData.Kind kind:data.active())if(kind.chance>0 && EnvironmentalCurseRules.proc(kind,player.getRandom().nextDouble())){if(kind==CurseData.Kind.PARANOIA)ParanoiaCurse.apply(player,data);else apply(player,kind);}
        if(player.tickCount%20==0)data.second();
        write(player,data);
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){
        if(event.getPlayer().level.isClientSide)return;
        CompoundNBT old=event.getOriginal().getPersistentData(),next=event.getPlayer().getPersistentData();next.remove(KEY);
        if(old.contains(KEY,10))next.put(KEY,old.getCompound(KEY).copy());
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void wetDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event){
        if(event.isCanceled() || !(event.getEntityLiving() instanceof PlayerEntity) || event.getEntityLiving().level.isClientSide || !AccessoryRules.validDamage(event.getAmount()))return;
        PlayerEntity player=(PlayerEntity)event.getEntityLiving();
        if(player.isInWaterOrRain())event.setAmount(EnvironmentalCurseRules.wetDamage(event.getAmount(),true,read(player).remaining(CurseData.Kind.HYDROPHOBIA)>0));
    }
    private PlayerCurses(){}
}
