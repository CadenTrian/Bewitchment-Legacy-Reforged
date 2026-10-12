package com.bewitchment.machine;

import com.bewitchment.block.BrazierBlock;
import com.bewitchment.item.TaglockItem;
import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class BrazierCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap(); net.minecraftforge.common.crafting.CraftingHelper.register(new ResourceLocation("minecraft:item"),net.minecraftforge.common.crafting.VanillaIngredientSerializer.INSTANCE);
        ResourceLocation id=new ResourceLocation("bewitchment:test_brazier");
        NonNullList<Ingredient> inputs=NonNullList.create();
        inputs.add(Ingredient.of(Items.COAL,Items.CHARCOAL));inputs.add(Ingredient.of(Items.COAL));
        BrazierRecipe recipe=new BrazierRecipe(id,inputs,CurseData.Kind.DARKNESS);
        Inventory inv=new Inventory(9);inv.setItem(8,new ItemStack(Items.COAL));inv.setItem(2,new ItemStack(Items.CHARCOAL));
        check(recipe.matches(inv,null),"Overlapping ingredients require complete shapeless matching");
        inv.setItem(2,new ItemStack(Items.COAL));check(recipe.matches(inv,null),"Repeated ingredient uses two slots");
        inv.setItem(2,ItemStack.EMPTY);check(!recipe.matches(inv,null),"Missing repeat rejected");
        inv.setItem(8,new ItemStack(Items.COAL,2));check(!recipe.matches(inv,null),"Stack cannot replace two slots");
        inv.setItem(2,new ItemStack(Items.COAL));check(!recipe.matches(inv,null),"Oversized slot rejected");
        inv.setItem(8,new ItemStack(Items.COAL));inv.setItem(0,new ItemStack(Items.STICK));check(!recipe.matches(inv,null),"Extra offerings rejected");inv.setItem(0,ItemStack.EMPTY);
        inputs.clear();recipe.getIngredients().clear();check(recipe.matches(inv,null),"Ingredient lists defensively copied");
        check(recipe.isSpecial() && recipe.getResultItem().isEmpty() && recipe.assemble(inv).isEmpty(),"No crafting output or automatic recipe unlock");
        check(!recipe.canCraftInDimensions(0,9) && !recipe.canCraftInDimensions(1,1) && recipe.canCraftInDimensions(3,3),"Dimensions checked");
        BrazierRecipe.Serializer serializer=new BrazierRecipe.Serializer();
        for(CurseData.Kind kind:CurseData.Kind.values()){
            JsonObject json=new JsonObject();json.addProperty("curse",kind.id);JsonArray array=new JsonArray();JsonObject ingredient=new JsonObject();ingredient.addProperty("item","minecraft:coal");array.add(ingredient);json.add("ingredients",array);
            BrazierRecipe parsed=serializer.fromJson(id,json);check(parsed.curse==kind,"JSON identity "+kind);
            PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());serializer.toNetwork(buffer,parsed);BrazierRecipe copy=serializer.fromNetwork(id,buffer);buffer.release();
            Inventory single=new Inventory(9);single.setItem(7,new ItemStack(Items.COAL));check(copy.curse==kind && copy.matches(single,null),"Network identity and ingredients "+kind);
        }
        for(int size:new int[]{0,10,-1}){PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());buffer.writeVarInt(size);boolean rejected=false;try{serializer.fromNetwork(id,buffer);}catch(IllegalArgumentException ex){rejected=true;}finally{buffer.release();}check(rejected,"Invalid network count "+size);}
        for(String json:new String[]{"{\"curse\":\"bewitchment:unknown\",\"ingredients\":[{\"item\":\"minecraft:coal\"}]}","{\"curse\":\"bewitchment:darkness\",\"ingredients\":[]}"}){boolean rejected=false;try{serializer.fromJson(id,new JsonParser().parse(json).getAsJsonObject());}catch(JsonSyntaxException ex){rejected=true;}check(rejected,"Invalid JSON rejected");}
        Inventory locks=new Inventory(9);check(BrazierTile.target(locks)==null,"No target without taglock");
        ItemStack lock=new ItemStack(new TaglockItem());locks.setItem(0,lock);check(BrazierTile.target(locks)==null,"Blank taglock rejected");
        UUID uuid=UUID.randomUUID();lock.getOrCreateTag().putString("boundId",uuid.toString());check(uuid.equals(BrazierTile.target(locks)),"Bound target resolved");
        locks.setItem(8,lock.copy());check(BrazierTile.target(locks)==null,"Ambiguous taglocks rejected");locks.setItem(8,ItemStack.EMPTY);
        lock.getOrCreateTag().putString("boundId","bad");check(BrazierTile.target(locks)==null,"Malformed target rejected");
        BrazierBlock block=new BrazierBlock();BrazierTile tile=new BrazierTile(TileEntityType.CHEST);
        for(int i=0;i<9;i++){ItemStack stack=new ItemStack(Items.PAPER);stack.getOrCreateTag().putInt("slot",i);tile.contents.setItem(i,stack);}
        CompoundNBT saved=tile.save(new CompoundNBT());BrazierTile loaded=new BrazierTile(TileEntityType.CHEST);loaded.load(block.defaultBlockState(),saved);
        for(int i=0;i<9;i++)check(loaded.contents.getItem(i).getTag().getInt("slot")==i,"Slot NBT survives save "+i);
        check(tile.getUpdateTag().isEmpty(),"Private contents omitted from chunk update");
        check(block.getStateDefinition().getPossibleStates().size()==4 && !block.defaultBlockState().getValue(BrazierBlock.LIT) && !block.defaultBlockState().getValue(BrazierBlock.HANGING),"Four states and safe defaults");
        System.out.println("PASS: "+checks+" Brazier checks");
    }
}
