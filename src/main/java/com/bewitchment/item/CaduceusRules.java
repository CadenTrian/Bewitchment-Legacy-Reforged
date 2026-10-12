package com.bewitchment.item;

import net.minecraft.util.Hand;

public final class CaduceusRules {
    public static final int DURABILITY=128, LIFETIME=400, COOLDOWN=40, RESCUE_COST=16;
    public static int count(int roll){if(roll<0 || roll>=3)throw new IllegalArgumentException("Invalid summon roll");return roll+1;}
    public static <T> int spawn(int roll,java.util.function.Supplier<T> create,java.util.function.Predicate<T> insert){
        int successes=0,amount=count(roll);
        for(int i=0;i<amount;i++){T candidate=create.get();if(candidate!=null && insert.test(candidate))successes++;}
        return successes;
    }
    public static Hand rescueHand(boolean main,boolean off){return main?Hand.MAIN_HAND:off?Hand.OFF_HAND:null;}
    public static float rescueHealth(float maximum){return Math.min(8,Math.max(0,maximum));}
    public static boolean fits(net.minecraft.util.math.AxisAlignedBB box,double minX,double maxX,double minZ,double maxZ){
        return box.minY>=0 && box.maxY<=256 && box.minX>=minX && box.maxX<=maxX && box.minZ>=minZ && box.maxZ<=maxZ;
    }
    private CaduceusRules(){}
}
