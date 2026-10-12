package com.bewitchment.block;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.JuniperKeyItem;
import com.bewitchment.item.SkeletonKeyItem;
import com.bewitchment.api.event.LockCheckEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID)
public final class JuniperLocks {
    public static boolean lock(BlockState state){return state.getBlock() instanceof JuniperChestBlock || state.getBlock() instanceof JuniperDoor || state.getBlock() instanceof JuniperGate || state.getBlock() instanceof JuniperTrapdoor;}
    public static BlockPos base(IBlockReader world,BlockPos pos){return base(world.getBlockState(pos),pos);}
    public static BlockPos base(BlockState state,BlockPos pos){return state.getBlock() instanceof JuniperDoor && state.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?pos.below():pos;}
    public static boolean access(PlayerEntity player,IBlockReader world,BlockPos pos,boolean message){
        BlockPos target=base(world,pos);
        boolean simulation=!message || player.level.isClientSide;
        boolean ordinary=MinecraftForge.EVENT_BUS.post(new LockCheckEvent(player,target,simulation));
        for(int i=0;!ordinary && i<player.inventory.getContainerSize();i++){
            ItemStack stack=player.inventory.getItem(i);
            if(stack.isEmpty())continue;
            ordinary=JuniperKeyItem.opens(stack,player.level.dimension().location(),target)
                || MinecraftForge.EVENT_BUS.post(new LockCheckEvent.KeyCheckEvent(player,target,simulation,stack));
        }
        ItemStack skeleton=ordinary?ItemStack.EMPTY:SkeletonKeyItem.carried(player);
        int cost=SkeletonKeyItem.cost(world.getBlockState(target).getBlock());
        boolean bypass=!ordinary && SkeletonKeyItem.canPay(player.experienceLevel,skeleton,SkeletonKeyItem.powerSource(player,cost),cost);
        LockCheckEvent.LockCheckedEvent checked=new LockCheckEvent.LockCheckedEvent(player,target,simulation,ordinary||bypass,message);
        boolean canceled=MinecraftForge.EVENT_BUS.post(checked);
        boolean opened=resolve(checked,canceled,bypass,()->SkeletonKeyItem.pay(player,skeleton,cost));
        if(!opened && checked.shouldSendMessage() && !simulation){
            String reason="message.bewitchment.juniper_locked";
            if(!skeleton.isEmpty() && !canceled && !bypass)reason=player.experienceLevel<SkeletonKeyItem.LEVEL_COST?"message.bewitchment.skeleton_key_xp":"message.bewitchment.skeleton_key_power";
            player.displayClientMessage(new TranslationTextComponent(reason,cost),true);
        }
        return opened;
    }
    /** Payment is deferred until every post-check listener has returned. */
    public static boolean resolve(LockCheckEvent.LockCheckedEvent event,boolean canceled,boolean bypass,java.util.function.BooleanSupplier payment){
        return !canceled && event.isOpened() && (!bypass || event.isSimulation() || payment.getAsBoolean());
    }
    public static void placed(World world,BlockPos pos,LivingEntity entity){
        if(world.isClientSide || !(entity instanceof PlayerEntity))return;
        PlayerEntity player=(PlayerEntity)entity;ItemStack key=new ItemStack(ModItems.JUNIPER_KEY.get());key.setTag(JuniperKeyItem.binding(world.dimension().location(),base(world,pos)));
        if(!player.inventory.add(key))player.drop(key,false);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent event){
        if(!event.getPlayer().isCreative() && lock(event.getState()) && !access(event.getPlayer(),event.getWorld(),event.getPos(),true))event.setCanceled(true);
    }
    private JuniperLocks(){}
}
