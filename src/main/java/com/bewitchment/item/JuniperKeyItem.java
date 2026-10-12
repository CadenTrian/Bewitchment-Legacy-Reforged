package com.bewitchment.item;

import com.bewitchment.block.JuniperLocks;
import com.bewitchment.registry.ModItems;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import java.util.*;
import javax.annotation.Nullable;

public final class JuniperKeyItem extends Item {
    private final boolean ring;
    public JuniperKeyItem(boolean ring){super(new Item.Properties().tab(ModItems.TAB));this.ring=ring;}
    public boolean ring(){return ring;}
    public static CompoundNBT binding(ResourceLocation dimension,BlockPos pos){CompoundNBT tag=new CompoundNBT();tag.putString("dimension",dimension.toString());tag.putLong("location",pos.asLong());return tag;}
    public static boolean valid(CompoundNBT tag){return tag.contains("location",4) && tag.contains("dimension",8) && ResourceLocation.tryParse(tag.getString("dimension"))!=null;}
    public static boolean matches(CompoundNBT tag,ResourceLocation dimension,BlockPos pos){return valid(tag) && tag.getString("dimension").equals(dimension.toString()) && tag.getLong("location")==pos.asLong();}
    public static List<CompoundNBT> bindings(ItemStack stack){
        List<CompoundNBT> result=new ArrayList<>();if(!(stack.getItem() instanceof JuniperKeyItem) || !stack.hasTag())return result;
        if(((JuniperKeyItem)stack.getItem()).ring){for(INBT entry:stack.getTag().getList("Keys",10)){CompoundNBT tag=(CompoundNBT)entry;if(valid(tag))result.add(tag.copy());}}
        else if(valid(stack.getTag()))result.add(stack.getTag().copy());
        return result;
    }
    public static boolean opens(ItemStack stack,ResourceLocation dimension,BlockPos pos){for(CompoundNBT tag:bindings(stack))if(matches(tag,dimension,pos))return true;return false;}
    public static ItemStack combine(Item ringItem,List<ItemStack> keys){
        Set<CompoundNBT> unique=new LinkedHashSet<>();for(ItemStack key:keys)for(CompoundNBT tag:bindings(key))unique.add(binding(new ResourceLocation(tag.getString("dimension")),BlockPos.of(tag.getLong("location"))));
        ItemStack result=new ItemStack(ringItem);ListNBT list=new ListNBT();for(CompoundNBT tag:unique)list.add(tag.copy());result.getOrCreateTag().put("Keys",list);return result;
    }
    @Override public ActionResultType useOn(ItemUseContext context){
        if(ring || context.getPlayer()==null || !context.getPlayer().isCreative() || !JuniperLocks.lock(context.getLevel().getBlockState(context.getClickedPos())))return ActionResultType.PASS;
        if(context.getItemInHand().hasTag() && valid(context.getItemInHand().getTag()))return ActionResultType.PASS;
        if(!context.getLevel().isClientSide){
            CompoundNBT tag=binding(context.getLevel().dimension().location(),JuniperLocks.base(context.getLevel(),context.getClickedPos()));
            context.getItemInHand().getOrCreateTag().merge(tag);
        }
        return ActionResultType.sidedSuccess(context.getLevel().isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable World world,List<ITextComponent> lines,ITooltipFlag flag){
        List<CompoundNBT> keys=bindings(stack);if(keys.isEmpty())lines.add(new TranslationTextComponent("tooltip.bewitchment.juniper_key.unbound"));
        for(CompoundNBT tag:keys){BlockPos pos=BlockPos.of(tag.getLong("location"));lines.add(new TranslationTextComponent("tooltip.bewitchment.juniper_key",pos.getX(),pos.getY(),pos.getZ(),tag.getString("dimension")));}
    }
}
