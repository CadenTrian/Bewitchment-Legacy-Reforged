package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class DesertificationCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final class Visitor implements SampledTerrainWork.Visitor {
        final Set<BlockPos> all=new HashSet<>(),selected=new HashSet<>();UUID owner;boolean allowed=true;
        public boolean allowed(UUID id){owner=id;return allowed;}
        public void visit(BlockPos p){throw new AssertionError("Lost sampling flag");}
        public void visit(BlockPos p,boolean chosen){if(p.getY()<0||p.getY()>=256||!all.add(p))throw new AssertionError("Invalid/repeated position");if(chosen)selected.add(p);}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(BlockState water:Blocks.WATER.getStateDefinition().getPossibleStates())for(boolean selected:new boolean[]{false,true})check(DesertificationRules.replacement(water,selected).isAir(),"Source and flowing water bypass sampling");
        for(Block b:new Block[]{Blocks.STONE,Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE,Blocks.STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS}){
            check(DesertificationRules.replacement(b.defaultBlockState(),true).is(Blocks.SANDSTONE),"Stone family becomes sandstone");check(DesertificationRules.replacement(b.defaultBlockState(),false)==null,"Unselected stone unchanged");
        }
        for(Block b:new Block[]{Blocks.GRASS_BLOCK,Blocks.COARSE_DIRT,Blocks.PODZOL})check(DesertificationRules.replacement(b.defaultBlockState(),true).is(Blocks.DIRT),"Soil becomes plain dirt");
        BlockState seamless=DesertificationRules.replacement(Blocks.SMOOTH_STONE.defaultBlockState(),true);
        check(seamless.is(Blocks.SANDSTONE_SLAB)&&seamless.getValue(SlabBlock.TYPE)==net.minecraft.state.properties.SlabType.DOUBLE,"Legacy seamless double slab becomes double sandstone slab");
        check(DesertificationRules.replacement(Blocks.SMOOTH_STONE.defaultBlockState(),false)==null,"Seamless conversion still needs sampling");
        for(Block[] pair:new Block[][]{{Blocks.SMOOTH_STONE_SLAB,Blocks.SANDSTONE_SLAB},{Blocks.STONE_BRICK_STAIRS,Blocks.SANDSTONE_STAIRS}})for(BlockState before:pair[0].getStateDefinition().getPossibleStates()){
            BlockState after=DesertificationRules.replacement(before,true);check(after.is(pair[1])&&after.getValues().equals(before.getValues()),"Preserve slab, stair and waterlogged states");check(DesertificationRules.replacement(before,false)==null,"Shape conversion needs selection");
        }
        for(Block b:new Block[]{Blocks.GRASS,Blocks.FERN,Blocks.POPPY,Blocks.OAK_SAPLING,Blocks.WHEAT,Blocks.BROWN_MUSHROOM})check(DesertificationRules.replacement(b.defaultBlockState(),true).is(Blocks.DEAD_BUSH),"Bush replacement candidate");
        for(Block b:new Block[]{Blocks.DIRT,Blocks.SAND,Blocks.SANDSTONE,Blocks.FARMLAND,Blocks.GRASS_PATH,Blocks.MYCELIUM,Blocks.COBBLESTONE,Blocks.STONE_SLAB,Blocks.DEAD_BUSH,Blocks.SEAGRASS,Blocks.CHEST,Blocks.AIR,Blocks.LAVA})for(boolean flag:new boolean[]{false,true})check(DesertificationRules.replacement(b.defaultBlockState(),flag)==null,"Unrelated, fluid-bearing or already converted block retained");
        check(DesertificationRules.susceptible(SquidEntity.class)&&DesertificationRules.susceptible(DolphinEntity.class)&&DesertificationRules.susceptible(CodEntity.class),"Water animals susceptible");
        check(DesertificationRules.susceptible(SlimeEntity.class)&&DesertificationRules.susceptible(MagmaCubeEntity.class),"Slime inheritance includes magma cubes");
        check(!DesertificationRules.susceptible(GuardianEntity.class)&&!DesertificationRules.susceptible(DrownedEntity.class)&&!DesertificationRules.susceptible(CowEntity.class),"Aquatic monsters and ordinary animals excluded");
        for(int amplifier:new int[]{Integer.MIN_VALUE,-1,0,1,2,255,Integer.MAX_VALUE})check(DesertificationRules.damage(amplifier)==6F*(Math.max(0,Math.min(255,amplifier))+1),"Damage bounded independently of terrain radius");
        UUID owner=UUID.randomUUID();BlockPos center=new BlockPos(1,64,-2);
        for(int amplifier:new int[]{Integer.MIN_VALUE,0,1,2,Integer.MAX_VALUE}){
            DesertificationWork work=new DesertificationWork();Visitor v=new Visitor();long seed=872;work.enqueue(center,amplifier,seed,owner);int r=DesertificationWork.radius(amplifier),side=2*r+1,total=side*side*side,examined=0;
            check(r>=2&&r<=6&&r%2==0,"Even legacy radii");
            while(work.pending()>0){int count=work.tick(v);check(count>0&&count<=128,"Per-job candidate budget");examined+=count;CompoundNBT saved=work.save(new CompoundNBT());work=new DesertificationWork();work.load(saved);}
            Set<BlockPos> expected=new HashSet<>();for(int i=0;i<total;i++)if(SampledTerrainWork.selected(seed,i))expected.add(center.offset(i%side-r,i/side%side-r,i/(side*side)-r));
            check(examined==total&&v.all.size()==total,"Every water candidate visited exactly once");check(v.selected.equals(expected),"Other terrain keeps deterministic sampling through reloads");check(owner.equals(v.owner),"Ownership survives reloads");
        }
        DesertificationWork full=new DesertificationWork();for(int i=0;i<32;i++)check(full.enqueue(center.offset(i*20,0,0),2,i,owner),"Capacity admission");check(!full.enqueue(center,0,0,null),"Reject queue overflow");Visitor v=new Visitor();check(full.tick(v)==512&&v.all.size()==512,"Shared candidate budget includes unselected positions");v.allowed=false;full.tick(v);check(full.pending()==0,"Unavailable owners discard work");
        DesertificationWork queue=new DesertificationWork();queue.enqueue(center,0,5,null);CompoundNBT valid=queue.save(new CompoundNBT());
        for(int radius:new int[]{0,1,3,5,9,Integer.MAX_VALUE}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("radius",radius);DesertificationWork loaded=new DesertificationWork();loaded.load(bad);check(loaded.pending()==0,"Reject foreign or invalid radii");}
        CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("cursor",125);DesertificationWork loaded=new DesertificationWork();loaded.load(bad);check(loaded.pending()==0,"Reject completed cursor at radius two");
        RuinWork old=new RuinWork();old.enqueue(center,0,5,null);Set<BlockPos> legacySeen=new HashSet<>();SampledTerrainWork.Visitor legacy=new SampledTerrainWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos p){legacySeen.add(p);}};while(old.pending()>0)old.tick(legacy);
        int expected=0;for(int i=0;i<343;i++)if(SampledTerrainWork.selected(5,i))expected++;check(legacySeen.size()==expected,"Existing visitors still see selected positions only");
        DesertificationEffect effect=new DesertificationEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0xffc400,"Legacy metadata");check(BrewIngredients.ids().contains("minecraft:coal")&&BrewIngredients.ids().contains("minecraft:charcoal"),"Both old coal metadata variants restored");
        System.out.println("PASS: "+checks+" Desertification water, sampling, shape, target, queue and compatibility checks");
    }
}
