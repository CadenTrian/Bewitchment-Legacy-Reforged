package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import java.util.*;
public final class HellworldCheck {
    private static int checks;private static final Map<ResourceLocation,Block> MAPPED=new HashMap<>();
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Block add(String name){
        AbstractBlock.Properties props=AbstractBlock.Properties.copy(Blocks.STONE);
        Block b=name.endsWith("stairs")?new StairsBlock(()->Blocks.STONE.defaultBlockState(),props):name.endsWith("slab")?new SlabBlock(props):name.endsWith("fence")?new FenceBlock(props):name.endsWith("wall")?new WallBlock(props):new Block(props);
        b.setRegistryName("bewitchment",name);MAPPED.put(b.getRegistryName(),b);return b;
    }
    private static Block target(String name){return MAPPED.get(new ResourceLocation("bewitchment",name));}
    private static BlockState replacement(BlockState state,boolean selected){return HellworldRules.replacement(state,selected,false,false,MAPPED::get);}
    private static void maps(Block source,Block target,boolean properties){
        for(BlockState before:source.getStateDefinition().getPossibleStates()){
            BlockState after=replacement(before,true);check(source==target?after==null:after!=null&&after.is(target),"Exact infernal mapping "+source.getRegistryName());
            if(properties&&source!=target)check(after.getValues().equals(before.getValues()),"Full shared shape state preserved");
            check(replacement(before,false)==null,"Unselected candidate unchanged");
        }
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(String name:new String[]{"scorned_bricks","scorned_brick_wall","scorned_brick_fence","ochre_despair_bricks","ochre_despair_brick_stairs","ochre_despair_brick_wall"})add(name);
        HellworldRules.validateMappings(MAPPED::get);
        for(Block source:new Block[]{Blocks.STONE,Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE})maps(source,Blocks.NETHERRACK,true);
        maps(add("perpetual_ice"),Blocks.NETHERRACK,true);
        for(Block source:new Block[]{Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL,Blocks.GRASS_BLOCK,Blocks.SAND,Blocks.RED_SAND})maps(source,Blocks.SOUL_SAND,false);
        maps(Blocks.BRICKS,target("scorned_bricks"),true);maps(Blocks.GRASS_PATH,Blocks.RED_NETHER_BRICKS,true);
        for(Block source:new Block[]{Blocks.STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS})maps(source,Blocks.NETHER_BRICKS,true);
        for(String name:new String[]{"embittered_bricks","mossy_scorned_bricks","crying_scorned_bricks"})maps(add(name),target("scorned_bricks"),true);
        for(String name:new String[]{"despair_bricks","despair_brick_stairs","despair_brick_wall"})maps(add(name),target("ochre_"+name),true);
        for(Block source:new Block[]{Blocks.COBBLESTONE_WALL,Blocks.MOSSY_COBBLESTONE_WALL})maps(source,target("scorned_brick_wall"),true);
        maps(add("embittered_brick_fence"),target("scorned_brick_fence"),true);
        for(Block source:new Block[]{Blocks.OAK_FENCE,Blocks.NETHER_BRICK_FENCE,add("despair_brick_fence")})maps(source,Blocks.NETHER_BRICK_FENCE,true);
        maps(Blocks.OAK_LEAVES,Blocks.NETHER_WART_BLOCK,false);
        for(Block source:new Block[]{Blocks.BRICK_STAIRS,Blocks.STONE_BRICK_STAIRS,add("embittered_brick_stairs")})maps(source,Blocks.NETHER_BRICK_STAIRS,true);
        for(Block source:new Block[]{Blocks.SMOOTH_STONE_SLAB,Blocks.SANDSTONE_SLAB,Blocks.PETRIFIED_OAK_SLAB,Blocks.COBBLESTONE_SLAB,Blocks.BRICK_SLAB,Blocks.STONE_BRICK_SLAB,Blocks.NETHER_BRICK_SLAB,Blocks.QUARTZ_SLAB,Blocks.RED_SANDSTONE_SLAB,add("embittered_bricks_slab")})maps(source,Blocks.NETHER_BRICK_SLAB,true);
        for(Block source:new Block[]{Blocks.SMOOTH_STONE,Blocks.SMOOTH_SANDSTONE,Blocks.SMOOTH_RED_SANDSTONE,Blocks.SMOOTH_QUARTZ}){BlockState after=replacement(source.defaultBlockState(),true);check(after.is(Blocks.NETHER_BRICK_SLAB)&&after.getValue(SlabBlock.TYPE)==SlabType.DOUBLE,"Seamless double slab mapping");}
        for(DyeColor color:DyeColor.values()){Block source=Registry.BLOCK.get(new ResourceLocation(color.getName()+"_wool"));BlockState after=replacement(source.defaultBlockState(),true);check(color==DyeColor.RED?after==null:after.is(Blocks.RED_WOOL),"Wool becomes red");}
        for(BlockState before:Blocks.OAK_LOG.getStateDefinition().getPossibleStates()){
            BlockState after=HellworldRules.replacement(before,true,true,true,MAPPED::get);check(after.is(Blocks.BONE_BLOCK)&&after.getValues().equals(before.getValues()),"Log axis preserved; log mapping precedes ore fallback");
        }
        check(HellworldRules.replacement(Blocks.DIAMOND_ORE.defaultBlockState(),true,false,true,MAPPED::get).is(Blocks.NETHER_QUARTZ_ORE),"Ore-tag fallback");
        check(HellworldRules.replacement(Blocks.DIAMOND_ORE.defaultBlockState(),false,false,true,MAPPED::get)==null&&replacement(Blocks.DIAMOND_ORE.defaultBlockState(),true)==null,"Ore requires tag and sampling");
        for(Block source:new Block[]{Blocks.AIR,Blocks.CHEST})check(HellworldRules.replacement(source.defaultBlockState(),true,false,true,MAPPED::get)==null,"No ore-tag override for air or tile entities");
        check(HellworldRules.replacement(Blocks.STONE.defaultBlockState(),true,true,true,MAPPED::get).is(Blocks.NETHERRACK),"Original primary mapping takes precedence over tags");
        for(Block source:new Block[]{Blocks.STONE_SLAB,Blocks.OAK_SLAB,Blocks.CUT_SANDSTONE_SLAB,Blocks.FIRE,Blocks.SOUL_FIRE,Blocks.WATER,Blocks.LAVA,Blocks.SNOW,Blocks.CHEST,Blocks.STONE_BRICK_WALL,Blocks.FARMLAND,Blocks.MYCELIUM,Blocks.OAK_FENCE_GATE,Blocks.RED_CONCRETE})check(replacement(source.defaultBlockState(),true)==null,"Unrelated families retained");
        check(HellworldRules.replacement(Blocks.BRICKS.defaultBlockState(),true,false,false,id->null)==null&&HellworldRules.replacement(Blocks.BRICKS.defaultBlockState(),true,false,false,id->Blocks.AIR)==null,"Missing custom destinations fail closed");
        try{HellworldRules.validateMappings(id->null);throw new AssertionError("Missing mapping accepted");}catch(IllegalStateException expected){checks++;}
        try{HellworldRules.validateMappings(id->Blocks.STONE);throw new AssertionError("Missing shape accepted");}catch(IllegalStateException expected){checks++;}
        for(int flags=0;flags<256;flags++)check(HellworldRules.conversionCandidate((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0,(flags&16)!=0,(flags&32)!=0,(flags&64)!=0,(flags&128)!=0)==(flags==3),"Every conversion eligibility combination");
        UUID source=UUID.randomUUID();int[] removed={0},inserted={0};
        check(!BlightRules.convert(source,()->false,()->removed[0]++)&&removed[0]==0,"Rejected insertion retains source");
        check(BlightRules.convert(source,()->{check(!BlightRules.convert(source,()->{inserted[0]++;return true;},()->removed[0]++),"Nested conversion blocked");inserted[0]++;return true;},()->removed[0]++)&&removed[0]==1&&inserted[0]==1,"Successful insertion removes source once");
        try{BlightRules.convert(source,()->{throw new IllegalStateException("callback");},()->removed[0]++);throw new AssertionError("Expected callback exception");}catch(IllegalStateException expected){checks++;}
        check(removed[0]==1&&BlightRules.convert(source,()->true,()->removed[0]++)&&removed[0]==2,"Failure releases conversion guard without removing source");
        for(int denied=0;denied<8;denied++){
            final int stage=denied;int[] events={0},writes={0};
            boolean result=TerrainBrewChange.apply(new TerrainBrewChange.Target(){private int gate;private boolean permit(){return ++gate!=stage;}public boolean allowed(){return permit();}public boolean unchanged(){return permit();}public boolean survives(){return permit();}public boolean canceled(){events[0]++;return !permit();}public boolean write(){writes[0]++;return true;}});
            check(result==(stage==0)&&writes[0]==(stage==0?1:0),"Permission, source mutation, support or cancellation blocks every write phase");
            check(events[0]==(stage==0||stage>=4?1:0),"Preconditions precede protection event");
        }
        UUID owner=UUID.randomUUID();BlockPos center=new BlockPos(0,64,0);
        for(int amp:new int[]{Integer.MIN_VALUE,0,1,2,Integer.MAX_VALUE}){
            HellworldWork work=new HellworldWork();work.enqueue(center,amp,37,owner);Set<BlockPos> all=new HashSet<>(),sampled=new HashSet<>();int total=0,r=HellworldWork.radius(amp),side=2*r+1;
            SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){if(!owner.equals(id))throw new AssertionError("Owner lost");return true;}public void visit(BlockPos p){throw new AssertionError("Fire needs all positions");}public void visit(BlockPos p,boolean selected){if(!all.add(p))throw new AssertionError("Repeated position");if(selected)sampled.add(p);}};
            while(work.pending()>0){int count=work.tick(visitor);check(count>0&&count<=128,"Per-job candidate limit");total+=count;CompoundNBT saved=work.save(new CompoundNBT());work=new HellworldWork();work.load(saved);}
            int expected=0;for(int i=0;i<side*side*side;i++)if(SampledTerrainWork.selected(37,i))expected++;
            check(total==side*side*side&&all.size()==total&&sampled.size()==expected&&r>=3&&r<=9,"All fire candidates and saved sampled terrain visited");
        }
        HellworldWork queue=new HellworldWork();for(int i=0;i<32;i++)check(queue.enqueue(center,2,i,null),"Capacity admission");check(!queue.enqueue(center,2,0,null),"Overflow rejected");check(queue.tick(new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos p){}})==512,"Shared dimension candidate budget");
        check(!HellworldWork.ID.equals(IceworldWork.ID),"Independent saved queue");HellworldEffect effect=new HellworldEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0xd20202&&HellworldRules.FIRE_SECONDS==15,"Original effect metadata and ignition duration");check(BrewIngredients.ids().contains("bewitchment:hellhound_horn"),"Original ingredient");
        System.out.println("PASS: "+checks+" Hellworld mapping states, ore/log precedence, conversion gates, protected edits and saved queue checks");
    }
}