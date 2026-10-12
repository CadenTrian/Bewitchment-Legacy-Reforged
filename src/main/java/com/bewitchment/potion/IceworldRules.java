package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.item.DyeColor;
import net.minecraft.potion.*;
import net.minecraft.state.Property;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class IceworldRules {
    public static List<EffectInstance> effects(boolean polarBear){return polarBear?Collections.emptyList():Arrays.asList(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,300,2),new EffectInstance(Effects.DIG_SLOWDOWN,300,2));}
    public interface Change extends TerrainBrewChange.Target {}
    public static boolean change(Change change){return TerrainBrewChange.apply(change);}
    private static String local(Block b){ResourceLocation id=b.getRegistryName();return id!=null&&id.getNamespace().equals("bewitchment")?id.getPath():"";}
    @Nullable public static BlockState replacement(BlockState state,boolean selected,boolean log,Function<ResourceLocation,Block> lookup){
        if(state.hasTileEntity())return null;Block b=state.getBlock();
        if(b instanceof AbstractFireBlock)return Blocks.AIR.defaultBlockState();
        if(!selected)return null;
        String name=local(b),mapped=null;Block target=null;boolean doubleSlab=false;
        if(b==Blocks.STONE||b==Blocks.GRANITE||b==Blocks.POLISHED_GRANITE||b==Blocks.DIORITE||b==Blocks.POLISHED_DIORITE||b==Blocks.ANDESITE||b==Blocks.POLISHED_ANDESITE||b==Blocks.NETHERRACK||b instanceof LeavesBlock)mapped="perpetual_ice";
        else if(b==Blocks.WATER){if(!state.getFluidState().isSource())return null;target=Blocks.ICE;}
        else if(b instanceof IceBlock||log)target=Blocks.PACKED_ICE;
        else if(b==Blocks.STONE_BRICKS||b==Blocks.CRACKED_STONE_BRICKS||b==Blocks.MOSSY_STONE_BRICKS||b==Blocks.CHISELED_STONE_BRICKS||b==Blocks.NETHER_BRICKS||b==Blocks.RED_NETHER_BRICKS||name.equals("scorned_bricks"))mapped="embittered_bricks";
        // Preserve the original branch order: generic fences/walls precede mossy variants.
        else if(b instanceof FenceBlock)mapped="perpetual_ice_fence";
        else if(b instanceof WallBlock)mapped="embittered_brick_wall";
        else if(name.equals("mossy_scorned_bricks"))mapped="crying_scorned_bricks";
        else if(name.equals("mossy_scorned_brick_stairs"))mapped="crying_scorned_brick_stairs";
        else if(name.equals("mossy_scorned_brick_slab"))mapped="crying_scorned_brick_slab";
        else if(b==Blocks.SMOOTH_STONE||b==Blocks.SMOOTH_SANDSTONE||b==Blocks.SMOOTH_RED_SANDSTONE||b==Blocks.SMOOTH_QUARTZ){mapped="embittered_bricks_slab";doubleSlab=true;}
        else if(b==Blocks.SMOOTH_STONE_SLAB||b==Blocks.SANDSTONE_SLAB||b==Blocks.PETRIFIED_OAK_SLAB||b==Blocks.COBBLESTONE_SLAB||b==Blocks.BRICK_SLAB||b==Blocks.STONE_BRICK_SLAB||b==Blocks.NETHER_BRICK_SLAB||b==Blocks.QUARTZ_SLAB||b==Blocks.RED_SANDSTONE_SLAB||name.equals("scorned_bricks_slab"))mapped="embittered_bricks_slab";
        else if(b==Blocks.STONE_BRICK_STAIRS||name.equals("scorned_brick_stairs"))mapped="embittered_brick_stairs";
        else {
            ResourceLocation id=b.getRegistryName();if(id==null||!id.getNamespace().equals("minecraft"))return null;
            for(DyeColor color:DyeColor.values())if(id.getPath().equals(color.getName()+"_wool"))target=Blocks.LIGHT_BLUE_WOOL;
        }
        if(mapped!=null){target=lookup.apply(new ResourceLocation("bewitchment",mapped));if(target==null||target==Blocks.AIR)return null;}
        if(target==null||target==b)return null;
        BlockState result=SporeCloudRules.copyProperties(state,target.defaultBlockState());
        if(doubleSlab){if(!result.hasProperty(SlabBlock.TYPE))return null;result=result.setValue(SlabBlock.TYPE,SlabType.DOUBLE);}
        return result;
    }
    public static void validateMappings(Function<ResourceLocation,Block> lookup){
        String[] names={"perpetual_ice","embittered_bricks","perpetual_ice_fence","embittered_brick_wall","crying_scorned_bricks","crying_scorned_brick_stairs","crying_scorned_brick_slab","embittered_bricks_slab","embittered_brick_stairs"};
        Block[] shapes={Blocks.STONE,Blocks.STONE,Blocks.OAK_FENCE,Blocks.COBBLESTONE_WALL,Blocks.STONE,Blocks.STONE_BRICK_STAIRS,Blocks.STONE_BRICK_SLAB,Blocks.STONE_BRICK_SLAB,Blocks.STONE_BRICK_STAIRS};
        for(int i=0;i<names.length;i++){
            Block target=lookup.apply(new ResourceLocation("bewitchment",names[i]));if(target==null||target==Blocks.AIR)throw new IllegalStateException("Missing Iceworld mapping "+names[i]);
            for(Property<?> property:shapes[i].defaultBlockState().getProperties())if(!target.defaultBlockState().hasProperty(property))throw new IllegalStateException("Lost Iceworld property "+property);
        }
    }
    private IceworldRules(){}
}