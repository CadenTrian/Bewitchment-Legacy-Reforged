package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class BlightCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final class Target implements BlightRules.Target {
        final BlockState before;BlockState current;boolean permit=true,write=true,changeEvent,changeLoot,throwLoot,throwDrop;
        int events,writes,loots;final List<ItemStack> source=new ArrayList<>(),drops=new ArrayList<>();
        Target(BlockState state){before=state;current=state;source.add(new ItemStack(Items.WHEAT_SEEDS,2));}
        public boolean permitted(){events++;if(changeEvent)current=Blocks.STONE.defaultBlockState();return permit;}
        public boolean unchanged(){return current.equals(before);}
        public List<ItemStack> loot(){loots++;if(throwLoot)throw new IllegalStateException("loot failed");if(changeLoot)current=Blocks.STONE.defaultBlockState();return source;}
        public boolean replace(BlockState replacement){writes++;if(!write||!unchanged())return false;current=replacement;return true;}
        public void drop(ItemStack stack){if(current.equals(before))throw new AssertionError("Drops before removal");drops.add(stack);if(throwDrop)throw new IllegalStateException("drop failed");}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Block block:new Block[]{Blocks.GRASS_BLOCK,Blocks.DIRT,Blocks.PODZOL,Blocks.MYCELIUM,Blocks.FARMLAND,Blocks.GRASS_PATH}){
            Target t=new Target(block.defaultBlockState());check(BlightRules.action(t.before)==BlightRules.Action.SOIL,"Soil classification");check(BlightRules.apply(t.before,t)&&t.current.is(Blocks.COARSE_DIRT),"Coarse soil conversion");check(t.loots==0&&t.drops.isEmpty(),"No loot from soil replacement");
        }
        for(Block block:new Block[]{Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.WHEAT,Blocks.OAK_SAPLING,Blocks.GRASS,Blocks.SEAGRASS,Blocks.CRIMSON_NYLIUM}){
            Target t=new Target(block.defaultBlockState());check(BlightRules.action(t.before)==BlightRules.Action.DESTROY,"Leaves and growables destroyed");check(BlightRules.apply(t.before,t)&&t.current.equals(t.before.getFluidState().createLegacyBlock()),"Retain underlying fluid");check(t.loots==1&&t.drops.size()==1,"Publish loot after removal");check(!BlightRules.apply(t.current,new Target(t.current)),"Removed position cannot drop again");
        }
        for(Block block:new Block[]{Blocks.COARSE_DIRT,Blocks.STONE,Blocks.AIR,Blocks.CHEST,Blocks.SAND,Blocks.GRAVEL,Blocks.NETHER_WART,Blocks.WATER,Blocks.DANDELION}){
            Target t=new Target(block.defaultBlockState());check(!BlightRules.apply(t.before,t)&&t.events==0&&t.writes==0&&t.loots==0,"Unrelated terrain has no callbacks");
        }
        BlockState plant=Blocks.WHEAT.defaultBlockState();
        for(int flags=0;flags<16;flags++){
            Target t=new Target(plant);t.permit=(flags&1)!=0;t.write=(flags&2)!=0;t.changeEvent=(flags&4)!=0;t.changeLoot=(flags&8)!=0;
            boolean succeeds=flags==3;check(BlightRules.apply(plant,t)==succeeds,"Permission and listener/write gates");check(t.drops.size()==(succeeds?1:0),"No rejected action drops");
            if(t.changeEvent||t.permit&&t.changeLoot)check(t.current.is(Blocks.STONE),"Preserve listener replacement");
        }
        Target copy=new Target(plant);copy.source.get(0).getOrCreateTag().putString("custom","retained");copy.source.add(ItemStack.EMPTY);BlightRules.apply(plant,copy);
        check(copy.drops.size()==1&&copy.drops.get(0)!=copy.source.get(0)&&copy.drops.get(0).getTag().getString("custom").equals("retained"),"Copy nonempty loot and NBT");copy.drops.get(0).shrink(1);check(copy.source.get(0).getCount()==2,"No loot alias");
        Target badLoot=new Target(plant);badLoot.throwLoot=true;try{BlightRules.apply(plant,badLoot);throw new AssertionError("Expected loot exception");}catch(IllegalStateException expected){}check(badLoot.current.equals(plant)&&badLoot.drops.isEmpty(),"Failed loot leaves plant intact");
        Target badDrop=new Target(plant);badDrop.throwDrop=true;try{BlightRules.apply(plant,badDrop);throw new AssertionError("Expected spawn exception");}catch(IllegalStateException expected){}check(badDrop.current.isAir()&&badDrop.drops.size()==1,"Failed publication does not restore harvestable plant");
        for(int amp:new int[]{Integer.MIN_VALUE,-1,0,1,2,255,Integer.MAX_VALUE})check(BlightRules.duration(amp)==120*(Math.max(0,Math.min(255,amp))+1),"Debuff duration bounded");
        for(CreatureAttribute type:new CreatureAttribute[]{CreatureAttribute.UNDEFINED,CreatureAttribute.UNDEAD,CreatureAttribute.ARTHROPOD})for(int flags=0;flags<4;flags++)check(BlightRules.susceptible(type,(flags&1)!=0,(flags&2)!=0)==(type!=CreatureAttribute.UNDEAD&&flags==0),"Undead, demon and spirit exclusions");
        for(int flags=0;flags<32;flags++)check(BlightRules.conversionCandidate((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0,(flags&16)!=0)==(flags==3),"Conversion excludes dead, modded, mounted and leashed sources");
        UUID id=UUID.randomUUID();int[] removed={0};check(!BlightRules.convert(id,()->false,()->removed[0]++)&&removed[0]==0,"Rejected insertion retains source");
        check(BlightRules.convert(id,()->{check(removed[0]==0,"Insert before remove");check(!BlightRules.convert(id,()->true,()->removed[0]++),"Block same-source reentrancy");return true;},()->removed[0]++)&&removed[0]==1,"Successful insertion removes exactly once");
        try{BlightRules.convert(id,()->{throw new IllegalStateException("insertion failed");},()->removed[0]++);throw new AssertionError("Expected insertion exception");}catch(IllegalStateException expected){}
        check(removed[0]==1&&BlightRules.convert(id,()->true,()->removed[0]++)&&removed[0]==2,"Exception releases guard and retains source until later successful retry");
        check(BlightRules.convert(id,()->BlightRules.convert(UUID.randomUUID(),()->true,()->{}),()->{}),"Independent conversions allowed");
        BlightWork queue=new BlightWork();queue.enqueue(new BlockPos(0,64,0),0,123,id);CompoundNBT saved=queue.save(new CompoundNBT());BlightWork loaded=new BlightWork();loaded.load(saved);check(loaded.save(new CompoundNBT()).equals(saved),"Blight queue checkpoint round trip");
        Set<BlockPos> positions=new HashSet<>();int total=0;SampledTerrainWork.Visitor visitor=new SampledTerrainWork.Visitor(){public boolean allowed(UUID owner){check(id.equals(owner),"Saved owner preserved");return true;}public void visit(BlockPos pos){if(!positions.add(pos))throw new AssertionError("Repeated position");}};
        while(loaded.pending()>0)total+=loaded.tick(visitor);check(total==343&&positions.size()>70&&positions.size()<160,"Complete radius-three sampled sweep");check(!BlightWork.ID.equals(MendingWork.ID)&&!BlightWork.ID.equals(RuinWork.ID),"Separate storage identity");
        BlightEffect effect=new BlightEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0x7f4000,"Legacy metadata");check(BrewIngredients.ids().contains("minecraft:rotten_flesh"),"Rotten Flesh ingredient");
        System.out.println("PASS: "+checks+" Blight soil, vegetation, loot transaction, debuff and conversion safeguards");
    }
}
