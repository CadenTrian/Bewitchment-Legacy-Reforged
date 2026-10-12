package com.bewitchment.potion;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import javax.annotation.Nullable;

public final class DesertificationRules {
    public static boolean susceptible(Class<? extends LivingEntity> type){return WaterMobEntity.class.isAssignableFrom(type)||SlimeEntity.class.isAssignableFrom(type);}
    public static float damage(int amplifier){return 6F*UtilityBrewEffect.level(amplifier);}
    @Nullable public static BlockState replacement(BlockState state,boolean selected){
        if(state.hasTileEntity())return null;Block b=state.getBlock();
        // The water removal in legacy was outside its one-in-three terrain roll.
        if(b==Blocks.WATER)return Blocks.AIR.defaultBlockState();
        if(!selected)return null;
        if(b==Blocks.STONE||b==Blocks.GRANITE||b==Blocks.POLISHED_GRANITE||b==Blocks.DIORITE||b==Blocks.POLISHED_DIORITE||b==Blocks.ANDESITE||b==Blocks.POLISHED_ANDESITE||b==Blocks.STONE_BRICKS||b==Blocks.CRACKED_STONE_BRICKS||b==Blocks.MOSSY_STONE_BRICKS||b==Blocks.CHISELED_STONE_BRICKS)return Blocks.SANDSTONE.defaultBlockState();
        if(b==Blocks.GRASS_BLOCK||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL)return Blocks.DIRT.defaultBlockState();
        // The old stone slab was renamed to smooth_stone_slab by vanilla's data fixer.
        // Its seamless double variant instead became the full smooth_stone block.
        if(b==Blocks.SMOOTH_STONE)return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.state.properties.SlabType.DOUBLE);
        if(b==Blocks.SMOOTH_STONE_SLAB)return SporeCloudRules.copyProperties(state,Blocks.SANDSTONE_SLAB.defaultBlockState());
        if(b==Blocks.STONE_BRICK_STAIRS)return SporeCloudRules.copyProperties(state,Blocks.SANDSTONE_STAIRS.defaultBlockState());
        if(b instanceof BushBlock&&b!=Blocks.DEAD_BUSH&&state.getFluidState().isEmpty())return Blocks.DEAD_BUSH.defaultBlockState();
        return null;
    }
    private DesertificationRules(){}
}
