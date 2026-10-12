package com.bewitchment.machine;
import java.util.*;
import java.util.function.Consumer;
/** Persist consumption before invoking effect callbacks; preserve any new assignment. */
public final class FortuneDispatch {
    public interface Store { FortuneData read();void write(FortuneData data); }
    private static final ThreadLocal<Set<UUID>> ACTIVE=ThreadLocal.withInitial(HashSet::new);
    public static boolean execute(UUID player,Store store,Consumer<FortuneData.Kind> apply){
        return executeIf(player,store,kind->true,apply);
    }
    public static boolean executeIf(UUID player,Store store,java.util.function.Predicate<FortuneData.Kind> eligible,Consumer<FortuneData.Kind> apply){
        Set<UUID> active=ACTIVE.get();if(!active.add(player))return false;
        try{
            FortuneData data=store.read();FortuneData.Kind kind=data.kind();if(kind==null || !eligible.test(kind))return false;
            data.take();
            store.write(data);apply.accept(kind);return true;
        }finally{active.remove(player);if(active.isEmpty())ACTIVE.remove();}
    }
    private FortuneDispatch(){}
}
