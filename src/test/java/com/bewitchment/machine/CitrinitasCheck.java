package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import java.util.*;
public final class CitrinitasCheck {
    private static int checks;
    private static final Map<ResourceLocation,Block> MAPPED=new HashMap<>();
    private static BlockState replacement(BlockState state){return CitrinitasRules.replacement(state,MAPPED::get);}
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        MAPPED.put(new ResourceLocation("bewitchment:despair_bricks"),new Block(AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment","despair_bricks"));
        MAPPED.put(new ResourceLocation("bewitchment:despair_brick_stairs"),new StairsBlock(()->Blocks.STONE.defaultBlockState(),AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment","despair_brick_stairs"));
        MAPPED.put(new ResourceLocation("bewitchment:despair_brick_wall"),new WallBlock(AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment","despair_brick_wall"));
        MAPPED.put(new ResourceLocation("bewitchment:despair_brick_fence"),new FenceBlock(AbstractBlock.Properties.copy(Blocks.STONE)).setRegistryName("bewitchment","despair_brick_fence"));
        CitrinitasRules.validateMappings(MAPPED::get);
        String[] families={"wool","carpet","terracotta","stained_glass","stained_glass_pane","concrete","concrete_powder"};
        Block[] destinations={Blocks.YELLOW_WOOL,Blocks.YELLOW_CARPET,Blocks.YELLOW_TERRACOTTA,Blocks.YELLOW_STAINED_GLASS,Blocks.YELLOW_STAINED_GLASS_PANE,Blocks.YELLOW_CONCRETE,Blocks.YELLOW_CONCRETE_POWDER};
        for(int f=0;f<families.length;f++)for(DyeColor color:DyeColor.values()){
            Block source=Registry.BLOCK.get(new ResourceLocation(color.getName()+"_"+families[f]));
            check(source!=Blocks.AIR,"Every colored source exists");
            for(BlockState before:source.getStateDefinition().getPossibleStates()){
                BlockState after=replacement(before);
                check(color==DyeColor.YELLOW?after==null:after!=null&&after.is(destinations[f])&&after.getValues().equals(before.getValues()),"All color and pane connection/water states preserved");
            }
        }
        for(Block[] pair:new Block[][]{{Blocks.RED_SAND,Blocks.SAND},{Blocks.RED_SANDSTONE,Blocks.SANDSTONE},{Blocks.CHISELED_RED_SANDSTONE,Blocks.SANDSTONE},{Blocks.CUT_RED_SANDSTONE,Blocks.SANDSTONE},{Blocks.RED_SANDSTONE_SLAB,Blocks.SANDSTONE_SLAB},{Blocks.RED_SANDSTONE_STAIRS,Blocks.SANDSTONE_STAIRS},{Blocks.TERRACOTTA,Blocks.YELLOW_TERRACOTTA},{Blocks.GLASS,Blocks.YELLOW_STAINED_GLASS},{Blocks.GLASS_PANE,Blocks.YELLOW_STAINED_GLASS_PANE}}){
            for(BlockState before:pair[0].getStateDefinition().getPossibleStates()){
                BlockState after=replacement(before);check(after!=null&&after.is(pair[1])&&after.getValues().equals(before.getValues()),"Legacy sandstone or clear-family transformation preserves states");
            }
        }
        BlockState seamless=replacement(Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState());check(seamless.is(Blocks.SANDSTONE_SLAB)&&seamless.getValue(SlabBlock.TYPE)==SlabType.DOUBLE,"Seamless red double slab mapping");
        Block[] masonry={Blocks.STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS,Blocks.STONE_BRICK_STAIRS,Blocks.COBBLESTONE_WALL,Blocks.MOSSY_COBBLESTONE_WALL,Blocks.OAK_FENCE,Blocks.NETHER_BRICK_FENCE};
        for(Block source:masonry)for(BlockState before:source.getStateDefinition().getPossibleStates()){
            String target=source instanceof FenceBlock?"despair_brick_fence":source instanceof WallBlock?"despair_brick_wall":source instanceof StairsBlock?"despair_brick_stairs":"despair_bricks";
            BlockState after=replacement(before);check(after!=null&&after.is(MAPPED.get(new ResourceLocation("bewitchment",target)))&&after.getValues().equals(before.getValues()),"Despair shape mapping preserves every connection, half and water state");
        }
        for(Block source:masonry){check(CitrinitasRules.replacement(source.defaultBlockState(),id->null)==null,"Missing masonry fails closed");check(CitrinitasRules.replacement(source.defaultBlockState(),id->Blocks.AIR)==null,"Air destination fails closed");}
        for(Block target:MAPPED.values())check(replacement(target.defaultBlockState())==null,"Existing Despair blocks remain unchanged");
        try{CitrinitasRules.validateMappings(id->null);throw new AssertionError("Missing mapping accepted");}catch(IllegalStateException expected){checks++;}
        try{CitrinitasRules.validateMappings(id->Blocks.STONE);throw new AssertionError("Missing shape properties accepted");}catch(IllegalStateException expected){checks++;}
        for(Block source:new Block[]{Blocks.SAND,Blocks.SANDSTONE,Blocks.STONE,Blocks.YELLOW_BED,Blocks.YELLOW_BANNER,Blocks.YELLOW_GLAZED_TERRACOTTA,Blocks.CHEST,Blocks.IRON_BARS,Blocks.RED_SANDSTONE_WALL,Blocks.CUT_RED_SANDSTONE_SLAB,Blocks.SMOOTH_RED_SANDSTONE_SLAB,Blocks.OAK_FENCE_GATE})check(replacement(source.defaultBlockState())==null,"Unrelated and newer families excluded");
        for(Item item:new Item[]{Items.LEATHER_HELMET,Items.LEATHER_CHESTPLATE,Items.LEATHER_LEGGINGS,Items.LEATHER_BOOTS}){
            ItemStack stack=new ItemStack(item);stack.setDamageValue(7);stack.getOrCreateTag().putString("custom","kept");stack.getOrCreateTagElement("display").putString("Name","custom name");
            check(CitrinitasRules.colorArmor(stack),"Dyeable worn armor accepted");check(((IDyeableArmorItem)item).getColor(stack)==0xfed83d,"Legacy yellow dye RGB, not firework or text color");
            check(stack.getDamageValue()==7&&stack.getCount()==1&&stack.getTag().getString("custom").equals("kept")&&stack.getTagElement("display").getString("Name").equals("custom name"),"Damage, count and unrelated NBT preserved");
            CompoundNBT once=stack.getTag().copy();check(CitrinitasRules.colorArmor(stack)&&stack.getTag().equals(once),"Yellow armor recoloring idempotent");
        }
        for(Item item:new Item[]{Items.IRON_CHESTPLATE,Items.DIAMOND_BOOTS,Items.LEATHER_HORSE_ARMOR,Items.SHIELD}){ItemStack stack=new ItemStack(item);CompoundNBT before=stack.hasTag()?stack.getTag().copy():null;check(!CitrinitasRules.colorArmor(stack)&&Objects.equals(before,stack.getTag()),"Non-worn or non-dyeable items untouched");}
        check(!CitrinitasRules.colorArmor(ItemStack.EMPTY),"Empty armor slot untouched");
        Item resin=new Item(new Item.Properties()).setRegistryName("bewitchment","birch_soul"),jar=new Item(new Item.Properties()).setRegistryName("bewitchment","empty_jar");
        ItemStack input=new ItemStack(resin,3);input.getOrCreateTag().putString("custom","kept");
        ItemStack first=BrewIngredients.returned(input,()->jar),second=BrewIngredients.returned(input,()->jar);first.shrink(1);
        check(second.getItem()==jar&&second.getCount()==1&&input.getCount()==3&&input.getTag().getString("custom").equals("kept"),"One fresh jar per ingredient without input mutation");
        check(!BrewIngredients.supported(new ItemStack(jar))&&BrewIngredients.returned(new ItemStack(jar),()->{throw new AssertionError("Unneeded jar lookup");}).isEmpty(),"Jar cannot reenter brew or produce a return");
        UUID owner=UUID.randomUUID();BlockPos center=new BlockPos(0,64,0);
        for(int amplifier:new int[]{Integer.MIN_VALUE,0,1,2,Integer.MAX_VALUE}){
            CitrinitasWork work=new CitrinitasWork();long seed=57;work.enqueue(center,amplifier,seed,owner);Set<BlockPos> seen=new HashSet<>();int examined=0,r=CitrinitasWork.radius(amplifier),side=2*r+1,total=side*side*side;
            SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){if(!owner.equals(id))throw new AssertionError("Lost owner");return true;}public void visit(BlockPos p){if(!seen.add(p))throw new AssertionError("Repeated sampled position");}};
            while(work.pending()>0){int n=work.tick(visitor);check(n>0&&n<=128,"Bounded per-job work");examined+=n;CompoundNBT saved=work.save(new CompoundNBT());work=new CitrinitasWork();work.load(saved);}
            int expected=0;for(int i=0;i<total;i++)if(SampledTerrainWork.selected(seed,i))expected++;
            check(examined==total&&seen.size()==expected&&r>=2&&r<=6,"Saved queue resumes deterministic sampled radius two/four/six");
        }
        CitrinitasWork queue=new CitrinitasWork();for(int i=0;i<32;i++)check(queue.enqueue(center,2,i,null),"Capacity admission");check(!queue.enqueue(center,2,0,null),"Overflow rejected");
        check(queue.tick(new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos p){}})==512,"Shared dimension work bound");
        check(!CitrinitasWork.ID.equals(DesertificationWork.ID),"Independent saved queue");
        CitrinitasEffect effect=new CitrinitasEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0xffff00,"Original metadata");check(BrewIngredients.ids().contains("bewitchment:birch_soul"),"Original ingredient registered");
        System.out.println("PASS: "+checks+" Citrinitas color families, shape states, armor NBT, jar returns and saved queue checks");
    }
}