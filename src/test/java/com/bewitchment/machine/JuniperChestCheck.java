package com.bewitchment.machine;

import com.bewitchment.block.*;
import com.bewitchment.client.JuniperChestRenderer;
import com.bewitchment.item.SkeletonKeyItem;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;

public final class JuniperChestCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();JuniperChestBlock block=new JuniperChestBlock();BlockState state=block.defaultBlockState();
        check(JuniperLocks.lock(state),"Chest uses shared lock pipeline");
        check(SkeletonKeyItem.cost(block)==1000,"Container bypass requires 1000 power");
        check(JuniperLocks.base(state,new BlockPos(1,2,3)).equals(new BlockPos(1,2,3)),"Chest binding stays on its position");
        check(state.getValue(JuniperChestBlock.FACING)==Direction.NORTH,"Default direction");
        check(block.rotate(state,Rotation.CLOCKWISE_90).getValue(JuniperChestBlock.FACING)==Direction.EAST,"Rotation");
        check(block.mirror(state,Mirror.LEFT_RIGHT).getValue(JuniperChestBlock.FACING)==Direction.SOUTH,"Mirror");
        check(block.getPistonPushReaction(state)==PushReaction.BLOCK,"Pistons cannot move lock or inventory");
        check(block.getRenderShape(state)==BlockRenderType.ENTITYBLOCK_ANIMATED,"Animated world rendering");
        check(block.getShape(state,null,BlockPos.ZERO,ISelectionContext.empty()).bounds().maxY==.875,"Original chest height");
        check(!state.hasProperty(net.minecraft.state.properties.BlockStateProperties.CHEST_TYPE),"No adjacent double-chest merging");
        JuniperChestTile chest=new JuniperChestTile(TileEntityType.CHEST);
        check(chest.getContainerSize()==27 && chest.isEmpty(),"27 visible slots initially empty");
        ItemStack named=new ItemStack(Items.DIAMOND,7);named.setHoverName(new StringTextComponent("Kept"));
        chest.setItem(0,named);chest.setItem(26,new ItemStack(Items.GOLD_INGOT,4));chest.setCustomName(new StringTextComponent("Private Chest"));
        CompoundNBT saved=chest.save(new CompoundNBT());
        JuniperChestTile loaded=new JuniperChestTile(TileEntityType.CHEST);loaded.load(state,saved);
        check(loaded.getItem(0).getCount()==7 && loaded.getItem(0).getItem()==Items.DIAMOND,"First slot persists");
        check(loaded.getItem(0).getHoverName().getString().equals("Kept"),"Item NBT persists");
        check(loaded.getItem(26).getCount()==4,"Last visible slot persists");
        check(loaded.getDisplayName().getString().equals("Private Chest"),"Custom chest name persists");
        check(loaded.getItem(0)!=chest.getItem(0),"Loaded inventory does not alias source stacks");
        check(loaded.removeItem(0,3).getCount()==3 && loaded.getItem(0).getCount()==4,"Partial removal preserves remainder");
        check(loaded.removeItemNoUpdate(26).getCount()==4 && loaded.getItem(26).isEmpty(),"Whole-slot removal");
        loaded.clearContent();check(loaded.isEmpty() && loaded.getContainerSize()==27,"Clear preserves slot count");
        for(Direction direction:Direction.values()){
            check(chest.getSlotsForFace(direction).length==0,"Hoppers cannot discover slots: "+direction);
            check(!chest.canPlaceItemThroughFace(0,named,direction) && !chest.canTakeItemThroughFace(0,named,direction),"Sided transfers refused: "+direction);
        }
        check(!chest.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null).isPresent(),"Unsided item handler is hidden");
        float lid=0;for(int i=0;i<20;i++)lid=JuniperChestTile.advanceLid(lid,2);
        check(lid==1,"Lid opens and clamps with multiple viewers");
        for(int i=0;i<20;i++)lid=JuniperChestTile.advanceLid(lid,0);
        check(lid==0,"Lid closes and clamps");
        check(Math.abs(JuniperChestTile.advanceLid(0,1)-.1F)<.0001F,"Legacy ten-tick opening rate");
        check(JuniperChestRenderer.lidRotation(0)==0,"Closed renderer angle");
        check(Math.abs(JuniperChestRenderer.lidRotation(1)+(float)Math.PI/2)<.0001F,"Open renderer angle");
        check(Math.abs(JuniperChestRenderer.lidRotation(.5F)+.875F*(float)Math.PI/2)<.0001F,"Original cubic lid easing");
        System.out.println("PASS: "+checks+" Juniper Chest checks");
    }
}
