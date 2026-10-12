package com.bewitchment.machine;

import com.google.gson.JsonSyntaxException;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class CallOfTheWildCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random rolls(int... bounds){return new Random(0){int index;@Override public int nextInt(int bound){return bound==3?bounds[Math.min(index++,bounds.length-1)]:17;}};}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        String[] pool={"lizard","lizard","owl","raven","snake","ocelot","wolf","parrot","druden","werewolf","horse","llama","donkey","sheep","cow","pig","chicken","toad"};
        for(int i=0;i<18;i++)check(CallOfTheWild.id(i).equals(pool[i]),"Legacy choice "+i);
        check(new HashSet<>(Arrays.asList(pool)).size()==17,"Seventeen distinct species across eighteen slots");
        for(int i=0;i<18;i++)check(CallOfTheWild.allowed(i,true)==(i!=8 && i!=9) && CallOfTheWild.allowed(i,false),"Peaceful selection policy "+i);
        boolean rejected=false;try{CallOfTheWild.id(18);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid pool index rejected");
        int[] histogram=new int[4];
        for(int second=0;second<3;second++)for(int third=0;third<3;third++)histogram[CallOfTheWild.plan(rolls(0,second,third,0),i->i,(e,p)->true).size()]++;
        check(histogram[1]==3 && histogram[2]==4 && histogram[3]==2,"Exact rerolled-bound probabilities 3/9, 4/9, 2/9");
        int[] attempts={0},created={0};
        List<Integer> empty=CallOfTheWild.plan(rolls(2),i->{created[0]++;return i;},(e,p)->{attempts[0]++;return false;});
        check(empty.isEmpty() && created[0]==3 && attempts[0]==48,"At most three candidates and sixteen attempts each");
        attempts[0]=0;created[0]=0;
        CallOfTheWild.plan(rolls(2),i->{created[0]++;return null;},(e,p)->{attempts[0]++;return true;});
        check(created[0]==3 && attempts[0]==0,"Null or disallowed candidates are skipped without redraw");
        attempts[0]=0;
        List<Integer> last=CallOfTheWild.plan(rolls(0),i->i,(e,p)->++attempts[0]==16);
        check(last.size()==1 && attempts[0]==16,"Final allowed placement attempt succeeds");
        List<Integer> seen=new ArrayList<>();
        List<Integer> all=CallOfTheWild.plan(rolls(2),i->i,(e,p)->{seen.add(p.size());return true;});
        check(all.size()==3 && seen.equals(Arrays.asList(0,1,2)),"Placement sees previously reserved bodies");
        final boolean[] immutable={false};
        CallOfTheWild.plan(rolls(0),i->i,(e,p)->{try{p.add(99);}catch(UnsupportedOperationException expected){immutable[0]=true;}return true;});
        check(immutable[0],"Placement cannot corrupt reservations");
        List<Integer> advances=new ArrayList<>();
        check(CallOfTheWild.insert(Arrays.asList(1,2,3),e->e==2,advances::add)==1 && advances.equals(Collections.singletonList(2)),"Only accepted entity advances; partial success retained");
        advances.clear();check(CallOfTheWild.insert(Arrays.asList(1,2),e->false,advances::add)==0 && advances.isEmpty(),"All insertion failures report zero and award no advancement");
        check(CallOfTheWild.insert(Collections.emptyList(),e->true,e->{throw new AssertionError();})==0,"Empty wave has no side effects");
        RitualEffect effect=RitualEffect.parse("call_of_the_wild");
        check(effect==RitualEffect.CALL_OF_THE_WILD && !effect.consumesOnStart() && !effect.continuous(),"Finite completion-consumed effect");
        check(effect.failure(false,true)==null && effect.failure(true,false)==null,"No dimension or day restriction");
        NonNullList<Ingredient> ingredients=NonNullList.create();ingredients.add(Ingredient.of(Items.GRASS));
        RitualRecipe recipe=new RitualRecipe(new ResourceLocation("bewitchment:test_wild"),ingredients,ItemStack.EMPTY,10,780,30,new int[]{1,1,-1},effect);
        check(recipe.acceptsRun("") && !recipe.acceptsRun("call_of_the_wild"),"Paid-on-start state cannot masquerade as this finite cast");
        check(recipe.circleType(0)==1 && recipe.circleType(1)==1 && recipe.circleType(2)==-1,"Two Normal rings");
        rejected=false;try{new RitualRecipe(new ResourceLocation("bewitchment:bad_wild"),ingredients,new ItemStack(Items.DIAMOND),10,780,30,new int[]{1,1,-1},effect);}catch(JsonSyntaxException expected){rejected=true;}check(rejected,"Wild ritual rejects item output");
        RitualClock clock=new RitualClock();int[] paid={0};RitualClock.Step step=null;
        for(int i=0;i<200;i++)step=clock.advance(10,30,cost->{paid[0]+=cost;return true;});
        check(step==RitualClock.Step.FINISHED && paid[0]==300,"Ten seconds charge ten running payments");
        check(780+paid[0]==1080,"Base total power 1080");
        clock.reset();for(int i=0;i<19;i++)clock.advance(10,30,cost->true);
        check(clock.advance(10,30,cost->false)==RitualClock.Step.NO_POWER && clock.ticks()==19,"Failed payment cannot advance cast");
        System.out.println("PASS: "+checks+" Call of the Wild pool, bounded planning, insertion, recipe and payment checks.");
    }
}
