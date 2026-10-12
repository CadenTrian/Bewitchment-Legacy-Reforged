package com.bewitchment.machine;

import com.bewitchment.api.event.WitchesCauldronEvent.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import java.util.*;
import java.util.function.Predicate;

/** Preparation only: event cancellation never starts or drains a batch. */
public final class BrewHooks {
    public static final class Prepared {
        public final ItemStack potion;
        public final int bottles;
        Prepared(ItemStack potion,int bottles){this.potion=potion.copy();this.bottles=bottles;}
    }
    public static Prepared prepare(CauldronTile cauldron,PlayerEntity player,List<ItemStack> ingredients,boolean alchemist,int leonards){
        return prepare(cauldron,player,ingredients,alchemist,leonards,event->MinecraftForge.EVENT_BUS.post(event));
    }
    static Prepared prepare(CauldronTile cauldron,PlayerEntity player,List<ItemStack> ingredients,boolean alchemist,int leonards,Predicate<Event> post){
        CreatePotionEvent create=new CreatePotionEvent(cauldron,player,BrewPlan.bottles(alchemist,leonards),alchemist);
        create.setAllowHigher(leonards>0);
        if(post.test(create)||create.getBottles()<=0)return null;
        BrewPlan plan=BrewIngredients.plan(ingredients,create.isBoosted(),create.allowsHigher()?1:0);
        if(plan.effects().isEmpty())return null;
        PotionCreatedEvent created=new PotionCreatedEvent(cauldron,create.getUser(),create.getBottles(),plan.potion());
        if(post.test(created)||!valid(created.getPotionStack(),created.getBottles()))return null;
        ItemStack result=created.getPotionStack().copy();result.getOrCreateTag().putBoolean("bewitchment_brew",true);
        return new Prepared(result,created.getBottles());
    }
    public static boolean valid(ItemStack potion,int bottles){return potion!=null&&!potion.isEmpty()&&potion.getItem() instanceof PotionItem&&potion.getCount()==1&&bottles>0;}
    /** Run under Forge so its event-class transformation is active. */
    public static void verify(){
        net.minecraftforge.eventbus.api.IEventBus bus=net.minecraftforge.eventbus.api.BusBuilder.builder().build();
        bus.addListener((com.bewitchment.api.event.WitchesCauldronEvent event)->event.setCanceled(true));
        if(!bus.post(new CreatePotionEvent(null,null,3,false)) || !bus.post(new PotionCreatedEvent(null,null,3,new ItemStack(Items.POTION))))
            throw new IllegalStateException("Both cauldron hooks must propagate cancellation");
        org.apache.logging.log4j.LogManager.getLogger().info("Verified both cauldron brewing hooks through Forge event dispatch");
    }
    private BrewHooks(){}
}
