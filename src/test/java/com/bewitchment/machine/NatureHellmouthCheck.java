package com.bewitchment.machine;

import com.google.gson.JsonParseException;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class NatureHellmouthCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void rejects(Runnable work, String message) {
        boolean rejected=false;
        try { work.run(); } catch (JsonParseException expected) { rejected=true; }
        check(rejected,message);
    }
    private static Random constant(int value) {
        return new Random(0) { @Override public int nextInt(int bound) {return value;} };
    }
    private static class Terrain implements NatureBlessing.Terrain {
        boolean loaded=true, border=true, convertible=true, editable=true;
        int reads, edits, writes;
        public boolean loaded(BlockPos pos) {return loaded;}
        public boolean insideBorder(BlockPos pos) {return border;}
        public boolean convertible(BlockPos pos) {reads++; return convertible;}
        public boolean mayEdit(BlockPos pos) {edits++; return editable;}
        public void restore(BlockPos pos) {writes++;}
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        check(NatureBlessing.converts(Blocks.COARSE_DIRT),"Coarse dirt restored");
        check(NatureBlessing.converts(Blocks.PODZOL),"Podzol restored like legacy dirt variant");
        for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.MYCELIUM,Blocks.FARMLAND,Blocks.GRASS_PATH,Blocks.STONE})
            check(!NatureBlessing.converts(block),"Unrelated terrain and existing dirt unchanged");
        check(NatureBlessing.inCircle(0,0) && NatureBlessing.inCircle(31,0),"Interior included");
        check(!NatureBlessing.inCircle(32,0) && !NatureBlessing.inCircle(-32,0),"Strict radius excludes boundary");
        check(NatureBlessing.inCircle(22,23) && !NatureBlessing.inCircle(23,23),"Circular rather than square boundary");
        check(!NatureBlessing.inCircle(Integer.MAX_VALUE,Integer.MAX_VALUE) && !NatureBlessing.inCircle(Integer.MIN_VALUE,Integer.MIN_VALUE),"Radius arithmetic does not wrap");
        check(NatureBlessing.minY(0)==0 && NatureBlessing.maxY(0)==255,"Low focal glyph clamps to build floor");
        check(NatureBlessing.minY(64)==0 && NatureBlessing.maxY(64)==255,"Typical focal glyph scans full build height");
        check(NatureBlessing.minY(255)==191 && NatureBlessing.maxY(255)==255,"High focal glyph preserves relative lower boundary");
        UUID owner=UUID.fromString("124f7b10-7dd5-4381-8b41-d52d37671692");
        BlockPos center=new BlockPos(-100,64,200);
        NatureBlessing job=new NatureBlessing(center,owner,0);
        check(job.total()==1048576 && !job.finished(),"Maximum scan is finite");
        check(job.position(0).equals(new BlockPos(-132,0,168)),"First scan position and negative coordinates");
        check(job.position(job.total()-1).equals(new BlockPos(-69,255,231)),"Last scan position");
        check(job.advance(0,p->{throw new AssertionError("Zero budget visited");})==0 && job.cursor()==0,"Zero budget cannot work");
        check(job.advance(-1,p->{throw new AssertionError("Negative budget visited");})==0 && job.cursor()==0,"Negative budget cannot work");
        int[] visits={0}; job.advance(4096,p->visits[0]++);
        check(job.cursor()==4096 && visits[0]==3205,"One tick visits exactly one circular layer");
        CompoundNBT saved=job.save();
        NatureBlessing resumed=NatureBlessing.read(center,saved);
        check(resumed!=null && resumed.cursor()==4096 && owner.equals(resumed.owner()),"Saved owner/progress resume");
        check(resumed.position(resumed.cursor()).getY()==1,"Resume starts at next layer");
        resumed.advance(1,p->{});
        check(job.cursor()==4096 && saved.getInt("Cursor")==4096,"Restored job has independent progress");
        int[] all={0}; boolean[] valid={true};
        NatureBlessing full=new NatureBlessing(center,owner,0);
        int calls=0;
        while(!full.finished()) {
            int scanned=full.advance(4096,p->{all[0]++; valid[0]&=p.getY()>=0 && p.getY()<=255 && NatureBlessing.inCircle(p.getX()+100,p.getZ()-200);});
            if(scanned>4096) throw new AssertionError("Work exceeded budget"); calls++;
        }
        check(calls==256 && all[0]==820480 && valid[0],"Entire scan covers each valid layer with bounded ticks");
        check(full.advance(4096,p->{throw new AssertionError("Replayed completed work");})==0,"Completed scan never replays");
        check(NatureBlessing.read(center,full.save())==null,"Completed saved job cannot restart");
        NatureBlessing nearEnd=new NatureBlessing(center,owner,1048570);
        check(nearEnd.advance(4096,p->{})==6 && nearEnd.finished(),"Last tick stops at scan end");
        NatureBlessing stopped=new NatureBlessing(center,owner,0);
        stopped.stop(); check(stopped.finished() && NatureBlessing.read(center,stopped.save())==null,"Stopped work cannot resume");
        NatureBlessing interrupted=new NatureBlessing(center,owner,0);
        int[] writes={0}; interrupted.advance(4096,p->{writes[0]++; interrupted.stop();});
        check(writes[0]==1 && interrupted.finished(),"Reentrant stop ends remaining callbacks");
        for(int cursor:new int[]{-1,Integer.MIN_VALUE,1048576,Integer.MAX_VALUE}) {
            CompoundNBT bad=saved.copy(); bad.putInt("Cursor",cursor);
            check(NatureBlessing.read(center,bad)==null,"Invalid progress rejected");
        }
        CompoundNBT bad=saved.copy(); bad.remove("Owner");
        check(NatureBlessing.read(center,bad)==null,"Missing owner rejected");
        bad=saved.copy(); bad.putString("Owner","bad");
        check(NatureBlessing.read(center,bad)==null,"Malformed owner rejected");
        bad=saved.copy(); bad.putInt("Version",2);
        check(NatureBlessing.read(center,bad)==null,"Unknown schema rejected");
        bad=saved.copy(); bad.putString("Cursor","4096");
        check(NatureBlessing.read(center,bad)==null,"Wrong cursor NBT type rejected");
        check(NatureBlessing.read(new BlockPos(0,-1,0),saved)==null && NatureBlessing.read(new BlockPos(0,256,0),saved)==null,"Invalid focal height rejected");
        Terrain terrain=new Terrain(); terrain.loaded=false;
        new NatureBlessing(center,owner,0).tick(terrain);
        check(terrain.reads==0 && terrain.edits==0 && terrain.writes==0,"Unloaded positions never read or written");
        terrain=new Terrain(); terrain.border=false;
        new NatureBlessing(center,owner,0).tick(terrain);
        check(terrain.reads==0 && terrain.writes==0,"Outside-border terrain never read or written");
        terrain=new Terrain(); terrain.convertible=false;
        new NatureBlessing(center,owner,0).tick(terrain);
        check(terrain.reads==3205 && terrain.edits==0 && terrain.writes==0,"Unrelated blocks skip permission checks and writes");
        terrain=new Terrain(); terrain.editable=false;
        new NatureBlessing(center,owner,0).tick(terrain);
        check(terrain.edits==3205 && terrain.writes==0,"Denied edits preserve terrain");
        terrain=new Terrain();
        new NatureBlessing(center,owner,0).tick(terrain);
        check(terrain.writes==3205,"Eligible permitted soil changes");
        NatureBlessing skip=new NatureBlessing(center,owner,0);
        Terrain unloaded=new Terrain(); unloaded.loaded=false; skip.tick(unloaded);
        Terrain loaded=new Terrain(); skip.tick(loaded);
        check(skip.cursor()==8192 && loaded.writes==3205,"Skipped unloaded positions are not retried");
        check(LesserHellmouth.failure(false,false)==null,"Dry non-Peaceful cast permitted");
        check("no_rain".equals(LesserHellmouth.failure(false,true)),"Rain prevents cast");
        check("hellmouth_peaceful".equals(LesserHellmouth.failure(true,false)),"Peaceful prevents cast");
        check("hellmouth_peaceful".equals(LesserHellmouth.failure(true,true)),"Difficulty failure takes precedence");
        check(LesserHellmouth.type(true)==EntityType.ZOMBIFIED_PIGLIN && LesserHellmouth.type(false)==EntityType.MAGMA_CUBE,"Legacy two-creature pool");
        check(LesserHellmouth.count(constant(0))==2,"Minimum legacy wave");
        check(LesserHellmouth.count(constant(3))==5,"Maximum legacy wave");
        check(LesserHellmouth.offset(constant(0))==-6 && LesserHellmouth.offset(constant(11))==5,"Legacy asymmetric placement interval");
        Random random=new Random(76103); int[] frequencies=new int[6]; boolean offsets=true;
        for(int i=0;i<100000;i++) {
            int count=LesserHellmouth.count(random); if(count<2 || count>5) throw new AssertionError("Wave outside bounds"); frequencies[count]++;
            int offset=LesserHellmouth.offset(random); offsets&=offset>=-6 && offset<=5;
        }
        check(offsets && frequencies[2]>24000 && frequencies[2]<26000 && frequencies[3]>36500 && frequencies[3]<38500
                && frequencies[4]>27100 && frequencies[4]<29100 && frequencies[5]>8400 && frequencies[5]<10400,"Legacy rerolled-bound distribution retained across 100000 samples");
        check(RitualEffect.NATURES_BLESSING.failure(false,false).equals("overworld"),"Storm ritual limited to Overworld");
        check(RitualEffect.NATURES_BLESSING.failure(true,false)==null && RitualEffect.NATURES_BLESSING.failure(true,true)==null,"Blessing accepts any time of day");
        final int[] storm={0};
        RitualEffect.NATURES_BLESSING.complete(new RitualEffect.Environment() {
            public long dayTime(){throw new AssertionError("Storm read time");}
            public void setDayTime(long value){throw new AssertionError("Storm changed time");}
            public int rainRoll(){throw new AssertionError("Storm replaced legacy weather duration");}
            public void startRain(int ticks){throw new AssertionError("Storm omitted thunder");}
            public void stopRain(){throw new AssertionError("Storm stopped rain");}
            public void startStorm(){storm[0]++;}
            public void stopStorm(){throw new AssertionError("Blessing stopped thunder");}
        });
        check(storm[0]==1,"Blessing requests rain and thunder once without altering time");
        NonNullList<Ingredient> inputs=NonNullList.create();
        inputs.add(Ingredient.of(Items.IRON_SWORD)); inputs.add(Ingredient.of(Items.NETHERRACK));
        inputs.add(Ingredient.of(Items.WHEAT)); inputs.add(Ingredient.of(Items.CHARCOAL));
        RitualRecipe lesser=new RitualRecipe(new ResourceLocation("bewitchment:lesser_test"),inputs,new ItemStack(Items.IRON_SWORD),15,1000,33,new int[]{2,-1,-1},RitualEffect.LESSER_HELLMOUTH);
        Inventory inventory=new Inventory(10);
        ItemStack tool=new ItemStack(Items.IRON_SWORD); tool.setDamageValue(42); tool.getOrCreateTag().putString("custom","keep");
        inventory.setItem(0,tool); inventory.setItem(1,new ItemStack(Items.NETHERRACK)); inventory.setItem(2,new ItemStack(Items.WHEAT)); inventory.setItem(3,new ItemStack(Items.CHARCOAL));
        check(lesser.matches(inventory,null),"Damaged tool accepted");
        check(lesser.assemble(inventory).getDamageValue()==0 && !lesser.assemble(inventory).getOrCreateTag().contains("custom"),"Success returns fresh tool");
        inventory.setItem(3,new ItemStack(Items.COAL)); check(!lesser.matches(inventory,null),"Coal does not substitute for charcoal");
        inputs=NonNullList.create(); for(int i=0;i<10;i++) inputs.add(Ingredient.of(Items.PAPER));
        RitualRecipe nature=new RitualRecipe(new ResourceLocation("bewitchment:nature_test"),inputs,ItemStack.EMPTY,150,1600,75,new int[]{3,1,3},RitualEffect.NATURES_BLESSING);
        check(!nature.effect.consumesOnStart() && nature.acceptsRun(""),"Blessing offerings kept until completion");
        final NonNullList<Ingredient> natureInputs=inputs;
        rejects(()->new RitualRecipe(nature.getId(),natureInputs,new ItemStack(Items.DIAMOND),150,1600,75,new int[]{3,1,3},RitualEffect.NATURES_BLESSING),"Blessing cannot silently discard a datapack output");
        for(int i=0;i<10;i++) inventory.setItem(i,new ItemStack(Items.PAPER));
        check(nature.matches(inventory,null),"Ten-slot recipe accepted");
        inventory.setItem(9,ItemStack.EMPTY); check(!nature.matches(inventory,null),"Missing final offering rejected");
        for(RitualRecipe recipe:new RitualRecipe[]{lesser,nature}) {
            RitualClock clock=new RitualClock(); int[] paid={recipe.startingPower};
            for(int i=0;i<recipe.seconds*20-1;i++) clock.advance(recipe.seconds,recipe.runningPower,cost->{paid[0]+=cost;return true;});
            check(clock.advance(recipe.seconds,recipe.runningPower,cost->false)==RitualClock.Step.NO_POWER,"Failed final payment does not finish");
            check(clock.advance(recipe.seconds,recipe.runningPower,cost->{paid[0]+=cost;return true;})==RitualClock.Step.FINISHED,"Final successful payment finishes");
            check(paid[0]==(recipe==lesser?1495:12850),"Exact total cost");
            check(RitualPower.startingCost(recipe.startingPower,true)+recipe.seconds*recipe.runningPower==(recipe==lesser?1345:12610),"Hecate changes startup only");
        }
        System.out.println("PASS: "+checks+" Nature blessing and Lesser Hellmouth checks plus 100000 seeded wave samples.");
    }
}