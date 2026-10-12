package com.bewitchment.item;

import com.bewitchment.machine.*;
import com.bewitchment.combat.PlayerCurses;
import com.bewitchment.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import java.util.List;
import javax.annotation.Nullable;

public final class KillContractItem extends Item {
    public KillContractItem(){super(new Properties().tab(ModItems.TAB).stacksTo(1));}
    public static KillContractData data(ItemStack stack){return stack.getItem() instanceof KillContractItem && stack.getCount()==1?KillContractData.read(stack.getTag()):null;}
    public static DonationContractData donationData(ItemStack stack){return stack.getItem() instanceof KillContractItem && stack.getCount()==1?DonationContractData.read(stack.getTag()):null;}
    public static java.util.UUID targetId(ItemStack stack){return stack.getItem() instanceof TaglockItem && stack.hasTag()?ChestSigilData.parse(stack.getTag().getString("boundId")):null;}
    public static boolean signed(ItemStack stack){KillContractData kill=data(stack);DonationContractData donation=donationData(stack);return kill!=null && kill.owner!=null || donation!=null && donation.owner!=null;}
    public static ItemStack donationTask(Item item,CurseData.Kind kind,int... amounts){ItemStack stack=new ItemStack(item);stack.setTag(DonationContractData.create(kind,amounts));return stack;}
    public static ItemStack task(Item item,CurseData.Kind kind,int total){ItemStack stack=new ItemStack(item);stack.setTag(KillContractData.create(kind,total));return stack;}
    @Override public void fillItemCategory(ItemGroup group,NonNullList<ItemStack> items){if(allowdedIn(group))for(CurseData.Kind kind:ContractTasks.kinds())items.add(ContractTasks.creative(this,kind));}
    @Override public boolean isFoil(ItemStack stack){KillContractData data=data(stack);DonationContractData donation=donationData(stack);return data!=null && data.complete() || donation!=null && donation.complete();}
    @Override public boolean isEnchantable(ItemStack stack){return false;}
    @Override public boolean isBookEnchantable(ItemStack stack,ItemStack book){return false;}
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack,net.minecraft.enchantment.Enchantment enchantment){return false;}
    @Override public ActionResult<ItemStack> use(World world,PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);KillContractData data=data(held);
        DonationContractData donation=donationData(held);
        if(data==null && donation==null || player.isSpectator() || !player.isAlive())return ActionResult.fail(held);
        if(world.isClientSide)return ActionResult.success(held);
        String message;
        java.util.UUID owner=data!=null?data.owner:donation.owner;
        if(owner==null){boolean signed=data!=null?KillContractData.sign(held.getTag(),player.getUUID(),player.getName().getString()):DonationContractData.sign(held.getTag(),player.getUUID(),player.getName().getString());message=signed?(donation!=null?"donation_signed":"signed"):"invalid";}
        else if(!player.getUUID().equals(owner))message="owner";
        else if(!(data!=null?data.complete():donation.complete()))message="incomplete";
        else {
            PlayerEntity recipient=player;
            boolean negative=donation!=null && !donation.kind.positive();
            if(negative){
                ItemStack taglock=player.getItemInHand(hand==Hand.MAIN_HAND?Hand.OFF_HAND:Hand.MAIN_HAND);
                java.util.UUID id=targetId(taglock);
                recipient=id==null?null:world.getServer().getPlayerList().getPlayer(id);
            }
            if(recipient==null)message="target_required";
            else {
                CurseData effects=PlayerCurses.read(recipient);
                if(data!=null?data.redeem(player.getUUID(),effects):donation.redeem(player.getUUID(),effects)){PlayerCurses.write(recipient,effects);held.shrink(1);message=negative?"queued":"redeemed";}
                else message="duplicate";
            }
        }
        player.inventory.setChanged();player.displayClientMessage(new TranslationTextComponent("contract.bewitchment."+message),true);
        if(message.equals("signed") || message.equals("donation_signed") || message.equals("redeemed") || message.equals("queued"))world.playSound(null,player.blockPosition(),SoundEvents.EXPERIENCE_ORB_PICKUP,SoundCategory.PLAYERS,1,1);
        return ActionResult.consume(held);
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable World world,List<ITextComponent> lines,ITooltipFlag flag){
        KillContractData data=data(stack);DonationContractData donation=donationData(stack);
        if(donation!=null){
            lines.add(new TranslationTextComponent("contract.bewitchment."+donation.kind.id.substring("bewitchment:".length())));
            lines.add(new TranslationTextComponent(donation.owner==null?"contract.bewitchment.unsigned":"contract.bewitchment.bound",donation.name));
            for(int i=0;i<DonationContractData.size(donation.kind);i++)lines.add(new TranslationTextComponent("contract.bewitchment.donation_progress",donation.done(i),donation.total(i),DonationContractData.ingredient(donation.kind,i).getDescription()));return;
        }
        if(data==null){lines.add(new TranslationTextComponent("contract.bewitchment.invalid"));return;}
        lines.add(new TranslationTextComponent("contract.bewitchment."+(data.kind==CurseData.Kind.BERSERKER?"berserker":"devouring_rage")));
        lines.add(new TranslationTextComponent(data.owner==null?"contract.bewitchment.unsigned":"contract.bewitchment.bound",data.name));
        lines.add(new TranslationTextComponent("contract.bewitchment.progress",data.done,data.total));
    }
}
