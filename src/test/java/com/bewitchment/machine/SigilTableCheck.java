package com.bewitchment.machine;

import com.bewitchment.block.SigilTableBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import io.netty.buffer.Unpooled;
import com.google.gson.JsonSyntaxException;

public final class SigilTableCheck {
    private static int checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static Inventory grid(int amount){Inventory grid=new Inventory(25);for(int i=0;i<25;i++)grid.setItem(i,new ItemStack(i==12?Items.GOLD_INGOT:Items.PAPER,amount));return grid;}
    public static void main(String[] args){
        Bootstrap.bootStrap();NonNullList<Ingredient> ingredients=NonNullList.create();for(int i=0;i<25;i++)ingredients.add(Ingredient.of(i==12?Items.GOLD_INGOT:Items.PAPER));
        SigilRecipe recipe=new SigilRecipe(new ResourceLocation("bewitchment:test_sigil"),ingredients,new ItemStack(Items.DIAMOND));
        check(recipe.matches(grid(1),null),"Exact grid matches");
        for(int i=0;i<25;i++){Inventory wrong=grid(1);wrong.setItem(i,ItemStack.EMPTY);check(!recipe.matches(wrong,null),"Missing cell rejected: "+i);}
        Inventory shifted=grid(1);shifted.setItem(11,new ItemStack(Items.GOLD_INGOT));shifted.setItem(12,new ItemStack(Items.PAPER));check(!recipe.matches(shifted,null),"No shifted pattern");
        check(!recipe.matches(new Inventory(9),null) && recipe.canCraftInDimensions(5,5) && !recipe.canCraftInDimensions(25,1),"Exactly five by five");
        ItemStack result=recipe.getResultItem();result.shrink(1);check(recipe.getResultItem().getCount()==1,"Defensive output copy");
        recipe.getIngredients().clear();check(recipe.matches(grid(1),null),"Defensive ingredient-list copy");
        Inventory input=grid(2),target=new Inventory(1);target.setItem(0,new ItemStack(Items.DIAMOND,64));
        check(!SigilCrafting.craft(recipe,input,target,1) && input.getItem(12).getCount()==2,"Full destination consumes nothing");
        target.setItem(0,new ItemStack(Items.STICK));check(!SigilCrafting.craft(recipe,input,target,1),"Wrong cursor item refused");
        ItemStack named=new ItemStack(Items.DIAMOND);named.getOrCreateTag().putString("Custom","name");target.setItem(0,named);
        check(!SigilCrafting.craft(recipe,input,target,1),"Different output NBT cannot merge");
        target.setItem(0,new ItemStack(Items.DIAMOND,63));check(SigilCrafting.craft(recipe,input,target,1) && target.getItem(0).getCount()==64,"Craft into final stack space");
        check(java.util.stream.IntStream.range(0,25).allMatch(i->input.getItem(i).getCount()==1),"Exactly one of every ingredient consumed");
        check(!SigilCrafting.craft(recipe,input,target,1) && input.getItem(0).getCount()==1,"Next craft stops when destination fills");
        target.setItem(0,ItemStack.EMPTY);check(SigilCrafting.craft(recipe,input,target,1) && input.isEmpty(),"Last recipe consumes all cells");
        check(!SigilCrafting.craft(recipe,input,target,1) && target.getItem(0).getCount()==1,"Repeated request cannot duplicate stale output");
        Inventory stale=grid(1);stale.setItem(12,new ItemStack(Items.IRON_INGOT));check(!SigilCrafting.craft(recipe,stale,new Inventory(1),1) && stale.getItem(0).getCount()==1,"Changed ingredient invalidates preview before consumption");
        Inventory batch=grid(64),bag=new Inventory(2);int crafted=0;while(crafted<64 && SigilCrafting.craft(recipe,batch,bag,2))crafted++;
        check(crafted==64 && batch.isEmpty() && bag.getItem(0).getCount()==64,"Stack-sized shift batch");
        check(!SigilCrafting.craft(null,grid(1),bag,2),"Missing or reloaded-away recipe refused");
        boolean invalid=false;try{new SigilRecipe(recipe.getId(),NonNullList.create(),new ItemStack(Items.DIAMOND));}catch(JsonSyntaxException ex){invalid=true;}check(invalid,"Wrong ingredient count rejected");
        invalid=false;try{new SigilRecipe(recipe.getId(),ingredients,new ItemStack(Items.DIAMOND,2));}catch(JsonSyntaxException ex){invalid=true;}check(invalid,"Output count must be one");
        SigilRecipe.Serializer serializer=new SigilRecipe.Serializer();PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());serializer.toNetwork(buffer,recipe);SigilRecipe copy=serializer.fromNetwork(recipe.getId(),buffer);buffer.release();
        check(copy.matches(grid(1),null) && copy.getResultItem().getItem()==Items.DIAMOND,"Network round trip");
        buffer=new PacketBuffer(Unpooled.buffer());buffer.writeVarInt(24);invalid=false;try{serializer.fromNetwork(recipe.getId(),buffer);}catch(IllegalArgumentException ex){invalid=true;}finally{buffer.release();}check(invalid,"Malformed network count refused");
        SigilTableBlock block=new SigilTableBlock();SigilTableTile tile=new SigilTableTile(TileEntityType.CHEST);tile.matrix.setItem(24,named.copy());
        CompoundNBT saved=tile.save(new CompoundNBT());SigilTableTile loaded=new SigilTableTile(TileEntityType.CHEST);loaded.load(block.defaultBlockState(),saved);
        check(loaded.matrix.getItem(24).getTag().getString("Custom").equals("name"),"Last input slot and NBT persist");
        check(!saved.contains("Output") && !saved.contains("recipe"),"Neither preview nor stale recipe ID persisted");
        check(!tile.getUpdateTag().contains("Items"),"Chunk update does not expose table inventory");
        check(block.rotate(block.defaultBlockState(),Rotation.CLOCKWISE_90).getValue(SigilTableBlock.FACING)==Direction.EAST,"Table rotation");
        System.out.println("PASS: "+checks+" Sigil Table checks");
    }
}
