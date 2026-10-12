package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import java.util.*;
public final class IceworldCheck {
    private static int checks;private static final Map<ResourceLocation,Block> MAPPED=new HashMap<>();
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Block add(String name,Block block){block.setRegistryName("bewitchment",name);MAPPED.put(block.getRegistryName(),block);return block;}
    private static Block shape(String name){
        AbstractBlock.Properties props=AbstractBlock.Properties.copy(Blocks.STONE);
        return name.endsWith("stairs")?new StairsBlock(()->Blocks.STONE.defaultBlockState(),props):name.endsWith("slab")?new SlabBlock(props):name.endsWith("fence")?new FenceBlock(props):name.endsWith("wall")?new WallBlock(props):new Block(props);
    }
    private static BlockState replacement(BlockState state,boolean selected){return IceworldRules.replacement(state,selected,false,MAPPED::get);}
    private static void maps(Block source,String target,boolean properties){
        Block expected=MAPPED.get(new ResourceLocation("bewitchment",target));
        for(BlockState before:source.getStateDefinition().getPossibleStates()){
            BlockState after=replacement(before,true);check(after!=null&&after.is(expected),"Exact ice mapping "+source.getRegistryName());
            if(properties)check(after.getValues().equals(before.getValues()),"All compatible shape states retained");
            check(replacement(before,false)==null,"Unselected terrain remains unchanged");
        }
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(String name:new String[]{"perpetual_ice","embittered_bricks","perpetual_ice_fence","embittered_brick_wall","crying_scorned_bricks","crying_scorned_brick_stairs","crying_scorned_brick_slab","embittered_bricks_slab","embittered_brick_stairs"})add(name,shape(name));
        IceworldRules.validateMappings(MAPPED::get);
        for(Block source:new Block[]{Blocks.STONE,Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE,Blocks.NETHERRACK,Blocks.OAK_LEAVES})maps(source,"perpetual_ice",false);
        for(Block source:new Block[]{Blocks.STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS,Blocks.NETHER_BRICKS,Blocks.RED_NETHER_BRICKS})maps(source,"embittered_bricks",true);
        for(Block source:new Block[]{Blocks.OAK_FENCE,Blocks.NETHER_BRICK_FENCE})maps(source,"perpetual_ice_fence",true);
        for(Block source:new Block[]{Blocks.COBBLESTONE_WALL,Blocks.MOSSY_COBBLESTONE_WALL})maps(source,"embittered_brick_wall",true);
        for(Block source:new Block[]{Blocks.SMOOTH_STONE_SLAB,Blocks.SANDSTONE_SLAB,Blocks.PETRIFIED_OAK_SLAB,Blocks.COBBLESTONE_SLAB,Blocks.BRICK_SLAB,Blocks.STONE_BRICK_SLAB,Blocks.NETHER_BRICK_SLAB,Blocks.QUARTZ_SLAB,Blocks.RED_SANDSTONE_SLAB})maps(source,"embittered_bricks_slab",true);
        maps(Blocks.STONE_BRICK_STAIRS,"embittered_brick_stairs",true);
        for(String name:new String[]{"scorned_bricks","scorned_bricks_slab","scorned_brick_stairs","mossy_scorned_bricks","mossy_scorned_brick_slab","mossy_scorned_brick_stairs","mossy_scorned_brick_fence","mossy_scorned_brick_wall"}){
            Block source=add(name,shape(name));String target=name.equals("scorned_bricks")?"embittered_bricks":name.equals("scorned_bricks_slab")?"embittered_bricks_slab":name.equals("scorned_brick_stairs")?"embittered_brick_stairs":name.endsWith("fence")?"perpetual_ice_fence":name.endsWith("wall")?"embittered_brick_wall":name.replace("mossy_","crying_");maps(source,target,true);
        }
        for(Block source:new Block[]{Blocks.SMOOTH_STONE,Blocks.SMOOTH_SANDSTONE,Blocks.SMOOTH_RED_SANDSTONE,Blocks.SMOOTH_QUARTZ}){BlockState after=replacement(source.defaultBlockState(),true);check(after.is(MAPPED.get(new ResourceLocation("bewitchment:embittered_bricks_slab")))&&after.getValue(SlabBlock.TYPE)==SlabType.DOUBLE,"Flattened seamless double slabs restored");}
        for(BlockState water:Blocks.WATER.getStateDefinition().getPossibleStates()){
            BlockState after=replacement(water,true);check(water.getFluidState().isSource()?after!=null&&after.is(Blocks.ICE):after==null,"Only source water freezes");check(replacement(water,false)==null,"Water freezing needs sampling");
        }
        for(Block source:new Block[]{Blocks.FIRE,Blocks.SOUL_FIRE})for(BlockState fire:source.getStateDefinition().getPossibleStates())for(boolean selected:new boolean[]{false,true})check(replacement(fire,selected).isAir(),"Every fire state clears without sampling");
        for(Block source:new Block[]{Blocks.ICE,Blocks.FROSTED_ICE})for(BlockState before:source.getStateDefinition().getPossibleStates())check(replacement(before,true).is(Blocks.PACKED_ICE),"Ice family packs");
        for(BlockState log:Blocks.OAK_LOG.getStateDefinition().getPossibleStates())check(IceworldRules.replacement(log,true,true,MAPPED::get).is(Blocks.PACKED_ICE)&&IceworldRules.replacement(log,false,true,MAPPED::get)==null,"Log-tagged blocks pack only when sampled");
        for(DyeColor color:DyeColor.values()){Block source=Registry.BLOCK.get(new ResourceLocation(color.getName()+"_wool"));BlockState after=replacement(source.defaultBlockState(),true);check(color==DyeColor.LIGHT_BLUE?after==null:after.is(Blocks.LIGHT_BLUE_WOOL),"Wool becomes light blue");}
        for(Block source:new Block[]{Blocks.STONE_SLAB,Blocks.OAK_SLAB,Blocks.RED_NETHER_BRICK_SLAB,Blocks.CUT_SANDSTONE_SLAB,Blocks.LAVA,Blocks.CHEST,Blocks.SNOW,Blocks.PACKED_ICE,Blocks.BLUE_ICE,Blocks.STONE_BRICK_WALL,Blocks.WHITE_CONCRETE,Blocks.OAK_FENCE_GATE}){
            if(source==Blocks.STONE_BRICK_WALL)continue; // All WallBlock subclasses intentionally match legacy.
            check(replacement(source.defaultBlockState(),true)==null,"Unrelated or newer family retained");
        }
        check(IceworldRules.replacement(Blocks.STONE.defaultBlockState(),true,false,id->null)==null&&IceworldRules.replacement(Blocks.STONE.defaultBlockState(),true,false,id->Blocks.AIR)==null,"Missing destinations fail closed");
        try{IceworldRules.validateMappings(id->null);throw new AssertionError("Missing mapping accepted");}catch(IllegalStateException expected){checks++;}
        try{IceworldRules.validateMappings(id->Blocks.STONE);throw new AssertionError("Shape mismatch accepted");}catch(IllegalStateException expected){checks++;}
        List<EffectInstance> effects=IceworldRules.effects(false);check(effects.size()==2&&effects.get(0).getEffect()==Effects.MOVEMENT_SLOWDOWN&&effects.get(1).getEffect()==Effects.DIG_SLOWDOWN,"Two original debuffs");
        for(EffectInstance effect:effects)check(effect.getDuration()==300&&effect.getAmplifier()==2,"Fixed fifteen-second level-three debuffs");check(IceworldRules.effects(true).isEmpty(),"Polar bears excluded from debuffs");
        for(int denied=0;denied<8;denied++){
            final int stage=denied;int[] calls={0,0};
            boolean result=IceworldRules.change(new IceworldRules.Change(){
                private int gate;private boolean permit(){return ++gate!=stage;}
                public boolean allowed(){return permit();}public boolean unchanged(){return permit();}public boolean survives(){return permit();}
                public boolean canceled(){calls[0]++;return !permit();}public boolean write(){calls[1]++;return true;}
            });
            check(result==(stage==0)&&calls[1]==(stage==0?1:0),"Permission, mutation, support or cancellation blocks write at every phase");
            check(calls[0]==(stage==0||stage>=4?1:0),"Rejected preconditions cannot post mutation events");
        }
        check(!IceworldRules.change(new IceworldRules.Change(){public boolean allowed(){return true;}public boolean unchanged(){return true;}public boolean survives(){return true;}public boolean canceled(){return false;}public boolean write(){return false;}}),"Failed world write reported");
        UUID owner=UUID.randomUUID();BlockPos center=new BlockPos(0,64,0);
        for(int amp:new int[]{Integer.MIN_VALUE,0,1,2,Integer.MAX_VALUE}){
            IceworldWork work=new IceworldWork();work.enqueue(center,amp,89,owner);Set<BlockPos> all=new HashSet<>(),selected=new HashSet<>();int total=0,r=IceworldWork.radius(amp),side=2*r+1;
            SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){if(!owner.equals(id))throw new AssertionError("Owner lost");return true;}public void visit(BlockPos p){throw new AssertionError("Fire requires all candidates");}public void visit(BlockPos p,boolean chosen){if(!all.add(p))throw new AssertionError("Repeated candidate");if(chosen)selected.add(p);}};
            while(work.pending()>0){int n=work.tick(visitor);check(n>0&&n<=128,"Per-job budget");total+=n;CompoundNBT saved=work.save(new CompoundNBT());work=new IceworldWork();work.load(saved);}
            int expected=0;for(int i=0;i<side*side*side;i++)if(SampledTerrainWork.selected(89,i))expected++;
            check(total==side*side*side&&all.size()==total&&selected.size()==expected&&r>=3&&r<=9,"Fire visits every candidate and sampled terrain persists");
        }
        IceworldWork queue=new IceworldWork();for(int i=0;i<32;i++)check(queue.enqueue(center,2,i,null),"Queue capacity");check(!queue.enqueue(center,2,0,null),"Overflow rejected");check(queue.tick(new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos p){}})==512,"Dimension budget");
        check(!IceworldWork.ID.equals(RuinWork.ID),"Independent persisted queue");IceworldEffect effect=new IceworldEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0x10dcdc,"Original effect metadata");
        for(String ingredient:new String[]{"minecraft:ice","minecraft:packed_ice","minecraft:snow_block"})check(BrewIngredients.ids().contains(ingredient),"Original ingredient registered");
        System.out.println("PASS: "+checks+" Iceworld mappings, shape states, fire, water, debuffs, protected changes and saved queue checks");
    }
}