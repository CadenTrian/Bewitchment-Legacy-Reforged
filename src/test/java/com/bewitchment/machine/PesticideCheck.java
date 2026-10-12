package com.bewitchment.machine;
import com.bewitchment.potion.*;
import com.bewitchment.combat.TerrainBrewImpacts;
import net.minecraft.block.*;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class PesticideCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    private static final class Terrain implements BrewTerrain.Terrain {
        int flags,reads,permissions,writes;Set<BlockPos> seen=new HashSet<>();
        Terrain(int flags){this.flags=flags;}
        public boolean loaded(BlockPos p){return (flags&1)!=0;}
        public boolean insideBorder(BlockPos p){if((flags&1)==0)throw new AssertionError("Border before loaded");return (flags&2)!=0;}
        public boolean eligible(BlockPos p){if((flags&3)!=3)throw new AssertionError("Read outside bounds");reads++;return (flags&4)!=0;}
        public boolean mayEdit(BlockPos p){if((flags&7)!=7)throw new AssertionError("Permission before eligibility");permissions++;return (flags&8)!=0;}
        public boolean clear(BlockPos p){if((flags&15)!=15)throw new AssertionError("Write without permission");writes++;seen.add(p);return (flags&16)!=0;}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();BlockPos center=new BlockPos(10,64,-10);
        for(int amp:new int[]{-1,0,1,2,3,255,Integer.MAX_VALUE}){
            Terrain t=new Terrain(31);int r=BrewTerrain.radius(amp),side=2*r+1,total=side*side*side;
            check(r>=1&&r<=3,"Terrain radius always bounded");
            check(BrewTerrain.clear(center,amp,t)==total&&t.writes==total,"Exact inclusive scan volume");
            check(t.seen.size()==total,"Positions are unique immutable snapshots");
            check(t.seen.contains(center.offset(r,r,r))&&t.seen.contains(center.offset(-r,-r,-r)),"Opposite corners included");
            check(!t.seen.contains(center.offset(r+1,0,0)),"Outside radius excluded");
        }
        for(int flags=0;flags<32;flags++){
            Terrain t=new Terrain(flags);int changed=BrewTerrain.clear(center,0,t);
            check(changed==(flags==31?27:0),"All load/border/type/permission/write outcome combinations");
            check(t.writes==((flags&15)==15?27:0),"Rejected stages cannot mutate");
        }
        for(int y:new int[]{0,1,254,255}){
            Terrain t=new Terrain(31);int layers=Math.min(255,y+3)-Math.max(0,y-3)+1;
            check(BrewTerrain.clear(new BlockPos(0,y,0),2,t)==49*layers,"Build-height clipping");
            check(t.seen.stream().allMatch(p->p.getY()>=0&&p.getY()<256),"No reads outside world height");
        }
        for(int y:new int[]{-10,270})check(BrewTerrain.clear(new BlockPos(0,y,0),2,new Terrain(31))==0,"Far outside build height does no work");
        for(int flags=0;flags<16;flags++)check(BrewTerrain.actorAllowed((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0)==(flags==11),"Actor world/alive/spectator/build gates");
        CompoundNBT cloud=new CompoundNBT();
        check(BrewTerrain.cloudPulse(cloud,"a",0)&&!BrewTerrain.cloudPulse(cloud,"a",0),"Tick zero and duplicate target dispatch");
        check(BrewTerrain.cloudPulse(cloud,"b",0),"Other terrain effect has an independent cloud marker");
        check(!BrewTerrain.cloudPulse(cloud.copy(),"a",0)&&BrewTerrain.cloudPulse(cloud,"a",5),"Cloud marker serialization and later tick");
        cloud.putString("a","invalid");check(BrewTerrain.cloudPulse(cloud,"a",5),"Malformed marker is repaired");
        check(PesticideEffect.eligible(Blocks.COBWEB.defaultBlockState()),"Cobweb qualifies");
        for(Block block:new Block[]{Blocks.AIR,Blocks.STONE,Blocks.CHEST,Blocks.GRASS,Blocks.WHEAT})check(!PesticideEffect.eligible(block.defaultBlockState()),"Other terrain and containers remain intact");
        check(PesticideEffect.susceptible(CreatureAttribute.ARTHROPOD)&&!PesticideEffect.susceptible(CreatureAttribute.UNDEAD)&&!PesticideEffect.susceptible(CreatureAttribute.UNDEFINED),"Only arthropods take brew damage");
        for(int amp:new int[]{0,1,2,255})check(PesticideEffect.damage(amp)==8F*(amp+1),"Legacy entity damage per level");
        check(PesticideEffect.damage(Integer.MAX_VALUE)==2048&&PesticideEffect.damage(-1)==8,"Damage arithmetic bounded separately from scan radius");
        PesticideEffect effect=new PesticideEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0x007f00,"Original effect metadata");
        BlockRayTraceResult hit=new BlockRayTraceResult(new Vector3d(10,64,-10),Direction.UP,center,false);
        check(TerrainBrewImpacts.center(hit).equals(center),"Impact uses hit block rather than projectile position");
        check(TerrainBrewImpacts.center(BlockRayTraceResult.miss(Vector3d.ZERO,Direction.UP,BlockPos.ZERO))==null,"Miss does not alter terrain");
        System.out.println("PASS: "+checks+" pesticide scan, permission order, cloud deduplication, impact and target checks");
    }
}
