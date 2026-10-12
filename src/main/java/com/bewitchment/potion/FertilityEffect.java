package com.bewitchment.potion;

import com.bewitchment.Bewitchment;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class FertilityEffect extends TerrainBrewEffect {
    public FertilityEffect(){super(EffectType.BENEFICIAL,0x40c000);}
    public static boolean readyAnimal(boolean alive,int age,boolean canFallInLove){return alive&&age==0&&canFallInLove;}
    public static boolean eligible(BlockState state){return state.getBlock() instanceof IGrowable&&!state.hasTileEntity();}
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        if(target instanceof AnimalEntity){AnimalEntity animal=(AnimalEntity)target;if(readyAnimal(animal.isAlive(),animal.getAge(),animal.canFallInLove()))animal.setInLove(null);}
    }
    private static FertilityWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(FertilityWork::new,FertilityWork.ID);}
    private static boolean allowed(ServerWorld world,PlayerEntity actor){return actor!=null&&BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        if(!allowed(world,actor))return 0;
        return work(world).enqueue(center,amplifier,owner instanceof PlayerEntity?owner.getUUID():null)?1:0;
    }
    @SubscribeEvent public static void tick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.world instanceof ServerWorld))return;
        ServerWorld world=(ServerWorld)event.world;
        work(world).tick(new FertilityWork.Visitor(){
            private PlayerEntity actor;
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return FertilityEffect.allowed(world,actor);}
            private boolean editable(BlockPos pos){return FertilityEffect.allowed(world,actor)&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public boolean visit(BlockPos pos){
                if(!world.hasChunkAt(pos)||!world.getWorldBorder().isWithinBounds(pos))return false;
                BlockState before=world.getBlockState(pos);
                if(!eligible(before)||!editable(pos))return false;
                // Growth can spread beyond its source. Require nearby chunks to be present first.
                if(!world.isAreaLoaded(pos,16))return false;
                IGrowable growable=(IGrowable)before.getBlock();
                return FertilityGrowth.apply(new FertilityGrowth.Target(){
                    public boolean canGrow(){return growable.isValidBonemealTarget(world,pos,before,false);}
                    public int event(){return ForgeEventFactory.onApplyBonemeal(actor,world,pos,before,new ItemStack(Items.BONE_MEAL));}
                    public boolean unchanged(){return editable(pos)&&world.getBlockState(pos).equals(before)&&world.isAreaLoaded(pos,16);}
                    public void grow(){growable.performBonemeal(world,world.random,pos,before);}
                });
            }
        });
    }
}
