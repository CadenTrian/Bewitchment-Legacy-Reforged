package com.bewitchment.potion;

import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import javax.annotation.Nullable;
import java.util.List;

public final class HarvestEffect extends TerrainBrewEffect {
    public HarvestEffect(){super(EffectType.BENEFICIAL,0xff7f00);}
    public static ItemStack harvestTool(int amplifier){
        ItemStack tool=new ItemStack(Items.WOODEN_HOE);int fortune=HarvestRules.fortune(amplifier);
        if(fortune>0)tool.enchant(Enchantments.BLOCK_FORTUNE,fortune);
        return tool;
    }
    @Override protected void affectLiving(LivingEntity target,int amplifier){} // Terrain only, as in legacy.
    private static boolean actorAllowed(ServerWorld world,PlayerEntity actor){return BrewTerrain.actorAllowed(actor.level==world,actor.isAlive(),actor.isSpectator(),actor.abilities.mayBuild);}
    @Override public int terrain(ServerWorld world,BlockPos center,@Nullable Entity owner,int amplifier){
        PlayerEntity actor=owner instanceof PlayerEntity?(PlayerEntity)owner:FakePlayerFactory.getMinecraft(world);
        if(!actorAllowed(world,actor))return 0;
        return BrewTerrain.clear(center,amplifier,new BrewTerrain.Terrain(){
            public boolean loaded(BlockPos pos){return world.hasChunkAt(pos);}
            public boolean insideBorder(BlockPos pos){return world.getWorldBorder().isWithinBounds(pos);}
            public boolean eligible(BlockPos pos){return HarvestRules.kind(world.getBlockState(pos))!=HarvestRules.Kind.NONE;}
            public boolean mayEdit(BlockPos pos){return actorAllowed(world,actor)&&world.mayInteract(actor,pos)&&actor.mayUseItemAt(pos,Direction.UP,ItemStack.EMPTY);}
            public boolean clear(BlockPos pos){
                BlockState before=world.getBlockState(pos);
                return HarvestRules.harvest(before,amplifier,world.random,new HarvestRules.Target(){
                    public boolean permitted(BlockState expected){return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world,pos,expected,actor));}
                    public boolean unchanged(BlockState expected){return world.hasChunkAt(pos)&&insideBorder(pos)&&mayEdit(pos)&&world.getBlockState(pos).equals(expected);}
                    public List<ItemStack> loot(BlockState expected,int fortune){
                        return Block.getDrops(expected,world,pos,null,actor,harvestTool(fortune));
                    }
                    public boolean replace(BlockState expected,BlockState replacement){return unchanged(expected)&&world.setBlock(pos,replacement,3);}
                    public void drop(ItemStack stack){Block.popResource(world,pos,stack);}
                });
            }
        });
    }
}
