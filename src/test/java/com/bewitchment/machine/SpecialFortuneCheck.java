package com.bewitchment.machine;

import com.bewitchment.combat.PetFortunes;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class SpecialFortuneCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static final class Access implements FortuneExecution.Access {
        FortuneData.Kind selected; int calls;
        public boolean hasCreditedAttacker() { throw new AssertionError("Attacker read"); }
        public boolean rainingAtPlayer() { throw new AssertionError("Weather read"); }
        public int difficulty() { throw new AssertionError("Difficulty read"); }
        public void effect(EffectInstance effect) { throw new AssertionError("Player effect"); }
        public void visualLightning() { throw new AssertionError("Lightning"); }
        public void lightningDamage(float amount) { throw new AssertionError("Damage"); }
        public void villager(boolean child) { throw new AssertionError("Villager"); }
        public void hostile(FortuneData.Kind kind) { throw new AssertionError("Hostile"); }
        public void special(FortuneData.Kind kind) { selected=kind; calls++; }
    }
    private static final class Store implements FortuneDispatch.Store {
        FortuneData data = new FortuneData();
        public FortuneData read() { FortuneData copy=new FortuneData(); copy.load(data.save()); return copy; }
        public void write(FortuneData next) { data=next; }
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        for (FortuneData.Kind kind : new FortuneData.Kind[]{FortuneData.Kind.DROP_ITEM,FortuneData.Kind.MEET_PET,FortuneData.Kind.CATS_AND_DOGS,FortuneData.Kind.CORNUCOPIA}) {
            Access access=new Access(); FortuneExecution.apply(kind,access);
            check(access.calls==1 && access.selected==kind,"Exactly one special callback without unrelated facts");
        }
        Set<Block> treasure=new HashSet<>(Arrays.asList(Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.SAND,Blocks.MYCELIUM,Blocks.GRAVEL,Blocks.SOUL_SAND));
        Set<Block> hellish=new HashSet<>(Arrays.asList(Blocks.SOUL_SAND,Blocks.NETHERRACK,Blocks.MAGMA_BLOCK,Blocks.GRAVEL,Blocks.NETHER_BRICKS,Blocks.NETHER_QUARTZ_ORE));
        Set<Block> blocks=new HashSet<>(treasure); blocks.addAll(hellish); blocks.addAll(Arrays.asList(Blocks.AIR,Blocks.STONE,Blocks.RED_SAND,Blocks.SOUL_SOIL));
        for (FortuneData.Kind kind : FortuneData.Kind.values()) for (Block block : blocks) for (boolean nether : new boolean[]{false,true})
            check(DiggingFortuneRules.eligible(kind,block,nether)==(kind==FortuneData.Kind.TREASURE && treasure.contains(block) || kind==FortuneData.Kind.HELLISH_FORTUNE && nether && hellish.contains(block)),"Exact legacy terrain and dimension matrix");
        UUID id=UUID.randomUUID();
        for (FortuneData.Kind kind : new FortuneData.Kind[]{FortuneData.Kind.TREASURE,FortuneData.Kind.HELLISH_FORTUNE}) {
            Store store=new Store(); store.data.assign(kind,0);
            for(int tick=0;tick<kind.minSeconds*20+10;tick++)store.data.tick();
            check(store.data.kind()==kind && store.data.remaining()==0,"Dig fortune remains pending at zero");
            Access access=new Access(); FortuneExecution.apply(kind,access); check(access.calls==0,"Timer does not generate treasure");
            check(!FortuneDispatch.executeIf(id,store,k->!k.digging(),k->{throw new AssertionError("Forced execution");}),"Forced timer execution preserves event fortune");
            check(store.data.kind()==kind,"Rejected condition preserves state");
            check(FortuneDispatch.executeIf(id,store,k->k==kind,k->{
                check(store.data.kind()==null,"Consume before loot callbacks");
                store.data.assign(FortuneData.Kind.VITALITY,0);
                check(!FortuneDispatch.executeIf(id,store,x->true,x->{throw new AssertionError("Reentry");}),"Loot callback reentry denied");
            }),"Qualifying event dispatches");
            check(store.data.kind()==FortuneData.Kind.VITALITY,"Loot callback replacement survives");
        }
        for (int edge : new int[]{0,1}) {
            int[] calls={0}; Random random=new Random(){@Override public int nextInt(int bound){calls[0]++;return edge==0?0:bound-1;}};
            check(AnimalShowerRules.count(random)==(edge==0?3:10),"Shower minimum and maximum attempts");
            check(calls[0]==(edge==0?4:11),"Legacy loop rerolls condition including exit");
            check(AnimalShowerRules.offset(random)==(edge==0?-6:5),"Shower horizontal endpoints");
            check(AnimalShowerRules.height(random)==(edge==0?24:55),"Shower height endpoints");
        }
        EntityType<?>[] pets={EntityType.CAT,EntityType.WOLF,EntityType.HORSE,EntityType.DONKEY,EntityType.MULE,EntityType.LLAMA,EntityType.PARROT};
        check(PetFortunes.POOL_SIZE==11,"Original eleven-member pet pool");
        for(int i=0;i<pets.length;i++)check(PetFortunes.type(i)==pets[i] && PetFortunes.type(i+11)==pets[i],"Vanilla pet mapping with modern cat migration");
        System.out.println("PASS: "+checks+" special fortune routing, digging eligibility, consumption, pet pool and shower bounds checks");
    }
}
