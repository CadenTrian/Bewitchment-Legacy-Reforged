package com.bewitchment.item;

import com.bewitchment.api.event.VoodooEvent;
import com.bewitchment.machine.ShelfInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.eventbus.api.Cancelable;
import java.util.UUID;

public final class VoodooCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        for (int flags = 0; flags < 8; flags++) {
            boolean known = (flags & 1) != 0, pvp = (flags & 2) != 0, team = (flags & 4) != 0;
            check(VoodooPoppetItem.playerAttackAllowed(false, known, pvp, team) == (flags == 7), "Other-player attacks require attribution, PvP and friendly-fire permission: " + flags);
            check(VoodooPoppetItem.playerAttackAllowed(true, known, pvp, team), "Self-targeting is independent of PvP: " + flags);
        }
        CompoundNBT data = new CompoundNBT(); data.putString("OtherMod", "retained");
        check(VoodooPoppetItem.markLavaAttempt(data), "First lava contact gets one attempt");
        check(!VoodooPoppetItem.markLavaAttempt(data), "Repeated lava ticks cannot drain a ward");
        check(!VoodooPoppetItem.markLavaAttempt(data.copy()), "Attempt marker survives persistent-data copying");
        check(data.getString("OtherMod").equals("retained"), "Do not overwrite unrelated entity data");
        check(VoodooPoppetItem.markLavaAttempt(new CompoundNBT()), "New dropped entity has a fresh attempt");
        check(VoodooEvent.class.isAnnotationPresent(Cancelable.class), "Integrations can cancel VoodooEvent");
        UUID owner = new UUID(11, 22);
        ShelfInventory shelf = new ShelfInventory(() -> {});
        ItemStack ward = new ItemStack(new PoppetItem(PoppetItem.Kind.VOODOO_PROTECTION));
        ward.getOrCreateTag().putString("boundId", owner.toString());
        shelf.setStackInSlot(4, ward);
        check(shelf.spend(PoppetItem.Kind.VOODOO_PROTECTION, owner) && shelf.getStackInSlot(4).getDamageValue() == 1, "Shelf consumes one matching voodoo ward charge");
        check(!shelf.spend(PoppetItem.Kind.VOODOO, owner), "Protection is not an offensive poppet");
        ItemStack attack = new ItemStack(new VoodooPoppetItem());
        attack.getOrCreateTag().putString("boundId", owner.toString()); attack.setDamageValue(49);
        check(PoppetItem.spend(attack, PoppetItem.Kind.VOODOO, owner) && attack.isEmpty(), "Fiftieth needle use removes offensive poppet");
        System.out.println("PASS: " + checks + " voodoo policy, lava and shelf checks");
    }
}
