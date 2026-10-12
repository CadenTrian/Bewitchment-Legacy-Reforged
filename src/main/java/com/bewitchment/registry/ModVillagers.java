package com.bewitchment.registry;

import com.bewitchment.trade.VillageTrades;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModVillagers {
    public static final DeferredRegister<PointOfInterestType> POIS=DeferredRegister.create(ForgeRegistries.POI_TYPES,"bewitchment");
    public static final DeferredRegister<VillagerProfession> PROFESSIONS=DeferredRegister.create(ForgeRegistries.PROFESSIONS,"bewitchment");
    public static final RegistryObject<PointOfInterestType> HEDGE_POI=POIS.register("hedge_witch",()->new PointOfInterestType("bewitchment:hedge_witch",ImmutableSet.copyOf(ModBlocks.BRAZIER.get().getStateDefinition().getPossibleStates()),1,1));
    public static final RegistryObject<PointOfInterestType> ALCHEMY_POI=POIS.register("alchemist",()->new PointOfInterestType("bewitchment:alchemist",ImmutableSet.copyOf(ModBlocks.DISTILLERY.get().getStateDefinition().getPossibleStates()),1,1));
    public static final RegistryObject<VillagerProfession> HEDGE_WITCH=PROFESSIONS.register("hedge_witch",()->new VillagerProfession("bewitchment:hedge_witch",HEDGE_POI.get(),ImmutableSet.of(),ImmutableSet.of(),SoundEvents.VILLAGER_WORK_CLERIC));
    public static final RegistryObject<VillagerProfession> ALCHEMIST=PROFESSIONS.register("alchemist",()->new VillagerProfession("bewitchment:alchemist",ALCHEMY_POI.get(),ImmutableSet.of(),ImmutableSet.of(),SoundEvents.VILLAGER_WORK_CLERIC));
    public static void register(IEventBus bus){POIS.register(bus);PROFESSIONS.register(bus);net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ModVillagers::trades);}
    private static void trades(VillagerTradesEvent event){
        boolean witch=event.getType()==HEDGE_WITCH.get();if(!witch&&event.getType()!=ALCHEMIST.get())return;
        for(int level=1;level<=5;level++){final int tier=level;for(VillageTrades.Entry entry:VillageTrades.entries(witch,level))event.getTrades().get(level).add((entity,random)->VillageTrades.offer(entry,tier,random));}
    }
    public static void verify(){
        int count=0;
        for(boolean witch:new boolean[]{false,true}){
            it.unimi.dsi.fastutil.ints.Int2ObjectMap<java.util.List<net.minecraft.entity.merchant.villager.VillagerTrades.ITrade>> table=new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
            for(int tier=1;tier<=5;tier++)table.put(tier,new java.util.ArrayList<>());
            trades(new VillagerTradesEvent(table,witch?HEDGE_WITCH.get():ALCHEMIST.get()));
            for(int tier=1;tier<=5;tier++)if(table.get(tier).size()!=VillageTrades.entries(witch,tier).size())throw new IllegalStateException("Missing trade level: "+tier);
            PointOfInterestType poi=witch?HEDGE_POI.get():ALCHEMY_POI.get();
            for(net.minecraft.block.BlockState state:poi.getBlockStates())if(PointOfInterestType.forState(state).orElse(null)!=poi)throw new IllegalStateException("Missing workstation state mapping: "+state);
            for(int level=1;level<=5;level++)for(VillageTrades.Entry entry:VillageTrades.entries(witch,level)){
                for(String name:VillageTrades.pool(entry.item))if(!ForgeRegistries.ITEMS.containsKey(new net.minecraft.util.ResourceLocation(VillageTrades.id(name))))throw new IllegalStateException("Missing villager trade item: "+name);
                for(int seed=0;seed<32;seed++){net.minecraft.item.MerchantOffer offer=VillageTrades.offer(entry,level,new java.util.Random(seed));if(offer.getResult().isEmpty()||offer.getBaseCostA().isEmpty())throw new IllegalStateException("Empty village offer");}
                count++;
            }
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Verified Hedge Witch/Alchemist workstations and {} trade definitions across ten levels",count);
    }
    private ModVillagers(){}
}
