package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class SporeCloudCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static BlockState result(Block b,boolean red){return SporeCloudRules.replacement(b.defaultBlockState(),red,id->null);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        check(result(Blocks.COBBLESTONE,false).is(Blocks.MOSSY_COBBLESTONE),"Cobblestone mossing");
        for(Block b:new Block[]{Blocks.STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS})check(result(b,false).is(Blocks.MOSSY_STONE_BRICKS),"Original stonebrick variants");
        check(result(Blocks.GRASS,false).is(Blocks.FERN),"Grass becomes fern");
        for(Block b:new Block[]{Blocks.POPPY,Blocks.DANDELION,Blocks.BLUE_ORCHID,Blocks.ALLIUM}){check(result(b,false).is(Blocks.BROWN_MUSHROOM),"Brown mushroom branch");check(result(b,true).is(Blocks.RED_MUSHROOM),"Red mushroom branch");}
        for(Block b:new Block[]{Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL})check(result(b,false).is(Blocks.MYCELIUM),"Dirt variants become mycelium");
        for(Block b:new Block[]{Blocks.AIR,Blocks.CHEST,Blocks.STONE,Blocks.GRASS_BLOCK,Blocks.FARMLAND,Blocks.GRASS_PATH,Blocks.FERN,Blocks.MYCELIUM,Blocks.MOSSY_COBBLESTONE,Blocks.MOSSY_STONE_BRICKS,Blocks.WHEAT,Blocks.OAK_LOG,Blocks.COBBLESTONE_STAIRS})check(result(b,false)==null,"Unrelated or already converted block unchanged");
        Block[][] shapes={{Blocks.STONE_BRICK_STAIRS,Blocks.MOSSY_STONE_BRICK_STAIRS},{Blocks.STONE_BRICK_SLAB,Blocks.MOSSY_STONE_BRICK_SLAB},{Blocks.OAK_FENCE,Blocks.SPRUCE_FENCE},{Blocks.COBBLESTONE_WALL,Blocks.MOSSY_COBBLESTONE_WALL}};
        for(Block[] pair:shapes)for(BlockState before:pair[0].getStateDefinition().getPossibleStates()){
            BlockState after=SporeCloudRules.copyProperties(before,pair[1].defaultBlockState());
            check(after.is(pair[1])&&after.getValues().equals(before.getValues()),"Preserve full shape, connection, slab and waterlogged state");
        }
        Map<ResourceLocation,Block> mapped=new HashMap<>();
        for(Map.Entry<String,String> entry:SporeCloudRules.MOSSY.entrySet()){
            Block source=new Block(AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment",entry.getKey());
            Block target=new Block(AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment",entry.getValue());
            mapped.put(source.getRegistryName(),source);mapped.put(target.getRegistryName(),target);
            check(SporeCloudRules.replacement(source.defaultBlockState(),false,mapped::get).is(target),"Exact Scorned mapping "+entry.getKey());
            check(SporeCloudRules.replacement(source.defaultBlockState(),false,id->null)==null,"Missing mapping fails closed");
            check(SporeCloudRules.replacement(source.defaultBlockState(),false,id->Blocks.AIR)==null,"Air mapping fails closed");
        }
        SporeCloudRules.validateMappings(mapped::get);check(SporeCloudRules.MOSSY.size()==5,"Five original Scorned block identities");
        try{SporeCloudRules.validateMappings(id->null);throw new AssertionError("Expected missing mapping rejection");}catch(IllegalStateException expected){checks++;}
        try{SporeCloudRules.MOSSY.put("other","other");throw new AssertionError("Expected immutable mappings");}catch(UnsupportedOperationException expected){checks++;}
        ItemStack stew=new ItemStack(Items.MUSHROOM_STEW,3);stew.getOrCreateTag().putString("custom","kept");ItemStack bowl=BrewIngredients.returned(stew),second=BrewIngredients.returned(stew);
        check(bowl.getItem()==Items.BOWL&&bowl.getCount()==1,"One bowl per consumed stew ingredient");bowl.shrink(1);check(second.getCount()==1&&stew.getCount()==3&&stew.getTag().getString("custom").equals("kept"),"Fresh returns do not mutate input");
        for(Item item:new Item[]{Items.BOWL,Items.BEETROOT_SOUP,Items.RABBIT_STEW,Items.SUSPICIOUS_STEW,Items.MILK_BUCKET})check(BrewIngredients.returned(new ItemStack(item)).isEmpty(),"No invented container returns");
        check(!BrewIngredients.supported(new ItemStack(Items.BOWL)),"Returned bowl cannot reenter brewing mixture");
        UUID owner=UUID.randomUUID();SporeCloudWork work=new SporeCloudWork();work.enqueue(new BlockPos(0,64,0),0,14,owner);CompoundNBT saved=work.save(new CompoundNBT());SporeCloudWork loaded=new SporeCloudWork();loaded.load(saved);check(loaded.save(new CompoundNBT()).equals(saved),"Saved queue round trip");
        Set<BlockPos> seen=new HashSet<>();int total=0;SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){check(owner.equals(id),"Owner persists");return true;}public void visit(BlockPos p){if(!seen.add(p))throw new AssertionError("Repeated position");}};
        while(loaded.pending()>0)total+=loaded.tick(visitor);check(total==343&&!seen.isEmpty(),"Complete sampled radius-three sweep");
        check(!SporeCloudWork.ID.equals(BlightWork.ID)&&!SporeCloudWork.ID.equals(RuinWork.ID),"Independent world save identity");
        check(BlightRules.conversionCandidate(true,true,false,false,false)&&!BlightRules.conversionCandidate(true,true,false,false,true),"Shared conversion gate excludes leads");
        int[] removals={0};UUID source=UUID.randomUUID();check(!BlightRules.convert(source,()->false,()->removals[0]++)&&removals[0]==0,"Failed insertion preserves cow");check(BlightRules.convert(source,()->true,()->removals[0]++)&&removals[0]==1,"Successful insertion removes once");
        SporeCloudEffect effect=new SporeCloudEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0x7f3f7f,"Original effect metadata");check(BrewIngredients.ids().contains("minecraft:mushroom_stew"),"Mushroom stew registered");
        System.out.println("PASS: "+checks+" Spore Cloud mappings, shape states, bowl returns, queue and conversion checks");
    }
}
