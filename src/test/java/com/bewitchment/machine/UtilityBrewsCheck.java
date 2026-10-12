package com.bewitchment.machine;
import com.bewitchment.potion.*;
import com.bewitchment.potion.UtilityBrewEffect.Kind;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class UtilityBrewsCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    private static final class Slot implements BrewEquipment.Slot {
        ItemStack stack;
        Slot(ItemStack stack){this.stack=stack;}
        public ItemStack get(){return stack;}
        public void set(ItemStack stack){this.stack=stack;}
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(Kind kind:Kind.values()){
            UtilityBrewEffect effect=new UtilityBrewEffect(kind);
            check(effect.isInstantenous()&&effect.isDurationEffectTick(1,0)&&!effect.isDurationEffectTick(0,0),"Instant potion dispatch metadata");
            check(effect.isBeneficial()==(kind==Kind.ABSENCE||kind==Kind.PURIFICATION),"Original beneficial classification");
            for(EffectType type:EffectType.values()){
                boolean expected=kind==Kind.ABSENCE||(kind==Kind.PURIFICATION&&type==EffectType.HARMFUL)||(kind==Kind.CORRUPTION&&type!=EffectType.HARMFUL);
                check(kind.removes(type)==expected,"Removal matrix including neutral effects");
            }
        }
        check(UtilityBrewEffect.sleepDuration(0)==200&&UtilityBrewEffect.sleepDuration(2)==600,"Ten seconds per sleep level");
        for(int amp:new int[]{0,1,2,255}){
            float threshold=2F*(amp+1);
            check(UtilityBrewEffect.lethalSleep(threshold,amp)&&!UtilityBrewEffect.lethalSleep(threshold+.1F,amp),"Sleep execution threshold is inclusive");
            check(!UtilityBrewEffect.lethalSleep(0,amp)&&!UtilityBrewEffect.lethalSleep(Float.NaN,amp),"Dead and invalid targets not executed");
            check(UtilityBrewEffect.leap(amp)==1+(amp+1)/4D,"Original additive leap velocity");
        }
        check(UtilityBrewEffect.level(-1)==1&&UtilityBrewEffect.level(Integer.MAX_VALUE)==256,"Bounded command amplifier work");
        for(int count:new int[]{1,16,64})for(int requested:new int[]{0,1,Integer.MAX_VALUE})for(boolean accepted:new boolean[]{false,true}){
            ItemStack original=new ItemStack(Items.PAPER,count);original.getOrCreateTag().putString("marker","retained");
            Slot slot=new Slot(original.copy());List<ItemStack> drops=new ArrayList<>();
            boolean result=BrewEquipment.drop(slot,requested,stack->{check(slot.get().getCount()==count-Math.min(count,requested),"Removal precedes publication");drops.add(stack.copy());return accepted;});
            int removed=requested==0?0:Math.min(count,requested);
            check(result==(requested>0&&accepted),"Commit requires nonempty accepted drop");
            check(slot.get().getCount()==count-(accepted?removed:0),"Rejection restores original count");
            check(requested==0?drops.isEmpty():drops.size()==1&&drops.get(0).getCount()==removed&&drops.get(0).getTag().getString("marker").equals("retained"),"Drop count and metadata preserved");
            check(original.getCount()==count,"Caller snapshot is not mutated");
        }
        Slot empty=new Slot(ItemStack.EMPTY);check(!BrewEquipment.drop(empty,1,s->{throw new AssertionError("Empty publication");}),"Empty slot is a harmless failed draw");
        Slot rollback=new Slot(new ItemStack(Items.DIAMOND,3));boolean threw=false;
        try{BrewEquipment.drop(rollback,1,s->{throw new IllegalStateException("rejected");});}catch(IllegalStateException ex){threw=true;}
        check(threw&&rollback.get().getCount()==3,"Exceptional publisher restores inventory");
        Slot hand=new Slot(new ItemStack(Items.PAPER,12));check(BrewEquipment.drop(hand,Integer.MAX_VALUE,s->s.getCount()==12)&&hand.get().isEmpty(),"Butterfingers drops the whole stack");
        check(!BrewEquipment.drop(hand,1,s->{throw new AssertionError("Duplicate drop");}),"Repeated empty drop cannot duplicate");
        ItemStack potion=new ItemStack(Items.POTION);
        check(BrewEquipment.consumingPotion(true,potion,potion),"Potion being drunk cannot be duplicated");
        check(!BrewEquipment.consumingPotion(false,potion,potion)&&!BrewEquipment.consumingPotion(true,potion,potion.copy()),"Inactive or opposite-hand potion is droppable");
        ItemStack food=new ItemStack(Items.APPLE);check(!BrewEquipment.consumingPotion(true,food,food),"Guard is limited to potion consumption");
        System.out.println("PASS: "+checks+" utility brew filtering, thresholds, velocity, drop rollback and consumption guards");
    }
}
