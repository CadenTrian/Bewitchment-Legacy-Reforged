package com.bewitchment.machine;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;

public final class SigilTableTile extends TileEntity implements INamedContainerProvider {
    public final Inventory matrix=new Inventory(25);
    public SigilTableTile(){this(ModMachines.SIGIL_TABLE.get());}
    SigilTableTile(TileEntityType<?> type){super(type);matrix.addListener(inventory->setChanged());}
    public SigilRecipe recipe(){return level==null?null:level.getRecipeManager().getAllRecipesFor(SigilRecipe.TYPE).stream()
        .filter(recipe->recipe.matches(matrix,level)).sorted(java.util.Comparator.comparing(recipe->recipe.getId().toString())).findFirst().orElse(null);}
    @Override public ITextComponent getDisplayName(){return new TranslationTextComponent("block.bewitchment.sigil_table");}
    @Override public Container createMenu(int id,PlayerInventory inventory,PlayerEntity player){return new SigilTableMenu(id,inventory,this);}
    @Override public CompoundNBT save(CompoundNBT tag){super.save(tag);NonNullList<ItemStack> items=NonNullList.withSize(25,ItemStack.EMPTY);for(int i=0;i<25;i++)items.set(i,matrix.getItem(i));ItemStackHelper.saveAllItems(tag,items);return tag;}
    @Override public void load(BlockState state,CompoundNBT tag){super.load(state,tag);NonNullList<ItemStack> items=NonNullList.withSize(25,ItemStack.EMPTY);ItemStackHelper.loadAllItems(tag,items);for(int i=0;i<25;i++)matrix.setItem(i,items.get(i));}
}
