package com.bewitchment.machine;

import java.util.*;

/** Random choices are resolved once before a box's effects are executed. */
public final class SealedBoxPlan {
    public static final int OUTCOMES=26;
    public static final class Gift {
        public final String id;public final int count;
        Gift(String id,int count){this.id=id;this.count=count;}
    }
    public static final class Potion {
        public final String id;public final int ticks,amplifier;
        Potion(String id,int ticks,int amplifier){this.id=id;this.ticks=ticks;this.amplifier=amplifier;}
    }
    public final int outcome,weatherTicks;
    public final List<Potion> potions;
    public final List<Gift> gifts,spawns;
    private SealedBoxPlan(int outcome,int weatherTicks,List<Gift> gifts,List<Gift> spawns,List<Potion> potions){
        this.outcome=outcome;this.weatherTicks=weatherTicks;this.potions=Collections.unmodifiableList(potions);
        this.gifts=Collections.unmodifiableList(gifts);this.spawns=Collections.unmodifiableList(spawns);
    }
    public static SealedBoxPlan random(Random random){return create(random.nextInt(OUTCOMES),random);}
    public static SealedBoxPlan create(int outcome,Random random){
        if(outcome<0 || outcome>=OUTCOMES)throw new IllegalArgumentException("Invalid sealed box outcome");
        Objects.requireNonNull(random,"random");List<Gift> gifts=new ArrayList<>(),spawns=new ArrayList<>();int weather=0;List<Potion> potions=new ArrayList<>();
        switch(outcome){
            case 2:potions.add(new Potion("minecraft:strength",24000,1));potions.add(new Potion("minecraft:resistance",24000,0));break;
            case 3:potions.add(new Potion("minecraft:speed",24000,1));potions.add(new Potion("minecraft:jump_boost",24000,0));break;
            case 19:potions.add(new Potion("minecraft:poison",1200,0));potions.add(new Potion("minecraft:weakness",24000,0));break;
            case 20:potions.add(new Potion("minecraft:wither",800,0));break;
            case 21:potions.add(new Potion("bewitchment:hellfire",24000,0));break;
            case 5:add(gifts,"minecraft:nether_star",1);break;
            case 6:int bats=10+random.nextInt(10);add(spawns,"minecraft:bat",bats);add(spawns,"bewitchment:raven",bats-10);break;
            case 7:
                add(gifts,"bewitchment:bottle_of_blood",1);add(gifts,"bewitchment:heart",1);
                String[] skulls={"skeleton_skull","wither_skeleton_skull","zombie_head","player_head","creeper_head","dragon_head"};
                add(gifts,"minecraft:"+skulls[random.nextInt(skulls.length)],1);break;
            case 8:
                String[] demons={"leonard","baphomet","lilith","herne","moloch"};String demon=demons[random.nextInt(demons.length)];
                add(gifts,"bewitchment:"+(random.nextBoolean()?"nether_brick":"scorned_brick")+"_"+demon+"_statue",1);break;
            case 9:add(gifts,"bewitchment:stew_of_the_grotesque",1);add(gifts,"minecraft:chicken",1+random.nextInt(64));add(gifts,"minecraft:mutton",1+random.nextInt(64));break;
            case 10:
                add(gifts,"bewitchment:embergrass",1);add(gifts,"bewitchment:blue_ink_cap",1);add(gifts,"bewitchment:spanish_moss",1);
                String[] flowers={"allium","azure_bluet","blue_orchid","dandelion","oxeye_daisy","poppy","tulip_orange","tulip_pink","tulip_red","tulip_white"};
                add(gifts,"bewitchment:flower_siphoning_"+flowers[random.nextInt(flowers.length)],1);add(gifts,"bewitchment:dragons_blood_sapling",1);break;
            case 11:for(String id:new String[]{"minecraft:ghast_tear","minecraft:blaze_rod","bewitchment:hellhound_horn","bewitchment:demon_heart"})add(gifts,id,1+random.nextInt(2));break;
            case 12:add(spawns,"bewitchment:feuerwurm",1+random.nextInt(3));break;
            case 13:add(spawns,"bewitchment:hellhound",1+random.nextInt(3));break;
            case 14:weather=(300+random.nextInt(600))*20;break;
            case 16:add(spawns,"bewitchment:shadow_person",1+random.nextInt(3));break;
            case 17:add(spawns,"bewitchment:ghost",1+random.nextInt(3));break;
            case 18:add(spawns,"bewitchment:druden",1+random.nextInt(2));break;
            case 23:add(spawns,"bewitchment:bafometyr",1+random.nextInt(2));break;
            case 24:add(spawns,"bewitchment:cleaver",1+random.nextInt(2));break;
            case 25:int count=1+random.nextInt(4);add(spawns,"minecraft:cave_spider",count);add(spawns,"minecraft:silverfish",count);break;
            default:break; // Runtime loot, accessory, ore, potion, time and lightning outcomes.
        }
        return new SealedBoxPlan(outcome,weather,gifts,spawns,potions);
    }
    private static void add(List<Gift> list,String id,int count){if(count>0)list.add(new Gift(id,count));}
    public static long midnight(long time){return time+Math.floorMod(17600-Math.floorMod(time,24000),24000);}
    public static boolean place(java.util.function.BooleanSupplier attempt){for(int i=0;i<16;i++)if(attempt.getAsBoolean())return true;return false;}
}