package com.bewitchment.registry;

import com.bewitchment.entity.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.*;

public final class ModCreatures {
    public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITIES,"bewitchment");
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,"bewitchment");
    public static final RegistryObject<EntityType<LizardEntity>> LIZARD=ENTITIES.register("lizard",()->EntityType.Builder.of(LizardEntity::new,EntityClassification.CREATURE).sized(.3F,.3F).clientTrackingRange(8).build("bewitchment:lizard"));
    public static final RegistryObject<EntityType<ToadEntity>> TOAD=ENTITIES.register("toad",()->EntityType.Builder.of(ToadEntity::new,EntityClassification.CREATURE).sized(1,.3F).clientTrackingRange(8).build("bewitchment:toad"));
    public static final RegistryObject<EntityType<OwlEntity>> OWL=ENTITIES.register("owl",()->EntityType.Builder.of(OwlEntity::new,EntityClassification.CREATURE).sized(.4F,.9F).clientTrackingRange(8).build("bewitchment:owl"));
    public static final RegistryObject<EntityType<RavenEntity>> RAVEN=ENTITIES.register("raven",()->EntityType.Builder.of(RavenEntity::new,EntityClassification.CREATURE).sized(.4F,.4F).clientTrackingRange(8).build("bewitchment:raven"));
    public static final RegistryObject<EntityType<SnakeEntity>> SNAKE=ENTITIES.register("snake",()->EntityType.Builder.of(SnakeEntity::new,EntityClassification.CREATURE).sized(1,.3F).clientTrackingRange(8).build("bewitchment:snake"));
    public static final RegistryObject<EntityType<FeuerwurmEntity>> FEUERWURM=ENTITIES.register("feuerwurm",()->EntityType.Builder.of(FeuerwurmEntity::new,EntityClassification.MONSTER).sized(1.45F,.85F).fireImmune().clientTrackingRange(8).build("bewitchment:feuerwurm"));
    public static final RegistryObject<EntityType<HellhoundEntity>> HELLHOUND=ENTITIES.register("hellhound",()->EntityType.Builder.of(HellhoundEntity::new,EntityClassification.MONSTER).sized(1,1.25F).fireImmune().clientTrackingRange(8).build("bewitchment:hellhound"));
    public static final RegistryObject<EntityType<ShadowPersonEntity>> SHADOW_PERSON=ENTITIES.register("shadow_person",()->EntityType.Builder.of(ShadowPersonEntity::new,EntityClassification.MONSTER).sized(1,2.25F).fireImmune().clientTrackingRange(8).build("bewitchment:shadow_person"));
    public static final RegistryObject<EntityType<BafometyrEntity>> BAFOMETYR=ENTITIES.register("bafometyr",()->EntityType.Builder.of(BafometyrEntity::new,EntityClassification.MONSTER).sized(.8F,2F).fireImmune().clientTrackingRange(8).build("bewitchment:bafometyr"));
    public static final RegistryObject<EntityType<DemonEntity>> DEMON=ENTITIES.register("demon",()->EntityType.Builder.of(DemonEntity::new,EntityClassification.MONSTER).sized(1.425F,3.8F).fireImmune().clientTrackingRange(8).build("bewitchment:demon"));
    public static final RegistryObject<EntityType<DemonessEntity>> DEMONESS=ENTITIES.register("demoness",()->EntityType.Builder.of(DemonessEntity::new,EntityClassification.MONSTER).sized(1.425F,3.8F).fireImmune().clientTrackingRange(8).build("bewitchment:demoness"));
    public static final RegistryObject<EntityType<BaphometEntity>> BAPHOMET=ENTITIES.register("baphomet",()->EntityType.Builder.of(BaphometEntity::new,EntityClassification.MONSTER).sized(1,3.6F).fireImmune().clientTrackingRange(8).build("bewitchment:baphomet"));
    public static final RegistryObject<Item> BAPHOMET_EGG=ModItems.ITEMS.register("baphomet_spawn_egg",()->new ForgeSpawnEggItem(BAPHOMET,0x472f22,0x690303,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<EntityType<LeonardEntity>> LEONARD=ENTITIES.register("leonard",()->EntityType.Builder.of(LeonardEntity::new,EntityClassification.MONSTER).sized(1.0F,3.8F).fireImmune().clientTrackingRange(8).build("bewitchment:leonard"));
    public static final RegistryObject<EntityType<CleaverEntity>> CLEAVER=ENTITIES.register("cleaver",()->EntityType.Builder.of(CleaverEntity::new,EntityClassification.MONSTER).sized(1,2.5F).fireImmune().clientTrackingRange(8).build("bewitchment:cleaver"));
    public static final RegistryObject<EntityType<GhostEntity>> GHOST=ENTITIES.register("ghost",()->EntityType.Builder.of(GhostEntity::new,EntityClassification.MONSTER).sized(.6F,1.8F).fireImmune().clientTrackingRange(8).build("bewitchment:ghost"));
    public static final RegistryObject<EntityType<ImpEntity>> IMP=ENTITIES.register("imp",()->EntityType.Builder.of(ImpEntity::new,EntityClassification.MONSTER).sized(.8F,1.6F).fireImmune().clientTrackingRange(8).build("bewitchment:imp"));
    public static final RegistryObject<EntityType<DrudenEntity>> DRUDEN=ENTITIES.register("druden",()->EntityType.Builder.of(DrudenEntity::new,EntityClassification.MONSTER).sized(1.425F,3.7F).clientTrackingRange(8).build("bewitchment:druden"));
    public static final RegistryObject<EntityType<WerewolfEntity>> WEREWOLF=ENTITIES.register("werewolf",()->EntityType.Builder.of(WerewolfEntity::new,EntityClassification.MONSTER).sized(1.2F,3.2F).clientTrackingRange(8).build("bewitchment:werewolf"));
    public static final RegistryObject<EntityType<CambionEntity>> CAMBION=ENTITIES.register("cambion",()->EntityType.Builder.of(CambionEntity::new,EntityClassification.MONSTER).sized(.8F,2F).clientTrackingRange(8).build("bewitchment:cambion"));
    public static final RegistryObject<EntityType<BlackDogEntity>> BLACK_DOG=ENTITIES.register("black_dog",()->EntityType.Builder.of(BlackDogEntity::new,EntityClassification.MONSTER).sized(1.08F,1.53F).fireImmune().clientTrackingRange(8).build("bewitchment:black_dog"));
    public static final RegistryObject<Item> LIZARD_EGG=ModItems.ITEMS.register("lizard_spawn_egg",()->new ForgeSpawnEggItem(LIZARD,0x568203,0x0070bb,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> TOAD_EGG=ModItems.ITEMS.register("toad_spawn_egg",()->new ForgeSpawnEggItem(TOAD,0xa9ba9d,0xc3b091,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> OWL_EGG=ModItems.ITEMS.register("owl_spawn_egg",()->new ForgeSpawnEggItem(OWL,0xaf813f,0x6e5127,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> RAVEN_EGG=ModItems.ITEMS.register("raven_spawn_egg",()->new ForgeSpawnEggItem(RAVEN,0x222222,0x280638,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> SNAKE_EGG=ModItems.ITEMS.register("snake_spawn_egg",()->new ForgeSpawnEggItem(SNAKE,0xff9779,0x696969,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> FEUERWURM_EGG=ModItems.ITEMS.register("feuerwurm_spawn_egg",()->new ForgeSpawnEggItem(FEUERWURM,0x7c0902,0x568203,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> HELLHOUND_EGG=ModItems.ITEMS.register("hellhound_spawn_egg",()->new ForgeSpawnEggItem(HELLHOUND,0x555555,0xed2939,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> SHADOW_PERSON_EGG=ModItems.ITEMS.register("shadow_person_spawn_egg",()->new ForgeSpawnEggItem(SHADOW_PERSON,0x000000,0x000000,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> BAFOMETYR_EGG=ModItems.ITEMS.register("bafometyr_spawn_egg",()->new ForgeSpawnEggItem(BAFOMETYR,0xf5f5dc,0x9f8170,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> DEMON_EGG=ModItems.ITEMS.register("demon_spawn_egg",()->new ForgeSpawnEggItem(DEMON,0x555555,0xed2939,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> DEMONESS_EGG=ModItems.ITEMS.register("demoness_spawn_egg",()->new ForgeSpawnEggItem(DEMONESS,0x555555,0xed2939,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> LEONARD_EGG=ModItems.ITEMS.register("leonard_spawn_egg",()->new ForgeSpawnEggItem(LEONARD,0x472f22,0xe8c91a,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> CLEAVER_EGG=ModItems.ITEMS.register("cleaver_spawn_egg",()->new ForgeSpawnEggItem(CLEAVER,0x5c4033,0x483c32,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> GHOST_EGG=ModItems.ITEMS.register("ghost_spawn_egg",()->new ForgeSpawnEggItem(GHOST,0x000000,0x8f9779,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> IMP_EGG=ModItems.ITEMS.register("imp_spawn_egg",()->new ForgeSpawnEggItem(IMP,0x660000,0x555d50,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> DRUDEN_EGG=ModItems.ITEMS.register("druden_spawn_egg",()->new ForgeSpawnEggItem(DRUDEN,0x228b22,0x8a3324,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> WEREWOLF_EGG=ModItems.ITEMS.register("werewolf_spawn_egg",()->new ForgeSpawnEggItem(WEREWOLF,0x9f8170,0x954535,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> CAMBION_EGG=ModItems.ITEMS.register("cambion_spawn_egg",()->new ForgeSpawnEggItem(CAMBION,0xc41e3a,0x483c32,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<Item> BLACK_DOG_EGG=ModItems.ITEMS.register("black_dog_spawn_egg",()->new ForgeSpawnEggItem(BLACK_DOG,0x000000,0x000000,new Item.Properties().tab(ModItems.TAB)));
    public static final RegistryObject<SoundEvent> TOAD_IDLE=sound("toad_idle"),TOAD_HURT=sound("toad_hurt"),TOAD_DEATH=sound("toad_death");
    public static final RegistryObject<SoundEvent> BAFOMETYR_IDLE=sound("bafometyr_living"),BAFOMETYR_HURT=sound("bafometyr_hurt"),BAFOMETYR_DEATH=sound("bafometyr_death");
    public static final RegistryObject<SoundEvent> CLEAVER_IDLE=sound("cleaver_living"),CLEAVER_HURT=sound("cleaver_hurt"),CLEAVER_DEATH=sound("cleaver_death");
    public static final RegistryObject<SoundEvent> WEREWOLF_HOWL=sound("werewolf_howl");
    private static RegistryObject<SoundEvent> sound(String name){return SOUNDS.register(name,()->new SoundEvent(new ResourceLocation("bewitchment",name)));}
    public static final RegistryObject<SoundEvent> RAVEN_CRY=sound("raven_cry"),OWL_HOOT=sound("owl_hoot");
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue STRUCTURE_SPAWNS, CAMBION_STRUCTURE_SPAWNS, SHADOW_STRUCTURE_SPAWNS;
    private static final ForgeConfigSpec.IntValue LIZARD_WEIGHT,TOAD_WEIGHT,SNAKE_WEIGHT,RAVEN_WEIGHT,OWL_WEIGHT,FEUERWURM_WEIGHT,HELLHOUND_WEIGHT,BLACK_DOG_WEIGHT,DRUDEN_WEIGHT,WEREWOLF_WEIGHT,BAFOMETYR_WEIGHT,CLEAVER_WEIGHT,GHOST_WEIGHT;
    static {
        ForgeConfigSpec.Builder b=new ForgeConfigSpec.Builder();
        b.comment("Natural creature spawning. Restart to apply. Zero disables natural spawning; eggs and summons remain available.");
        LIZARD_WEIGHT=b.worldRestart().defineInRange("lizardWeight",6,0,1000);
        TOAD_WEIGHT=b.worldRestart().defineInRange("toadWeight",20,0,1000);
        SNAKE_WEIGHT=b.worldRestart().defineInRange("snakeWeight",20,0,1000);
        RAVEN_WEIGHT=b.worldRestart().defineInRange("ravenWeight",20,0,1000);
        OWL_WEIGHT=b.worldRestart().defineInRange("owlWeight",20,0,1000);
        FEUERWURM_WEIGHT=b.worldRestart().defineInRange("feuerwurmWeight",3,0,1000);
        HELLHOUND_WEIGHT=b.worldRestart().defineInRange("hellhoundWeight",6,0,1000);
        BLACK_DOG_WEIGHT=b.worldRestart().defineInRange("blackDogWeight",3,0,1000);
        DRUDEN_WEIGHT=b.worldRestart().defineInRange("drudenWeight",1,0,1000);
        WEREWOLF_WEIGHT=b.worldRestart().defineInRange("werewolfWeight",1,0,1000);
        BAFOMETYR_WEIGHT=b.worldRestart().defineInRange("bafometyrWeight",1,0,1000);
        CLEAVER_WEIGHT=b.worldRestart().defineInRange("cleaverWeight",1,0,1000);
        GHOST_WEIGHT=b.worldRestart().defineInRange("ghostWeight",3,0,1000);
        STRUCTURE_SPAWNS=b.comment("Enable legacy structure spawn pools. Existing zero creature weights also disable structure entries; positive biome weights do not change legacy structure weights.").worldRestart().define("structureSpawns",true);
        CAMBION_STRUCTURE_SPAWNS=b.worldRestart().define("cambionStructureSpawns",true);
        SHADOW_STRUCTURE_SPAWNS=b.worldRestart().define("shadowPersonStructureSpawns",true);
        SPEC=b.build();
    }
    public static void attributes(EntityAttributeCreationEvent event){event.put(BAPHOMET.get(),BaphometEntity.attributes().build());event.put(LEONARD.get(),LeonardEntity.attributes().build());event.put(DEMON.get(),DemonEntity.attributes().build());event.put(DEMONESS.get(),DemonEntity.attributes().build());event.put(LIZARD.get(),LizardEntity.attributes().build());event.put(TOAD.get(),ToadEntity.attributes().build());event.put(SNAKE.get(),SnakeEntity.attributes().build());event.put(FEUERWURM.get(),FeuerwurmEntity.attributes().build());event.put(HELLHOUND.get(),HellhoundEntity.attributes().build());event.put(IMP.get(),ImpEntity.attributes().build());event.put(GHOST.get(),GhostEntity.attributes().build());event.put(CLEAVER.get(),CleaverEntity.attributes().build());event.put(BAFOMETYR.get(),BafometyrEntity.attributes().build());event.put(SHADOW_PERSON.get(),ShadowPersonEntity.attributes().build());event.put(WEREWOLF.get(),WerewolfEntity.attributes().build());event.put(DRUDEN.get(),DrudenEntity.attributes().build());event.put(CAMBION.get(),CambionEntity.attributes().build());event.put(BLACK_DOG.get(),BlackDogEntity.attributes().build());event.put(OWL.get(),LegacyBirdEntity.attributes(true).build());event.put(RAVEN.get(),LegacyBirdEntity.attributes(false).build());}
    public static void verify(net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event){
        event.enqueueWork(()->{
            if(((SpawnEggItem)BAPHOMET_EGG.get()).getType(null)!=BAPHOMET.get())throw new IllegalStateException("Incorrect Baphomet spawn egg");
            if(!net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(BAPHOMET.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(LEONARD.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(DEMONESS.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(DEMON.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(GHOST.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(CLEAVER.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(BAFOMETYR.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(SHADOW_PERSON.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(LIZARD.get())
                    || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(TOAD.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(SNAKE.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(RAVEN.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(OWL.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(FEUERWURM.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(HELLHOUND.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(BLACK_DOG.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(IMP.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(WEREWOLF.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(DRUDEN.get()) || !net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes.hasSupplier(CAMBION.get()))
                throw new IllegalStateException("Missing Bewitchment creature attributes");
            if(((SpawnEggItem)GHOST_EGG.get()).getType(null)!=GHOST.get() || ((SpawnEggItem)CLEAVER_EGG.get()).getType(null)!=CLEAVER.get() || ((SpawnEggItem)BAFOMETYR_EGG.get()).getType(null)!=BAFOMETYR.get() || ((SpawnEggItem)SHADOW_PERSON_EGG.get()).getType(null)!=SHADOW_PERSON.get() || ((SpawnEggItem)LIZARD_EGG.get()).getType(null)!=LIZARD.get() || ((SpawnEggItem)TOAD_EGG.get()).getType(null)!=TOAD.get() || ((SpawnEggItem)SNAKE_EGG.get()).getType(null)!=SNAKE.get() || ((SpawnEggItem)RAVEN_EGG.get()).getType(null)!=RAVEN.get() || ((SpawnEggItem)OWL_EGG.get()).getType(null)!=OWL.get() || ((SpawnEggItem)FEUERWURM_EGG.get()).getType(null)!=FEUERWURM.get() || ((SpawnEggItem)HELLHOUND_EGG.get()).getType(null)!=HELLHOUND.get() || ((SpawnEggItem)BLACK_DOG_EGG.get()).getType(null)!=BLACK_DOG.get() || ((SpawnEggItem)IMP_EGG.get()).getType(null)!=IMP.get() || ((SpawnEggItem)WEREWOLF_EGG.get()).getType(null)!=WEREWOLF.get() || ((SpawnEggItem)DRUDEN_EGG.get()).getType(null)!=DRUDEN.get() || ((SpawnEggItem)CAMBION_EGG.get()).getType(null)!=CAMBION.get())
                throw new IllegalStateException("Incorrect Bewitchment spawn egg entity");
            org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment creature attributes and spawn eggs verified");
            com.bewitchment.machine.CallOfTheWild.verifyPool();
            com.bewitchment.machine.Hellmouth.verifyPool();
            com.bewitchment.machine.GreaterHellmouth.verifyPool();
            com.bewitchment.machine.SpiritualRift.verifyPool();
        });
    }
    public static net.minecraft.entity.EntityType<?> structureCreature(com.bewitchment.world.StructureSpawnRules.Creature creature) {
        switch(creature) {
            case CLEAVER:return CLEAVER.get(); case BAFOMETYR:return BAFOMETYR.get();
            case FEUERWURM:return FEUERWURM.get(); case HELLHOUND:return HELLHOUND.get();
            case CAMBION:return CAMBION.get(); case SHADOW_PERSON:return SHADOW_PERSON.get();
            case GHOST:return GHOST.get(); case BLACK_DOG:return BLACK_DOG.get();
            default:throw new AssertionError(creature);
        }
    }
    public static boolean structureCreatureEnabled(com.bewitchment.world.StructureSpawnRules.Creature creature) {
        switch(creature) {
            case CLEAVER:return CLEAVER_WEIGHT.get()>0; case BAFOMETYR:return BAFOMETYR_WEIGHT.get()>0;
            case FEUERWURM:return FEUERWURM_WEIGHT.get()>0; case HELLHOUND:return HELLHOUND_WEIGHT.get()>0;
            case CAMBION:return CAMBION_STRUCTURE_SPAWNS.get(); case SHADOW_PERSON:return SHADOW_STRUCTURE_SPAWNS.get();
            case GHOST:return GHOST_WEIGHT.get()>0; case BLACK_DOG:return BLACK_DOG_WEIGHT.get()>0;
            default:throw new AssertionError(creature);
        }
    }
    public static void placements(){
        EntitySpawnPlacementRegistry.register(CAMBION.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(SHADOW_PERSON.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(GHOST.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(CLEAVER.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL);
        EntitySpawnPlacementRegistry.register(BAFOMETYR.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL);
        EntitySpawnPlacementRegistry.register(WEREWOLF.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && WerewolfRules.canSpawn(world.getLevel().isDay(),world.getLevel().getMoonPhase()) && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(DRUDEN.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(BLACK_DOG.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && !BlackDogRules.vanishes(world.getLevel().isDay(),world.getLevel().isRaining(),false) && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(FEUERWURM.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && world.getLevel().dimensionType().ultraWarm() && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(HELLHOUND.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && world.getLevel().dimensionType().ultraWarm() && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(IMP.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->world.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && world.getLevel().dimensionType().ultraWarm() && MobEntity.checkMobSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(LIZARD.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,AnimalEntity::checkAnimalSpawnRules);
        EntitySpawnPlacementRegistry.register(TOAD.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,AnimalEntity::checkAnimalSpawnRules);
        EntitySpawnPlacementRegistry.register(OWL.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->!world.getLevel().isDay() && AnimalEntity.checkAnimalSpawnRules(type,world,reason,pos,random));
        EntitySpawnPlacementRegistry.register(RAVEN.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,AnimalEntity::checkAnimalSpawnRules);
        EntitySpawnPlacementRegistry.register(SNAKE.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,AnimalEntity::checkAnimalSpawnRules);
    }
    public static void biomes(BiomeLoadingEvent event){
        if(event.getName()==null)return;
        RegistryKey<net.minecraft.world.biome.Biome> key=RegistryKey.create(Registry.BIOME_REGISTRY,event.getName());
        if(DRUDEN_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.DENSE) || BiomeDictionary.hasType(key,BiomeDictionary.Type.CONIFEROUS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.MAGICAL) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY)))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(DRUDEN.get(),DRUDEN_WEIGHT.get(),1,3));
        if(WEREWOLF_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.CONIFEROUS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SNOWY) || BiomeDictionary.hasType(key,BiomeDictionary.Type.MAGICAL) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY)))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(WEREWOLF.get(),WEREWOLF_WEIGHT.get(),1,4));
        if(LIZARD_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SWAMP) || BiomeDictionary.hasType(key,BiomeDictionary.Type.JUNGLE)))
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(LIZARD.get(),LIZARD_WEIGHT.get(),1,2));
        if(SNAKE_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.PLAINS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.HILLS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SANDY)))
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(SNAKE.get(),SNAKE_WEIGHT.get(),1,4));
        if(RAVEN_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.PLAINS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.WASTELAND) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY)))
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(RAVEN.get(),RAVEN_WEIGHT.get(),1,4));
        if(OWL_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.DENSE) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY)))
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(OWL.get(),OWL_WEIGHT.get(),1,4));
        if(CLEAVER_WEIGHT.get()>0 && BiomeDictionary.hasType(key,BiomeDictionary.Type.NETHER))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(CLEAVER.get(),CLEAVER_WEIGHT.get(),0,1));
        if(BAFOMETYR_WEIGHT.get()>0 && BiomeDictionary.hasType(key,BiomeDictionary.Type.NETHER))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(BAFOMETYR.get(),BAFOMETYR_WEIGHT.get(),0,1));
        if(FEUERWURM_WEIGHT.get()>0 && BiomeDictionary.hasType(key,BiomeDictionary.Type.NETHER))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(FEUERWURM.get(),FEUERWURM_WEIGHT.get(),1,3));
        if(HELLHOUND_WEIGHT.get()>0 && BiomeDictionary.hasType(key,BiomeDictionary.Type.NETHER))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(HELLHOUND.get(),HELLHOUND_WEIGHT.get(),1,3));
        if(GHOST_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.PLAINS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.WASTELAND) || BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY) || BiomeDictionary.hasType(key,BiomeDictionary.Type.MAGICAL)))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(GHOST.get(),GHOST_WEIGHT.get(),0,2));
        if(BLACK_DOG_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.PLAINS) || BiomeDictionary.hasType(key,BiomeDictionary.Type.WASTELAND) || BiomeDictionary.hasType(key,BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(key,BiomeDictionary.Type.SPOOKY) || BiomeDictionary.hasType(key,BiomeDictionary.Type.MAGICAL)))
            event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(BLACK_DOG.get(),BLACK_DOG_WEIGHT.get(),1,3));
        if(TOAD_WEIGHT.get()>0 && (BiomeDictionary.hasType(key,BiomeDictionary.Type.SWAMP) || BiomeDictionary.hasType(key,BiomeDictionary.Type.WET) || BiomeDictionary.hasType(key,BiomeDictionary.Type.JUNGLE)))
            event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(TOAD.get(),TOAD_WEIGHT.get(),1,4));
    }
    private ModCreatures(){}
}
