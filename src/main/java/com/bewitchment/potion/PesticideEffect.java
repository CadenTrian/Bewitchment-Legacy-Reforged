package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import javax.annotation.Nullable;

public final class PesticideEffect extends TerrainBrewEffect {
    public PesticideEffect(){super(EffectType.HARMFUL,0x007f00);}
    public static boolean eligible(BlockState state){return state.getBlock() instanceof WebBlock&&!state.hasTileEntity();}
    public static boolean susceptible(CreatureAttribute type){return type==CreatureAttribute.ARTHROPOD;}
    public static float damage(int amplifier){return 8F*UtilityBrewEffect.level(amplifier);}
    @Override protected void affectLiving(LivingEntity target,int amplifier){if(susceptible(target.getMobType()))target.hurt(DamageSource.MAGIC,damage(amplifier));}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        if(!BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild))return 0;
        return BrewTerrain.clear(center,amplifier,new BrewTerrain.Terrain(){
            public boolean loaded(BlockPos pos){return world.hasChunkAt(pos);}
            public boolean insideBorder(BlockPos pos){return world.getWorldBorder().isWithinBounds(pos);}
            public boolean eligible(BlockPos pos){return PesticideEffect.eligible(world.getBlockState(pos));}
            public boolean mayEdit(BlockPos pos){return world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public boolean clear(BlockPos pos){
                BlockState before=world.getBlockState(pos);
                if(!PesticideEffect.eligible(before)||MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,before,actor)))return false;
                // Protection listeners may also mutate the world; never overwrite their replacement.
                if(!world.hasChunkAt(pos)||!world.getBlockState(pos).equals(before))return false;
                return world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            }
        });
    }
}
