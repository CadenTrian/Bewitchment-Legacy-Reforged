package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class RuinCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static RuinWork copy(RuinWork work){RuinWork result=new RuinWork();result.load(work.save(new CompoundNBT()));return result;}
    private static final class Visitor implements RuinWork.Visitor {
        final Set<BlockPos> seen=new HashSet<>();boolean allow=true;UUID owner;
        public boolean allowed(UUID id){owner=id;return allow;}
        public void visit(BlockPos pos){if(!seen.add(pos))throw new AssertionError("Repeated position");if(pos.getY()<0||pos.getY()>=256)throw new AssertionError("Outside build height");}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();BlockPos center=new BlockPos(20,64,-30);UUID owner=UUID.randomUUID();
        for(int amp:new int[]{Integer.MIN_VALUE,-1,0,1,2,3,255,Integer.MAX_VALUE}){
            RuinWork work=new RuinWork();Visitor visitor=new Visitor();int radius=RuinWork.radius(amp),side=2*radius+1,total=side*side*side,examined=0;
            check(radius>=3&&radius<=9,"Clamped radius");check(work.enqueue(center,amp,72,owner),"Admit job");
            int ticks=0;
            while(work.pending()>0){int count=work.tick(visitor);check(count>0&&count<=128,"Single job bounded");examined+=count;work=copy(work);if(++ticks>60)throw new AssertionError("Never completes");}
            check(examined==total,"Every candidate consumed once");check(owner.equals(visitor.owner),"Owner survives saves");
            Set<BlockPos> expected=new HashSet<>();for(int i=0;i<total;i++)if(RuinWork.selected(72,i))expected.add(center.offset(i%side-radius,i/side%side-radius,i/(side*side)-radius));
            check(visitor.seen.equals(expected),"Save/reload preserves positions and random outcomes");check(expected.size()>total/4&&expected.size()<total/2,"Approximate one-third selection");
        }
        RuinWork full=new RuinWork();for(int i=0;i<32;i++)check(full.enqueue(center,2,i,null),"Capacity admission");check(!full.enqueue(center,0,0,null),"Reject overflow");
        check(full.tick(new RuinWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos pos){}})==512,"Dimension budget shared across jobs");
        Visitor denied=new Visitor();denied.allow=false;full.tick(denied);check(full.pending()==0&&denied.seen.isEmpty(),"Invalid owners discard all pending work without edits");
        RuinWork work=new RuinWork();check(!work.enqueue(null,0,0,null),"Reject missing center");check(!work.enqueue(new BlockPos(0,-1,0),0,0,null),"Reject low center");check(!work.enqueue(new BlockPos(0,256,0),0,0,null),"Reject high center");
        for(int y:new int[]{0,255}){work=new RuinWork();work.enqueue(new BlockPos(0,y,0),2,3,null);Visitor v=new Visitor();while(work.pending()>0)work.tick(v);check(!v.seen.isEmpty(),"Boundary scan still visits valid height");}
        work=new RuinWork();work.enqueue(center,0,2,owner);CompoundNBT valid=work.save(new CompoundNBT());
        for(String field:new String[]{"center","radius","seed","cursor","playerOwned","owner"}){
            CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).remove(field);RuinWork loaded=new RuinWork();loaded.load(bad);check(loaded.pending()==0,"Missing field rejected: "+field);
        }
        for(int radius:new int[]{Integer.MIN_VALUE,0,1,4,10,Integer.MAX_VALUE}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("radius",radius);RuinWork loaded=new RuinWork();loaded.load(bad);check(loaded.pending()==0,"Invalid radius rejected");}
        for(int cursor:new int[]{-1,343,Integer.MAX_VALUE}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("cursor",cursor);RuinWork loaded=new RuinWork();loaded.load(bad);check(loaded.pending()==0,"Invalid cursor rejected");}
        CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putBoolean("playerOwned",false);RuinWork loaded=new RuinWork();loaded.load(bad);check(loaded.pending()==0,"Cannot downgrade owner to fake player");
        bad=valid.copy();bad.putInt("version",2);loaded.load(bad);check(loaded.pending()==0,"Unknown version rejected");
        bad=valid.copy();bad.getList("jobs",10).getCompound(0).putIntArray("owner",new int[]{1,2});loaded.load(bad);check(loaded.pending()==0,"Malformed UUID rejected");
        bad=valid.copy();bad.getList("jobs",10).getCompound(0).putString("cursor","0");loaded.load(bad);check(loaded.pending()==0,"Wrong field type rejected");
        bad=valid.copy();ListNBT many=bad.getList("jobs",10);for(int i=0;i<40;i++)many.add(many.getCompound(0).copy());loaded.load(bad);check(loaded.pending()==32,"Saved queue also capped");
        RuinWork interrupted=new RuinWork();interrupted.enqueue(center,0,2,null);Set<BlockPos> attempted=new HashSet<>();
        try{interrupted.tick(new RuinWork.Visitor(){public boolean allowed(UUID id){return true;}public void visit(BlockPos pos){attempted.add(pos);throw new IllegalStateException("simulated callback failure");}});throw new AssertionError("Expected callback failure");}catch(IllegalStateException expected){}
        interrupted=copy(interrupted);Visitor resumed=new Visitor();while(interrupted.pending()>0)interrupted.tick(resumed);check(Collections.disjoint(attempted,resumed.seen),"Failed callback does not repeat after reload");
        for(Block block:new Block[]{Blocks.STONE,Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE})check(RuinEffect.replacement(block.defaultBlockState()).is(Blocks.COBBLESTONE),"Stone conversion");
        check(RuinEffect.replacement(Blocks.COBBLESTONE.defaultBlockState()).is(Blocks.GRAVEL),"Cobble conversion");
        for(Block block:new Block[]{Blocks.GRAVEL,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL,Blocks.GRASS_BLOCK})check(RuinEffect.replacement(block.defaultBlockState()).is(Blocks.SAND),"Soil conversion");
        for(Block block:new Block[]{Blocks.DANDELION,Blocks.POPPY,Blocks.GRASS,Blocks.FERN,Blocks.WHEAT,Blocks.CARROTS,Blocks.POTATOES,Blocks.BEETROOTS})check(RuinEffect.replacement(block.defaultBlockState()).is(Blocks.DEAD_BUSH),"Plant conversion");
        for(Block block:new Block[]{Blocks.CHEST,Blocks.AIR,Blocks.STONE_BRICKS,Blocks.DIAMOND_ORE,Blocks.MYCELIUM,Blocks.FARMLAND,Blocks.SAND,Blocks.DEAD_BUSH,Blocks.CACTUS,Blocks.OAK_LOG})check(RuinEffect.replacement(block.defaultBlockState())==null,"Unrelated block retained");
        check(RuinEffect.duration(0)==200&&RuinEffect.duration(1)==400&&RuinEffect.duration(2)==600,"Legacy debuff duration");check(RuinEffect.duration(Integer.MAX_VALUE)==51200&&RuinEffect.duration(-1)==200,"Duration overflow clamped");
        check(new RuinEffect().isInstantenous(),"Instant delivery");
        System.out.println("PASS: "+checks+" Ruin checks");
    }
}
