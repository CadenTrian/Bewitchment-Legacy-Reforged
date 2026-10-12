package com.bewitchment.machine;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
/** First extraction freezes output and allowance; no changes can refill a partly bottled batch. */
public final class BrewBatch {
    private ItemStack potion=ItemStack.EMPTY;
    private int remaining;
    public boolean started(){return !potion.isEmpty();}
    public int remaining(){return remaining;}
    public void start(BrewPlan plan){start(plan.potion(),plan.bottles);}
    public void start(ItemStack output,int bottles){if(started())throw new IllegalStateException("Already bottled");if(!BrewHooks.valid(output,bottles))throw new IllegalArgumentException("Invalid brew output");potion=output.copy();potion.getOrCreateTag().putBoolean("bewitchment_brew",true);remaining=bottles;}
    public ItemStack take(){if(!started() || remaining<=0)return ItemStack.EMPTY;remaining--;return potion.copy();}
    public void clear(){potion=ItemStack.EMPTY;remaining=0;}
    public CompoundNBT save(){CompoundNBT tag=new CompoundNBT();tag.putInt("remaining",remaining);if(started())tag.put("potion",potion.save(new CompoundNBT()));return tag;}
    public boolean load(CompoundNBT tag){clear();if(!tag.contains("potion"))return !tag.contains("remaining") || tag.getInt("remaining")==0;
        ItemStack stored=ItemStack.of(tag.getCompound("potion"));int count=tag.getInt("remaining");
        if(!(stored.getItem() instanceof PotionItem) || stored.getCount()!=1 || !tag.contains("remaining",3) || count<=0 || !stored.hasTag() || !stored.getTag().getBoolean("bewitchment_brew"))return false;
        potion=stored.copy();remaining=count;return true;
    }
}