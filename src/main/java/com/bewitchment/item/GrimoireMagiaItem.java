package com.bewitchment.item;

import com.bewitchment.machine.MagicPowerSource;
import com.bewitchment.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import java.util.List;
import javax.annotation.Nullable;

/** Legacy portable magic: a single carried book must cover the complete cost. */
public class GrimoireMagiaItem extends Item {
    public static final int CAPACITY = 1000;
    public GrimoireMagiaItem() { super(new Properties().stacksTo(1).tab(ModItems.TAB)); }
    public int storageCapacity() { return CAPACITY; }
    public static int capacity(ItemStack stack) {
        int limit = stack.getItem() instanceof GrimoireMagiaItem ? ((GrimoireMagiaItem)stack.getItem()).storageCapacity() : CAPACITY;
        int stored = stack.hasTag() ? stack.getTag().getInt("maxAmount") : 0;
        return stored > 0 ? Math.min(limit, stored) : limit;
    }
    public static int power(ItemStack stack) {
        return stack.hasTag() ? Math.max(0, Math.min(capacity(stack), stack.getTag().getInt("amount"))) : 0;
    }
    public static void setPower(ItemStack stack, int amount) {
        int max = capacity(stack);
        stack.getOrCreateTag().putInt("maxAmount", max);
        stack.getOrCreateTag().putInt("amount", Math.max(0, Math.min(max, amount)));
    }
    public static boolean drain(ItemStack stack, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof GrimoireMagiaItem) || amount <= 0 || power(stack) < amount) return false;
        setPower(stack, power(stack) - amount); return true;
    }
    public static boolean drain(PlayerEntity player, int amount) {
        if (player.level.isClientSide || amount <= 0) return false;
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            if (drain(player.inventory.getItem(slot), amount)) { player.inventory.setChanged(); return true; }
        }
        return false;
    }
    /** Transfer is all-or-nothing at the altar; excess received power is lost at capacity. */
    public static boolean charge(ItemStack stack, MagicPowerSource source, int cost, int received) {
        if (stack.isEmpty() || !(stack.getItem() instanceof GrimoireMagiaItem) || cost <= 0 || received <= 0
                || power(stack) >= capacity(stack) || !source.tryDrainMagic(cost)) return false;
        setPower(stack, power(stack) + Math.min(received, capacity(stack) - power(stack))); return true;
    }
    public static boolean aliceVariant(ItemStack stack) {
        String name = stack.getHoverName().getString();
        return name.equalsIgnoreCase("The Grimoire of Alice") || name.equalsIgnoreCase("Grimoire of Alice");
    }
    @Override public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
        if (!allowdedIn(tab)) return;
        for (int amount : new int[]{0, CAPACITY}) { ItemStack stack = new ItemStack(this); setPower(stack, amount); items.add(stack); }
    }
    @Override public boolean showDurabilityBar(ItemStack stack) { return power(stack) < capacity(stack); }
    @Override public double getDurabilityForDisplay(ItemStack stack) { return 1.0 - (double) power(stack) / capacity(stack); }
    @Override public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> lines, ITooltipFlag flag) {
        lines.add(new TranslationTextComponent("tooltip.bewitchment.grimoire_magia", power(stack), capacity(stack)));
    }
}
