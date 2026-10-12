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
public final class DesertificationEffect extends TerrainBrewEffect {
    public DesertificationEffect(){super(EffectType.BENEFICIAL,0xffc400);}
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        if(DesertificationRules.susceptible(target.getClass()))target.hurt(DamageSource.MAGIC,DesertificationRules.damage(amplifier));
    }
    private static DesertificationWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(DesertificationWork::new,DesertificationWork.ID);}
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
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return DesertificationEffect.allowed(world,actor);}
            private boolean editable(BlockPos pos){return DesertificationEffect.allowed(world,actor)&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public void visit(BlockPos pos){visit(pos,true);}
            @Override public void visit(BlockPos pos,boolean selected){
                if(!editable(pos))return;BlockState before=world.getBlockState(pos);
                BlockState after=DesertificationRules.replacement(before,selected);
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
