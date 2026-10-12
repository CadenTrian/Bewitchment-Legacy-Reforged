package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.item.DyeColor;
import net.minecraft.state.Property;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import java.util.function.Function;
import javax.annotation.Nullable;
public final class HellworldRules {
    public static final int FIRE_SECONDS=15;
    public static boolean conversionCandidate(boolean alive,boolean exactType,boolean baby,boolean tame,boolean passenger,boolean vehicle,boolean leashed,boolean peaceful){return alive&&exactType&&!baby&&!tame&&!passenger&&!vehicle&&!leashed&&!peaceful;}
    private static String local(Block b){ResourceLocation id=b.getRegistryName();return id!=null&&id.getNamespace().equals("bewitchment")?id.getPath():"";}
    @Nullable public static BlockState replacement(BlockState state,boolean selected,boolean log,boolean ore,Function<ResourceLocation,Block> lookup){
        if(!selected||state.hasTileEntity())return null;Block b=state.getBlock(),target=null;String name=local(b),mapped=null;boolean doubleSlab=false;
        if(b==Blocks.STONE||b==Blocks.GRANITE||b==Blocks.POLISHED_GRANITE||b==Blocks.DIORITE||b==Blocks.POLISHED_DIORITE||b==Blocks.ANDESITE||b==Blocks.POLISHED_ANDESITE||name.equals("perpetual_ice"))target=Blocks.NETHERRACK;
        else if(b==Blocks.GRASS_BLOCK||b==Blocks.DIRT||b==Blocks.COARSE_DIRT||b==Blocks.PODZOL||b==Blocks.SAND||b==Blocks.RED_SAND)target=Blocks.SOUL_SAND;
        else if(b==Blocks.BRICKS||name.equals("embittered_bricks")||name.equals("mossy_scorned_bricks")||name.equals("crying_scorned_bricks"))mapped="scorned_bricks";
        else if(b==Blocks.STONE_BRICKS||b==Blocks.CRACKED_STONE_BRICKS||b==Blocks.MOSSY_STONE_BRICKS||b==Blocks.CHISELED_STONE_BRICKS)target=Blocks.NETHER_BRICKS;
        else if(b==Blocks.GRASS_PATH)target=Blocks.RED_NETHER_BRICKS;
        else if(log)target=Blocks.BONE_BLOCK;
        else if(name.equals("despair_bricks")||name.equals("despair_brick_stairs")||name.equals("despair_brick_wall"))mapped="ochre_"+name;
        else if(b==Blocks.COBBLESTONE_WALL||b==Blocks.MOSSY_COBBLESTONE_WALL)mapped="scorned_brick_wall";
        else if(name.equals("embittered_brick_fence"))mapped="scorned_brick_fence";
        else if(b instanceof LeavesBlock)target=Blocks.NETHER_WART_BLOCK;
        else if(b==Blocks.BRICK_STAIRS||b==Blocks.STONE_BRICK_STAIRS||name.equals("embittered_brick_stairs"))target=Blocks.NETHER_BRICK_STAIRS;
        else if(b instanceof FenceBlock)target=Blocks.NETHER_BRICK_FENCE;
        else if(b==Blocks.SMOOTH_STONE||b==Blocks.SMOOTH_SANDSTONE||b==Blocks.SMOOTH_RED_SANDSTONE||b==Blocks.SMOOTH_QUARTZ){target=Blocks.NETHER_BRICK_SLAB;doubleSlab=true;}
        else if(b==Blocks.SMOOTH_STONE_SLAB||b==Blocks.SANDSTONE_SLAB||b==Blocks.PETRIFIED_OAK_SLAB||b==Blocks.COBBLESTONE_SLAB||b==Blocks.BRICK_SLAB||b==Blocks.STONE_BRICK_SLAB||b==Blocks.NETHER_BRICK_SLAB||b==Blocks.QUARTZ_SLAB||b==Blocks.RED_SANDSTONE_SLAB||name.equals("embittered_bricks_slab"))target=Blocks.NETHER_BRICK_SLAB;
        else {
            ResourceLocation id=b.getRegistryName();
            if(id!=null&&id.getNamespace().equals("minecraft"))for(DyeColor color:DyeColor.values())if(id.getPath().equals(color.getName()+"_wool"))target=Blocks.RED_WOOL;
            if(target==null&&ore&&!state.isAir())target=Blocks.NETHER_QUARTZ_ORE;
        }
        if(mapped!=null){target=lookup.apply(new ResourceLocation("bewitchment",mapped));if(target==null||target==Blocks.AIR)return null;}
        if(target==null||target==b)return null;
        BlockState result=SporeCloudRules.copyProperties(state,target.defaultBlockState());
        return doubleSlab?result.setValue(SlabBlock.TYPE,SlabType.DOUBLE):result;
    }
    public static void validateMappings(Function<ResourceLocation,Block> lookup){
        String[] names={"scorned_bricks","scorned_brick_wall","scorned_brick_fence","ochre_despair_bricks","ochre_despair_brick_stairs","ochre_despair_brick_wall"};
        Block[] shapes={Blocks.STONE,Blocks.COBBLESTONE_WALL,Blocks.OAK_FENCE,Blocks.STONE,Blocks.STONE_BRICK_STAIRS,Blocks.COBBLESTONE_WALL};
        for(int i=0;i<names.length;i++){
            Block target=lookup.apply(new ResourceLocation("bewitchment",names[i]));if(target==null||target==Blocks.AIR)throw new IllegalStateException("Missing Hellworld mapping "+names[i]);
            for(Property<?> p:shapes[i].defaultBlockState().getProperties())if(!target.defaultBlockState().hasProperty(p))throw new IllegalStateException("Lost Hellworld property "+p);
        }
    }
    private HellworldRules(){}
}