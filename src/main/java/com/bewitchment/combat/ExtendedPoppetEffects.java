package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.item.PoppetItem;
import com.bewitchment.item.PoppetItem.Kind;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;
import java.util.Map;
import java.util.EnumMap;
import java.util.WeakHashMap;
import net.minecraft.util.Hand;
import java.util.function.BooleanSupplier;

/** Legacy tool rescue and carried damage transfer, with explicit reentry protection. */
@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class ExtendedPoppetEffects {
    private static final ThreadLocal<Boolean> REDIRECTING = ThreadLocal.withInitial(() -> false);
    private static final Map<PlayerEntity, Map<Hand, ItemStack>> BROKEN = new WeakHashMap<>();

    public static boolean transfer(float amount, BooleanSupplier damageTarget) {
        if (!AccessoryRules.validDamage(amount) || REDIRECTING.get()) return false;
        REDIRECTING.set(true);
        try { return damageTarget.getAsBoolean(); }
        finally { REDIRECTING.remove(); }
    }

    public static ItemStack rescuedTool(ItemStack original) {
        if (original.isEmpty() || !original.isDamageableItem() || original.getCount() != 1
                || original.getItem() instanceof PoppetItem) return ItemStack.EMPTY;
        ItemStack copy = original.copy();
        // Forge supplies the pre-break snapshot. Keep it usable even if another hook supplied max damage.
        copy.setDamageValue(Math.min(copy.getDamageValue(), copy.getMaxDamage() - 1));
        return copy;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void brokenTool(PlayerDestroyItemEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide || event.getHand() == null) return;
        ItemStack original = event.getOriginal(), restored = rescuedTool(original);
        if (restored.isEmpty()) return;
        // Preserve the original nearby-frame exclusion without loading chunks.
        for (ItemFrameEntity frame : player.level.getEntitiesOfClass(ItemFrameEntity.class,
                new net.minecraft.util.math.AxisAlignedBB(player.getX()-5,player.getY()-5,player.getZ()-5,
                        player.getX()+5,player.getY()+5,player.getZ()+5))) {
            if (ItemStack.matches(frame.getItem(), original)) return;
        }
        // Several vanilla callers clear the hand AFTER this event. Restore only at tick end.
        BROKEN.computeIfAbsent(player, key -> new EnumMap<>(Hand.class)).putIfAbsent(event.getHand(), restored);
    }

    @SubscribeEvent
    public static void finishTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (player.level.isClientSide || event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Map<Hand, ItemStack> pending = BROKEN.remove(player);
        if (pending == null || !player.isAlive()) return;
        for (Map.Entry<Hand, ItemStack> entry : pending.entrySet()) {
            // Do not replace a new item inserted by the player or another mod.
            if (player.getItemInHand(entry.getKey()).isEmpty() && PoppetEffects.spend(player, Kind.TOOL)) {
                player.setItemInHand(entry.getKey(), entry.getValue());
                player.inventory.setChanged();
                player.playSound(SoundEvents.EVOKER_CAST_SPELL, 5, 1);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void vampiric(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity) || !(event.getEntityLiving().level instanceof ServerWorld)
                || !AccessoryRules.validDamage(event.getAmount()) || REDIRECTING.get()) return;
        PlayerEntity player = (PlayerEntity)event.getEntityLiving();
        ServerWorld world = (ServerWorld)player.level;
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack stack = player.inventory.getItem(slot);
            UUID bound = PoppetItem.binding(stack);
            if (!PoppetItem.usable(stack, Kind.VAMPIRIC, bound) || player.getUUID().equals(bound)) continue;
            Entity target = world.getEntity(bound);
            if (!(target instanceof LivingEntity) || !target.isAlive()) continue;
            if (transfer(event.getAmount(), () -> target.hurt(event.getSource(), event.getAmount()))) {
                PoppetItem.spend(stack, Kind.VAMPIRIC, bound);
                player.inventory.setChanged();
                event.setAmount(0);
                player.playSound(SoundEvents.WITCH_DRINK, 2, 1);
                return;
            }
        }
    }
    private ExtendedPoppetEffects() {}
}
