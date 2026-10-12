package com.bewitchment.machine;

import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class HarvestCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final class Roll extends Random {
        final int value;int calls,lastBound;
        Roll(int value){this.value=value;}
        @Override public int nextInt(int bound){calls++;lastBound=bound;return Math.floorMod(value,bound);}
    }
    private static final class Target implements HarvestRules.Target {
        BlockState state;boolean permitted=true,write=true,changeOnPermission,changeOnLoot,throwLoot,throwDrop;
        int permissions,loots,writes,fortune=-1;final List<ItemStack> emitted=new ArrayList<>(),source=new ArrayList<>();
        Target(BlockState state){this.state=state;source.add(new ItemStack(Items.WHEAT,2));source.add(new ItemStack(Items.WHEAT_SEEDS,3));}
        public boolean permitted(BlockState expected){permissions++;if(changeOnPermission)state=Blocks.STONE.defaultBlockState();return permitted;}
        public boolean unchanged(BlockState expected){return state.equals(expected);}
        public List<ItemStack> loot(BlockState expected,int fortune){loots++;this.fortune=fortune;if(throwLoot)throw new IllegalStateException("loot failed");if(changeOnLoot)state=Blocks.STONE.defaultBlockState();return source;}
        public boolean replace(BlockState expected,BlockState replacement){writes++;if(!write||!unchanged(expected))return false;state=replacement;return true;}
        public void drop(ItemStack stack){if(state.getBlock() instanceof CropsBlock&&((CropsBlock)state.getBlock()).isMaxAge(state))throw new AssertionError("Published before resetting crop");emitted.add(stack);if(throwDrop)throw new IllegalStateException("item publication failed");}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Block block:new Block[]{Blocks.WHEAT,Blocks.CARROTS,Blocks.POTATOES,Blocks.BEETROOTS}){
            CropsBlock crop=(CropsBlock)block;
            for(int age=0;age<=crop.getMaxAge();age++){
                BlockState before=crop.getStateForAge(age);Target target=new Target(before);Roll random=new Roll(0);
                boolean mature=age==crop.getMaxAge();
                check(HarvestRules.kind(before)==(mature?HarvestRules.Kind.CROP:HarvestRules.Kind.NONE),"Only mature crops eligible");
                check(HarvestRules.harvest(before,1,random,target)==mature,"Harvest maturity gate");
                check(random.calls==0,"Crop harvest is not random");
                check(target.emitted.size()==(mature?2:0)&&target.loots==(mature?1:0),"No immature seed farming");
                check(target.state.equals(mature?crop.getStateForAge(0):before),"Reset only mature crops");
                check(!HarvestRules.harvest(target.state,1,random,target),"Repeated immediate sweep produces nothing");
            }
        }
        BlockState wheat=((CropsBlock)Blocks.WHEAT).getStateForAge(7);
        for(int amplifier:new int[]{Integer.MIN_VALUE,-1,0,1,2,3,255,Integer.MAX_VALUE}){
            Target target=new Target(wheat);check(HarvestRules.harvest(wheat,amplifier,new Roll(0),target),"Valid crop harvest");
            int fortune=Math.max(0,Math.min(2,amplifier));check(target.fortune==fortune,"Loot fortune bounded");
            ItemStack tool=HarvestEffect.harvestTool(amplifier);check(tool.getItem()==Items.WOODEN_HOE&&EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,tool)==fortune,"Virtual loot tool fortune");
        }
        Target cancelled=new Target(wheat);cancelled.permitted=false;
        check(!HarvestRules.harvest(wheat,0,new Roll(0),cancelled)&&cancelled.writes==0&&cancelled.loots==0&&cancelled.emitted.isEmpty(),"Cancelled event cannot reset or drop");
        Target eventChanged=new Target(wheat);eventChanged.changeOnPermission=true;
        check(!HarvestRules.harvest(wheat,0,new Roll(0),eventChanged)&&eventChanged.state.is(Blocks.STONE)&&eventChanged.loots==0,"Preserve protection listener replacement");
        Target lootChanged=new Target(wheat);lootChanged.changeOnLoot=true;
        check(!HarvestRules.harvest(wheat,0,new Roll(0),lootChanged)&&lootChanged.writes==0&&lootChanged.emitted.isEmpty(),"Loot listener replacement aborts reset and publication");
        Target refused=new Target(wheat);refused.write=false;
        check(!HarvestRules.harvest(wheat,0,new Roll(0),refused)&&refused.state.equals(wheat)&&refused.emitted.isEmpty(),"Failed write cannot publish items");
        Target brokenLoot=new Target(wheat);brokenLoot.throwLoot=true;
        try{HarvestRules.harvest(wheat,0,new Roll(0),brokenLoot);throw new AssertionError("Expected loot failure");}catch(IllegalStateException expected){}
        check(brokenLoot.state.equals(wheat)&&brokenLoot.writes==0&&brokenLoot.emitted.isEmpty(),"Loot exception leaves crop intact");
        Target brokenDrop=new Target(wheat);brokenDrop.throwDrop=true;
        try{HarvestRules.harvest(wheat,0,new Roll(0),brokenDrop);throw new AssertionError("Expected publication failure");}catch(IllegalStateException expected){}
        check(brokenDrop.state.equals(((CropsBlock)Blocks.WHEAT).getStateForAge(0)),"Publication failure does not roll back committed crop");
        check(!HarvestRules.harvest(brokenDrop.state,0,new Roll(0),brokenDrop)&&brokenDrop.emitted.size()==1,"Publication failure cannot duplicate loot on immediate retry");
        Target nbt=new Target(wheat);nbt.source.get(0).getOrCreateTag().putString("custom","preserved");nbt.source.add(ItemStack.EMPTY);
        check(HarvestRules.harvest(wheat,0,new Roll(0),nbt)&&nbt.emitted.size()==2,"Empty loot ignored");
        check(nbt.emitted.get(0)!=nbt.source.get(0)&&nbt.emitted.get(0).getTag().getString("custom").equals("preserved"),"Loot copies retain NBT");nbt.emitted.get(0).shrink(1);check(nbt.source.get(0).getCount()==2,"No mutable loot alias");
        BlockState oak=Blocks.OAK_LEAVES.defaultBlockState();check(HarvestRules.kind(oak)==HarvestRules.Kind.APPLE,"Oak apple branch");
        for(int roll=0;roll<10;roll++){
            Target target=new Target(oak);Roll random=new Roll(roll);boolean changed=HarvestRules.harvest(oak,2,random,target);
            check(changed==(roll==0)&&random.lastBound==10,"One-in-ten apple chance");check(target.state.equals(oak)&&target.writes==0&&target.loots==0,"Apple does not remove leaves or evaluate leaf loot");
            check(target.emitted.size()==(roll==0?1:0),"At most one apple");if(changed)check(target.emitted.get(0).getItem()==Items.APPLE&&target.emitted.get(0).getCount()==1,"Exact apple output");
        }
        for(Block block:new Block[]{Blocks.GRASS,Blocks.FERN}){
            BlockState plant=block.defaultBlockState();check(HarvestRules.kind(plant)==HarvestRules.Kind.PUMPKIN,"Replaceable growable pumpkin branch");
            for(int roll=0;roll<4;roll++){Target target=new Target(plant);Roll random=new Roll(roll);check(HarvestRules.harvest(plant,0,random,target)==(roll==0),"One-in-four pumpkin chance");check(target.emitted.isEmpty()&&target.loots==0,"Pumpkin replacement has no plant drops");if(roll==0)check(target.state.is(Blocks.CARVED_PUMPKIN)&&target.state.getValue(CarvedPumpkinBlock.FACING).getAxis().isHorizontal(),"Legacy faced pumpkin");}
        }
        for(int facing=0;facing<4;facing++){
            final int direction=facing;Random random=new Random(){int calls;public int nextInt(int bound){return calls++==0?0:direction;}};
            Target target=new Target(Blocks.GRASS.defaultBlockState());HarvestRules.harvest(target.state,0,random,target);
            check(target.state.getValue(CarvedPumpkinBlock.FACING)==Direction.from2DDataValue(facing),"All horizontal pumpkin facings");
        }
        for(BlockState before:new BlockState[]{oak,Blocks.GRASS.defaultBlockState()}){Target target=new Target(before);target.permitted=false;check(!HarvestRules.harvest(before,0,new Roll(0),target)&&target.emitted.isEmpty()&&target.writes==0,"Protection covers apples and pumpkins");}
        for(Block block:new Block[]{Blocks.AIR,Blocks.CHEST,Blocks.STONE,Blocks.DIRT,Blocks.OAK_SAPLING,Blocks.BIRCH_LEAVES,Blocks.DARK_OAK_LEAVES,Blocks.PUMPKIN,Blocks.CARVED_PUMPKIN,Blocks.CACTUS,Blocks.NETHER_WART,Blocks.SUGAR_CANE,Blocks.WATER})check(HarvestRules.kind(block.defaultBlockState())==HarvestRules.Kind.NONE,"Unrelated terrain retained");
        final Set<BlockPos> positions=new HashSet<>();final int[] crops={0};
        BrewTerrain.Terrain terrain=new BrewTerrain.Terrain(){public boolean loaded(BlockPos p){return true;}public boolean insideBorder(BlockPos p){return true;}public boolean eligible(BlockPos p){return true;}public boolean mayEdit(BlockPos p){return true;}public boolean clear(BlockPos p){positions.add(p);Target t=new Target(wheat);boolean result=HarvestRules.harvest(wheat,2,new Roll(0),t);if(result)crops[0]++;return result;}};
        check(BrewTerrain.clear(new BlockPos(0,64,0),Integer.MAX_VALUE,terrain)==343&&positions.size()==343&&crops[0]==343,"Complete bounded harvest sweep");
        HarvestEffect effect=new HarvestEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0xff7f00,"Original Harvest metadata");
        check(BrewIngredients.ids().contains("minecraft:pumpkin")&&BrewIngredients.ids().contains("minecraft:carved_pumpkin"),"Both pumpkin ingredient identities");
        System.out.println("PASS: "+checks+" Harvest maturity, transaction, loot, chance, protection and traversal checks");
    }
}
