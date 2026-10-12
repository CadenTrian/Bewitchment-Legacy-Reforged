package com.bewitchment.machine;

import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class SpiritualRiftCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random rolls(int limit,int slot){return new Random(0){int call;@Override public int nextInt(int bound){if(bound!=12)throw new AssertionError("Random bound");return call++%2==0?limit:slot;}};}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        String[] expected={"vex","black_dog","ghost","druden","skeleton","husk","stray","shadow_person","enderman","endermite","cambion","zombie"};
        for(int i=0;i<12;i++)check(SpiritualRift.id(i).equals(expected[i]) && SpiritualRift.plan(rolls(0,i),n->n,(e,p)->true).equals(Arrays.asList(i,i)),"Original slot selectable "+i);
        for(int invalid:new int[]{-1,12}){boolean rejected=false;try{SpiritualRift.id(invalid);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"Invalid slot rejected");}
        check(SpiritualRift.plan(rolls(11,0),n->n,(e,p)->true).size()==13,"Maximum thirteen candidates");
        check(SpiritualRift.plan(new Random(0){int call;@Override public int nextInt(int bound){return call++<4?11:0;}},n->n,(e,p)->true).size()==2,"Loop bound rerolled after each candidate");
        int[] created={0},attempts={0};
        check(SpiritualRift.plan(rolls(11,0),n->{created[0]++;return n;},(e,p)->{attempts[0]++;return false;}).isEmpty() && created[0]==13 && attempts[0]==208,"Failed placement bounded to 208 attempts");
        created[0]=0;attempts[0]=0;
        SpiritualRift.plan(rolls(11,0),n->{created[0]++;return null;},(e,p)->{attempts[0]++;return true;});
        check(created[0]==13 && attempts[0]==0,"Null creation skipped without placement");
        attempts[0]=0;check(SpiritualRift.plan(rolls(0,0),n->n,(e,p)->++attempts[0]%16==0).size()==2 && attempts[0]==32,"Final placement attempt accepted");
        List<Integer> reservations=new ArrayList<>();SpiritualRift.plan(rolls(0,0),n->n,(e,p)->{reservations.add(p.size());return true;});
        check(reservations.equals(Arrays.asList(0,1)),"Earlier bodies reserved");
        boolean[] immutable={false};SpiritualRift.plan(rolls(0,0),n->n,(e,p)->{try{p.add(99);}catch(UnsupportedOperationException ex){immutable[0]=true;}return true;});
        check(immutable[0],"Placement cannot mutate reservations");
        List<Integer> awards=new ArrayList<>();check(CallOfTheWild.insert(Arrays.asList(1,2,3),n->n==2,awards::add)==1 && awards.equals(Arrays.asList(2)),"Only inserted creatures award advancement");
        awards.clear();check(CallOfTheWild.insert(Arrays.asList(1,2),n->false,awards::add)==0 && awards.isEmpty(),"Total insertion failure is detectable");
        RitualEffect effect=RitualEffect.parse("spiritual_rift");
        check(effect==RitualEffect.SPIRITUAL_RIFT && !effect.continuous() && !effect.consumesOnStart() && !effect.conjuration(),"Finite wave classification");
        check("rift_night".equals(effect.failure(true,true)) && "rift_night".equals(effect.failure(false,true)),"Daytime activation rejected");
        check(effect.failure(true,false)==null && effect.failure(false,false)==null,"Night accepted without dimension restriction");
        RitualClock clock=new RitualClock();int[] paid={0};boolean early=false;
        for(int i=0;i<299;i++)early|=clock.advance(15,66,c->{paid[0]+=c;return true;})==RitualClock.Step.FINISHED;
        check(!early && paid[0]==924,"No early wave");
        check(clock.advance(15,66,c->false)==RitualClock.Step.NO_POWER,"Final payment required");
        check(clock.advance(15,66,c->{paid[0]+=c;return true;})==RitualClock.Step.FINISHED && paid[0]+1250==2240,"Exact total power");
        NonNullList<Ingredient> ingredients=NonNullList.create();Item[] items={Items.WHEAT,Items.WHEAT,Items.SLIME_BALL,Items.SLIME_BALL,Items.GLOWSTONE_DUST,Items.ENDER_PEARL};
        for(Item item:items)ingredients.add(Ingredient.of(item));
        RitualRecipe recipe=new RitualRecipe(new ResourceLocation("bewitchment:rift_test"),ingredients,ItemStack.EMPTY,15,1250,66,new int[]{3,3,-1},effect);
        net.minecraft.inventory.Inventory inputs=new net.minecraft.inventory.Inventory(10);for(int i=0;i<items.length;i++)inputs.setItem(i,new ItemStack(items[i]));
        check(recipe.matches(inputs,null),"Six offerings with duplicates match");
        inputs.setItem(1,ItemStack.EMPTY);check(!recipe.matches(inputs,null),"Second wormwood equivalent required");inputs.setItem(1,new ItemStack(Items.WHEAT));
        inputs.setItem(3,ItemStack.EMPTY);check(!recipe.matches(inputs,null),"Second ectoplasm equivalent required");
        check(recipe.getResultItem().isEmpty() && recipe.assemble(inputs).isEmpty(),"No item output");
        boolean rejected=false;try{new RitualRecipe(recipe.getId(),ingredients,new ItemStack(Items.DIAMOND),15,1250,66,new int[]{3,3,-1},effect);}catch(com.google.gson.JsonSyntaxException ex){rejected=true;}check(rejected,"Reject invented item output");
        check(recipe.acceptsRun("") && !recipe.acceptsRun("spiritual_rift"),"Finite consumed-state replay rejected");
        net.minecraft.network.PacketBuffer buffer=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
        try{RitualRecipe.Serializer serializer=new RitualRecipe.Serializer();serializer.toNetwork(buffer,recipe);RitualRecipe copy=serializer.fromNetwork(recipe.getId(),buffer);
            check(copy.effect==effect && copy.circleType(0)==3 && copy.circleType(1)==3 && copy.circleType(2)==-1,"Two Ender rings survive networking");
            check(copy.seconds==15 && copy.startingPower==1250 && copy.runningPower==66 && copy.getIngredients().size()==6 && copy.getResultItem().isEmpty() && !buffer.isReadable(),"Complete recipe packet round trip");
        }finally{buffer.release();}
        RitualInventory storage=new RitualInventory(()->{});ItemStack original=new ItemStack(Items.ENDER_PEARL);original.getOrCreateTag().putString("marker","original");storage.insertItem(0,original,false);
        List<ItemStack> refunded=storage.takeAll();check(refunded.size()==1 && refunded.get(0).getTag().getString("marker").equals("original"),"Refund preserves offering data");
        check(storage.takeAll().isEmpty(),"Cannot refund twice");
        System.out.println("PASS: "+checks+" Spiritual Rift pool, bounds, settlement, recipe, network and payment checks.");
    }
}
