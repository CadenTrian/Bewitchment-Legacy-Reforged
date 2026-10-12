package com.bewitchment.machine;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import java.util.*;

/** One delayed fortune per player; countdown measures eligible online ticks. */
public final class FortuneData {
    public enum Kind {
        VITALITY("vitality",false,60,600), RESILIENCE("resilience",false,60,600),
        SALAMANDER_SKIN("salamander_skin",false,60,600), GOOD_LUCK("good_luck",false,120,600),
        BAD_LUCK("bad_luck",true,120,600), ILLNESS("illness",true,300,1800),
        COURAGE("courage",false,60,600), THUNDERBOLT("thunderbolt",true,120,600),
        DEATH("death",true,1200,10800), MEET_MERCHANT("meet_merchant",false,60,1200),
        ORPHAN("orphan",false,300,1800), MEET_ZOMBIE("meet_zombie",true,300,1500),
        MEET_WITCH("meet_witch",true,180,1200), MEET_SILVERFISH("meet_silverfish",true,300,1800),
        MEET_BLAZE("meet_blaze",true,60,180),
        MEET_CAMBION("meet_cambion",true,300,1500), MEET_DRUDEN("meet_druden",true,300,1500),
        MEET_SHADOW_PERSON("meet_shadow_person",true,300,1500), MEET_FEUERWURM("meet_feuerwurm",true,60,180),
        MEET_BAFOMETYR("meet_bafometyr",true,60,180), MEET_CLEAVER("meet_cleaver",true,60,180),
        MEET_DEMON("meet_demon",true,60,180), MEET_DIRE_WOLF("meet_dire_wolf",true,300,1200),
        DROP_ITEM("drop_item",true,30,900), MEET_PET("meet_pet",false,120,3600),
        CATS_AND_DOGS("cats_and_dogs",true,300,900), CORNUCOPIA("cornucopia",false,300,900),
        TREASURE("treasure",false,69,420), HELLISH_FORTUNE("hellish_fortune",false,66,666);
        public final String id;public final boolean negative;public final int minSeconds,maxSeconds;
        Kind(String id,boolean negative,int min,int max){this.id=id;this.negative=negative;minSeconds=min;maxSeconds=max;}
        public String key(){return "bewitchment:"+id;}
        public boolean digging(){return this==TREASURE || this==HELLISH_FORTUNE;}
        public int delay(int roll){return minSeconds+Math.floorMod(roll,maxSeconds-minSeconds);}
        public static Kind parse(String id){for(Kind kind:values())if(kind.id.equals(id)||kind.key().equals(id))return kind;return null;}
        /** Effect payload only; conditional eligibility is handled by FortuneExecution. */
        public List<EffectInstance> effects(int difficulty){
            int amplifier=1+Math.max(0,Math.min(3,difficulty));List<EffectInstance> result=new ArrayList<>();
            switch(this){
                case VITALITY:result.add(effect(Effects.REGENERATION,500,amplifier));break;
                case RESILIENCE:result.add(effect(Effects.DAMAGE_RESISTANCE,1000,amplifier));break;
                case SALAMANDER_SKIN:result.add(effect(Effects.FIRE_RESISTANCE,3600,0));break;
                case GOOD_LUCK:result.add(effect(Effects.LUCK,666,amplifier));break;
                case BAD_LUCK:result.add(effect(Effects.UNLUCK,666,amplifier));break;
                case ILLNESS:for(Effect type:new Effect[]{Effects.HUNGER,Effects.CONFUSION,Effects.WEAKNESS})result.add(effect(type,215,amplifier));break;
                case COURAGE:result.add(effect(Effects.DAMAGE_BOOST,500,amplifier));break;
                case DEATH:result.add(effect(Effects.WITHER,215,amplifier));break;
            }
            return result;
        }
        private static EffectInstance effect(Effect effect,int duration,int amplifier){return new EffectInstance(effect,duration,amplifier,false,false);}
    }
    private Kind kind;private int ticks;
    public Kind kind(){return kind;}public int remaining(){return ticks;}
    public boolean assign(Kind next,int roll){if(next==null)return false;kind=next;ticks=next.delay(roll)*20;return true;}
    public Kind take(){Kind result=kind;kind=null;ticks=0;return result;}
    public boolean tick(){if(kind==null)return false;if(ticks>0)ticks--;return ticks==0;}
    public CompoundNBT save(){CompoundNBT tag=new CompoundNBT();if(kind!=null){tag.putInt("version",1);tag.putString("kind",kind.key());tag.putInt("ticks",ticks);}return tag;}
    public void load(CompoundNBT tag){
        take();if(!tag.contains("version",3)||tag.getInt("version")!=1||!tag.contains("kind",8)||!tag.contains("ticks",3))return;
        Kind loaded=Kind.parse(tag.getString("kind"));int remaining=tag.getInt("ticks");
        if(loaded==null||remaining<0||remaining>(loaded.maxSeconds-1)*20)return;kind=loaded;ticks=remaining;
    }
}
