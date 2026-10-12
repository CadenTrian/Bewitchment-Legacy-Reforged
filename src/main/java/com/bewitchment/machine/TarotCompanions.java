package com.bewitchment.machine;

import java.util.UUID;
import java.util.function.Predicate;

/** Bounded snapshot of loaded entities, never a claim about unloaded pets. */
public final class TarotCompanions {
    public static final int LIMIT=4096;
    public enum Result { FOUND, NONE, INCOMPLETE }
    public static boolean owned(boolean alive,boolean tamed,UUID owner,UUID target){return alive&&tamed&&owner!=null&&owner.equals(target);}
    public static <T> Result scan(Iterable<? extends Iterable<T>> worlds,Predicate<T> matches,int limit){
        if(limit<0)throw new IllegalArgumentException("Negative companion search budget");
        int visited=0;
        for(Iterable<T> world:worlds){
            java.util.Iterator<T> entities=world.iterator();
            while(entities.hasNext()){
                if(visited==limit)return Result.INCOMPLETE;
                visited++;
                if(matches.test(entities.next()))return Result.FOUND;
            }
        }
        return Result.NONE;
    }
    private TarotCompanions(){}
}
