package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class FertilityCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static FertilityWork copy(FertilityWork work){FertilityWork result=new FertilityWork();result.load(work.save(new CompoundNBT()));return result;}
    private static final class Visitor implements FertilityWork.Visitor {
        final Set<BlockPos> seen=new HashSet<>();boolean permit=true,growth=true;UUID owner;int calls;
        public boolean allowed(UUID id){owner=id;return permit;}
        public boolean visit(BlockPos pos){if(pos.getY()<0||pos.getY()>=256||!seen.add(pos))throw new AssertionError("Invalid/repeated position");calls++;return growth;}
    }
    private static final class Growth implements FertilityGrowth.Target {
        boolean valid=true,same=true,validAfter=true;int result,events,grown,reads;
        public boolean canGrow(){return ++reads==1?valid:validAfter;}
        public int event(){events++;return result;}
        public boolean unchanged(){return same;}
        public void grow(){grown++;}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();BlockPos center=new BlockPos(3,64,-4);UUID owner=UUID.randomUUID();
        for(int amp:new int[]{Integer.MIN_VALUE,-1,0,1,2,255,Integer.MAX_VALUE}){
            FertilityWork work=new FertilityWork();Visitor visitor=new Visitor();int radius=FertilityWork.radius(amp),side=radius*2+1,total=side*side*side,examined=0;
            check(radius>=1&&radius<=3,"Radius bounded");check(work.enqueue(center,amp,owner),"Queue admission");
            while(work.pending()>0){int previous=visitor.calls,count=work.tick(visitor);check(count>0&&count<=8&&visitor.calls-previous<=8,"Growth budget survives every tick");examined+=count;work=copy(work);}
            check(examined==total&&visitor.seen.size()==total,"Exact complete cube across save/reload");check(owner.equals(visitor.owner),"Owner persists");
            check(visitor.seen.contains(center.offset(-radius,-radius,-radius))&&visitor.seen.contains(center.offset(radius,radius,radius)),"Inclusive corners");
        }
        FertilityWork queue=new FertilityWork();for(int i=0;i<32;i++)check(queue.enqueue(center.offset(i*10,0,0),2,owner),"Accept up to capacity");check(!queue.enqueue(center,0,null),"Reject overflow");
        Visitor empty=new Visitor();empty.growth=false;check(queue.tick(empty)==128&&empty.calls==128,"Candidate budget shared across jobs");
        Visitor growing=new Visitor();check(queue.tick(growing)==8&&growing.calls==8,"Growth budget shared across jobs");
        Visitor denied=new Visitor();denied.permit=false;queue.tick(denied);check(queue.pending()==0&&denied.calls==0,"Unavailable owners discard remaining jobs");
        for(int y:new int[]{0,255}){FertilityWork w=new FertilityWork();w.enqueue(new BlockPos(0,y,0),2,null);Visitor v=new Visitor();while(w.pending()>0)w.tick(v);check(v.seen.size()==196,"Height clipped without losing valid positions");}
        queue=new FertilityWork();check(!queue.enqueue(null,0,null)&&!queue.enqueue(new BlockPos(0,-1,0),0,null)&&!queue.enqueue(new BlockPos(0,256,0),0,null),"Invalid centers rejected");queue.enqueue(center,0,owner);CompoundNBT valid=queue.save(new CompoundNBT());
        for(String key:new String[]{"center","radius","cursor","playerOwned","owner"}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).remove(key);FertilityWork w=new FertilityWork();w.load(bad);check(w.pending()==0,"Required field: "+key);}
        for(int radius:new int[]{-1,0,4,Integer.MAX_VALUE}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("radius",radius);FertilityWork w=new FertilityWork();w.load(bad);check(w.pending()==0,"Invalid radius rejected");}
        for(int cursor:new int[]{-1,27,Integer.MAX_VALUE}){CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putInt("cursor",cursor);FertilityWork w=new FertilityWork();w.load(bad);check(w.pending()==0,"Invalid cursor rejected");}
        CompoundNBT bad=valid.copy();bad.getList("jobs",10).getCompound(0).putBoolean("playerOwned",false);FertilityWork loaded=new FertilityWork();loaded.load(bad);check(loaded.pending()==0,"Cannot downgrade player ownership");
        bad=valid.copy();bad.getList("jobs",10).getCompound(0).putIntArray("owner",new int[]{1});loaded.load(bad);check(loaded.pending()==0,"Malformed UUID rejected");
        bad=valid.copy();bad.putInt("version",2);loaded.load(bad);check(loaded.pending()==0,"Unknown version rejected");
        bad=valid.copy();ListNBT many=bad.getList("jobs",10);for(int i=0;i<40;i++)many.add(many.getCompound(0).copy());loaded.load(bad);check(loaded.pending()==32,"Saved queue capped");
        FertilityWork interrupted=new FertilityWork();interrupted.enqueue(center,0,null);Set<BlockPos> attempted=new HashSet<>();
        try{interrupted.tick(new FertilityWork.Visitor(){public boolean allowed(UUID id){return true;}public boolean visit(BlockPos pos){attempted.add(pos);throw new IllegalStateException("growth callback failed");}});throw new AssertionError("Expected failure");}catch(IllegalStateException expected){}
        interrupted=copy(interrupted);Visitor resumed=new Visitor();while(interrupted.pending()>0)interrupted.tick(resumed);check(Collections.disjoint(attempted,resumed.seen)&&resumed.seen.size()==26,"Interrupted callback is not replayed");
        for(int result:new int[]{-1,0,1})for(int flags=0;flags<8;flags++){
            Growth g=new Growth();g.result=result;g.valid=(flags&1)!=0;g.same=(flags&2)!=0;g.validAfter=(flags&4)!=0;
            check(FertilityGrowth.apply(g)==g.valid,"Only dispatched events consume growth budget");check(g.events==(g.valid?1:0),"Invalid target skips bonemeal event");check(g.grown==(result==0&&flags==7?1:0),"Cancellation, external handling, stale state and eligibility gates");
        }
        for(int age:new int[]{Integer.MIN_VALUE,-1,0,1,6000,Integer.MAX_VALUE})for(int flags=0;flags<4;flags++)check(FertilityEffect.readyAnimal((flags&1)!=0,age,(flags&2)!=0)==(age==0&&flags==3),"Only living ready adults enter love mode");
        for(Block b:new Block[]{Blocks.WHEAT,Blocks.CARROTS,Blocks.BEETROOTS,Blocks.OAK_SAPLING,Blocks.GRASS_BLOCK,Blocks.BROWN_MUSHROOM})check(FertilityEffect.eligible(b.defaultBlockState()),"Growable target accepted");
        for(Block b:new Block[]{Blocks.STONE,Blocks.AIR,Blocks.CHEST,Blocks.WATER,Blocks.NETHER_WART})check(!FertilityEffect.eligible(b.defaultBlockState()),"Non-growable target excluded");
        FertilityEffect effect=new FertilityEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0x40c000,"Original metadata");check(BrewIngredients.ids().contains("minecraft:blue_orchid"),"Blue Orchid ingredient");
        System.out.println("PASS: "+checks+" Fertility queue, persistence, growth event and breeding readiness checks");
    }
}
