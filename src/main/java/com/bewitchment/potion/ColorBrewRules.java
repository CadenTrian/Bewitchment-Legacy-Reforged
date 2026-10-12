package com.bewitchment.potion;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import javax.annotation.Nullable;

/** Shared vanilla color families; sandstone and masonry remain brew-specific. */
public final class ColorBrewRules {
    private static final String[] FAMILIES={"wool","carpet","terracotta","stained_glass","stained_glass_pane","concrete","concrete_powder"};
    public static boolean colorArmor(ItemStack stack,DyeColor color){
        if(stack.isEmpty()||!(stack.getItem() instanceof ArmorItem)||!(stack.getItem() instanceof IDyeableArmorItem))return false;
        ((IDyeableArmorItem)stack.getItem()).setColor(stack,color.getColorValue());return true;
    }
    @Nullable public static BlockState replacement(BlockState state,DyeColor color){
        if(state.hasTileEntity())return null;Block block=state.getBlock();String family=null;
        if(block==Blocks.TERRACOTTA)family="terracotta";
        else if(block==Blocks.GLASS)family="stained_glass";
        else if(block==Blocks.GLASS_PANE)family="stained_glass_pane";
        else {
            ResourceLocation id=block.getRegistryName();if(id==null||!id.getNamespace().equals("minecraft"))return null;
            for(DyeColor from:DyeColor.values())for(String candidate:FAMILIES)
                if(id.getPath().equals(from.getName()+"_"+candidate))family=candidate;
        }
        if(family==null)return null;
        Block target=Registry.BLOCK.get(new ResourceLocation("minecraft",color.getName()+"_"+family));
        return target==Blocks.AIR||target==block?null:SporeCloudRules.copyProperties(state,target.defaultBlockState());
    }
    private ColorBrewRules(){}
}