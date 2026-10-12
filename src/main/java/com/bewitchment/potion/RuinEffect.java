package com.bewitchment.potion;

import com.bewitchment.Bewitchment;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class RuinEffect extends TerrainBrewEffect {
    public RuinEffect(){super(EffectType.HARMFUL,0x606060);}
    public static int duration(int amplifier){return 200*UtilityBrewEffect.level(amplifier);}
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        target.addEffect(new EffectInstance(Effects.WEAKNESS,duration(amplifier),1));
        target.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN,duration(amplifier),1));
    }
    @Nullable public static BlockState replacement(BlockState state){
        if(state.hasTileEntity())return null;Block block=state.getBlock();
        if(block==Blocks.STONE||block==Blocks.GRANITE||block==Blocks.POLISHED_GRANITE||block==Blocks.DIORITE||block==Blocks.POLISHED_DIORITE||block==Blocks.ANDESITE||block==Blocks.POLISHED_ANDESITE)return Blocks.COBBLESTONE.defaultBlockState();
        if(block==Blocks.COBBLESTONE)return Blocks.GRAVEL.defaultBlockState();
        if(block==Blocks.GRAVEL||block==Blocks.DIRT||block==Blocks.COARSE_DIRT||block==Blocks.PODZOL||block==Blocks.GRASS_BLOCK)return Blocks.SAND.defaultBlockState();
        if(block instanceof FlowerBlock||block instanceof TallGrassBlock||block instanceof CropsBlock)return Blocks.DEAD_BUSH.defaultBlockState();
        return null;
    }
    private static RuinWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(RuinWork::new,RuinWork.ID);}
    private static boolean allowed(ServerWorld world,PlayerEntity actor){return actor!=null&&BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        if(!allowed(world,actor))return 0;
        return work(world).enqueue(center,amplifier,world.random.nextLong(),owner instanceof PlayerEntity?owner.getUUID():null)?1:0;
    }
    @SubscribeEvent public static void tick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.world instanceof ServerWorld))return;
        ServerWorld world=(ServerWorld)event.world;
        work(world).tick(new RuinWork.Visitor(){
            private PlayerEntity actor;
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return RuinEffect.allowed(world,actor);}
            public void visit(BlockPos pos){
                if(!world.hasChunkAt(pos)||!world.getWorldBorder().isWithinBounds(pos))return;
                BlockState before=world.getBlockState(pos),after=replacement(before);
                if(after==null||!world.mayInteract(actor,pos)||!actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY))return;
                if(after.getBlock()==Blocks.DEAD_BUSH&&!after.canSurvive(world,pos))return;
                if(MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor)))return;
                if(!RuinEffect.allowed(world,actor)||!world.hasChunkAt(pos)||!world.getBlockState(pos).equals(before))return;
                if(after.getBlock()==Blocks.DEAD_BUSH&&!after.canSurvive(world,pos))return;
                world.setBlock(pos,after,3);
            }
        });
    }
}
