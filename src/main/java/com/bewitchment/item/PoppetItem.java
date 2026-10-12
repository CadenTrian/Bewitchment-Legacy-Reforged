package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import java.util.*;

public class PoppetItem extends Item {
    public enum Kind {
        BINDING("binding", 50), CLUMSY("clumsy", 50), DEATH("deathprotection", 1),
        EARTH("earthprotection", 50), FLAME("flameprotection", 50), HUNGER("hungerprotection", 50),
        SPIRIT("spiritbane", 50), WASTING("wasting", 50), WATER("waterprotection", 1),
        VOODOO("voodoo", 50), VOODOO_PROTECTION("voodooprotection", 50),
        TOOL("tool", 1), VAMPIRIC("vampiric", 50);
        public final String suffix;
        public final int uses;
        Kind(String suffix, int uses) { this.suffix = suffix; this.uses = uses; }
    }
    private final Kind kind;
    public PoppetItem(Kind kind) { super(new Properties().tab(ModItems.TAB).durability(kind.uses)); this.kind = kind; }
    public Kind kind() { return kind; }
    public static UUID binding(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("boundId", 8)) return null;
        String id = stack.getTag().getString("boundId");
        try {
            UUID parsed = UUID.fromString(id);
            return parsed.toString().equalsIgnoreCase(id) ? parsed : null;
        } catch (IllegalArgumentException ignored) { return null; }
    }
    public static boolean canBind(ItemStack poppet, ItemStack taglock) {
        return poppet.getItem() instanceof PoppetItem && !poppet.isEmpty()
                && (!poppet.hasTag() || !poppet.getTag().contains("boundId"))
                && taglock.getItem() instanceof TaglockItem && !taglock.isEmpty()
                && binding(taglock) != null && taglock.getTag().contains("boundName", 8)
                && !taglock.getTag().getString("boundName").isEmpty();
    }
    /** Server-authoritative, preserving unrelated display/custom data on the poppet. */
    public static boolean bind(ItemStack poppet, ItemStack taglock) {
        if (!canBind(poppet, taglock)) return false;
        poppet.getOrCreateTag().putString("boundId", binding(taglock).toString());
        poppet.getOrCreateTag().putString("boundName", taglock.getTag().getString("boundName"));
        taglock.shrink(1);
        return true;
    }
    /** Read-only eligibility shared by actual spending and status inspection. */
    public static boolean usable(ItemStack stack, Kind kind, UUID owner) {
        return stack.getItem() instanceof PoppetItem && ((PoppetItem)stack.getItem()).kind == kind
                && owner != null && owner.equals(binding(stack)) && !stack.isEmpty()
                && stack.getDamageValue() < kind.uses;
    }
    /** Charge only a usable, correctly bound poppet. Last charge removes it immediately. */
    public static boolean spend(ItemStack stack, Kind kind, UUID owner) {
        if (!usable(stack,kind,owner)) return false;
        int damage = stack.getDamageValue() + 1;
        if (damage >= kind.uses) stack.shrink(1);
        else stack.setDamageValue(damage);
        return true;
    }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack taglock = player.getItemInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (!canBind(stack, taglock)) return ActionResult.pass(stack);
        if (!world.isClientSide) {
            bind(stack, taglock);
            player.inventory.setChanged();
            world.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (binding(stack) != null) tooltip.add(new TranslationTextComponent("tooltip.bewitchment.poppet.bound", stack.getTag().getString("boundName")));
        else tooltip.add(new TranslationTextComponent("tooltip.bewitchment.poppet.bind"));
        tooltip.add(new TranslationTextComponent("tooltip.bewitchment.poppet." + kind.suffix));
        if (kind != Kind.VOODOO && kind != Kind.VAMPIRIC) tooltip.add(new TranslationTextComponent("tooltip.bewitchment.poppet."
                + (kind == Kind.VOODOO_PROTECTION ? "voodoo_storage" : "carried")));
    }
    @Override public boolean isFoil(ItemStack stack) { return binding(stack) != null; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return false; }
}
