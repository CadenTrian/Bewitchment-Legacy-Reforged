package com.bewitchment.potion;
import com.bewitchment.Bewitchment;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.BooleanSupplier;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class IceworldEffect extends TerrainBrewEffect {
    public IceworldEffect(){super(EffectType.HARMFUL,0x10dcdc);}
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        for(EffectInstance effect:IceworldRules.effects(target instanceof PolarBearEntity))target.addEffect(effect);
        target.clearFire();
    }
    private static IceworldWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(IceworldWork::new,IceworldWork.ID);}
    private static boolean allowed(ServerWorld world,PlayerEntity actor){return actor!=null&&BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    private static boolean editable(ServerWorld world,PlayerEntity actor,BlockPos pos){return allowed(world,actor)&&pos.getY()>=0&&pos.getY()<256&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
    private static boolean replace(ServerWorld world,PlayerEntity actor,BlockPos pos,BlockState before,BlockState after,BooleanSupplier support){
        return IceworldRules.change(new IceworldRules.Change(){
            public boolean allowed(){return editable(world,actor,pos);}
            public boolean unchanged(){return world.getBlockState(pos).equals(before);}
            public boolean survives(){return support.getAsBoolean()&&after.canSurvive(world,pos);}
            public boolean canceled(){return MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor));}
            public boolean write(){return world.setBlock(pos,after,3);}
        });
    }
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        return allowed(world,actor)&&work(world).enqueue(center,amplifier,world.random.nextLong(),owner instanceof PlayerEntity?owner.getUUID():null)?1:0;
    }
    @SubscribeEvent public static void tick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.world instanceof ServerWorld))return;ServerWorld world=(ServerWorld)event.world;
        work(world).tick(new SampledTerrainWork.Visitor(){
            private PlayerEntity actor;
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return IceworldEffect.allowed(world,actor);}
            public void visit(BlockPos pos){visit(pos,true);}
            @Override public void visit(BlockPos pos,boolean selected){
                if(!editable(world,actor,pos))return;
                BlockState before=world.getBlockState(pos),after=IceworldRules.replacement(before,selected,before.is(BlockTags.LOGS),ForgeRegistries.BLOCKS::getValue);
                if(after!=null&&!after.equals(before)&&!replace(world,actor,pos,before,after,()->true))return;
                if(world.random.nextInt(5)!=0||!editable(world,actor,pos))return;
                BlockPos above=pos.above();if(!editable(world,actor,above))return;
                BlockState top=world.getBlockState(above);
                if(!top.isAir()||!world.getBlockState(pos).isCollisionShapeFullBlock(world,pos))return;
                replace(world,actor,above,top,Blocks.SNOW.defaultBlockState(),()->editable(world,actor,pos)&&world.getBlockState(pos).isCollisionShapeFullBlock(world,pos));
            }
        });
    }
}