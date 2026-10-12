package com.bewitchment.item;

import com.bewitchment.api.event.VoodooEvent;
import com.bewitchment.registry.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import java.util.UUID;

public final class VoodooPoppetItem extends PoppetItem {
    private static final String LAVA_ATTEMPT = "bewitchment:voodoo_lava_attempted";
    public VoodooPoppetItem() { super(Kind.VOODOO); }
    private static LivingEntity target(ServerWorld world, ItemStack stack) {
        UUID id = binding(stack);
        Entity entity = id == null ? null : world.getEntity(id);
        return entity instanceof LivingEntity && entity.isAlive() ? (LivingEntity)entity : null;
    }
    public static boolean playerAttackAllowed(boolean self, boolean knownCaster, boolean pvp, boolean friendlyFireAllowed) {
        return self || knownCaster && pvp && friendlyFireAllowed;
    }
    private static boolean allowed(ServerWorld world, PlayerEntity caster, LivingEntity target) {
        if (target.isSpectator()) return false;
        if (!(target instanceof PlayerEntity)) return true;
        return playerAttackAllowed(caster == target, caster != null, world.getServer().isPvpAllowed(),
                caster != null && ((PlayerEntity)target).canHarmPlayer(caster));
    }
    private static void message(PlayerEntity player, String key) {
        player.displayClientMessage(new TranslationTextComponent("poppet." + key), true);
    }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (binding(stack) == null || !player.isShiftKeyDown()) return super.use(world, player, hand);
        if (world.isClientSide) return ActionResult.success(stack);
        if (stack.getDamageValue() >= Kind.VOODOO.uses) return ActionResult.fail(stack);
        ItemStack needle = ItemStack.EMPTY;
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack candidate = player.inventory.getItem(slot);
            if (candidate.getItem() == ModItems.BONE_NEEDLE.get()) { needle = candidate; break; }
        }
        if (needle.isEmpty()) { message(player, "need_needle"); return ActionResult.fail(stack); }
        LivingEntity target = target((ServerWorld)world, stack);
        if (target == null) { message(player, "not_found"); return ActionResult.fail(stack); }
        if (!allowed((ServerWorld)world, player, target)) { message(player, "pvp_blocked"); return ActionResult.fail(stack); }
        if (MinecraftForge.EVENT_BUS.post(new VoodooEvent(player, target))) {
            message(player, "protect"); return ActionResult.fail(stack);
        }
        // Pay before damage: self-targeting can otherwise drop the unpaid inventory on death.
        if (needle.isEmpty() || needle.getItem() != ModItems.BONE_NEEDLE.get()
                || !spend(stack, Kind.VOODOO, target.getUUID())) return ActionResult.fail(stack);
        needle.shrink(1);
        player.inventory.setChanged();
        target.hurt(DamageSource.indirectMagic(player, player), 2);
        return ActionResult.consume(stack);
    }
    /** Once per dropped entity, including across saving, so lava cannot drain a ward every tick. */
    public static boolean markLavaAttempt(net.minecraft.nbt.CompoundNBT data) {
        if (data.getBoolean(LAVA_ATTEMPT)) return false;
        data.putBoolean(LAVA_ATTEMPT, true); return true;
    }
    @Override public boolean onEntityItemUpdate(ItemStack stack, ItemEntity item) {
        if (!(item.level instanceof ServerWorld) || binding(item.getItem()) == null || item.getItem().getDamageValue() >= Kind.VOODOO.uses) return false;
        ServerWorld world = (ServerWorld)item.level;
        if (!world.getFluidState(item.blockPosition()).is(FluidTags.LAVA)
                && !world.getFluidState(item.blockPosition().below()).is(FluidTags.LAVA)) return false;
        if (!markLavaAttempt(item.getPersistentData())) return false;
        LivingEntity target = target(world, item.getItem());
        if (target == null || target.fireImmune()) return false;
        PlayerEntity caster = item.getThrower() == null ? null : world.getServer().getPlayerList().getPlayer(item.getThrower());
        if (allowed(world, caster, target) && !MinecraftForge.EVENT_BUS.post(new VoodooEvent(caster, target, VoodooEvent.Action.LAVA)))
            target.setSecondsOnFire(30);
        return false; // Keep normal item ticking and lava destruction.
    }
}
