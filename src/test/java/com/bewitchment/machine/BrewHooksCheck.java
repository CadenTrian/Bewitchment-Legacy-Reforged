package com.bewitchment.machine;

import com.bewitchment.api.event.WitchesCauldronEvent.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import java.util.*;

public final class BrewHooksCheck {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    public static void main(String[] args){
        net.minecraft.util.registry.Bootstrap.bootStrap();
        List<ItemStack> ingredients=Arrays.asList(new ItemStack(Items.SUGAR),new ItemStack(Items.GLOWSTONE_DUST));
        for(boolean alchemist:new boolean[]{false,true})for(int leonards:new int[]{0,1,3}){
            int[] calls={0};
            BrewHooks.Prepared prepared=BrewHooks.prepare(null,null,ingredients,alchemist,leonards,event->{calls[0]++;return false;});
            BrewPlan original=BrewIngredients.plan(ingredients,alchemist,leonards);
            check(calls[0]==2&&prepared.bottles==original.bottles,"Two hooks and original allowance");
            check(ItemStack.matches(prepared.potion,original.potion()),"Unmodified hooks preserve original effects and bonuses");
        }
        for(int cancel=1;cancel<=2;cancel++){
            final int at=cancel;int[] calls={0};
            check(BrewHooks.prepare(null,null,ingredients,false,0,event->{calls[0]++;return calls[0]==at;})==null,"Either event cancels creation");
            check(calls[0]==cancel,"First cancellation suppresses second hook");
        }
        BrewHooks.Prepared modified=BrewHooks.prepare(null,null,ingredients,false,0,event->{
            if(event instanceof CreatePotionEvent){CreatePotionEvent create=(CreatePotionEvent)event;create.setBoosted(true);create.setAllowHigher(true);create.setBottles(7);}
            else{PotionCreatedEvent created=(PotionCreatedEvent)event;check(created.getBottles()==7,"Creation modifications reach second event");check(PotionUtils.getCustomEffects(created.getPotionStack()).get(0).getAmplifier()==2,"Boost and higher cap applied");created.setBottles(5);created.setPotionStack(PotionUtils.setCustomEffects(new ItemStack(Items.LINGERING_POTION),Arrays.asList(new EffectInstance(Effects.LUCK,120,1))));}
            return false;
        });
        check(modified.bottles==5&&modified.potion.getItem()==Items.LINGERING_POTION,"Final event can customize form, effects and count");
        BrewBatch batch=new BrewBatch();batch.start(modified.potion,modified.bottles);modified.potion.setCount(0);
        for(int i=0;i<5;i++){
            ItemStack bottle=batch.take();check(bottle.getCount()==1&&PotionUtils.getCustomEffects(bottle).get(0).getEffect()==Effects.LUCK,"Frozen custom output remains isolated");
            if(i<4){BrewBatch loaded=new BrewBatch();check(loaded.load(batch.save())&&loaded.remaining()==4-i,"Custom event output survives save/load");batch=loaded;}
        }
        check(batch.take().isEmpty(),"Custom allowance cannot duplicate bottles");
        for(int count:new int[]{0,-1,Integer.MIN_VALUE})check(BrewHooks.prepare(null,null,ingredients,false,0,event->{if(event instanceof PotionCreatedEvent)((PotionCreatedEvent)event).setBottles(count);return false;})==null,"Invalid final count rejected");
        for(ItemStack invalid:Arrays.asList(null,ItemStack.EMPTY,new ItemStack(Items.DIAMOND),new ItemStack(Items.POTION,2)))
            check(BrewHooks.prepare(null,null,ingredients,false,0,event->{if(event instanceof PotionCreatedEvent)((PotionCreatedEvent)event).setPotionStack(invalid);return false;})==null,"Invalid output rejected");
        int[] calls={0};check(BrewHooks.prepare(null,null,ingredients,false,0,event->{calls[0]++;((CreatePotionEvent)event).setBottles(0);return false;})==null&&calls[0]==1,"Invalid initial count stops preparation");
        System.out.println("PASS: "+checks+" brewing hook cancellation, mutation, bonus parity and persistence checks");
    }
}
