package com.bewitchment.potion;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import java.util.*;

/** A dimension-local, bounded queue. A cursor is consumed before invoking world code. */
public final class FertilityWork extends WorldSavedData {
    public static final String ID="bewitchment_fertility_work";
    public static final int CAPACITY=32, BUDGET=128, GROWTH_BUDGET=8;
    private final Deque<Job> jobs=new ArrayDeque<>();
    public interface Visitor { boolean allowed(UUID owner); boolean visit(BlockPos pos); }
    private static final class Job {
        final BlockPos center; final int radius; final UUID owner; int cursor;
        Job(BlockPos center,int radius,UUID owner,int cursor){this.center=center.immutable();this.radius=radius;this.owner=owner;this.cursor=cursor;}
        int size(){int side=radius*2+1;return side*side*side;}
        BlockPos position(int index){int side=radius*2+1;return center.offset(index%side-radius,index/side%side-radius,index/(side*side)-radius);}
    }
    public FertilityWork(){super(ID);}
    public static int radius(int amplifier){return Math.max(0,Math.min(2,amplifier))+1;}
    public int pending(){return jobs.size();}
    public boolean enqueue(BlockPos center,int amplifier,UUID owner){
        if(center==null||jobs.size()>=CAPACITY||center.getY()<0||center.getY()>=256)return false;
        jobs.addLast(new Job(center,radius(amplifier),owner,0));setDirty();return true;
    }
    public int tick(Visitor visitor){
        int examined=0,growth=0,turns=jobs.size();
        while(examined<BUDGET&&growth<GROWTH_BUDGET&&turns-->0&&!jobs.isEmpty()){
            Job job=jobs.removeFirst();setDirty();
            if(!visitor.allowed(job.owner))continue;
            int count=Math.min(32,Math.min(BUDGET-examined,job.size()-job.cursor));
            // Requeue before calling external code, preserving progress even if a callback throws.
            jobs.addLast(job);
            for(int i=0;i<count&&growth<GROWTH_BUDGET;i++){
                int index=job.cursor++;examined++;BlockPos pos=job.position(index);
                if(pos.getY()>=0&&pos.getY()<256&&visitor.visit(pos))growth++;
            }
            if(job.cursor==job.size())jobs.remove(job);
        }
        return examined;
    }
    @Override public CompoundNBT save(CompoundNBT tag){
        tag.putInt("version",1);ListNBT list=new ListNBT();
        for(Job job:jobs){CompoundNBT entry=new CompoundNBT();entry.putLong("center",job.center.asLong());entry.putInt("radius",job.radius);entry.putInt("cursor",job.cursor);entry.putBoolean("playerOwned",job.owner!=null);if(job.owner!=null)entry.putUUID("owner",job.owner);list.add(entry);}
        tag.put("jobs",list);return tag;
    }
    @Override public void load(CompoundNBT tag){
        jobs.clear();if(!tag.contains("version",3)||tag.getInt("version")!=1)return;
        ListNBT list=tag.getList("jobs",10);
        for(int i=0;i<list.size()&&jobs.size()<CAPACITY;i++){
            CompoundNBT e=list.getCompound(i);
            if(!e.contains("center",4)||!e.contains("radius",3)||!e.contains("cursor",3)||!e.contains("playerOwned",1))continue;
            boolean owned=e.getBoolean("playerOwned");
            if(owned&&!e.hasUUID("owner")||!owned&&e.contains("owner"))continue;
            BlockPos center=BlockPos.of(e.getLong("center"));int radius=e.getInt("radius"),cursor=e.getInt("cursor");
            if(center.getY()<0||center.getY()>=256||(radius!=1&&radius!=2&&radius!=3))continue;
            Job job=new Job(center,radius,owned?e.getUUID("owner"):null,cursor);
            if(cursor>=0&&cursor<job.size())jobs.addLast(job);
        }
    }
}
