package com.bewitchment.potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.CompoundNBT;

/** Bounded loaded-area traversal, shared by terrain brew delivery paths. */
public final class BrewTerrain {
    public interface Terrain {
        boolean loaded(BlockPos pos);
        boolean insideBorder(BlockPos pos);
        boolean eligible(BlockPos pos);
        boolean mayEdit(BlockPos pos);
        boolean clear(BlockPos pos);
    }
    public static int radius(int amplifier){return Math.max(0,Math.min(2,amplifier))+1;}
    public static boolean actorAllowed(boolean sameWorld,boolean alive,boolean spectator,boolean mayBuild){return sameWorld&&alive&&!spectator&&mayBuild;}
    public static int clear(BlockPos center,int amplifier,Terrain terrain){
        int changed=0,r=radius(amplifier);
        for(BlockPos cursor:BlockPos.betweenClosed(center.offset(-r,-r,-r),center.offset(r,r,r))){
            BlockPos pos=cursor.immutable();
            if(pos.getY()>=0&&pos.getY()<256&&terrain.loaded(pos)&&terrain.insideBorder(pos)&&terrain.eligible(pos)&&terrain.mayEdit(pos)&&terrain.clear(pos))changed++;
        }
        return changed;
    }
    public static boolean cloudPulse(CompoundNBT data,String key,long tick){
        if(data.contains(key,4)&&data.getLong(key)==tick)return false;
        data.putLong(key,tick);return true;
    }
    private BrewTerrain(){}
}
