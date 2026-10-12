package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import javax.annotation.Nullable;

public final class RubedoRules {
    public static boolean colorArmor(ItemStack stack){return ColorBrewRules.colorArmor(stack,DyeColor.RED);}
    @Nullable public static BlockState replacement(BlockState state){
        if(state.hasTileEntity())return null;Block block=state.getBlock();Block target=null;
        if(block==Blocks.SAND)target=Blocks.RED_SAND;
        else if(block==Blocks.SANDSTONE||block==Blocks.CHISELED_SANDSTONE||block==Blocks.CUT_SANDSTONE)target=Blocks.RED_SANDSTONE;
        // Vanilla flattened seamless double sandstone slabs into smooth sandstone.
        else if(block==Blocks.SMOOTH_SANDSTONE)return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.DOUBLE);
        else if(block==Blocks.SANDSTONE_SLAB)target=Blocks.RED_SANDSTONE_SLAB;
        else if(block==Blocks.SANDSTONE_STAIRS)target=Blocks.RED_SANDSTONE_STAIRS;
        else return ColorBrewRules.replacement(state,DyeColor.RED);
        if(target==null||target==block)return null;
        return SporeCloudRules.copyProperties(state,target.defaultBlockState());
    }
    private RubedoRules(){}
}