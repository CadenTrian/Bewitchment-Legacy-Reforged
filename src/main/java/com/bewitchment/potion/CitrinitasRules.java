package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.state.Property;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class CitrinitasRules {
    public static boolean colorArmor(ItemStack stack){return ColorBrewRules.colorArmor(stack,DyeColor.YELLOW);}
    @Nullable public static BlockState replacement(BlockState state,Function<ResourceLocation,Block> lookup){
        if(state.hasTileEntity())return null;Block b=state.getBlock(),target=null;String masonry=null;
        if(b==Blocks.RED_SAND)target=Blocks.SAND;
        else if(b==Blocks.RED_SANDSTONE||b==Blocks.CHISELED_RED_SANDSTONE||b==Blocks.CUT_RED_SANDSTONE)target=Blocks.SANDSTONE;
        else if(b==Blocks.SMOOTH_RED_SANDSTONE)return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.DOUBLE);
        else if(b==Blocks.RED_SANDSTONE_SLAB)target=Blocks.SANDSTONE_SLAB;
        else if(b==Blocks.RED_SANDSTONE_STAIRS)target=Blocks.SANDSTONE_STAIRS;
        else if(b==Blocks.STONE_BRICKS||b==Blocks.CRACKED_STONE_BRICKS||b==Blocks.MOSSY_STONE_BRICKS||b==Blocks.CHISELED_STONE_BRICKS)masonry="despair_bricks";
        else if(b==Blocks.STONE_BRICK_STAIRS)masonry="despair_brick_stairs";
        else if(b==Blocks.COBBLESTONE_WALL||b==Blocks.MOSSY_COBBLESTONE_WALL)masonry="despair_brick_wall";
        else if(b instanceof FenceBlock)masonry="despair_brick_fence";
        else return ColorBrewRules.replacement(state,DyeColor.YELLOW);
        if(masonry!=null){target=lookup.apply(new ResourceLocation("bewitchment",masonry));if(target==null||target==Blocks.AIR)return null;}
        return target==b?null:SporeCloudRules.copyProperties(state,target.defaultBlockState());
    }
    public static void validateMappings(Function<ResourceLocation,Block> lookup){
        String[] names={"despair_bricks","despair_brick_stairs","despair_brick_wall","despair_brick_fence"};
        Block[] sources={Blocks.STONE_BRICKS,Blocks.STONE_BRICK_STAIRS,Blocks.COBBLESTONE_WALL,Blocks.OAK_FENCE};
        for(int i=0;i<names.length;i++){
            Block target=lookup.apply(new ResourceLocation("bewitchment",names[i]));
            if(target==null||target==Blocks.AIR)throw new IllegalStateException("Missing Citrinitas mapping "+names[i]);
            for(Property<?> property:sources[i].defaultBlockState().getProperties())if(!target.defaultBlockState().hasProperty(property))throw new IllegalStateException("Lost Citrinitas property "+property);
        }
    }
    private CitrinitasRules(){}
}