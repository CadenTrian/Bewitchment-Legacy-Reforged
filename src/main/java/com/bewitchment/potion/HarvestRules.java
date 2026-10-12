package com.bewitchment.potion;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import java.util.*;

/** Plans one harvest and commits its block change before publishing any items. */
public final class HarvestRules {
    public enum Kind { NONE, CROP, APPLE, PUMPKIN }
    public interface Target {
        boolean permitted(BlockState expected);
        boolean unchanged(BlockState expected);
        List<ItemStack> loot(BlockState expected,int fortune);
        boolean replace(BlockState expected,BlockState replacement);
        void drop(ItemStack stack);
    }
    public static int fortune(int amplifier){return Math.max(0,Math.min(2,amplifier));}
    public static Kind kind(BlockState state){
        if(state.hasTileEntity())return Kind.NONE;
        Block block=state.getBlock();
        // Never harvest an immature crop or let it fall through to the pumpkin branch.
        if(block instanceof CropsBlock)return ((CropsBlock)block).isMaxAge(state)?Kind.CROP:Kind.NONE;
        if(block==Blocks.OAK_LEAVES)return Kind.APPLE;
        if(block instanceof IGrowable&&state.getMaterial().isReplaceable()&&state.getFluidState().isEmpty())return Kind.PUMPKIN;
        return Kind.NONE;
    }
    public static boolean selected(Kind kind,Random random){
        if(kind==Kind.APPLE)return random.nextInt(10)==0;
        if(kind==Kind.PUMPKIN)return random.nextInt(4)==0;
        return kind==Kind.CROP;
    }
    public static boolean harvest(BlockState before,int amplifier,Random random,Target target){
        Kind kind=kind(before);
        if(!selected(kind,random)||!target.permitted(before)||!target.unchanged(before))return false;
        if(kind==Kind.APPLE){target.drop(new ItemStack(Items.APPLE));return true;}
        if(kind==Kind.PUMPKIN){
            // Legacy pumpkins had a carved face and a horizontal facing.
            BlockState pumpkin=Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING,Direction.from2DDataValue(random.nextInt(4)));
            return target.replace(before,pumpkin);
        }
        List<ItemStack> drops=new ArrayList<>();
        for(ItemStack stack:target.loot(before,fortune(amplifier)))if(!stack.isEmpty())drops.add(stack.copy());
        BlockState replanted=before.setValue(((CropsBlock)before.getBlock()).getAgeProperty(),0);
        // Loot modifiers are external callbacks; they may have replaced this crop.
        if(!target.unchanged(before)||!target.replace(before,replanted))return false;
        for(ItemStack stack:drops)target.drop(stack);
        return true;
    }
    private HarvestRules(){}
}
