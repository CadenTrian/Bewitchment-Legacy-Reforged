package com.bewitchment.item;

import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;
import com.bewitchment.machine.MagicPowerSource;

public final class GrimoireCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static final class Source implements MagicPowerSource {
        int power, calls;
        Source(int power) { this.power = power; }
        public boolean tryDrainMagic(int amount) { calls++; if (power < amount) return false; power -= amount; return true; }
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        GrimoireMagiaItem item = new GrimoireMagiaItem();
        ItemStack book = new ItemStack(item);
        check(book.getMaxStackSize() == 1 && GrimoireMagiaItem.power(book) == 0 && GrimoireMagiaItem.capacity(book) == 1000, "Fresh/given books are empty and chargeable");
        check(item.showDurabilityBar(book) && item.getDurabilityForDisplay(book) == 1, "Fresh book displays empty power bar");
        check(!book.hasTag(), "Reading fresh books does not write NBT client-side");
        Source altar = new Source(49);
        check(!GrimoireMagiaItem.charge(book, altar, 50, 25) && altar.power == 49 && !book.hasTag(), "Insufficient altar power changes neither side");
        altar.power = 150;
        check(GrimoireMagiaItem.charge(book, altar, 50, 25) && altar.power == 100 && GrimoireMagiaItem.power(book) == 25, "Carried transfer costs 50 and delivers 25");
        check(GrimoireMagiaItem.charge(book, altar, 100, 25) && altar.power == 0 && GrimoireMagiaItem.power(book) == 50, "Placed transfer costs 100 and delivers 25");
        book.getOrCreateTag().putString("OtherMod", "retained");
        check(!GrimoireMagiaItem.drain(book, 51) && GrimoireMagiaItem.power(book) == 50, "Failed drain is atomic");
        check(!GrimoireMagiaItem.drain(book, -1) && !GrimoireMagiaItem.drain(book, 0), "Reject nonpositive drain amounts");
        check(GrimoireMagiaItem.drain(book, 50) && GrimoireMagiaItem.power(book) == 0, "Exact balance can be drained");
        check(book.getTag().getString("OtherMod").equals("retained"), "Power updates preserve unrelated NBT");
        check(!GrimoireMagiaItem.drain(book, 1), "Empty books cannot subsidize power");
        GrimoireMagiaItem.setPower(book, 999); altar.power = 100;
        check(GrimoireMagiaItem.charge(book, altar, 50, 25) && GrimoireMagiaItem.power(book) == 1000 && altar.power == 50, "Nearly-full transfer clamps output but keeps legacy full cost");
        int calls = altar.calls;
        check(!GrimoireMagiaItem.charge(book, altar, 50, 25) && altar.calls == calls && altar.power == 50, "Full book never drains altar");
        check(!item.showDurabilityBar(book) && item.getDurabilityForDisplay(book) == 0, "Full power hides empty portion of bar");
        GrimoireMagiaItem.setPower(book, Integer.MAX_VALUE);
        check(GrimoireMagiaItem.power(book) == 1000, "Overflow-sized stored values clamp");
        GrimoireMagiaItem.setPower(book, -100);
        check(GrimoireMagiaItem.power(book) == 0, "Negative stored values clamp");
        book.getTag().putInt("maxAmount", 0); book.getTag().putInt("amount", -2);
        check(GrimoireMagiaItem.capacity(book) == 1000 && item.getDurabilityForDisplay(book) == 1, "Malformed zero capacity never divides by zero");
        book.getTag().putInt("maxAmount", 200); book.getTag().putInt("amount", 900);
        check(GrimoireMagiaItem.power(book) == 200 && GrimoireMagiaItem.capacity(book) == 200, "Smaller saved capacities are respected");
        ItemStack reloaded = new ItemStack(item); reloaded.setTag(book.getTag().copy());
        check(GrimoireMagiaItem.power(reloaded) == 200 && reloaded.getTag().getString("OtherMod").equals("retained"), "Power survives tag save/copy");
        calls = altar.calls;
        check(!GrimoireMagiaItem.charge(new ItemStack(Items.BOOK), altar, 50, 25) && altar.calls == calls, "Ordinary books cannot drain altar");
        check(!GrimoireMagiaItem.drain(new ItemStack(Items.BOOK), 1) && !GrimoireMagiaItem.drain(ItemStack.EMPTY, 1), "Only Grimoires can provide power");
        GrimoireMagiaItem.setPower(book, 0); calls = altar.calls;
        check(!GrimoireMagiaItem.charge(book, altar, 0, 25) && !GrimoireMagiaItem.charge(book, altar, 50, -25) && altar.calls == calls, "Invalid transfers never call the source");
        book.setHoverName(new StringTextComponent("tHe GrImOiRe Of AlIcE"));
        check(GrimoireMagiaItem.aliceVariant(book), "Alice cover matches case-insensitively");
        book.setHoverName(new StringTextComponent("Grimoire of Alice"));
        check(GrimoireMagiaItem.aliceVariant(book), "Short Alice alias works");
        book.setHoverName(new StringTextComponent("Not Grimoire of Alice"));
        check(!GrimoireMagiaItem.aliceVariant(book), "Unrelated names do not select cover");
        NonNullList<ItemStack> variants = NonNullList.create(); item.fillItemCategory(ItemGroup.TAB_SEARCH, variants);
        check(variants.size() == 2 && GrimoireMagiaItem.power(variants.get(0)) == 0 && GrimoireMagiaItem.power(variants.get(1)) == 1000, "Creative/search supplies empty and full variants");
        BastardsGrimoireItem reserveItem = new BastardsGrimoireItem();
        ItemStack reserve = new ItemStack(reserveItem);
        check(GrimoireMagiaItem.capacity(reserve) == 2147483646 && GrimoireMagiaItem.power(reserve) == 0 && !reserve.hasTag(), "Given reserve book is empty, chargeable and readable without NBT writes");
        NonNullList<ItemStack> reserves = NonNullList.create(); reserveItem.fillItemCategory(ItemGroup.TAB_SEARCH, reserves);
        check(reserves.size() == 1 && GrimoireMagiaItem.power(reserves.get(0)) == 2147483646, "One full legacy creative reserve, without duplicate entries");
        reserve = reserves.get(0);
        check(reserve.getMaxStackSize() == 1 && !reserveItem.showDurabilityBar(reserve), "Full testing book is unstackable with no depleted bar");
        check(GrimoireMagiaItem.drain(reserve, 50) && GrimoireMagiaItem.power(reserve) == 2147483596, "Reserve pays ordinary accessory costs");
        check(reserveItem.showDurabilityBar(reserve) && reserveItem.getDurabilityForDisplay(reserve) > 0 && reserveItem.getDurabilityForDisplay(reserve) < 1, "Large power bar uses floating-point division safely");
        GrimoireMagiaItem.setPower(reserve, Integer.MAX_VALUE);
        check(GrimoireMagiaItem.power(reserve) == 2147483646, "Reserve clamps to original maximum below signed integer limit");
        GrimoireMagiaItem.setPower(reserve, 2147483645); altar.power = 100;
        check(GrimoireMagiaItem.charge(reserve, altar, 50, Integer.MAX_VALUE) && GrimoireMagiaItem.power(reserve) == 2147483646 && altar.power == 50, "Near-limit charge never overflows, even for oversized incoming amounts");
        check(!GrimoireMagiaItem.charge(reserve, altar, 50, 25) && altar.power == 50, "Full reserve never drains altar");
        ItemStack reserveCopy = reserve.copy();
        check(GrimoireMagiaItem.drain(reserveCopy, 2147483646) && GrimoireMagiaItem.power(reserveCopy) == 0 && GrimoireMagiaItem.power(reserve) == 2147483646, "Exact large payment is atomic and copied books own their NBT");
        check(GrimoireMagiaItem.charge(reserveCopy, altar, 50, 25) && GrimoireMagiaItem.power(reserveCopy) == 25, "Empty reserve charges at normal carried rate");
        ItemStack ordinary = new ItemStack(item); ordinary.setTag(reserve.getTag().copy());
        check(GrimoireMagiaItem.capacity(ordinary) == 1000 && GrimoireMagiaItem.power(ordinary) == 1000, "Reserve NBT cannot increase ordinary Grimoire capacity");
        reserveCopy.getOrCreateTag().putInt("maxAmount", -1);
        check(GrimoireMagiaItem.capacity(reserveCopy) == 2147483646, "Malformed reserve capacity falls back to its own limit");
        System.out.println("PASS: " + checks + " Grimoire power, transfer, NBT and variant checks.");
    }
}
