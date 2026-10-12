package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Bootstrap;

public final class RitualInhibitionCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static CompoundNBT state(CompoundNBT data){return data.getCompound(RitualInhibition.KEY);}
    public static void main(String[] args){
        Bootstrap.bootStrap();CompoundNBT data=new CompoundNBT();data.putString("other_mod","preserve");
        check(!RitualInhibition.blocked(state(data)),"New player can cast");
        RitualInhibition.advance(data);check(data.size()==1,"Idle tick creates no state");
        RitualInhibition.reset(data);check(RitualInhibition.remaining(state(data))==24000,"Full legacy duration");
        check(RitualInhibition.blocked(state(data)),"Immediate inhibition");
        RitualInhibition.advance(data);check(RitualInhibition.remaining(state(data))==23999,"One player tick decrements once");
        for(int i=0;i<23998;i++)RitualInhibition.advance(data);
        check(RitualInhibition.remaining(state(data))==1 && RitualInhibition.blocked(state(data)),"Still blocked at final countdown tick");
        RitualInhibition.advance(data);check(RitualInhibition.remaining(state(data))==0 && RitualInhibition.blocked(state(data)),"Legacy zero-count boundary still blocked");
        CompoundNBT saved=data.copy();check(RitualInhibition.blocked(state(saved)),"Save preserves boundary flag");
        RitualInhibition.advance(data);check(!RitualInhibition.blocked(state(data)) && !data.contains(RitualInhibition.KEY),"Next tick releases and removes state");
        check(data.getString("other_mod").equals("preserve"),"Expiry preserves unrelated data");
        RitualInhibition.reset(data);RitualInhibition.advance(data);RitualInhibition.reset(data);
        check(RitualInhibition.remaining(state(data))==24000,"Reapplication resets rather than adds duration");
        CompoundNBT clone=new CompoundNBT();clone.putInt("another_mod",9);RitualInhibition.copy(data,clone);
        check(RitualInhibition.remaining(state(clone))==24000 && RitualInhibition.blocked(state(clone)),"Clone retains countdown and lockout");
        RitualInhibition.advance(clone);check(RitualInhibition.remaining(state(data))==24000,"Clone state is independent");
        check(clone.getInt("another_mod")==9 && !clone.contains("other_mod"),"Copy only this feature's data");
        RitualInhibition.copy(new CompoundNBT(),clone);check(!clone.contains(RitualInhibition.KEY),"Uninhibited clone clears stale state");
        state(data).putInt("remaining",Integer.MAX_VALUE);check(RitualInhibition.remaining(state(data))==24000,"Oversized persisted timer bounded");
        RitualInhibition.advance(data);check(RitualInhibition.remaining(state(data))==23999,"Tick repairs oversized timer");
        state(data).putInt("remaining",-1);RitualInhibition.advance(data);check(!data.contains(RitualInhibition.KEY),"Negative timer expires safely");
        data.putString(RitualInhibition.KEY,"invalid");RitualInhibition.advance(data);check(!data.contains(RitualInhibition.KEY),"Wrong NBT type is removed safely");
        CompoundNBT incomplete=new CompoundNBT();incomplete.putInt("remaining",5);check(RitualInhibition.blocked(incomplete),"Positive timer blocks even without boolean");
        check(!Kind.FAILURE.positive && Kind.FAILURE.cooldown==600,"Legacy targeting and cooldown");
        check(Kind.fromId("bewitchment:sigil_failure")==Kind.FAILURE,"Persistent sigil identity");
        check(Kind.FAILURE.effects().isEmpty(),"Failure is player state, not a cleansing-removable potion");
        System.out.println("PASS: "+checks+" ritual inhibition checks");
    }
}
