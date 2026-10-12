package com.bewitchment.machine;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
public final class BrewBatchCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        Item[] inputs={Items.APPLE,Items.SPIDER_EYE,Items.GLISTERING_MELON_SLICE,Items.CARROT,Items.INK_SAC,Items.PORKCHOP,Items.COOKED_PORKCHOP,Items.RABBIT_HIDE,Items.SUGAR,Items.BROWN_MUSHROOM,Items.SUGAR_CANE,Items.RABBIT_FOOT,Items.COD,Items.RED_MUSHROOM,Items.CHORUS_FRUIT};
        Effect[] expected={Effects.HEAL,Effects.HARM,Effects.REGENERATION,Effects.NIGHT_VISION,Effects.BLINDNESS,Effects.DAMAGE_BOOST,Effects.DAMAGE_RESISTANCE,Effects.JUMP,Effects.MOVEMENT_SPEED,Effects.MOVEMENT_SLOWDOWN,Effects.DIG_SPEED,Effects.LUCK,Effects.WATER_BREATHING,Effects.CONFUSION,Effects.LEVITATION};
        int[] times={1,1,400,2400,200,1300,600,900,900,800,600,2400,3000,200,80};
        for(int i=0;i<inputs.length;i++){List<EffectInstance> effects=BrewIngredients.effects(new ItemStack(inputs[i]));check(BrewIngredients.supported(new ItemStack(inputs[i])) && effects.size()==1 && effects.get(0).getEffect()==expected[i] && effects.get(0).getDuration()==times[i],"Original vanilla ingredient and duration");}
        for(Item item:new Item[]{Items.REDSTONE,Items.GLOWSTONE_DUST,Items.GUNPOWDER})check(BrewIngredients.supported(new ItemStack(item)) && BrewIngredients.effects(new ItemStack(item)).isEmpty(),"Modifier is not an effect ingredient");
        check(!BrewIngredients.supported(new ItemStack(Items.EMERALD)),"Unsupported input rejected");
        for(boolean alchemist:new boolean[]{false,true})for(int leonards:new int[]{0,1,2}){
            BrewPlan plan=BrewIngredients.plan(Arrays.asList(new ItemStack(Items.SUGAR),new ItemStack(Items.REDSTONE),new ItemStack(Items.GLOWSTONE_DUST),new ItemStack(Items.GUNPOWDER)),alchemist,leonards);
            BrewBatch batch=new BrewBatch();check(batch.take().isEmpty(),"Unstarted batch produces nothing");batch.start(plan);
            int allowance=3+(alchemist?1:0)+leonards;check(batch.remaining()==allowance,"Exact first-extraction allowance");
            boolean rejected=false;try{batch.start(plan);}catch(IllegalStateException ex){rejected=true;}check(rejected,"Cannot refill started batch");
            for(int i=0;i<allowance;i++){
                ItemStack bottle=batch.take();check(bottle.getItem()==Items.SPLASH_POTION && bottle.getCount()==1 && batch.remaining()==allowance-i-1,"One bottle per committed withdrawal");
                bottle.getOrCreateTag().putBoolean("bewitchment_brew",false);
                if(batch.remaining()>0){BrewBatch loaded=new BrewBatch();check(loaded.load(batch.save()) && loaded.remaining()==batch.remaining(),"Partly bottled batch persists exactly");batch=loaded;}
            }
            check(batch.take().isEmpty() && batch.remaining()==0,"Exhausted batch cannot duplicate output");batch.clear();check(!batch.started() && batch.load(new CompoundNBT()),"Empty state and older save compatibility");
        }
        BrewBatch batch=new BrewBatch();batch.start(BrewIngredients.plan(Arrays.asList(new ItemStack(Items.SUGAR)),false,0));CompoundNBT valid=batch.save();
        for(int count:new int[]{-1,0}){CompoundNBT bad=valid.copy();bad.putInt("remaining",count);check(!batch.load(bad) && !batch.started(),"Invalid remaining count fails closed");}
        CompoundNBT bad=valid.copy();bad.put("potion",new ItemStack(Items.DIAMOND).save(new CompoundNBT()));check(!batch.load(bad),"Non-potion rejected");
        bad=valid.copy();bad.putString("remaining","3");check(!batch.load(bad),"Wrong count type rejected");
        bad=valid.copy();bad.remove("potion");check(!batch.load(bad),"Missing frozen potion cannot reroll remaining batch");
        check(BrewIngredients.returned(new ItemStack(Items.APPLE)).isEmpty(),"Ordinary ingredients return no container");
        System.out.println("PASS: "+checks+" brew ingredients, exact bottling, persistence and exhaustion checks");
    }
}