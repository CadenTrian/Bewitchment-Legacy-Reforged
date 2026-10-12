package com.bewitchment.item;

import com.bewitchment.block.*;
import com.bewitchment.machine.JuniperKeyRecipe;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class JuniperKeyCheck {
    private static int checks;
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();ResourceLocation overworld=new ResourceLocation("minecraft:overworld"),nether=new ResourceLocation("minecraft:the_nether");
        BlockPos pos=new BlockPos(-31,64,419);JuniperKeyItem keyItem=new JuniperKeyItem(false),ringItem=new JuniperKeyItem(true);
        ItemStack key=new ItemStack(keyItem);key.setTag(JuniperKeyItem.binding(overworld,pos));
        check(JuniperKeyItem.opens(key,overworld,pos),"Exact binding opens");
        check(!JuniperKeyItem.opens(key,nether,pos),"Same coordinates in another dimension refused");
        check(!JuniperKeyItem.opens(key,overworld,pos.above()),"Different position refused");
        check(!JuniperKeyItem.opens(new ItemStack(keyItem),overworld,BlockPos.ZERO),"Blank key never opens origin");
        check(!JuniperKeyItem.valid(new CompoundNBT()),"Missing tags rejected");
        CompoundNBT bad=key.getTag().copy();bad.putString("dimension","INVALID SPACE");check(!JuniperKeyItem.valid(bad),"Malformed dimension rejected");
        bad=key.getTag().copy();bad.putInt("dimension",0);check(!JuniperKeyItem.valid(bad),"Legacy numeric dimension never silently aliases modern Overworld");
        bad=key.getTag().copy();bad.putInt("location",0);check(!JuniperKeyItem.valid(bad),"Wrong position type rejected");
        check(BlockPos.of(key.getTag().getLong("location")).equals(pos),"Negative coordinate persistence");
        ItemStack second=new ItemStack(keyItem);second.setTag(JuniperKeyItem.binding(nether,pos));
        ItemStack ring=JuniperKeyItem.combine(ringItem,Arrays.asList(key,second));
        check(JuniperKeyItem.bindings(ring).size()==2 && JuniperKeyItem.opens(ring,overworld,pos) && JuniperKeyItem.opens(ring,nether,pos),"Ring carries distinct dimensions");
        check(!JuniperKeyItem.opens(ring,overworld,pos.below()),"Ring does not widen coordinate access");
        ItemStack named=key.copy();named.setHoverName(new net.minecraft.util.text.StringTextComponent("Same lock"));
        ItemStack merged=JuniperKeyItem.combine(ringItem,Arrays.asList(ring,key,named));
        check(JuniperKeyItem.bindings(merged).size()==2,"Merge deduplicates lock identity even with different display names");
        merged.getTag().getList("Keys",10).getCompound(0).putLong("location",BlockPos.ZERO.asLong());
        check(JuniperKeyItem.opens(ring,overworld,pos) && JuniperKeyItem.opens(key,overworld,pos),"Ring output does not alias source bindings");
        List<CompoundNBT> read=JuniperKeyItem.bindings(ring);read.get(0).putLong("location",0);check(JuniperKeyItem.opens(ring,overworld,pos),"Read bindings are defensive copies");
        check(JuniperKeyItem.bindings(JuniperKeyItem.combine(ringItem,Arrays.asList(new ItemStack(keyItem),new ItemStack(ringItem)))).isEmpty(),"Unbound inputs give no phantom access");
        ItemStack wrong=new ItemStack(Items.STICK);wrong.setTag(key.getTag().copy());check(!JuniperKeyItem.opens(wrong,overworld,pos),"NBT on unrelated item cannot unlock");
        ListNBT corrupt=new ListNBT();corrupt.add(new CompoundNBT());ItemStack corruptRing=new ItemStack(ringItem);corruptRing.getOrCreateTag().put("Keys",corrupt);check(JuniperKeyItem.bindings(corruptRing).isEmpty(),"Malformed ring entries ignored");
        ItemStack planks=new ItemStack(Items.OAK_PLANKS);
        check(JuniperKeyRecipe.accepts(Arrays.asList(key,planks),Items.OAK_PLANKS,true),"Key plus matching planks duplicate");
        check(JuniperKeyRecipe.accepts(Arrays.asList(ring,planks),Items.OAK_PLANKS,true),"Ring duplication supported");
        check(!JuniperKeyRecipe.accepts(Arrays.asList(key,planks,new ItemStack(Items.DIAMOND)),Items.OAK_PLANKS,true),"Extra ingredients refused instead of consumed");
        check(!JuniperKeyRecipe.accepts(Arrays.asList(key,key,planks),Items.OAK_PLANKS,true),"Two keys cannot duplicate");
        check(!JuniperKeyRecipe.accepts(Arrays.asList(key,planks,planks),Items.OAK_PLANKS,true),"Two plank slots cannot duplicate");
        check(!JuniperKeyRecipe.accepts(Arrays.asList(key,new ItemStack(Items.BIRCH_PLANKS)),Items.OAK_PLANKS,true),"Wrong plank type refused");
        check(JuniperKeyRecipe.accepts(Arrays.asList(key,ring),Items.AIR,false),"Keys and rings combine");
        check(!JuniperKeyRecipe.accepts(Arrays.asList(key),Items.AIR,false) && !JuniperKeyRecipe.accepts(Arrays.asList(key,planks),Items.AIR,false),"Ring requires at least two key inputs only");
        JuniperKeyRecipe duplicate=new JuniperKeyRecipe(new ResourceLocation("bewitchment:duplicate_test"),true),combine=new JuniperKeyRecipe(new ResourceLocation("bewitchment:ring_test"),false);
        check(duplicate.canCraftInDimensions(3,3) && !duplicate.canCraftInDimensions(2,2),"Duplication requires nine cells");
        check(combine.canCraftInDimensions(2,1) && !combine.canCraftInDimensions(1,1),"Ring requires two cells");
        CraftingInventory grid=new CraftingInventory(new Container(null,-1){@Override public boolean stillValid(PlayerEntity player){return true;}},3,3);grid.setItem(1,key);grid.setItem(4,planks);
        ItemStack remainder=duplicate.getRemainingItems(grid).get(1);check(remainder.getCount()==1 && remainder!=key && remainder.getTag().equals(key.getTag()),"Duplication returns a copied original key");
        check(combine.getRemainingItems(grid).stream().allMatch(ItemStack::isEmpty),"Combining consumes keys without duplication");
        JuniperDoor door=new JuniperDoor();JuniperGate gate=new JuniperGate();JuniperTrapdoor trap=new JuniperTrapdoor();
        check(JuniperLocks.base(door.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),pos.above()).equals(pos),"Upper half normalizes to lower lock");
        check(JuniperLocks.base(door.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),pos).equals(pos),"Lower half stays at binding");
        check(JuniperLocks.base(trap.defaultBlockState(),pos).equals(pos) && JuniperLocks.base(gate.defaultBlockState(),pos).equals(pos),"Single-block locks keep their coordinates");
        check(JuniperLocks.lock(door.defaultBlockState()) && JuniperLocks.lock(gate.defaultBlockState()) && JuniperLocks.lock(trap.defaultBlockState()) && !JuniperLocks.lock(Blocks.OAK_DOOR.defaultBlockState()),"Only restored Juniper entrances are locks");
        check(door.getPistonPushReaction(door.defaultBlockState())==PushReaction.BLOCK && gate.getPistonPushReaction(gate.defaultBlockState())==PushReaction.BLOCK,"Door and gate resist pistons");
        check(gate.getFlammability(gate.defaultBlockState(),null,pos,Direction.UP)==20,"Existing gate flammability preserved");
        check(!door.defaultBlockState().getValue(DoorBlock.OPEN) && !gate.defaultBlockState().getValue(FenceGateBlock.OPEN) && !trap.defaultBlockState().getValue(TrapDoorBlock.OPEN),"Closed default states");
        System.out.println("PASS: "+checks+" Juniper binding, dimensions, ring merging, duplication and lock-state checks.");
    }
}
