package com.bewitchment.entity;

import com.bewitchment.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/** Server interaction decisions also used by the standalone regression checks. */
public final class SnakeRules {
    public static int skin(int value){return value>=0 && value<6?value:0;}
    public static int cooldown(int value){return Math.max(0,Math.min(3600,value));}
    public static boolean tamingFood(Item item){return item==Items.CHICKEN || item==Items.RABBIT || item==Items.EGG || item==Items.RABBIT_FOOT || item==ModItems.LIZARD_LEG.get();}
    public static boolean canMilk(boolean baby,int cooldown,boolean successfulRoll){return !baby && cooldown==0 && successfulRoll;}
    public static boolean milkAngers(int cooldown,boolean successfulRoll){return cooldown>0 || !successfulRoll;}
    public static float sway(float movement){return Math.max(-1,Math.min(1,movement));}
    private SnakeRules(){}
}
