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
public final class SporeCloudEffect extends TerrainBrewEffect {
    public SporeCloudEffect(){super(EffectType.HARMFUL,0x7f3f7f);}
    private static boolean candidate(MobEntity source){return BlightRules.conversionCandidate(source.isAlive(),source.getType()==EntityType.COW,source.isPassenger(),source.isVehicle(),source.isLeashed());}
    @Override protected void affectLiving(LivingEntity original,int amplifier){
        if(!(original instanceof CowEntity)||!candidate((MobEntity)original))return;
        CowEntity source=(CowEntity)original;MooshroomEntity mushroom=EntityType.MOOSHROOM.create(source.level);
        if(mushroom==null)return;
        boolean converted=BlightRules.convert(source.getUUID(),()->{
            if(!ForgeEventFactory.canLivingConvert(source,EntityType.MOOSHROOM,timer->{})||!candidate(source))return false;
            mushroom.moveTo(source.getX(),source.getY(),source.getZ(),source.yRot,source.xRot);
            mushroom.setAge(source.getAge());mushroom.setNoAi(source.isNoAi());mushroom.setInvulnerable(source.isInvulnerable());
            mushroom.setCustomName(source.getCustomName());mushroom.setCustomNameVisible(source.isCustomNameVisible());
            if(source.isPersistenceRequired())mushroom.setPersistenceRequired();
            mushroom.setHealth(Math.min(mushroom.getMaxHealth(),source.getHealth()));
            return ((ServerWorld)source.level).addFreshEntity(mushroom);
        },source::remove);
        if(converted)ForgeEventFactory.onLivingConvert(source,mushroom);
    }
    private static SporeCloudWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(SporeCloudWork::new,SporeCloudWork.ID);}
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
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return SporeCloudEffect.allowed(world,actor);}
            private boolean editable(BlockPos pos){return SporeCloudEffect.allowed(world,actor)&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public void visit(BlockPos pos){
                if(!editable(pos))return;BlockState before=world.getBlockState(pos);
                BlockState after=SporeCloudRules.replacement(before,world.random.nextBoolean(),net.minecraftforge.registries.ForgeRegistries.BLOCKS::getValue);
                if(after==null||after.equals(before))return;
                if((after.getBlock() instanceof BushBlock)&&!after.canSurvive(world,pos))return;
                if(MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor)))return;
                if(!editable(pos)||!world.getBlockState(pos).equals(before))return;
                if((after.getBlock() instanceof BushBlock)&&!after.canSurvive(world,pos))return;
                world.setBlock(pos,after,3);
            }
        });
    }
}
