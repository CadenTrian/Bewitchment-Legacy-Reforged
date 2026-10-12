package com.bewitchment.machine;

import com.bewitchment.item.PoppetItem;
import com.bewitchment.item.PoppetItem.Kind;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class PoppetShelfCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static ItemStack poppet(Kind kind, UUID owner) {
        ItemStack stack = new ItemStack(new PoppetItem(kind));
        stack.getOrCreateTag().putString("boundId", owner.toString());
        stack.getOrCreateTag().putString("boundName", "Shelf Witch");
        return stack;
    }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (int slot = 0; slot < 9; slot++) {
                Vector3d center = ShelfLayout.center(facing, slot);
                check(ShelfLayout.slot(facing, center.x, center.y, center.z) == slot, "Rendered stack must match clicked cell: " + facing + " " + slot);
            }
            check(ShelfLayout.slot(facing, 1, 1, 1) >= 0 && ShelfLayout.slot(facing, 1, 1, 1) < 9, "Exact block edge clamps safely");
            check(ShelfLayout.slot(facing, -.001, -.001, -.001) >= 0 && ShelfLayout.slot(facing, -.001, -.001, -.001) < 9, "Roundoff outside block clamps safely");
        }
        int[] updates = {0}; ShelfInventory shelf = new ShelfInventory(() -> updates[0]++);
        ItemStack input = new ItemStack(Items.STICK, 40);
        check(shelf.insertItem(0, input, true).isEmpty() && shelf.getStackInSlot(0).isEmpty() && updates[0] == 0, "Simulated insertion must not change shelf or send updates");
        check(shelf.insertItem(0, input, false).isEmpty() && shelf.getStackInSlot(0).getCount() == 40 && input.getCount() == 40, "Insertion preserves caller-owned input");
        input.setCount(1);
        check(shelf.getStackInSlot(0).getCount() == 40, "Stored stacks do not alias caller stacks");
        check(shelf.insertItem(0, new ItemStack(Items.STICK, 40), false).getCount() == 16 && shelf.getStackInSlot(0).getCount() == 64, "Overflow remains with caller");
        int before = updates[0];
        check(shelf.extractItem(0, 5, true).getCount() == 5 && shelf.getStackInSlot(0).getCount() == 64 && updates[0] == before, "Simulated extraction is read-only");
        ItemStack out = shelf.extractItem(0, 64, false);
        check(out.getCount() == 64 && shelf.getStackInSlot(0).isEmpty(), "Extraction drains exactly once");
        check(shelf.extractItem(0, 64, false).isEmpty(), "Repeated extraction cannot duplicate contents");
        check(shelf.insertItem(1, new ItemStack(Items.DIAMOND_SWORD, 2), false).getCount() == 1 && shelf.getStackInSlot(1).getCount() == 1, "Respect unstackable item limits");
        ItemStack named = new ItemStack(Items.PAPER, 5); named.getOrCreateTag().putString("boundName", "Preserved name");
        shelf.setStackInSlot(8, named);
        CompoundNBT saved = shelf.serializeNBT();
        ShelfInventory restored = new ShelfInventory(() -> {}); restored.deserializeNBT(saved);
        check(restored.getSlots() == 9 && restored.getStackInSlot(8).getCount() == 5 && restored.getStackInSlot(8).getTag().getString("boundName").equals("Preserved name"), "Round-trip all slots and item NBT");
        saved.putInt("Size", 2); restored.deserializeNBT(saved);
        check(restored.getSlots() == 9 && restored.getStackInSlot(8).getCount() == 5, "Saved size cannot truncate the shelf");
        saved.putInt("Size", 1000000); restored.deserializeNBT(saved);
        check(restored.getSlots() == 9, "Saved size cannot allocate arbitrary slot counts");
        UUID owner = new UUID(1, 2), other = new UUID(3, 4);
        ShelfInventory magic = new ShelfInventory(() -> updates[0]++);
        magic.setStackInSlot(0, poppet(Kind.EARTH, other));
        magic.setStackInSlot(1, poppet(Kind.FLAME, owner));
        magic.setStackInSlot(2, poppet(Kind.EARTH, owner));
        magic.setStackInSlot(3, poppet(Kind.EARTH, owner));
        before = updates[0];
        check(magic.spend(Kind.EARTH, owner) && updates[0] == before + 1, "Activation dirties and synchronizes one slot");
        check(magic.getStackInSlot(0).getDamageValue() == 0 && magic.getStackInSlot(1).getDamageValue() == 0, "Skip other owners and other poppet types");
        check(magic.getStackInSlot(2).getDamageValue() == 1 && magic.getStackInSlot(3).getDamageValue() == 0, "Only first matching poppet spends a charge");
        magic.getStackInSlot(2).setDamageValue(49); magic.spend(Kind.EARTH, owner);
        check(magic.getStackInSlot(2).isEmpty() && magic.getStackInSlot(3).getDamageValue() == 0, "Last use removes one poppet without charging the next");
        magic.setStackInSlot(4, poppet(Kind.DEATH, owner));
        check(magic.spend(Kind.DEATH, owner) && magic.getStackInSlot(4).isEmpty(), "Single-use shelf protection is consumed");
        before = updates[0];
        check(!magic.spend(Kind.WATER, owner) && updates[0] == before, "Missing protection does not mutate or synchronize");
        System.out.println("PASS: " + checks + " Poppet Shelf layout, inventory and charge checks");
    }
}
