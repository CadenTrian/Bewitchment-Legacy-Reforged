package com.bewitchment.item;

import com.bewitchment.combat.PoppetEffects;
import com.bewitchment.item.PoppetItem.Kind;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class PoppetCheck {
    private static int checks;
    private static final UUID OWNER = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static ItemStack taglock(String id, String name) {
        ItemStack stack = new ItemStack(new TaglockItem(), 2);
        stack.getOrCreateTag().putString("boundId", id);
        stack.getOrCreateTag().putString("boundName", name);
        return stack;
    }
    private static ItemStack poppet(Kind kind) { return new ItemStack(new PoppetItem(kind)); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        ItemStack earth = poppet(Kind.EARTH), lock = taglock(OWNER.toString(), "Test Witch");
        earth.getOrCreateTag().putString("CustomData", "retained"); earth.setDamageValue(7);
        check(PoppetItem.bind(earth, lock), "Allow binding with unrelated NBT");
        check(lock.getCount() == 1, "Consume exactly one taglock");
        check(earth.getTag().getString("CustomData").equals("retained") && earth.getDamageValue() == 7, "Keep custom data and damage");
        check(OWNER.equals(PoppetItem.binding(earth)) && earth.getTag().getString("boundName").equals("Test Witch"), "Copy binding identity and name");
        check(!PoppetItem.bind(earth, lock) && lock.getCount() == 1, "Cannot rebind or waste taglocks");
        ItemStack fresh = poppet(Kind.FLAME);
        check(!PoppetItem.bind(fresh, taglock("bad-uuid", "Witch")), "Reject malformed UUIDs");
        check(!PoppetItem.bind(fresh, taglock("1-1-1-1-1", "Witch")), "Reject noncanonical abbreviated UUIDs");
        check(!PoppetItem.bind(fresh, taglock(OWNER.toString(), "")), "Require target name");
        ItemStack impostor = new ItemStack(Items.STICK); impostor.setTag(lock.getTag().copy());
        check(!PoppetItem.bind(fresh, impostor), "Only real taglocks bind");
        check(!PoppetItem.spend(fresh, Kind.FLAME, OWNER), "Unbound items cannot activate");
        check(!PoppetItem.spend(earth, Kind.FLAME, OWNER) && earth.getDamageValue() == 7, "Wrong kind does not consume durability");
        check(!PoppetItem.spend(earth, Kind.EARTH, new UUID(1, 2)) && earth.getDamageValue() == 7, "Wrong owner does not consume durability");
        ItemStack loaded = poppet(Kind.EARTH); loaded.setTag(earth.getTag().copy());
        check(OWNER.equals(PoppetItem.binding(loaded)) && loaded.getDamageValue() == 7, "Binding survives NBT copying");
        for (Kind kind : Kind.values()) {
            ItemStack stack = poppet(kind); PoppetItem.bind(stack, taglock(OWNER.toString(), "Witch"));
            int uses = 0; while (PoppetItem.spend(stack, kind, OWNER)) { if (++uses > 50) throw new AssertionError("Unlimited poppet"); }
            check(uses == kind.uses && stack.isEmpty(), "Exact charge count and immediate final consumption: " + kind);
        }
        ItemStack spent = poppet(Kind.EARTH); PoppetItem.bind(spent, taglock(OWNER.toString(), "Witch")); spent.setDamageValue(50);
        check(!PoppetItem.spend(spent, Kind.EARTH, OWNER), "Already spent data cannot activate");
        check(PoppetEffects.protection(DamageSource.FALL) == Kind.EARTH, "Recognize falling");
        check(PoppetEffects.protection(DamageSource.IN_FIRE) == Kind.FLAME && PoppetEffects.protection(DamageSource.ON_FIRE) == Kind.FLAME, "Recognize both fire sources");
        check(PoppetEffects.protection(DamageSource.LAVA) == null, "Legacy flame protection excludes lava");
        check(PoppetEffects.protection(DamageSource.DROWN) == Kind.WATER && PoppetEffects.protection(DamageSource.STARVE) == Kind.HUNGER, "Recognize drowning and starvation");
        check(PoppetEffects.protectedDamage(Kind.EARTH, 20) == 5, "Earth reduces final damage by 75 percent");
        check(Math.abs(PoppetEffects.protectedDamage(Kind.FLAME, 10) - 6) < .001F, "Flame reduces final damage by 40 percent");
        check(Math.abs(PoppetEffects.protectedDamage(Kind.SPIRIT, 10) - 6) < .001F, "Spirit reduces final damage by 40 percent");
        check(PoppetEffects.protectedDamage(Kind.HUNGER, 2) == 0 && PoppetEffects.protectedDamage(Kind.WATER, 2) == 0, "Hunger and water suppress matching damage");
        check(PoppetEffects.protection(DamageSource.MAGIC) == null, "No general magic immunity");
        System.out.println("PASS: " + checks + " poppet binding, durability and protection checks");
    }
}
