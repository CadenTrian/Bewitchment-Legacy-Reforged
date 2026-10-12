package com.bewitchment.potion;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import java.util.*;

/** Limits apply before external world/event callbacks, including rejected attempts. */
public final class ArachnophobiaRules {
    public static final int ATTEMPTS=16, DESCENT=16, BUFF_TICKS=2400;
    private static final ThreadLocal<Set<UUID>> ACTIVE=ThreadLocal.withInitial(HashSet::new);
    public static int count(int amplifier){return 2*(Math.max(0,Math.min(3,amplifier))+1);}
    public static boolean webCandidate(BlockState state){return !state.hasTileEntity()&&state.getFluidState().isEmpty()&&state.getMaterial().isReplaceable()&&(!(state.getBlock() instanceof SnowBlock)||state.getValue(SnowBlock.LAYERS)==1);}
    public static boolean guarded(UUID target,Runnable action){
        Set<UUID> active=ACTIVE.get();if(!active.add(target))return false;
        try{action.run();return true;}finally{active.remove(target);if(active.isEmpty())ACTIVE.remove();}
    }
    public interface SpawnSteps {
        boolean valid();
        boolean permitted();
        boolean special();
        void initialize();
        boolean insert();
    }
    public static boolean spawn(SpawnSteps steps){
        if(!steps.valid()||!steps.permitted()||!steps.valid())return false;
        boolean handled=steps.special();if(!steps.valid())return false;
        if(!handled)steps.initialize();
        return steps.valid()&&steps.insert();
    }
    public static final class Budget {
        private long tick;private boolean initialized;private int mobs,positions;
        private void reset(long now){if(!initialized||tick!=now){initialized=true;tick=now;mobs=positions=0;}}
        public boolean mob(long now){reset(now);if(mobs>=16)return false;mobs++;return true;}
        public boolean position(long now){reset(now);if(positions>=128)return false;positions++;return true;}
    }
    private ArachnophobiaRules(){}
}