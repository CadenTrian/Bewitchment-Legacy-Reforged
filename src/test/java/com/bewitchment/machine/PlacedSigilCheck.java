package com.bewitchment.machine;

import com.bewitchment.block.PlacedSigilBlock;
import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class PlacedSigilCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
        for(Kind kind:Kind.values()){
            PlacedSigilData data=new PlacedSigilData();check(data.install(kind,owner),"Install "+kind);
            check(data.remaining()==168000 && data.delay()==kind.cooldown,"Initial lifetime and warmup "+kind);
            check(data.targets.targets(owner)==kind.positive && data.targets.targets(other)!=kind.positive,"Targeting "+kind);
            for(int i=0;i<kind.cooldown;i++)if(data.advance()!=PlacedSigilData.Step.WAIT)throw new AssertionError("Early pulse "+kind);
            check(data.advance()==PlacedSigilData.Step.PULSE && data.delay()==kind.cooldown,"First pulse boundary "+kind);
            check(data.advance()==(kind.cooldown==0?PlacedSigilData.Step.PULSE:PlacedSigilData.Step.WAIT),"Following pulse delay "+kind);
            PlacedSigilData copy=new PlacedSigilData();copy.load(data.save());
            check(copy.remaining()==data.remaining() && copy.delay()==data.delay() && copy.targets.targets(owner)==kind.positive,"Persistence "+kind);
            check(!copy.install(Kind.LUCK,other),"Cannot reset lifetime through reinstall "+kind);
        }
        PlacedSigilData data=new PlacedSigilData();check(data.advance()==PlacedSigilData.Step.EXPIRE,"Unconfigured sigil expires safely");
        data.install(Kind.BINDING,owner);data.targets.toggle(other.toString());
        check(!data.targets.targets(other),"Mob or player UUID can be exempted by taglock");
        CompoundNBT saved=data.save();saved.putInt("timer",1);saved.putInt("delay",0);data.load(saved);
        check(data.advance()==PlacedSigilData.Step.PULSE && data.remaining()==0,"Last lifetime tick still pulses like legacy");
        check(data.advance()==PlacedSigilData.Step.EXPIRE && data.advance()==PlacedSigilData.Step.EXPIRE,"Expiry does not resume effects");
        saved.putInt("timer",Integer.MAX_VALUE);saved.putInt("delay",Integer.MAX_VALUE);data.load(saved);
        check(data.remaining()==168000 && data.delay()==600,"Oversized timers clamp");
        saved.putInt("timer",-1);saved.putInt("delay",-1);data.load(saved);check(data.remaining()==0 && data.delay()==0,"Negative timers clamp");
        saved.getCompound("targeting").putString("sigil","bewitchment:unknown");data.load(saved);check(data.advance()==PlacedSigilData.Step.EXPIRE,"Unknown sigil is inert and expires");
        AxisAlignedBB area=PlacedSigilData.area(new BlockPos(10,20,-30));
        check(area.minX==4 && area.maxX==16 && area.minY==17 && area.maxY==23 && area.minZ==-36 && area.maxZ==-24,"Exact legacy 12x6x12 corner-centered area");
        check(!area.intersects(new AxisAlignedBB(16,20,-30,17,21,-29)) && area.intersects(new AxisAlignedBB(15.9,20,-30,17,21,-29)),"Entity box intersection boundary");
        PlacedSigilBlock block=new PlacedSigilBlock();BlockPos pos=new BlockPos(-13,64,41);
        check(PlacedSigilBlock.variation(pos)==3,"Position variation formula");
        int extreme=PlacedSigilBlock.variation(new BlockPos(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE));check(extreme>=0 && extreme<10,"No coordinate overflow into invalid variant");
        for(Direction face:Direction.values()){
            BlockState state=block.defaultBlockState().setValue(PlacedSigilBlock.FACING,face);
            AxisAlignedBB box=block.getShape(state,null,pos,ISelectionContext.empty()).bounds();
            double thickness=face.getAxis()==Direction.Axis.X?box.getXsize():face.getAxis()==Direction.Axis.Y?box.getYsize():box.getZsize();
            check(Math.abs(thickness-.01)<.00001,"Thin selection shape "+face);
            check(state.getCollisionShape(null,pos).isEmpty(),"No collision "+face);
            check(block.getPistonPushReaction(state)==PushReaction.DESTROY,"Piston destruction "+face);
        }
        check(block.rotate(block.defaultBlockState().setValue(PlacedSigilBlock.FACING,Direction.NORTH),Rotation.CLOCKWISE_90).getValue(PlacedSigilBlock.FACING)==Direction.EAST,"Wall rotation");
        check(block.rotate(block.defaultBlockState(),Rotation.CLOCKWISE_90).getValue(PlacedSigilBlock.FACING)==Direction.UP,"Floor rotation preserves attachment");
        check(block.defaultBlockState().getLightEmission()==4,"Legacy light level");
        PlacedSigilTile tile=new PlacedSigilTile(TileEntityType.CHEST);tile.install(Kind.MENDING,owner);
        tile.data.targets.toggle(other.toString());tile.data.advance();
        PlacedSigilTile loaded=new PlacedSigilTile(TileEntityType.CHEST);loaded.load(block.defaultBlockState(),tile.save(new CompoundNBT()));
        check(loaded.kind()==Kind.MENDING && loaded.data.remaining()==167999 && loaded.data.delay()==599,"Tile state persists");
        check(loaded.data.targets.targets(other),"Tile target list persists");
        CompoundNBT update=tile.getUpdateTag();
        check(update.getString("sigil").equals(Kind.MENDING.id()) && !update.contains("sigilData") && !update.toString().contains(owner.toString()),"Client update contains identity without targeting list");
        System.out.println("PASS: "+checks+" placed sigil checks");
    }
}
