package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import java.util.UUID;
import java.util.function.Consumer;

/** Installation/list edits do not also activate the sigil on the same click. */
public final class DoorSigilInteraction {
    public enum Result { NONE, DENIED, INSTALLED, TOGGLED, INVALID_TAGLOCK, TRIGGERED }
    public static Result apply(ChestSigilData data, UUID player, Kind heldSigil, String boundId,
                               boolean mayEdit, Runnable consume, Consumer<Kind> effect) {
        if (player == null) return Result.DENIED;
        if (data.kind() == null && heldSigil != null) {
            if (!mayEdit) return Result.DENIED;
            if (data.install(heldSigil, player)) { consume.run(); return Result.INSTALLED; }
            return Result.NONE;
        }
        if (data.kind() != null && boundId != null) {
            if (!mayEdit) return Result.DENIED;
            return data.toggle(boundId) ? Result.TOGGLED : Result.INVALID_TAGLOCK;
        }
        if (data.trigger(player)) { effect.accept(data.kind()); return Result.TRIGGERED; }
        return Result.NONE;
    }
    private DoorSigilInteraction() {}
}
