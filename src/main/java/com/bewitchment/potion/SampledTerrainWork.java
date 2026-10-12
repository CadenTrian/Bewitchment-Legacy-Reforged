package com.bewitchment.potion;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import java.util.*;

/** A dimension-local, bounded queue. A cursor is consumed before invoking world code. */
public class SampledTerrainWork extends WorldSavedData {
    public static final int CAPACITY=32, BUDGET=512;
    private final Deque<Job> jobs=new ArrayDeque<>();
    public interface Visitor {
        boolean allowed(UUID owner);
        void visit(BlockPos pos);
        default void visit(BlockPos pos,boolean selected){if(selected)visit(pos);}
    }
    private static final class Job {
        final BlockPos center; final int radius; final long seed; final UUID owner; int cursor;
        Job(BlockPos center,int radius,long seed,UUID owner,int cursor){this.center=center.immutable();this.radius=radius;this.seed=seed;this.owner=owner;this.cursor=cursor;}
        int size(){int side=radius*2+1;return side*side*side;}
        BlockPos position(int index){int side=radius*2+1;return center.offset(index%side-radius,index/side%side-radius,index/(side*side)-radius);}
    }
    protected SampledTerrainWork(String id){super(id);}
    public static int radius(int amplifier){return 3*(Math.max(0,Math.min(2,amplifier))+1);}
    protected int radiusForAmplifier(int amplifier){return radius(amplifier);}
    protected boolean validRadius(int radius){return radius==3||radius==6||radius==9;}
    public static boolean selected(long seed,int index){return new Random(seed ^ (0x9e3779b97f4a7c15L*index)).nextInt(3)==0;}
    public int pending(){return jobs.size();}
    public boolean enqueue(BlockPos center,int amplifier,long seed,UUID owner){
        if(center==null||jobs.size()>=CAPACITY||center.getY()<0||center.getY()>=256)return false;
        jobs.addLast(new Job(center,radiusForAmplifier(amplifier),seed,owner,0));setDirty();return true;
    }
    public int tick(Visitor visitor){
        int examined=0,turns=jobs.size();
        while(examined<BUDGET&&turns-->0&&!jobs.isEmpty()){
            Job job=jobs.removeFirst();setDirty();
            if(!visitor.allowed(job.owner))continue;
            int count=Math.min(128,Math.min(BUDGET-examined,job.size()-job.cursor));
            // Requeue before calling external code, preserving progress even if a callback throws.
            jobs.addLast(job);
            for(int i=0;i<count;i++){
                int index=job.cursor++;examined++;BlockPos pos=job.position(index);
                if(pos.getY()>=0&&pos.getY()<256)visitor.visit(pos,selected(job.seed,index));
            }
            if(job.cursor==job.size())jobs.remove(job);
        }
        return examined;
    }
    @Override public CompoundNBT save(CompoundNBT tag){
        tag.putInt("version",1);ListNBT list=new ListNBT();
        for(Job job:jobs){CompoundNBT entry=new CompoundNBT();entry.putLong("center",job.center.asLong());entry.putInt("radius",job.radius);entry.putLong("seed",job.seed);entry.putInt("cursor",job.cursor);entry.putBoolean("playerOwned",job.owner!=null);if(job.owner!=null)entry.putUUID("owner",job.owner);list.add(entry);}
        tag.put("jobs",list);return tag;
    }
    @Override public void load(CompoundNBT tag){
        jobs.clear();if(!tag.contains("version",3)||tag.getInt("version")!=1)return;
        ListNBT list=tag.getList("jobs",10);
        for(int i=0;i<list.size()&&jobs.size()<CAPACITY;i++){
            CompoundNBT e=list.getCompound(i);
            if(!e.contains("center",4)||!e.contains("radius",3)||!e.contains("seed",4)||!e.contains("cursor",3)||!e.contains("playerOwned",1))continue;
            boolean owned=e.getBoolean("playerOwned");
            if(owned&&!e.hasUUID("owner")||!owned&&e.contains("owner"))continue;
            BlockPos center=BlockPos.of(e.getLong("center"));int radius=e.getInt("radius"),cursor=e.getInt("cursor");
            if(center.getY()<0||center.getY()>=256||!validRadius(radius))continue;
            Job job=new Job(center,radius,e.getLong("seed"),owned?e.getUUID("owner"):null,cursor);
            if(cursor>=0&&cursor<job.size())jobs.addLast(job);
        }
    }
}
