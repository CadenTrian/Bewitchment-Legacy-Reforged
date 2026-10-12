package com.bewitchment.potion;

import com.bewitchment.Bewitchment;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class MendingEffect extends TerrainBrewEffect {
    private static final ResourceLocation DEMONS=new ResourceLocation("bewitchment","demons"),SPIRITS=new ResourceLocation("bewitchment","spirits");
    private static final class Conversion {static final Method START=ObfuscationReflectionHelper.findMethod(ZombieVillagerEntity.class,"func_191991_a",UUID.class,int.class);}
    public MendingEffect(){super(EffectType.BENEFICIAL,0xffff00);}
    public static void validateConversionHook(){if(Conversion.START.getReturnType()!=void.class)throw new IllegalStateException("Unexpected zombie conversion method");}
    public static boolean heals(CreatureAttribute type,boolean demon,boolean spirit){return type!=CreatureAttribute.UNDEAD&&!demon&&!spirit;}
    public static float healing(int amplifier){return 6F*UtilityBrewEffect.level(amplifier);}
    @Nullable public static UUID existingCurer(CompoundNBT saved){return saved.hasUUID("ConversionPlayer")?saved.getUUID("ConversionPlayer"):null;}
    @Override protected void affectLiving(LivingEntity target,int amplifier){
        if(target instanceof ZombieVillagerEntity){
            // Let vanilla tick complete the conversion, preserving its Forge conversion events.
            UUID starter=existingCurer(target.saveWithoutId(new CompoundNBT()));
            try{Conversion.START.invoke(target,starter,0);}catch(ReflectiveOperationException exception){throw new IllegalStateException("Cannot start Mending conversion",exception);}
        }
        if(heals(target.getMobType(),EntityTypeTags.getAllTags().getTagOrEmpty(DEMONS).contains(target.getType()),EntityTypeTags.getAllTags().getTagOrEmpty(SPIRITS).contains(target.getType())))target.heal(healing(amplifier));
    }
    public static boolean eligible(BlockState state){Block b=state.getBlock();return !state.hasTileEntity()&&(b==Blocks.DIRT||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL||b==Blocks.MYCELIUM);}
    public static boolean canRestore(BlockState state,boolean opaqueAbove){return eligible(state)&&!opaqueAbove;}
    private static MendingWork work(ServerWorld world){return world.getDataStorage().computeIfAbsent(MendingWork::new,MendingWork.ID);}
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
            public boolean allowed(UUID owner){actor=owner==null?FakePlayerFactory.getMinecraft(world):world.getServer().getPlayerList().getPlayer(owner);return MendingEffect.allowed(world,actor);}
            private boolean editable(BlockPos pos){return MendingEffect.allowed(world,actor)&&world.hasChunkAt(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            private boolean opaqueAbove(BlockPos pos){return world.getBlockState(pos.above()).isSolidRender(world,pos.above());}
            public void visit(BlockPos pos){
                if(!editable(pos))return;BlockState before=world.getBlockState(pos);
                if(!canRestore(before,opaqueAbove(pos))||MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor)))return;
                if(!editable(pos)||!world.getBlockState(pos).equals(before)||opaqueAbove(pos))return;
                world.setBlock(pos,Blocks.GRASS_BLOCK.defaultBlockState(),3);
            }
        });
    }
}
