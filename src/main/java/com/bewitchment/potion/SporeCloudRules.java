package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class SporeCloudRules {
    public static final Map<String,String> MOSSY;
    static {
        Map<String,String> map=new LinkedHashMap<>();
        map.put("scorned_bricks","mossy_scorned_bricks");
        map.put("scorned_bricks_slab","mossy_scorned_brick_slab");
        for(String shape:new String[]{"stairs","fence","wall"})map.put("scorned_brick_"+shape,"mossy_scorned_brick_"+shape);
        MOSSY=Collections.unmodifiableMap(map);
    }
    public static BlockState copyProperties(BlockState source,BlockState target){
        for(Property<?> property:source.getProperties())if(target.hasProperty(property))target=copy(source,target,property);
        return target;
    }
    private static <T extends Comparable<T>> BlockState copy(BlockState source,BlockState target,Property<T> property){return target.setValue(property,source.getValue(property));}
    @Nullable public static BlockState replacement(BlockState state,boolean red,Function<ResourceLocation,Block> lookup){
        if(state.hasTileEntity())return null;Block b=state.getBlock();
        if(b==Blocks.COBBLESTONE)return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        if(b==Blocks.STONE_BRICKS||b==Blocks.CRACKED_STONE_BRICKS||b==Blocks.CHISELED_STONE_BRICKS)return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        ResourceLocation id=b.getRegistryName();
        if(id!=null&&id.getNamespace().equals("bewitchment")&&MOSSY.containsKey(id.getPath())){
            Block target=lookup.apply(new ResourceLocation("bewitchment",MOSSY.get(id.getPath())));
            return target==null||target==Blocks.AIR?null:copyProperties(state,target.defaultBlockState());
        }
        if(b instanceof TallGrassBlock)return b==Blocks.FERN?null:Blocks.FERN.defaultBlockState();
        if(b instanceof FlowerBlock)return (red?Blocks.RED_MUSHROOM:Blocks.BROWN_MUSHROOM).defaultBlockState();
        if(b==Blocks.DIRT||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL)return Blocks.MYCELIUM.defaultBlockState();
        return null;
    }
    public static void validateMappings(Function<ResourceLocation,Block> lookup){
        for(Map.Entry<String,String> entry:MOSSY.entrySet()){
            Block before=lookup.apply(new ResourceLocation("bewitchment",entry.getKey())),after=lookup.apply(new ResourceLocation("bewitchment",entry.getValue()));
            if(before==null||after==null||before==Blocks.AIR||after==Blocks.AIR)throw new IllegalStateException("Missing Spore Cloud mapping "+entry);
            for(Property<?> property:before.defaultBlockState().getProperties())if(!after.defaultBlockState().hasProperty(property))throw new IllegalStateException("Lost Spore Cloud property "+property);
        }
    }
    private SporeCloudRules(){}
}
