package com.bewitchment.machine;

import net.minecraft.item.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class GreaterHellmouthCheck {
    private static int checks;
    private static void check(boolean value,String message) { checks++; if(!value)throw new AssertionError(message); }
    // Bound and creature selection use the same bound, so distinguish them by call order.
    private static Random rolls(int... limits) {
        return new Random(0) {
            int call;
            @Override public int nextInt(int bound) {
                if(bound!=6)throw new AssertionError("Unexpected random bound");
                int current=call++;
                return current%2==0 ? limits[Math.min(current/2,limits.length-1)] : 3;
            }
        };
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        String[] expected={"wither_skeleton","hellhound","bafometyr","cleaver","shadow_person","ghast"};
        for(int i=0;i<6;i++)check(GreaterHellmouth.id(i).equals(expected[i]),"Original weighted slot "+i);
        for(int invalid:new int[]{-1,6}) {
            boolean rejected=false;try{GreaterHellmouth.id(invalid);}catch(IllegalArgumentException e){rejected=true;}
            check(rejected,"Reject invalid slot "+invalid);
        }
        int[] histogram=new int[7];
        for(int a=0;a<6;a++)for(int b=0;b<6;b++)for(int c=0;c<6;c++)for(int d=0;d<6;d++)for(int e=0;e<6;e++) {
            histogram[GreaterHellmouth.plan(rolls(0,a,b,c,d,e,0),i->i,(candidate,p)->true).size()]++;
        }
        check(Arrays.equals(histogram,new int[]{0,1296,2160,2160,1440,600,120}),"Exact 7776-case count distribution");
        int[] created={0},attempts={0};
        check(GreaterHellmouth.plan(rolls(5),i->{created[0]++;return i;},(e,p)->{attempts[0]++;return false;}).isEmpty()
                && created[0]==6 && attempts[0]==96,"Failed placements stop after six times sixteen attempts");
        created[0]=0;attempts[0]=0;
        GreaterHellmouth.plan(rolls(5),i->{created[0]++;return null;},(e,p)->{attempts[0]++;return true;});
        check(created[0]==6 && attempts[0]==0,"Null creations skipped without redraw");
        attempts[0]=0;
        check(GreaterHellmouth.plan(rolls(0),i->i,(e,p)->++attempts[0]%16==0).size()==1 && attempts[0]==16,"Sixteenth placement attempt accepted");
        List<Integer> reservations=new ArrayList<>();
        GreaterHellmouth.plan(rolls(5),i->i,(e,p)->{reservations.add(p.size());return true;});
        check(reservations.equals(Arrays.asList(0,1,2,3,4,5)),"Every body sees earlier reservations");
        boolean[] immutable={false};
        GreaterHellmouth.plan(rolls(0),i->i,(e,p)->{try{p.add(99);}catch(UnsupportedOperationException ex){immutable[0]=true;}return true;});
        check(immutable[0],"Reservations cannot be mutated by placement");
        List<Integer> advances=new ArrayList<>();
        check(CallOfTheWild.insert(Arrays.asList(1,2,3),e->e==2,advances::add)==1 && advances.equals(Collections.singletonList(2)),"Shared insertion awards only successful member");
        advances.clear();
        check(CallOfTheWild.insert(Arrays.asList(1,2),e->false,advances::add)==0 && advances.isEmpty(),"All rejected yields zero and no advancement");
        check(LesserHellmouth.failure(true,false).equals("hellmouth_peaceful"),"Peaceful activation rejected");
        check(LesserHellmouth.failure(false,true).equals("no_rain"),"Rain activation rejected");
        check(LesserHellmouth.failure(false,false)==null,"Dry non-Peaceful accepted");
        RitualEffect effect=RitualEffect.parse("greater_hellmouth");
        check(effect==RitualEffect.GREATER_HELLMOUTH && !effect.consumesOnStart() && !effect.conjuration() && !effect.continuous(),"Finite wave effect");
        check(effect.failure(false,true)==null && effect.failure(true,false)==null,"No day or dimension restriction");
        RitualClock clock=new RitualClock();int[] cost={0};boolean early=false;
        for(int i=0;i<299;i++)early|=clock.advance(15,66,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED;
        check(!early && cost[0]==924,"No early completion");
        check(clock.advance(15,66,c->false)==RitualClock.Step.NO_POWER,"Last failed payment blocks wave");
        check(clock.advance(15,66,c->{cost[0]+=c;return true;})==RitualClock.Step.FINISHED && cost[0]==990 && 1500+cost[0]==2490,"Fifteen paid seconds and exact total");
        RitualInventory inventory=new RitualInventory(()->{});
        ItemStack original=new ItemStack(Items.IRON_SWORD);original.setDamageValue(51);original.getOrCreateTag().putString("marker","offering");
        inventory.insertItem(0,original,false);inventory.insertItem(1,new ItemStack(Items.CHARCOAL),false);
        List<ItemStack> refund=inventory.takeAll();
        check(refund.size()==2 && refund.get(0).getDamageValue()==51 && refund.get(0).getTag().getString("marker").equals("offering"),"Original tool data retained for all-failure refund");
        check(inventory.takeAll().isEmpty(),"Settled storage cannot refund twice");
        check(RitualConjuration.fits(new net.minecraft.util.math.AxisAlignedBB(-2,64,-2,2,68,2),-3,3,-3,3),"Whole Ghast body fits open bounds");
        check(!RitualConjuration.fits(new net.minecraft.util.math.AxisAlignedBB(-2,253,-2,2,257,2),-3,3,-3,3),"Ghast head above build height rejected");
        check(!RitualConjuration.fits(new net.minecraft.util.math.AxisAlignedBB(-2,64,-2,2,68,2),-1,3,-3,3),"Center inside border does not excuse body crossing it");
        net.minecraft.util.NonNullList<net.minecraft.item.crafting.Ingredient> ingredients=net.minecraft.util.NonNullList.create();
        for(Item item:new Item[]{Items.IRON_SWORD,Items.WHEAT,Items.POTION,Items.BLAZE_ROD,Items.BLAZE_ROD,Items.GUNPOWDER,Items.GUNPOWDER})ingredients.add(net.minecraft.item.crafting.Ingredient.of(item));
        RitualRecipe recipe=new RitualRecipe(new net.minecraft.util.ResourceLocation("bewitchment:greater_test"),ingredients,new ItemStack(Items.IRON_SWORD),15,1500,66,new int[]{2,2,2},effect);
        net.minecraft.inventory.Inventory inputs=new net.minecraft.inventory.Inventory(10);
        Item[] items={Items.IRON_SWORD,Items.WHEAT,Items.POTION,Items.BLAZE_ROD,Items.BLAZE_ROD,Items.GUNPOWDER,Items.GUNPOWDER};
        for(int i=0;i<items.length;i++)inputs.setItem(i,new ItemStack(items[i]));
        inputs.getItem(0).setDamageValue(40);inputs.getItem(0).getOrCreateTag().putString("marker","original");
        check(recipe.matches(inputs,null),"Seven offerings with repeated rods/powder match");
        inputs.setItem(6,ItemStack.EMPTY);check(!recipe.matches(inputs,null),"Missing second powder cannot match");inputs.setItem(6,new ItemStack(Items.GUNPOWDER));
        ItemStack output=recipe.assemble(inputs);
        check(output.getDamageValue()==0 && !output.getOrCreateTag().contains("marker"),"Successful output is a fresh tool");
        check(recipe.acceptsRun("") && !recipe.acceptsRun("greater_hellmouth"),"Finite cast cannot resume an already-consumed state");
        net.minecraft.network.PacketBuffer packet=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
        try {
            RitualRecipe.Serializer serializer=new RitualRecipe.Serializer();serializer.toNetwork(packet,recipe);RitualRecipe copy=serializer.fromNetwork(recipe.getId(),packet);
            check(copy.effect==effect && copy.circleType(0)==2 && copy.circleType(1)==2 && copy.circleType(2)==2,"Three Nether rings survive networking");
            check(copy.seconds==15 && copy.startingPower==1500 && copy.runningPower==66 && copy.getIngredients().size()==7 && !packet.isReadable(),"Costs, duplicate offerings and duration consume exact packet");
        } finally {packet.release();}
        System.out.println("PASS: "+checks+" Greater Hellmouth pool, exact count distribution, bounded placement, insertion, refunds, recipe and payment checks.");
    }
}
