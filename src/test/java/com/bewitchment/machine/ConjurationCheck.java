package com.bewitchment.machine;

import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Bootstrap;

public final class ConjurationCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        AxisAlignedBB inside = new AxisAlignedBB(-.5, 64, -.5, .5, 67.5, .5);
        check(RitualConjuration.fits(inside,-10,10,-10,10), "Mob fully within height and border fits");
        check(!RitualConjuration.fits(inside,0,10,-10,10), "Partial west-border overlap rejected");
        check(!RitualConjuration.fits(inside,-10,0,-10,10), "Partial east-border overlap rejected");
        check(!RitualConjuration.fits(inside,-10,10,0,10), "Partial north-border overlap rejected");
        check(!RitualConjuration.fits(inside,-10,10,-10,0), "Partial south-border overlap rejected");
        check(RitualConjuration.fits(new AxisAlignedBB(0,0,0,1,256,1),0,1,0,1), "Exact legal boundaries accepted");
        check(!RitualConjuration.fits(new AxisAlignedBB(0,-.01,0,1,3,1),-10,10,-10,10), "Below-world body rejected");
        check(!RitualConjuration.fits(new AxisAlignedBB(0,254,0,1,257.5,1),-10,10,-10,10), "Wither cannot extend above build height");
        check(RitualConjuration.blessed(0) && RitualConjuration.blessed(.099999F), "Witch blessing lower ten percent");
        check(!RitualConjuration.blessed(.1F) && !RitualConjuration.blessed(.999F), "Witch blessing excludes threshold and upper range");
        check(!RitualConjuration.blessed(Float.NaN) && !RitualConjuration.blessed(-1), "Invalid rolls do not grant blessing");
        EffectInstance blessing = RitualConjuration.blessing();
        check(blessing.getEffect() == Effects.REGENERATION && blessing.getDuration() == 6000 && blessing.getAmplifier() == 2 && !blessing.isVisible(), "Legacy five-minute Regeneration III without particles");

        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(Items.IRON_SWORD)); ingredients.add(Ingredient.of(Items.WITHER_SKELETON_SKULL));
        for (int i=0;i<4;i++) ingredients.add(Ingredient.of(Items.SOUL_SAND));
        for (int i=0;i<3;i++) ingredients.add(Ingredient.of(Items.WHEAT));
        ingredients.add(Ingredient.of(Items.GHAST_TEAR));
        Inventory inventory = new Inventory(10);
        ItemStack tool = new ItemStack(Items.IRON_SWORD); tool.setDamageValue(37); tool.getOrCreateTag().putString("owner", "retained on failure");
        inventory.setItem(0,tool); inventory.setItem(1,new ItemStack(Items.WITHER_SKELETON_SKULL));
        for(int i=2;i<6;i++) inventory.setItem(i,new ItemStack(Items.SOUL_SAND));
        for(int i=6;i<9;i++) inventory.setItem(i,new ItemStack(Items.WHEAT));
        inventory.setItem(9,new ItemStack(Items.GHAST_TEAR));
        RitualRecipe recipe = new RitualRecipe(new ResourceLocation("bewitchment","test_conjuration"),ingredients,new ItemStack(Items.IRON_SWORD),15,1332,66,new int[]{2,2,2},RitualEffect.CONJURE_WITHER);
        check(recipe.matches(inventory,null), "All ten offerings match, including damaged tool");
        inventory.setItem(5,ItemStack.EMPTY); check(!recipe.matches(inventory,null), "Repeated Soul Sand requires four separate offers");
        inventory.setItem(5,new ItemStack(Items.SOUL_SAND));
        check(recipe.assemble(inventory).getDamageValue()==0 && (recipe.assemble(inventory).getTag()==null || !recipe.assemble(inventory).getTag().contains("owner")), "Successful return is a fresh tool, as in legacy");
        check(recipe.effect.conjuration() && !recipe.effect.consumesOnStart() && recipe.acceptsRun(""), "Conjuration retains finite offering recovery");
        check(!recipe.acceptsRun("hungry_flames"), "Consumed active ritual cannot become a conjuration");
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            RitualRecipe.Serializer serializer = new RitualRecipe.Serializer(); serializer.toNetwork(buffer,recipe);
            RitualRecipe copy = serializer.fromNetwork(recipe.getId(),buffer);
            check(copy.effect==RitualEffect.CONJURE_WITHER && copy.getIngredients().size()==10 && copy.circleType(2)==2 && copy.seconds==15 && copy.startingPower==1332 && copy.runningPower==66, "Conjuration packet preserves full-capacity recipe");
            check(copy.getResultItem().getItem()==Items.IRON_SWORD && copy.getResultItem().getDamageValue()==0, "Fresh returned tool synchronizes");
        } finally {buffer.release();}
        RitualInventory offers = new RitualInventory(() -> {});
        for(int i=0;i<10;i++) offers.insertItem(i,inventory.getItem(i),false);
        NonNullList<ItemStack> refund = offers.takeAll();
        check(refund.size()==10 && refund.get(0).getDamageValue()==37 && refund.get(0).getTag().getString("owner").equals("retained on failure"), "Failed spawn can return all original offers with tool NBT");
        check(offers.takeAll().isEmpty(), "Cleared storage cannot refund a second time");
        refund.get(0).getTag().putString("owner","changed");
        check(tool.getTag().getString("owner").equals("retained on failure"), "Refund does not alias input inventory");
        RitualClock clock = new RitualClock(); int[] paid={0}; boolean early=false;
        for(int i=0;i<299;i++) early |= clock.advance(15,66,cost -> {paid[0]+=cost; return true;})==RitualClock.Step.FINISHED;
        check(!early && paid[0]==924, "No conjuration before fifteen full seconds");
        check(clock.advance(15,66,cost -> false)==RitualClock.Step.NO_POWER, "Final payment failure prevents conjuration");
        check(clock.advance(15,66,cost -> {paid[0]+=cost;return true;})==RitualClock.Step.FINISHED && paid[0]==990, "Wither running total is 990");
        check(RitualPower.startingCost(750,true)==637 && RitualPower.startingCost(1332,true)==1132, "Conjuration startup discounts match legacy truncation");
        System.out.println("PASS: " + checks + " conjuration bounds, blessing, recipe, settlement and payment checks.");
    }
}
