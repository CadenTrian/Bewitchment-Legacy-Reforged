package com.bewitchment.potion;

import net.minecraft.block.*;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.item.ItemStack;
import java.util.*;
import java.util.function.BooleanSupplier;

public final class BlightRules {
    public enum Action { NONE, SOIL, DESTROY }
    public interface Target {
        boolean permitted();
        boolean unchanged();
        List<ItemStack> loot();
        boolean replace(BlockState replacement);
        void drop(ItemStack stack);
    }
    private static final ThreadLocal<Set<UUID>> CONVERTING=ThreadLocal.withInitial(HashSet::new);
    public static int duration(int amplifier){return 120*UtilityBrewEffect.level(amplifier);}
    public static boolean susceptible(CreatureAttribute type,boolean demon,boolean spirit){return type!=CreatureAttribute.UNDEAD&&!demon&&!spirit;}
    public static Action action(BlockState state){
        if(state.hasTileEntity())return Action.NONE;Block b=state.getBlock();
        if(b==Blocks.COARSE_DIRT)return Action.NONE;
        if(b==Blocks.DIRT||b==Blocks.GRASS_BLOCK||b==Blocks.PODZOL||b==Blocks.MYCELIUM||b==Blocks.FARMLAND||b==Blocks.GRASS_PATH)return Action.SOIL;
        return b instanceof LeavesBlock||b instanceof IGrowable?Action.DESTROY:Action.NONE;
    }
    public static boolean apply(BlockState before,Target target){
        Action action=action(before);if(action==Action.NONE||!target.permitted()||!target.unchanged())return false;
        if(action==Action.SOIL)return target.replace(Blocks.COARSE_DIRT.defaultBlockState());
        List<ItemStack> drops=new ArrayList<>();for(ItemStack stack:target.loot())if(!stack.isEmpty())drops.add(stack.copy());
        if(!target.unchanged()||!target.replace(before.getFluidState().createLegacyBlock()))return false;
        for(ItemStack stack:drops)target.drop(stack);return true;
    }
    public static boolean conversionCandidate(boolean alive,boolean vanillaMooshroom,boolean passenger,boolean vehicle,boolean leashed){return alive&&vanillaMooshroom&&!passenger&&!vehicle&&!leashed;}
    /** Prevent reentrant insertion and retain the source until insertion succeeds. */
    public static boolean convert(UUID source,BooleanSupplier insert,Runnable remove){
        Set<UUID> active=CONVERTING.get();if(!active.add(source))return false;
        try{if(!insert.getAsBoolean())return false;remove.run();return true;}finally{active.remove(source);if(active.isEmpty())CONVERTING.remove();}
    }
    private BlightRules(){}
}
