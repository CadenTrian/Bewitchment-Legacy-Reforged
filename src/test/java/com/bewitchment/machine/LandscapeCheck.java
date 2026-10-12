package com.bewitchment.machine;

import com.bewitchment.item.BolineItem;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.*;
import java.util.*;

public final class LandscapeCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static class Cells implements LandscapeRitual.Cells {
        final Biome[] values=new Biome[1024];
        int permissions,writes;
        BlockPos denied;
        Cells(Biome original){Arrays.fill(values,original);}
        public boolean permitted(int x,int y,int z){permissions++;return !new BlockPos(x,y,z).equals(denied);}
        public Biome get(int x,int y,int z){return values[LandscapeRitual.biomeIndex(x,y,z)];}
        public void set(int x,int y,int z,Biome value){writes++;values[LandscapeRitual.biomeIndex(x,y,z)]=value;}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        Biome plains=WorldGenRegistries.BIOME.get(Biomes.PLAINS), desert=WorldGenRegistries.BIOME.get(Biomes.DESERT);
        check(plains!=null && desert!=null && plains!=desert,"Distinct registered biome fixtures");
        boolean[] indices=new boolean[1024]; boolean unique=true;
        for(int x=0;x<4;x++)for(int y=0;y<64;y++)for(int z=0;z<4;z++){
            int i=LandscapeRitual.biomeIndex(x,y,z); unique &= i>=0 && i<1024 && !indices[i]; indices[i]=true;
        }
        check(unique,"Every quart cell has a unique packed index");
        check(LandscapeRitual.biomeIndex(3,63,3)==1023,"Top corner fits vanilla array");
        BlockPos center=new BlockPos(0,64,0);
        check(LandscapeRitual.columnInside(center,0,0),"Central column included");
        check(!LandscapeRitual.columnInside(center,28,8),"Partially outside column excluded");
        check(LandscapeRitual.columnInside(center,-4,-4),"Negative aligned cell included");
        check(!LandscapeRitual.columnInside(center,-32,0),"Strict boundary excluded");
        Cells cells=new Cells(plains);
        int changed=LandscapeRitual.transform(center,new ChunkPos(0,0),desert,cells);
        check(changed==1024 && cells.writes==1024 && cells.permissions==65536,"Full interior chunk changes all vertical cells with every represented block checked");
        check(Arrays.stream(cells.values).allMatch(b->b==desert),"All changed biome references retained");
        int permissions=cells.permissions;
        check(LandscapeRitual.transform(center,new ChunkPos(0,0),desert,cells)==0 && cells.permissions==permissions,"Unchanged biome skips permission work and writes");
        cells=new Cells(plains);cells.denied=new BlockPos(3,255,3);
        changed=LandscapeRitual.transform(center,new ChunkPos(0,0),desert,cells);
        check(changed==1023 && cells.get(0,63,0)==plains,"One denied block protects its entire vertical quart cell");
        check(cells.get(0,62,0)==desert && cells.get(1,63,0)==desert,"Permission denial does not leak to unrelated cells");
        cells=new Cells(plains);
        check(LandscapeRitual.transform(center,new ChunkPos(2,2),desert,cells)==0 && cells.permissions==0,"Outside chunk causes no permission calls or writes");
        cells=new Cells(plains);
        check(LandscapeRitual.transform(center,new ChunkPos(-1,-1),desert,cells)==1024,"Negative chunk coordinates map correctly");
        UUID owner=UUID.fromString("bf9a0048-c0e2-4d21-9f36-9c5e01e87970");
        ResourceLocation target=new ResourceLocation("minecraft:desert");
        LandscapeRitual job=new LandscapeRitual(center,owner,target,false);
        check(job.chunk(0).equals(new ChunkPos(-2,-2)) && job.chunk(24).equals(new ChunkPos(2,2)),"Bounded five-by-five chunk traversal");
        Set<ChunkPos> chunks=new HashSet<>();for(int i=0;i<25;i++)chunks.add(job.chunk(i));
        check(chunks.size()==25,"No duplicate chunk work");
        CompoundNBT saved=job.save();saved.putInt("ChunkCursor",7);
        LandscapeRitual resumed=LandscapeRitual.read(center,saved);
        check(resumed!=null && resumed.save().getInt("ChunkCursor")==7 && owner.equals(resumed.owner()),"Biome target, owner and chunk progress survive save");
        check(resumed.save().getString("Biome").equals("minecraft:desert"),"Stable registry identifier persisted");
        resumed.stop();check(resumed.finished() && LandscapeRitual.read(center,resumed.save())==null,"Stopped biome work cannot replay");
        for(int cursor:new int[]{-1,26,Integer.MAX_VALUE}){
            CompoundNBT bad=saved.copy();bad.putInt("ChunkCursor",cursor);
            check(LandscapeRitual.read(center,bad)==null,"Invalid chunk cursor rejected");
        }
        for(String invalid:new String[]{"","BAD NAME","minecraft:bad name"}){
            CompoundNBT bad=saved.copy();bad.putString("Biome",invalid);
            check(LandscapeRitual.read(center,bad)==null,"Malformed biome identifier rejected");
        }
        CompoundNBT bad=saved.copy();bad.remove("Owner");check(LandscapeRitual.read(center,bad)==null,"Missing owner rejected");
        bad=saved.copy();bad.putInt("Version",2);check(LandscapeRitual.read(center,bad)==null,"Unknown job version rejected");
        bad=saved.copy();bad.putString("Salt","false");check(LandscapeRitual.read(center,bad)==null,"Wrong salt flag type rejected");
        bad=saved.copy();bad.putInt("ChunkCursor",25);check(LandscapeRitual.read(center,bad)==null,"Completed biome-only job discarded");
        LandscapeRitual salt=new LandscapeRitual(center,owner,target,true);
        CompoundNBT saltData=salt.save();saltData.putInt("ChunkCursor",25);
        check(LandscapeRitual.read(center,saltData)!=null,"Salt job proceeds after biome phase");
        NatureBlessing soil=new NatureBlessing(center,owner,4096,true);
        saltData.put("Soil",soil.save());
        LandscapeRitual saltResume=LandscapeRitual.read(center,saltData);
        check(saltResume!=null && saltResume.save().getCompound("Soil").getInt("Cursor")==4096,"Nested soil progress resumes without redoing biomes");
        bad=saltData.copy();bad.putString("Biome","minecraft:plains");check(LandscapeRitual.read(center,bad)==null,"Salt target cannot become arbitrary biome");
        bad=saltData.copy();bad.putInt("ChunkCursor",24);check(LandscapeRitual.read(center,bad)==null,"Soil cannot precede biome phase completion");
        bad=saltData.copy();bad.getCompound("Soil").putUUID("Owner",UUID.randomUUID());check(LandscapeRitual.read(center,bad)==null,"Mismatched nested owner rejected");
        bad=saltData.copy();bad.getCompound("Soil").putBoolean("Salt",false);check(LandscapeRitual.read(center,bad)==null,"Restorative soil mode rejected inside salt job");
        saltResume.stop();check(saltResume.finished() && LandscapeRitual.read(center,saltResume.save())==null,"Stopped salt work cannot replay");
        check(soil.position(0).getY()==4 && soil.total()==252*4096,"Salt soil starts sixty blocks below focal glyph");
        NatureBlessing high=new NatureBlessing(new BlockPos(0,255,0),owner,0,true);
        check(high.position(0).getY()==195 && high.total()==61*4096,"High salt cast stays in legal build height");
        for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{Blocks.DIRT,Blocks.PODZOL,Blocks.GRASS_BLOCK,Blocks.GRASS_PATH,Blocks.MYCELIUM,Blocks.FARMLAND})
            check(NatureBlessing.salts(block),"Legacy soil converts to coarse dirt");
        for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{Blocks.COARSE_DIRT,Blocks.STONE,Blocks.SAND,Blocks.OAK_LOG})
            check(!NatureBlessing.salts(block),"Other terrain and existing coarse dirt unchanged");
        CompoundNBT legacy=new NatureBlessing(center,owner,4096).save();legacy.remove("Salt");
        check(!NatureBlessing.read(center,legacy).salted(),"Existing Nature blessing jobs remain compatible");
        BolineItem boline=new BolineItem();ItemStack tool=new ItemStack(boline);
        check(BolineItem.recordedBiome(tool)==null,"Unbound tool rejected");
        tool.getOrCreateTag().putString("Biome","minecraft:ocean");
        check(new ResourceLocation("minecraft:ocean").equals(BolineItem.recordedBiome(tool)),"Ocean is accepted without old numeric zero restriction");
        tool.getTag().putString("Biome","example:custom");
        check(new ResourceLocation("example:custom").equals(BolineItem.recordedBiome(tool)),"Custom biome names supported");
        tool.getTag().putString("Biome","invalid value");check(BolineItem.recordedBiome(tool)==null,"Malformed tool data rejected");
        tool.getTag().putInt("Biome",1);check(BolineItem.recordedBiome(tool)==null,"Legacy numeric data not misinterpreted");
        tool.getTag().putString("Biome","minecraft:desert");
        Inventory offerings=new Inventory(10);offerings.setItem(6,tool);
        check(target.equals(LandscapeRitual.target(offerings)),"Boline found in arbitrary offering slot");
        offerings.setItem(7,tool.copy());check(LandscapeRitual.target(offerings)==null,"Ambiguous Bolines rejected");
        offerings.clearContent();ItemStack paper=new ItemStack(Items.PAPER);paper.getOrCreateTag().putString("Biome","minecraft:desert");offerings.setItem(0,paper);
        check(LandscapeRitual.target(offerings)==null,"Only a Boline supplies biome target");
        check(target.equals(LandscapeRitual.target(RitualEffect.SOWING_SALT,offerings)),"Salt has fixed desert target");
        check(RitualEffect.BIOME_SHIFT.failure(false,true)==null && "overworld".equals(RitualEffect.SOWING_SALT.failure(false,true)),"Local biome shift and Overworld weather ritual restrictions");
        int[] stops={0};
        RitualEffect.SOWING_SALT.complete(new RitualEffect.Environment(){
            public long dayTime(){throw new AssertionError();}
            public void setDayTime(long time){throw new AssertionError();}
            public int rainRoll(){throw new AssertionError();}
            public void startRain(int ticks){throw new AssertionError();}
            public void stopRain(){throw new AssertionError("Thunder omitted");}
            public void startStorm(){throw new AssertionError();}
            public void stopStorm(){stops[0]++;}
        });
        check(stops[0]==1,"Salt stops rain and thunder once without changing time");
        check(RitualPower.startingCost(1300,true)+50*33==2755 && 1300+50*33==2950,"Biome Shift power totals");
        System.out.println("PASS: "+checks+" landscape biome, permission, persistence, targeting and soil checks.");
    }
}
