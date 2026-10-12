package com.bewitchment.potion;

/** One growth dispatch. True means a bonemeal event consumed the tick budget. */
public final class FertilityGrowth {
    public interface Target {
        boolean canGrow();
        int event();
        boolean unchanged();
        void grow();
    }
    public static boolean apply(Target target){
        if(!target.canGrow())return false;
        int result=target.event();
        if(result!=0)return true; // Cancelled or handled externally: no second growth.
        if(target.unchanged()&&target.canGrow())target.grow();
        return true;
    }
    private FertilityGrowth(){}
}
