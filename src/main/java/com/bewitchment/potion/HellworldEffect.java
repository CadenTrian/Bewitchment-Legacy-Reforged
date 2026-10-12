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
public final class HellworldEffect extends TerrainBrewEffect {
    public HellworldEffect(){super(EffectType.HARMFUL,0xd20202);}
    private static boolean candidate(MobEntity source,ServerWorld world){
        boolean exact=source.getType()==EntityType.WOLF||source.getType()==com.bewitchment.registry.ModCreatures.SNAKE.get();
        return source.level==world&&HellworldRules.conversionCandidate(source.isAlive(),exact,source.isBaby(),source instanceof net.minecraft.entity.passive.TameableEntity&&((net.minecraft.entity.passive.TameableEntity)source).isTame(),source.isPassenger(),source.isVehicle(),source.isLeashed(),world.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL);
    }
    private static boolean safe(ServerWorld world,MobEntity source,MobEntity destination){
        net.minecraft.util.math.AxisAlignedBB box=destination.getBoundingBox();
        return candidate(source,world)&&destination.level==world&&destination.isAlive()&&box.minY>=0&&box.maxY<=world.getMaxBuildHeight()
            &&world.hasChunksAt(new BlockPos(box.minX,box.minY,box.minZ),new BlockPos(box.maxX,box.maxY,box.maxZ))
            &&world.getWorldBorder().isWithinBounds(box)&&world.noCollision(source,box)&&!world.containsAnyLiquid(box);
    }
    @Override protected void affectLiving(LivingEntity original,int amplifier){
        ServerWorld world=(ServerWorld)original.level;LivingEntity affected=original;
        if(original instanceof MobEntity&&candidate((MobEntity)original,world)){
            MobEntity source=(MobEntity)original;
            EntityType<? extends MobEntity> type=source.getType()==EntityType.WOLF?com.bewitchment.registry.ModCreatures.HELLHOUND.get():com.bewitchment.registry.ModCreatures.FEUERWURM.get();
            MobEntity destination=type.create(world);
            if(destination!=null){
                boolean converted=BlightRules.convert(source.getUUID(),()->{
                    if(!net.minecraftforge.event.ForgeEventFactory.canLivingConvert(source,type,timer->{})||!candidate(source,world))return false;
                    destination.moveTo(source.getX(),source.getY(),source.getZ(),source.yRot,source.xRot);
                    if(!safe(world,source,destination))return false;
                    destination.finalizeSpawn(world,world.getCurrentDifficultyAt(destination.blockPosition()),SpawnReason.CONVERSION,null,null);
                    if(!safe(world,source,destination))return false;
                    destination.setNoAi(source.isNoAi());destination.setInvulnerable(source.isInvulnerable());
                    destination.setCustomName(source.getCustomName());destination.setCustomNameVisible(source.isCustomNameVisible());
                    if(source.isPersistenceRequired())destination.setPersistenceRequired();
                    destination.setHealth(Math.min(destination.getMaxHealth(),source.getHealth()));
                    return safe(world,source,destination)&&world.addFreshEntity(destination);
                },source::remove);
                if(converted){net.minecraftforge.event.ForgeEventFactory.onLivingConvert(source,destination);affected=destination;}
            }
        }
        if(affected.isAlive())affected.setSecondsOnFire(HellworldRules.FIRE_SECONDS);
    }
    private static HellworldWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(HellworldWork::new,HellworldWork.ID);}
    private static boolean allowed(ServerWorld world,PlayerEntity actor){return actor!=null&&BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    private static boolean editable(ServerWorld world,PlayerEntity actor,BlockPos pos){return allowed(world,actor)&&pos.getY()>=0&&pos.getY()<256&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
    private static boolean replace(ServerWorld world,PlayerEntity actor,BlockPos pos,BlockState before,BlockState after,BooleanSupplier support){
        return TerrainBrewChange.apply(new TerrainBrewChange.Target(){
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
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return HellworldEffect.allowed(world,actor);}
            public void visit(BlockPos pos){visit(pos,true);}
            @Override public void visit(BlockPos pos,boolean selected){
                if(!editable(world,actor,pos))return;
                BlockState before=world.getBlockState(pos),after=HellworldRules.replacement(before,selected,before.is(BlockTags.LOGS),before.is(net.minecraftforge.common.Tags.Blocks.ORES),ForgeRegistries.BLOCKS::getValue);
                if(after!=null&&!after.equals(before)&&!replace(world,actor,pos,before,after,()->true))return;
                if(world.random.nextInt(5)!=0||!editable(world,actor,pos))return;
                BlockPos above=pos.above();if(!editable(world,actor,above))return;
                BlockState top=world.getBlockState(above);
                if(!top.isAir())return;
                replace(world,actor,above,top,Blocks.FIRE.defaultBlockState(),()->editable(world,actor,pos)&&world.hasChunksAt(above.offset(-1,-1,-1),above.offset(1,1,1)));
            }
        });
    }
}