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
public final class RubedoCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        String[] families={"wool","carpet","terracotta","stained_glass","stained_glass_pane","concrete","concrete_powder"};
        Block[] destinations={Blocks.RED_WOOL,Blocks.RED_CARPET,Blocks.RED_TERRACOTTA,Blocks.RED_STAINED_GLASS,Blocks.RED_STAINED_GLASS_PANE,Blocks.RED_CONCRETE,Blocks.RED_CONCRETE_POWDER};
        for(int f=0;f<families.length;f++)for(DyeColor color:DyeColor.values()){
            Block source=Registry.BLOCK.get(new ResourceLocation(color.getName()+"_"+families[f]));
            check(source!=Blocks.AIR,"Every colored source exists");
            for(BlockState before:source.getStateDefinition().getPossibleStates()){
                BlockState after=RubedoRules.replacement(before);
                check(color==DyeColor.RED?after==null:after!=null&&after.is(destinations[f])&&after.getValues().equals(before.getValues()),"All color and pane connection/water states preserved");
            }
        }
        for(Block[] pair:new Block[][]{{Blocks.SAND,Blocks.RED_SAND},{Blocks.SANDSTONE,Blocks.RED_SANDSTONE},{Blocks.CHISELED_SANDSTONE,Blocks.RED_SANDSTONE},{Blocks.CUT_SANDSTONE,Blocks.RED_SANDSTONE},{Blocks.SANDSTONE_SLAB,Blocks.RED_SANDSTONE_SLAB},{Blocks.SANDSTONE_STAIRS,Blocks.RED_SANDSTONE_STAIRS},{Blocks.TERRACOTTA,Blocks.RED_TERRACOTTA},{Blocks.GLASS,Blocks.RED_STAINED_GLASS},{Blocks.GLASS_PANE,Blocks.RED_STAINED_GLASS_PANE}}){
            for(BlockState before:pair[0].getStateDefinition().getPossibleStates()){
                BlockState after=RubedoRules.replacement(before);check(after!=null&&after.is(pair[1])&&after.getValues().equals(before.getValues()),"Exact legacy transformation with shape preservation");
            }
        }
        BlockState seamless=RubedoRules.replacement(Blocks.SMOOTH_SANDSTONE.defaultBlockState());check(seamless.is(Blocks.RED_SANDSTONE_SLAB)&&seamless.getValue(SlabBlock.TYPE)==SlabType.DOUBLE,"Seamless legacy double slab mapping");
        for(Block source:new Block[]{Blocks.RED_SAND,Blocks.RED_SANDSTONE,Blocks.STONE,Blocks.RED_BED,Blocks.RED_BANNER,Blocks.WHITE_GLAZED_TERRACOTTA,Blocks.CHEST,Blocks.IRON_BARS,Blocks.SANDSTONE_WALL,Blocks.CUT_SANDSTONE_SLAB,Blocks.SMOOTH_SANDSTONE_SLAB})check(RubedoRules.replacement(source.defaultBlockState())==null,"Unrelated, tile-entity and newer shape families excluded");
        for(Item item:new Item[]{Items.LEATHER_HELMET,Items.LEATHER_CHESTPLATE,Items.LEATHER_LEGGINGS,Items.LEATHER_BOOTS}){
            ItemStack stack=new ItemStack(item);stack.setDamageValue(7);stack.getOrCreateTag().putString("custom","kept");stack.getOrCreateTagElement("display").putString("Name","custom name");
            check(RubedoRules.colorArmor(stack),"Dyeable worn armor accepted");check(((IDyeableArmorItem)item).getColor(stack)==0xb02e26,"Legacy red dye RGB, not firework or text color");
            check(stack.getDamageValue()==7&&stack.getCount()==1&&stack.getTag().getString("custom").equals("kept")&&stack.getTagElement("display").getString("Name").equals("custom name"),"Damage, count and unrelated NBT preserved");
            CompoundNBT once=stack.getTag().copy();check(RubedoRules.colorArmor(stack)&&stack.getTag().equals(once),"Red armor recoloring idempotent");
        }
        for(Item item:new Item[]{Items.IRON_CHESTPLATE,Items.DIAMOND_BOOTS,Items.LEATHER_HORSE_ARMOR,Items.SHIELD}){ItemStack stack=new ItemStack(item);CompoundNBT before=stack.hasTag()?stack.getTag().copy():null;check(!RubedoRules.colorArmor(stack)&&Objects.equals(before,stack.getTag()),"Non-worn or non-dyeable items untouched");}
        check(!RubedoRules.colorArmor(ItemStack.EMPTY),"Empty armor slot untouched");
        Item resin=new Item(new Item.Properties()).setRegistryName("bewitchment","acacia_resin"),jar=new Item(new Item.Properties()).setRegistryName("bewitchment","empty_jar");
        ItemStack input=new ItemStack(resin,3);input.getOrCreateTag().putString("custom","kept");
        ItemStack first=BrewIngredients.returned(input,()->jar),second=BrewIngredients.returned(input,()->jar);first.shrink(1);
        check(second.getItem()==jar&&second.getCount()==1&&input.getCount()==3&&input.getTag().getString("custom").equals("kept"),"One fresh jar per ingredient without input mutation");
        check(!BrewIngredients.supported(new ItemStack(jar))&&BrewIngredients.returned(new ItemStack(jar),()->{throw new AssertionError("Unneeded jar lookup");}).isEmpty(),"Jar cannot reenter brew or produce a return");
        UUID owner=UUID.randomUUID();BlockPos center=new BlockPos(0,64,0);
        for(int amplifier:new int[]{Integer.MIN_VALUE,0,1,2,Integer.MAX_VALUE}){
            RubedoWork work=new RubedoWork();long seed=57;work.enqueue(center,amplifier,seed,owner);Set<BlockPos> seen=new HashSet<>();int examined=0,r=RubedoWork.radius(amplifier),side=2*r+1,total=side*side*side;
            SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){if(!owner.equals(id))throw new AssertionError("Lost owner");return true;}public void visit(BlockPos p){if(!seen.add(p))throw new AssertionError("Repeated sampled position");}};
            while(work.pending()>0){int n=work.tick(visitor);check(n>0&&n<=128,"Bounded per-job work");examined+=n;CompoundNBT saved=work.save(new CompoundNBT());work=new RubedoWork();work.load(saved);}
            int expected=0;for(int i=0;i<total;i++)if(SampledTerrainWork.selected(seed,i))expected++;
            check(examined==total&&seen.size()==expected&&r>=2&&r<=6,"Saved queue resumes deterministic sampled radius two/four/six");
        }
        RubedoWork queue=new RubedoWork();for(int i=0;i<32;i++)check(queue.enqueue(center,2,i,null),"Capacity admission");check(!queue.enqueue(center,2,0,null),"Overflow rejected");
        check(queue.tick(new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos p){}})==512,"Shared dimension work bound");
        check(!RubedoWork.ID.equals(DesertificationWork.ID),"Independent saved queue");
        RubedoEffect effect=new RubedoEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0xff0000,"Original metadata");check(BrewIngredients.ids().contains("bewitchment:acacia_resin"),"Original ingredient registered");
        System.out.println("PASS: "+checks+" Rubedo color families, shape states, armor NBT, jar returns and saved queue checks");
    }
}