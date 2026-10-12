package com.bewitchment.machine;

import com.bewitchment.item.WaystoneItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;

public final class TeleportCheck {
    private static int checks;
    private static void check(boolean value, String message) {checks++; if(!value) throw new AssertionError(message);}
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        WaystoneItem item=new WaystoneItem();
        ItemStack stone=new ItemStack(item);
        ResourceLocation dimension=new ResourceLocation("minecraft:overworld");
        BlockPos pos=new BlockPos(-100,64,200);
        check(stone.getMaxStackSize()==1 && stone.getMaxDamage()==3,"Three-use singleton");
        check(WaystoneItem.location(stone)==null,"Unbound destination rejected");
        stone.getOrCreateTag().putString("custom","preserved");
        check(WaystoneItem.bind(stone,pos,dimension),"Initial binding");
        check(pos.equals(WaystoneItem.location(stone)) && dimension.equals(WaystoneItem.dimension(stone)),"Position and dimension round trip");
        check(!WaystoneItem.bind(stone,pos.above(),dimension),"Cannot rebind");
        check("preserved".equals(stone.getTag().getString("custom")),"Binding preserves custom data");
        check(item.isFoil(stone),"Bound glint");
        ItemStack once=WaystoneItem.afterUse(stone,false);
        check(once.getDamageValue()==1 && stone.getDamageValue()==0,"Damage only returned copy");
        check(pos.equals(WaystoneItem.location(once)) && "preserved".equals(once.getTag().getString("custom")),"Use preserves binding and custom data");
        ItemStack twice=WaystoneItem.afterUse(once,false);
        check(twice.getDamageValue()==2,"Second use");
        check(WaystoneItem.afterUse(twice,false).isEmpty(),"Third use breaks");
        check(WaystoneItem.afterUse(twice,true).getDamageValue()==2,"Creative preserves remaining uses");
        ItemStack broken=stone.copy(); broken.setDamageValue(3);
        check(WaystoneItem.afterUse(broken,true).isEmpty(),"Already exhausted stone rejected");
        for(String bad:new String[]{"","NOT A DIMENSION","minecraft:bad space"}) {
            ItemStack corrupt=stone.copy(); corrupt.getTag().putString("dimension",bad);
            check(WaystoneItem.location(corrupt)==null,"Malformed dimension rejected");
        }
        ItemStack corrupt=stone.copy(); corrupt.getTag().putInt("location",2);
        check(WaystoneItem.location(corrupt)==null,"Wrong NBT type rejected");
        check(!WaystoneItem.bind(corrupt,pos,dimension),"Malformed existing binding not overwritten");
        for(BlockPos bad:new BlockPos[]{new BlockPos(0,-1,0),new BlockPos(0,256,0),new BlockPos(30000000,64,0),new BlockPos(0,64,-30000001)})
            check(!WaystoneItem.bind(new ItemStack(item),bad,dimension),"Out-of-world binding rejected");
        Inventory inputs=new Inventory(10); inputs.setItem(4,stone);
        check(RitualTeleport.soleOffering(inputs)==stone,"Offering found in arbitrary slot");
        check(RitualTeleport.returnedOffering(inputs,false).getDamageValue()==1,"Ritual returns worn stone");
        inputs.setItem(5,new ItemStack(Items.PAPER));
        check(RitualTeleport.soleOffering(inputs).isEmpty(),"Extra offering rejected");
        check(RitualTeleport.returnedOffering(inputs,false).isEmpty(),"Extra offering cannot generate return");
        inputs.clearContent(); inputs.setItem(0,new ItemStack(Items.PAPER));
        check(RitualTeleport.returnedOffering(inputs,false).isEmpty(),"Non-waystone has no remainder");
        inputs.getItem(0).setCount(2); check(RitualTeleport.soleOffering(inputs).isEmpty(),"Oversized offering rejected"); inputs.getItem(0).setCount(1);
        check(RitualTeleport.bounds(BlockPos.ZERO).getXsize()==7 && RitualTeleport.bounds(BlockPos.ZERO).minY==-3,"Legacy seven-block volume");
        NonNullList<Ingredient> ingredients=NonNullList.create(); ingredients.add(Ingredient.of(Items.PAPER));
        RitualRecipe recipe=new RitualRecipe(new ResourceLocation("bewitchment:teleport_test"),ingredients,ItemStack.EMPTY,5,450,20,new int[]{3,-1,-1},RitualEffect.TELEPORT);
        check(recipe.matches(inputs,null) && recipe.acceptsRun(""),"Single target finite cast");
        try {new RitualRecipe(recipe.getId(),ingredients,new ItemStack(Items.DIAMOND),5,450,20,new int[]{3,-1,-1},RitualEffect.TELEPORT); throw new AssertionError("Output accepted");} catch(com.google.gson.JsonSyntaxException expected) {checks++;}
        ingredients.add(Ingredient.of(Items.PAPER));
        try {new RitualRecipe(recipe.getId(),ingredients,ItemStack.EMPTY,5,450,20,new int[]{3,-1,-1},RitualEffect.TELEPORT); throw new AssertionError("Two offerings accepted");} catch(com.google.gson.JsonSyntaxException expected) {checks++;}
        RitualClock clock=new RitualClock(); int[] paid={450};
        for(int tick=1;tick<=100;tick++) {
            RitualClock.Step step=clock.advance(5,20,amount->{paid[0]+=amount;return true;});
            if(tick==100) check(step==RitualClock.Step.FINISHED,"Completes at five seconds");
        }
        check(paid[0]==550 && RitualPower.startingCost(450,true)+100==482,"Legacy total and Hecate discount");
        System.out.println("Teleport checks passed: "+checks);
    }
}
