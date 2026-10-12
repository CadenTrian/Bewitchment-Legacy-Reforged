package com.bewitchment.machine;

import com.bewitchment.block.*;
import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import java.util.UUID;

public final class DragonsBloodChestCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
        for(Kind kind:Kind.values()){
            ChestSigilData data=new ChestSigilData();
            check(data.install(kind,owner) && !data.install(Kind.LUCK,other),"Only one sigil: "+kind);
            check(data.targets(owner)==kind.positive && data.targets(other)!=kind.positive,"Initial targeting: "+kind);
            UUID affected=kind.positive?owner:other;
            check(data.trigger(affected) && data.cooldown()==kind.cooldown,"Cooldown starts on affected opening: "+kind);
            check(data.trigger(affected)==(kind.cooldown==0),"Repeat trigger cooldown: "+kind);
            ChestSigilData loaded=new ChestSigilData();loaded.load(data.save());
            check(loaded.kind()==kind && loaded.cooldown()==kind.cooldown && loaded.targets(owner)==kind.positive,"State round trip: "+kind);
        }
        ChestSigilData data=new ChestSigilData();
        check(!data.trigger(owner) && !data.toggle(owner.toString()),"Empty chest has no sigil behavior");
        data.install(Kind.MENDING,owner);
        check(!data.trigger(other) && data.cooldown()==0,"Unaffected visitor does not consume cooldown");
        check(data.toggle(other.toString()) && data.targets(other),"Taglock adds identity");
        check(data.toggle(other.toString()) && !data.targets(other),"Taglock removes identity");
        check(!data.toggle("not a UUID") && !data.toggle("1-1-1-1-1") && !data.toggle(null),"Malformed identities rejected");
        data.trigger(owner);for(int i=0;i<599;i++)data.tick();check(data.cooldown()==1 && !data.trigger(owner),"Cooldown boundary before expiry");
        data.tick();check(data.trigger(owner),"Trigger allowed exactly at expiry");
        CompoundNBT state=data.save();state.putInt("cooldown",Integer.MAX_VALUE);data.load(state);check(data.cooldown()==600,"Oversized cooldown clamped");
        state.putInt("cooldown",-1);data.load(state);check(data.cooldown()==0,"Negative cooldown clamped");
        ChestSigilData replacement=new ChestSigilData();replacement.install(Kind.MENDING,other);data.load(replacement.save());
        check(!data.targets(owner) && data.targets(other),"Reload replaces rather than accumulates identities");
        state.putString("sigil","bewitchment:sigil_missing");data.load(state);check(data.kind()==null && data.cooldown()==0 && !data.targets(owner),"Unknown sigil safely ignored");
        check(Kind.MENDING.effects().get(0).getEffect()==Effects.REGENERATION && Kind.MENDING.effects().get(0).getDuration()==100,"Mending effect");
        check(Kind.BINDING.effects().get(0).getAmplifier()==5 && Kind.BINDING.effects().get(0).getDuration()==300,"Binding Slowness VI");
        check(Kind.LUCK.effects().get(0).getEffect()==Effects.LUCK && Kind.LUCK.effects().get(0).getDuration()==6000,"Luck duration");
        check(Kind.SHRIEKING.effects().get(0).getEffect()==Effects.CONFUSION && Kind.SHRIEKING.effects().get(0).getDuration()==100,"Shrieking nausea");
        check(Kind.MENDING.effects().get(0)!=Kind.MENDING.effects().get(0),"Fresh effect instances per trigger");
        DragonsBloodChestBlock block=new DragonsBloodChestBlock();
        check(!JuniperLocks.lock(block.defaultBlockState()),"Dragon chest is not a Juniper lock");
        check(block.rotate(block.defaultBlockState(),Rotation.CLOCKWISE_90).getValue(DragonsBloodChestBlock.FACING)==Direction.EAST,"Chest rotates");
        DragonsBloodChestTile chest=new DragonsBloodChestTile(TileEntityType.CHEST);
        check(chest.getContainerSize()==27,"All 27 slots accessible");
        chest.setItem(26,new ItemStack(Items.DIAMOND,3));chest.setCustomName(new StringTextComponent("Dragon hoard"));chest.sigil.install(Kind.BINDING,owner);chest.sigil.trigger(other);
        DragonsBloodChestTile loaded=new DragonsBloodChestTile(TileEntityType.CHEST);loaded.load(block.defaultBlockState(),chest.save(new CompoundNBT()));
        check(loaded.getItem(26).getCount()==3 && loaded.getItem(26).getItem()==Items.DIAMOND,"Last slot persists");
        check(loaded.getDisplayName().getString().equals("Dragon hoard"),"Custom name persists");
        check(loaded.sigil.kind()==Kind.BINDING && loaded.sigil.cooldown()==600 && !loaded.sigil.targets(owner),"Sigil persists alongside inventory");
        check(!chest.getUpdateTag().contains("ChestSigil") && !chest.getUpdateTag().contains("Items"),"Chunk updates omit inventory and player list");
        InvWrapper automation=new InvWrapper(loaded);
        check(automation.insertItem(0,new ItemStack(Items.GOLD_INGOT,4),false).isEmpty() && loaded.getItem(0).getCount()==4,"Automation insertion");
        check(automation.extractItem(26,2,false).getCount()==2 && loaded.getItem(26).getCount()==1,"Automation extraction");
        System.out.println("PASS: "+checks+" Dragon Blood Chest and sigil checks");
    }
}
