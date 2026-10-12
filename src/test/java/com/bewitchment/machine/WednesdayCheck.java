package com.bewitchment.machine;

import com.bewitchment.potion.WednesdayEffect;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.time.*;
import java.util.*;

public final class WednesdayCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random roll(int value){return new Random(0){@Override public int nextInt(int bound){if(value>=bound)throw new AssertionError("Invalid test roll");return value;}};}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        LocalDate monday=LocalDate.of(2026,10,5);
        for(int day=0;day<7;day++)check((WednesdayRules.failure(true,monday.plusDays(day))==null)==(day==2),"Exact calendar weekday "+day);
        check("wednesday_disabled".equals(WednesdayRules.failure(false,monday.plusDays(2))),"Disabled even on Wednesday");
        check("wednesday_day".equals(WednesdayRules.failure(true,monday)),"Enabled wrong-day feedback");
        com.bewitchment.config.RitualConfig.SPEC.setConfig(com.electronwill.nightconfig.core.CommentedConfig.inMemory());
        check(!com.bewitchment.config.RitualConfig.WEDNESDAY.get(),"Legacy opt-in default preserved");
        check(WednesdayRules.lifetime(roll(0))==60 && WednesdayRules.lifetime(roll(99))==159,"Expiry duration endpoints");
        check(WednesdayRules.height(roll(0))==2 && WednesdayRules.height(roll(5))==7,"Elevation endpoints");
        check(WednesdayRules.extra(10,roll(0)) && !WednesdayRules.extra(10,roll(5)) && !WednesdayRules.extra(11,roll(0)),"One-in-six chance only every ten world ticks");
        check(WednesdayRules.expires(1) && !WednesdayRules.expires(0) && !WednesdayRules.expires(2) && !WednesdayRules.expires(Integer.MAX_VALUE),"Only final effect tick expires");
        WednesdayEffect effect=new WednesdayEffect();check(effect.isDurationEffectTick(1,0) && !effect.isDurationEffectTick(2,9),"Effect scheduler uses final tick regardless of amplifier");
        check(effect.getColor()==0x00ff00 && effect.getCategory()==net.minecraft.potion.EffectType.HARMFUL,"Legacy effect identity");
        Random random=new Random(2026);boolean bounded=true;for(int i=0;i<10000;i++){double v=WednesdayRules.velocity(random);bounded&=v>-1 && v<1;}check(bounded,"Launch velocity stays within original range");
        check(WednesdayRitual.plan(roll(0),()->1,(e,p)->true).size()==64,"Minimum 64 final candidates");
        check(WednesdayRitual.plan(roll(127),()->1,(e,p)->true).size()==191,"Maximum 191 final candidates");
        check(WednesdayRitual.plan(new Random(0){int n;@Override public int nextInt(int bound){return n++<64?127:0;}},()->1,(e,p)->true).size()==64,"Wave bound rerolled rather than cached");
        int[] made={0},attempts={0};check(WednesdayRitual.plan(roll(127),()->{made[0]++;return 1;},(e,p)->{attempts[0]++;return false;}).isEmpty() && made[0]==191 && attempts[0]==3056,"Blocked wave has finite placement budget");
        attempts[0]=0;check(WednesdayRitual.plan(roll(0),()->1,(e,p)->++attempts[0]%16==0).size()==64 && attempts[0]==1024,"Sixteenth placement attempt accepted");
        made[0]=0;attempts[0]=0;WednesdayRitual.plan(roll(127),()->{made[0]++;return null;},(e,p)->{attempts[0]++;return true;});check(made[0]==191 && attempts[0]==0,"Null creations do not cause retries");
        List<Integer> sizes=new ArrayList<>();WednesdayRitual.plan(roll(0),()->1,(e,p)->{sizes.add(p.size());return true;});check(sizes.get(0)==0 && sizes.get(63)==63,"Prior raised bodies reserved");
        boolean[] immutable={false};WednesdayRitual.plan(roll(0),()->1,(e,p)->{try{p.add(2);}catch(UnsupportedOperationException ex){immutable[0]=true;}return true;});check(immutable[0],"Placement cannot change reservation list");
        RitualClock clock=new RitualClock();int[] paid={0};boolean early=false;for(int i=0;i<199;i++)early|=clock.advance(10,66,c->{paid[0]+=c;return true;})==RitualClock.Step.FINISHED;
        check(!early && paid[0]==594,"No early final shower");check(clock.advance(10,66,c->false)==RitualClock.Step.NO_POWER,"Final payment required");check(clock.advance(10,66,c->{paid[0]+=c;return true;})==RitualClock.Step.FINISHED && paid[0]+666==1326,"Exact total cost");
        RitualEffect ritual=RitualEffect.parse("wednesday");check(ritual==RitualEffect.WEDNESDAY && !ritual.consumesOnStart() && !ritual.continuous(),"Finite ritual classification");
        NonNullList<Ingredient> ingredients=NonNullList.create();Item[] items={Items.WHEAT,Items.WHEAT,Items.WHEAT,Items.CARROT,Items.SLIME_BALL,Items.POTION};for(Item item:items)ingredients.add(Ingredient.of(item));
        RitualRecipe recipe=new RitualRecipe(new ResourceLocation("bewitchment:wednesday_test"),ingredients,ItemStack.EMPTY,10,666,66,new int[]{1,3,-1},ritual);
        net.minecraft.inventory.Inventory inputs=new net.minecraft.inventory.Inventory(10);for(int i=0;i<items.length;i++)inputs.setItem(i,new ItemStack(items[i]));check(recipe.matches(inputs,null),"Six offerings match");
        inputs.setItem(2,ItemStack.EMPTY);check(!recipe.matches(inputs,null),"Third toe equivalent required");
        check(recipe.assemble(inputs).isEmpty(),"No item output or invented jar return");
        boolean rejected=false;try{new RitualRecipe(recipe.getId(),ingredients,new ItemStack(Items.DIAMOND),10,666,66,new int[]{1,3,-1},ritual);}catch(com.google.gson.JsonSyntaxException ex){rejected=true;}check(rejected,"Reject invented output");
        net.minecraft.network.PacketBuffer packet=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());try{RitualRecipe.Serializer serializer=new RitualRecipe.Serializer();serializer.toNetwork(packet,recipe);RitualRecipe copy=serializer.fromNetwork(recipe.getId(),packet);
            check(copy.effect==ritual && copy.circleType(0)==1 && copy.circleType(1)==3 && copy.circleType(2)==-1,"Mixed circles and effect round trip");
            check(copy.seconds==10 && copy.startingPower==666 && copy.runningPower==66 && copy.getIngredients().size()==6 && copy.getResultItem().isEmpty() && !packet.isReadable(),"Complete packet preserved");
        }finally{packet.release();}
        List<Integer> awards=new ArrayList<>();check(CallOfTheWild.insert(Arrays.asList(1,2,3),n->n==2,awards::add)==1 && awards.equals(Collections.singletonList(2)),"Only inserted toads award advancement");
        RitualInventory inventory=new RitualInventory(()->{});ItemStack offering=new ItemStack(Items.POTION);offering.getOrCreateTag().putString("original","kept");inventory.insertItem(0,offering,false);List<ItemStack> refund=inventory.takeAll();check(refund.size()==1 && "kept".equals(refund.get(0).getTag().getString("original")) && inventory.takeAll().isEmpty(),"Refund preserves data without replay");
        check(!RitualConjuration.fits(new net.minecraft.util.math.AxisAlignedBB(0,255,0,1,257,1),-10,10,-10,10),"Raised body above height rejected");
        System.out.println("PASS: "+checks+" Wednesday calendar, opt-in, wave, expiry, recipe and settlement checks.");
    }
}
