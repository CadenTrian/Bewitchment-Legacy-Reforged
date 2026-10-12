package com.bewitchment.potion;

import com.bewitchment.Bewitchment;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class BlightEffect extends TerrainBrewEffect {
    private static final ResourceLocation DEMONS=new ResourceLocation("bewitchment","demons"),SPIRITS=new ResourceLocation("bewitchment","spirits");
    public BlightEffect(){super(EffectType.HARMFUL,0x7f4000);}
    private static boolean candidate(MobEntity source){return BlightRules.conversionCandidate(source.isAlive(),source.getType()==EntityType.MOOSHROOM,source.isPassenger(),source.isVehicle(),source.isLeashed());}
    @Override protected void affectLiving(LivingEntity original,int amplifier){
        LivingEntity target=original;
        if(original instanceof MooshroomEntity&&candidate((MobEntity)original)){
            MooshroomEntity source=(MooshroomEntity)original;CowEntity cow=EntityType.COW.create(source.level);
            if(cow!=null){
                boolean converted=BlightRules.convert(source.getUUID(),()->{
                    if(!ForgeEventFactory.canLivingConvert(source,EntityType.COW,timer->{})||!candidate(source))return false;
                    cow.moveTo(source.getX(),source.getY(),source.getZ(),source.yRot,source.xRot);
                    cow.setAge(source.getAge());cow.setNoAi(source.isNoAi());cow.setInvulnerable(source.isInvulnerable());
                    cow.setCustomName(source.getCustomName());cow.setCustomNameVisible(source.isCustomNameVisible());
                    if(source.isPersistenceRequired())cow.setPersistenceRequired();
                    cow.setHealth(Math.min(cow.getMaxHealth(),source.getHealth()));
                    return ((ServerWorld)source.level).addFreshEntity(cow);
                },source::remove);
                if(converted){ForgeEventFactory.onLivingConvert(source,cow);target=cow;}
            }
        }
        if(target.isAlive()&&BlightRules.susceptible(target.getMobType(),EntityTypeTags.getAllTags().getTagOrEmpty(DEMONS).contains(target.getType()),EntityTypeTags.getAllTags().getTagOrEmpty(SPIRITS).contains(target.getType()))){
            target.addEffect(new EffectInstance(Effects.WEAKNESS,BlightRules.duration(amplifier),1));
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,BlightRules.duration(amplifier),1));
        }
    }
    private static BlightWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(BlightWork::new,BlightWork.ID);}
    private static boolean allowed(ServerWorld world,PlayerEntity actor){return actor!=null&&BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        return allowed(world,actor)&&work(world).enqueue(center,amplifier,world.random.nextLong(),owner instanceof PlayerEntity?owner.getUUID():null)?1:0;
    }
    @SubscribeEvent public static void tick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.world instanceof ServerWorld))return;
        ServerWorld world=(ServerWorld)event.world;
        work(world).tick(new SampledTerrainWork.Visitor(){
            private PlayerEntity actor;
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return BlightEffect.allowed(world,actor);}
            private boolean editable(BlockPos pos){return BlightEffect.allowed(world,actor)&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public void visit(BlockPos pos){
                if(!editable(pos))return;BlockState before=world.getBlockState(pos);
                BlightRules.apply(before,new BlightRules.Target(){
                    public boolean permitted(){return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor));}
                    public boolean unchanged(){return editable(pos)&&world.getBlockState(pos).equals(before);}
                    public List<ItemStack> loot(){return Block.getDrops(before,world,pos,null,actor,ItemStack.EMPTY);}
                    public boolean replace(BlockState replacement){
                        if(!unchanged()||!world.setBlock(pos,replacement,3))return false;
                        if(BlightRules.action(before)==BlightRules.Action.DESTROY)world.levelEvent(2001,pos,Block.getId(before));
                        return true;
                    }
                    public void drop(ItemStack stack){Block.popResource(world,pos,stack);}
                });
            }
        });
    }
}
