package com.bewitchment.machine;
import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import java.util.*;

public final class TarotHistoryCheck {
    private static int checks;
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);checks++;}
    public static void main(String[] args){
        net.minecraft.util.registry.Bootstrap.bootStrap();
        TarotHistory.Category[] categories=TarotHistory.Category.values();
        check(categories.length==11,"Eleven restored categories");
        String[] original={"NOTHING","FORTUNE","EFFECTS","CURSES","ATTIRE"};
        for(int i=0;i<original.length;i++)check(TarotHistory.choose(i).name().equals(original[i]),"Original category adapter order preserved");
        int[] frequencies=new int[11];for(int i=0;i<1100;i++)frequencies[TarotHistory.choose(i).ordinal()]++;
        for(int count:frequencies)check(count==100,"Uniform bounded selection cycle");
        for(int roll:new int[]{Integer.MIN_VALUE,-23,-11,-1,0,10,11,22,Integer.MAX_VALUE})check(TarotHistory.choose(roll)==categories[Math.floorMod(roll,11)],"Extreme and wrapped rolls safe");
        check(TarotHistory.choose(9)==TarotHistory.Category.COMPANIONS,"Companion category selectable");
        check(TarotHistory.choose(10)==TarotHistory.Category.PROTECTION,"Protection category selectable");
        int[] values={Integer.MIN_VALUE,-1,0,1,10,Integer.MAX_VALUE-1,Integer.MAX_VALUE};
        for(int first:values)for(int second:values){
            long expected=Math.max(0L,first)+Math.max(0L,second);int capped=expected>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)expected;
            check(TarotHistory.add(first,second)==capped,"Counts never go negative or overflow");
            check(TarotHistory.peopleKills(first,second)==capped,"Player/villager total uses safe addition");
        }
        List<EntityType<?>> pool=Arrays.asList(EntityType.ZOMBIE,EntityType.SKELETON,EntityType.SLIME,EntityType.COW,EntityType.VILLAGER,EntityType.PLAYER);
        int[] reads={0};
        check(TarotHistory.monsterKills(pool,type->{reads[0]++;if(type==EntityType.ZOMBIE)return 4;if(type==EntityType.SKELETON)return 7;if(type==EntityType.SLIME)return 2;throw new AssertionError("Read passive/player statistics");})==13,"Only MONSTER classification contributes");
        check(reads[0]==3,"Non-monster stats not queried");
        check(TarotHistory.monsterKills(Collections.emptyList(),type->{throw new AssertionError();})==0,"Empty registry view safe");
        check(TarotHistory.monsterKills(Arrays.asList(EntityType.ZOMBIE,EntityType.SKELETON),type->Integer.MAX_VALUE)==Integer.MAX_VALUE,"Aggregate saturation");
        check(TarotHistory.monsterKills(Collections.singletonList(EntityType.ZOMBIE),type->-1)==0,"Corrupt negative stat ignored");
        for(int mask=0;mask<16;mask++)check(TarotHistory.defeatedBoss(mask&1,mask&2,mask&4,mask&8)==(mask!=0),"Any supported boss statistic qualifies");
        check(!TarotHistory.defeatedBoss(-1,Integer.MIN_VALUE,0),"Nonpositive boss records do not qualify");
        UUID reader=UUID.randomUUID(),target=UUID.randomUUID(),stranger=UUID.randomUUID();
        for(int mask=0;mask<16;mask++){
            DemonPledges data=new DemonPledges();
            if((mask&1)!=0)data.add(reader,DemonPledges.Patron.LEONARD);
            if((mask&2)!=0)data.add(reader,DemonPledges.Patron.BAPHOMET);
            if((mask&4)!=0)data.add(target,DemonPledges.Patron.LEONARD);
            if((mask&8)!=0)data.add(target,DemonPledges.Patron.BAPHOMET);
            CompoundNBT before=data.save(new CompoundNBT());
            check(TarotHistory.pledged(data,target)==((mask&12)!=0),"Target pledge independent of reader pledge");
            check(!TarotHistory.pledged(data,stranger)&&!TarotHistory.pledged(data,null),"Unpledged and missing identities safe");
            check(before.equals(data.save(new CompoundNBT())),"Reading never changes pledge data");
            DemonPledges loaded=new DemonPledges();loaded.load(before);
            check(TarotHistory.pledged(loaded,target)==((mask&12)!=0),"Saved pledge round trip keeps target result");
        }
        System.out.println("PASS: "+checks+" tarot history category, registry classification, count overflow, boss and target-pledge checks");
    }
}
