package com.bewitchment.machine;

import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class LeonardConjurationCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        RitualEffect effect=RitualEffect.parse("conjure_leonard");
        check(effect==RitualEffect.CONJURE_LEONARD && effect.conjuration(),"Leonard effect selects conjuration path");
        check(!effect.consumesOnStart() && !effect.continuous() && !effect.instant(),"Finite completion-consumed cast");
        check("leonard_night".equals(effect.failure(true,true)) && "leonard_night".equals(effect.failure(false,true)),"Both dimensions reject daytime activation");
        check(effect.failure(true,false)==null && effect.failure(false,false)==null,"Night permitted without Overworld restriction");
        check(LeonardConjurationTrigger.nearby(0) && LeonardConjurationTrigger.nearby(99.99),"Closest player radius includes inside boundary");
        check(!LeonardConjurationTrigger.nearby(100) && !LeonardConjurationTrigger.nearby(-1) && !LeonardConjurationTrigger.nearby(Double.NaN),"Radius is strict and rejects invalid distances");
        BlockPos center=new BlockPos(17,64,-17);Set<BlockPos> positions=new HashSet<>();
        for(BlockPos pos:LeonardConjurationTrigger.positions(center))positions.add(pos.immutable());
        check(positions.size()==125,"Death scan bounded to 125 block positions");
        check(positions.contains(center.offset(-2,-2,-2)) && positions.contains(center.offset(2,2,2)),"Both scan corners included");
        check(!positions.contains(center.offset(3,0,0)) && !positions.contains(center.offset(0,-3,0)),"Scan excludes positions beyond two blocks");
        check(RitualCircle.accepts(4,1) && RitualCircle.accepts(4,2) && RitualCircle.accepts(4,3),"Middle Any ring accepts all ordinary chalk types");
        check(!RitualCircle.accepts(4,0) && !RitualCircle.accepts(4,4),"Any ring excludes focal/invalid glyphs");
        check(RitualCircle.accepts(2,2) && !RitualCircle.accepts(2,1) && !RitualCircle.accepts(2,3),"Inner/outer Nether rings remain strict");
        NonNullList<Ingredient> ingredients=NonNullList.create();
        ingredients.add(Ingredient.of(Items.IRON_SWORD));ingredients.add(Ingredient.of(Items.GOLD_INGOT));ingredients.add(Ingredient.of(Items.WHEAT));ingredients.add(Ingredient.of(Items.BONE));for(Item item:new Item[]{Items.DIAMOND,Items.APPLE,Items.ROTTEN_FLESH,Items.GLASS_BOTTLE})ingredients.add(Ingredient.of(item));
        RitualRecipe recipe=new RitualRecipe(new ResourceLocation("bewitchment:test_leonard"),ingredients,new ItemStack(Items.IRON_SWORD),15,1332,66,new int[]{2,2,2},effect);
        Inventory input=new Inventory(10);ItemStack tool=new ItemStack(Items.IRON_SWORD);tool.setDamageValue(50);tool.getOrCreateTag().putString("marker","original");
        input.setItem(0,tool);input.setItem(1,new ItemStack(Items.GOLD_INGOT));input.setItem(2,new ItemStack(Items.WHEAT));input.setItem(3,new ItemStack(Items.BONE));input.setItem(4,new ItemStack(Items.DIAMOND));input.setItem(5,new ItemStack(Items.APPLE));input.setItem(6,new ItemStack(Items.ROTTEN_FLESH));input.setItem(7,new ItemStack(Items.GLASS_BOTTLE));
        check(recipe.matches(input,null),"Damaged named tool matches offering");
        check(recipe.assemble(input).getDamageValue()==0 && (recipe.assemble(input).getTag()==null || !recipe.assemble(input).getTag().contains("marker")),"Success returns a fresh tool");
        check(recipe.acceptsRun("") && !recipe.acceptsRun("conjure_leonard"),"No already-consumed activation state accepted");
        PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());
        try{RitualRecipe.Serializer serializer=new RitualRecipe.Serializer();serializer.toNetwork(buffer,recipe);RitualRecipe copy=serializer.fromNetwork(recipe.getId(),buffer);
            check(copy.effect==effect && copy.circleType(0)==2 && copy.circleType(1)==2 && copy.circleType(2)==2,"Leonard effect and Nether middle ring survive networking");
            check(copy.seconds==15 && copy.startingPower==1332 && copy.runningPower==66 && !buffer.isReadable(),"Costs and duration consume exact packet");
        }finally{buffer.release();}
        RitualInventory inventory=new RitualInventory(()->{});for(int i=0;i<8;i++)inventory.insertItem(i,input.getItem(i),false);
        List<ItemStack> refund=inventory.takeAll();
        check(refund.size()==8 && refund.get(0).getDamageValue()==50 && refund.get(0).getTag().getString("marker").equals("original"),"Rejected spawn retains original tool damage/NBT in refund");
        check(inventory.takeAll().isEmpty(),"No duplicate refund from cleared storage");
        RitualClock clock=new RitualClock();int[] cost={0};boolean early=false;
        for(int i=0;i<299;i++)early|=clock.advance(15,66,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED;
        check(!early && cost[0]==924,"No completion before final paid second");
        check(clock.advance(15,66,c->false)==RitualClock.Step.NO_POWER,"Final failed payment prevents Leonard completion");
        check(clock.advance(15,66,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED && cost[0]==990,"Fifteen running payments total 990");
        check(1332+cost[0]==2322,"Base total power 2322");
        check(RitualPower.startingCost(1332,true)==1132,"Hecate discount retains integer truncation");
        check(RitualConjuration.fits(new AxisAlignedBB(0,64,0,1,67.8,1),-10,10,-10,10),"Leonard body fits clear legal bounds");
        check(!RitualConjuration.fits(new AxisAlignedBB(0,253,0,1,256.8,1),-10,10,-10,10),"Leonard body above build height rejected");
        System.out.println("PASS: "+checks+" Leonard conjuration activation, scan, circles, packet, refund and payment checks.");
    }
}
