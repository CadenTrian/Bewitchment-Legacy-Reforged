package com.bewitchment.machine;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.bewitchment.block.BrazierBlock;

public final class BrazierAutomationCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();Inventory inventory=new Inventory(9);AtomicBoolean open=new AtomicBoolean(true);
        BrazierItemHandler handler=new BrazierItemHandler(inventory,open::get);
        check(handler.getSlots()==9,"All offering slots exposed");
        for(int slot=0;slot<9;slot++){
            ItemStack input=new ItemStack(Items.PAPER,64);input.getOrCreateTag().putInt("offering",slot);
            ItemStack simulated=handler.insertItem(slot,input,true);
            check(simulated.getCount()==63 && input.getCount()==64 && inventory.getItem(slot).isEmpty(),"Insert simulation is read only "+slot);
            ItemStack remainder=handler.insertItem(slot,input,false);
            check(remainder.getCount()==63 && inventory.getItem(slot).getCount()==1 && handler.getSlotLimit(slot)==1,"One offering per slot "+slot);
            input.getOrCreateTag().putInt("offering",99);remainder.getOrCreateTag().putInt("offering",98);ItemStack view=handler.getStackInSlot(slot);view.getOrCreateTag().putInt("offering",97);view.grow(20);
            check(inventory.getItem(slot).getTag().getInt("offering")==slot && inventory.getItem(slot).getCount()==1,"Input, remainder and view cannot mutate stored NBT "+slot);
            check(handler.insertItem(slot,new ItemStack(Items.PAPER),false).getCount()==1,"Occupied matching slot never merges "+slot);
        }
        for(int slot=0;slot<9;slot++){
            ItemStack preview=handler.extractItem(slot,64,true);check(preview.getCount()==1 && inventory.getItem(slot).getCount()==1,"Extraction simulation preserves contents "+slot);
            preview.getOrCreateTag().putInt("offering",99);ItemStack extracted=handler.extractItem(slot,64,false);
            check(extracted.getTag().getInt("offering")==slot && inventory.getItem(slot).isEmpty(),"Exact NBT extracted once "+slot);
        }
        check(handler.extractItem(0,1,false).isEmpty(),"Repeated extraction produces nothing");
        handler.insertItem(0,new ItemStack(Items.COAL),false);open.set(false);
        check(handler.getStackInSlot(0).isEmpty() && !handler.isItemValid(1,new ItemStack(Items.COAL)),"Closed access hides contents and denies validity");
        check(handler.extractItem(0,1,false).isEmpty() && handler.extractItem(0,1,true).isEmpty() && inventory.getItem(0).getCount()==1,"Blocked real and simulated extraction preserve offering");
        check(handler.insertItem(1,new ItemStack(Items.COAL,3),false).getCount()==3 && handler.insertItem(1,new ItemStack(Items.COAL,3),true).getCount()==3 && inventory.getItem(1).isEmpty(),"Blocked real and simulated insertion preserve input");
        open.set(true);check(handler.extractItem(0,0,false).isEmpty() && handler.extractItem(0,-1,false).isEmpty() && inventory.getItem(0).getCount()==1,"Nonpositive extraction cannot change counts");
        check(handler.insertItem(1,ItemStack.EMPTY,false).isEmpty() && !handler.isItemValid(1,ItemStack.EMPTY),"Empty insertion is harmless");
        inventory.setItem(0,new ItemStack(Items.COAL,7));check(handler.extractItem(0,3,false).getCount()==3 && inventory.getItem(0).getCount()==4,"Malformed oversized saved slot can be recovered without loss");inventory.clearContent();
        NonNullList<Ingredient> ingredients=NonNullList.create();ingredients.add(Ingredient.of(Items.COAL));ingredients.add(Ingredient.of(Items.COAL));
        IncenseRecipe recipe=new IncenseRecipe(new ResourceLocation("bewitchment:automation_check"),ingredients,IncenseData.Kind.INTENSITY);
        ItemStack remaining=new ItemStack(Items.COAL,2);remaining=handler.insertItem(8,remaining,false);remaining=handler.insertItem(2,remaining,false);
        check(remaining.isEmpty() && recipe.matches(inventory,null),"Automation produces a valid duplicate-ingredient recipe");
        handler.extractItem(2,1,false);check(!recipe.matches(inventory,null),"Extraction invalidates recipe immediately");
        BrazierTile tile=new BrazierTile(TileEntityType.CHEST);BrazierItemHandler shared=new BrazierItemHandler(tile.contents,()->true);ItemStack named=new ItemStack(Items.PAPER);named.getOrCreateTag().putString("boundId","stored-target");shared.insertItem(8,named,false);
        BrazierTile restored=new BrazierTile(TileEntityType.CHEST);restored.load(new BrazierBlock().defaultBlockState(),tile.save(new CompoundNBT()));
        check(restored.contents.getItem(8).getTag().getString("boundId").equals("stored-target"),"Automated offerings share persisted manual inventory");
        for(int slot:new int[]{-1,9,Integer.MAX_VALUE}){boolean rejected=false;try{handler.insertItem(slot,new ItemStack(Items.COAL),false);}catch(IndexOutOfBoundsException ex){rejected=true;}check(rejected,"Invalid slot rejected "+slot);}
        check(tile.getUpdateTag().isEmpty(),"Automation does not publish private offering NBT");
        System.out.println("PASS: "+checks+" Brazier automation checks");
    }
}
