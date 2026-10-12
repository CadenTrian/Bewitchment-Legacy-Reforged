package com.bewitchment.item;

import com.bewitchment.api.event.LockCheckEvent;
import com.bewitchment.block.*;
import com.bewitchment.config.ToolConfig;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.concurrent.atomic.AtomicInteger;

public final class SkeletonKeyCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        SkeletonKeyItem item=new SkeletonKeyItem();ItemStack key=new ItemStack(item);
        check(key.getMaxStackSize()==1,"Keys do not stack");
        check(key.getMaxDamage()==10,"Legacy default durability");
        check(ToolConfig.SKELETON_KEY_USES.get()==10,"Default config");
        check(!item.isEnchantable(key),"Table disabled");
        check(!item.isBookEnchantable(key,new ItemStack(Items.ENCHANTED_BOOK)),"Books disabled");
        check(!item.isValidRepairItem(key,key),"Repair disabled");
        check(SkeletonKeyItem.cost(new JuniperGate())==250,"Gate cost");
        check(SkeletonKeyItem.cost(new JuniperDoor())==500,"Door cost");
        check(SkeletonKeyItem.cost(new JuniperTrapdoor())==750,"Trapdoor cost");
        check(SkeletonKeyItem.cost(Blocks.CHEST)==1000,"Reserved container cost");
        ItemStack book=new ItemStack(new GrimoireMagiaItem());GrimoireMagiaItem.setPower(book,750);
        check(SkeletonKeyItem.canPay(10,key,book,750),"Exact threshold accepted");
        check(!SkeletonKeyItem.canPay(9,key,book,750),"Ten levels required");
        check(!SkeletonKeyItem.canPay(10,ItemStack.EMPTY,book,750),"Key required");
        check(!SkeletonKeyItem.canPay(10,new ItemStack(Items.STICK),book,750),"Wrong item refused");
        check(!SkeletonKeyItem.canPay(10,key,book,751),"Insufficient book refused");
        check(!SkeletonKeyItem.canPower(book,0),"Invalid cost refused");
        ItemStack fake=new ItemStack(Items.BOOK);fake.setTag(book.getTag().copy());
        check(!SkeletonKeyItem.canPower(fake,250),"Copied power NBT on ordinary book refused");
        check(GrimoireMagiaItem.power(book)==750 && key.getDamageValue()==0,"Affordability queries preserve items");
        check(!GrimoireMagiaItem.drain(book,751) && GrimoireMagiaItem.power(book)==750,"Failed drain leaves power intact");
        check(GrimoireMagiaItem.drain(book,750) && GrimoireMagiaItem.power(book)==0,"Exact drain exhausts book");
        AtomicInteger payments=new AtomicInteger();
        LockCheckEvent.LockCheckedEvent event=new LockCheckEvent.LockCheckedEvent(null,BlockPos.ZERO,true,true,false);
        for(int i=0;i<100;i++)if(!JuniperLocks.resolve(event,false,true,()->{payments.incrementAndGet();return true;}))throw new AssertionError("Prediction denied");
        check(payments.get()==0,"Repeated mining prediction never pays");
        event=new LockCheckEvent.LockCheckedEvent(null,BlockPos.ZERO,false,true,true);
        check(JuniperLocks.resolve(event,false,false,()->{payments.incrementAndGet();return true;}) && payments.get()==0,"Ordinary key or integration grant is free");
        check(!JuniperLocks.resolve(event,true,true,()->{payments.incrementAndGet();return true;}) && payments.get()==0,"Canceled final event never pays");
        event.setResult(false);
        check(!JuniperLocks.resolve(event,false,true,()->{payments.incrementAndGet();return true;}) && payments.get()==0,"Final denial never pays");
        event.setResult(true);
        check(JuniperLocks.resolve(event,false,true,()->{payments.incrementAndGet();return true;}) && payments.get()==1,"Accepted bypass pays once");
        check(!JuniperLocks.resolve(event,false,true,()->false),"Failed final payment refuses access");
        event.setSendMessage(false);check(!event.shouldSendMessage(),"Integration can suppress feedback");
        LockCheckEvent.KeyCheckEvent keyEvent=new LockCheckEvent.KeyCheckEvent(null,BlockPos.ZERO,true,key);
        keyEvent.getKey().shrink(1);check(key.getCount()==1 && keyEvent.getKey().getCount()==1,"Event key snapshots protect inventory");
        // Standalone checks do not run Forge's event class transformer.
        check(LockCheckEvent.KeyCheckEvent.class.isAnnotationPresent(net.minecraftforge.eventbus.api.Cancelable.class)
            && LockCheckEvent.LockCheckedEvent.class.isAnnotationPresent(net.minecraftforge.eventbus.api.Cancelable.class),"Integration events declare cancellation");
        System.out.println("PASS: "+checks+" Skeleton Key checks");
    }
}
