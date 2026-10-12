package com.bewitchment.machine;

import com.bewitchment.block.*;
import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IBlockReader;
import java.lang.reflect.Proxy;
import java.util.*;
import static com.bewitchment.machine.DoorSigilInteraction.Result.*;

public final class DoorSigilCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        UUID owner = UUID.randomUUID(), visitor = UUID.randomUUID();
        for (Kind kind : Kind.values()) {
            ChestSigilData data = new ChestSigilData();
            ItemStack selectedHand = new ItemStack(Items.PAPER, 3), otherHand = new ItemStack(Items.PAPER, 5);
            int[] effects = {0};
            java.util.function.Consumer<Kind> apply = k -> { check(k == kind && data.cooldown() == kind.cooldown, "Cooldown committed before correct effect"); effects[0]++; };
            check(DoorSigilInteraction.apply(data, owner, kind, null, false, () -> selectedHand.shrink(1), apply) == DENIED,
                    "Build permission required to install");
            check(data.kind() == null && selectedHand.getCount() == 3, "Denied install leaves item and state unchanged");
            check(DoorSigilInteraction.apply(data, owner, kind, null, true, () -> selectedHand.shrink(1), apply) == INSTALLED, "All eleven sigils install");
            check(selectedHand.getCount() == 2 && otherHand.getCount() == 5 && effects[0] == 0, "Exactly one selected-hand item consumed without triggering");
            UUID target = kind.positive ? owner : visitor, excluded = kind.positive ? visitor : owner;
            check(DoorSigilInteraction.apply(data, excluded, null, null, false, () -> {throw new AssertionError("Consumption");}, apply) == NONE,
                    "Positive whitelist / negative blacklist");
            check(DoorSigilInteraction.apply(data, target, null, null, false, () -> {throw new AssertionError("Consumption");}, apply) == TRIGGERED,
                    "Ordinary interaction can trigger without build permission");
            check(effects[0] == 1, "One effect callback");
            check(DoorSigilInteraction.apply(data, target, null, null, true, () -> {}, apply) == (kind.cooldown > 0 ? NONE : TRIGGERED), "Shared cooldown including zero-cooldown Shrieking");
            CompoundNBT saved = data.save();
            ChestSigilData restored = new ChestSigilData(); restored.load(saved);
            check(restored.kind() == kind && restored.cooldown() == data.cooldown() && restored.targets(target), "Saved targeting and cooldown");
            while (restored.cooldown() > 0) restored.tick();
            check(restored.trigger(target), "Loaded ticks restore eligibility at exact cooldown boundary");
            String before = data.save().toString();
            check(DoorSigilInteraction.apply(data, owner, null, visitor.toString(), false, () -> {}, apply) == DENIED
                    && data.save().toString().equals(before), "Unauthorized list edit denied");
            check(DoorSigilInteraction.apply(data, owner, null, "malformed", true, () -> {}, apply) == INVALID_TAGLOCK
                    && data.save().toString().equals(before), "Invalid taglock cannot edit or trigger");
            check(DoorSigilInteraction.apply(data, owner, null, visitor.toString(), true, () -> {throw new AssertionError("Taglock consumed");}, apply) == TOGGLED,
                    "Taglock toggles without consumption");
            check(data.targets(visitor) == kind.positive, "Visitor membership added");
            check(DoorSigilInteraction.apply(data, owner, null, visitor.toString(), true, () -> {}, apply) == TOGGLED
                    && data.targets(visitor) == !kind.positive, "Second taglock use removes membership");
        }
        blocksAndPackets(owner, visitor);
        System.out.println("Dragon's Blood door/trapdoor checks passed: " + checks);
    }
    private static void blocksAndPackets(UUID owner, UUID visitor) {
        DragonsBloodDoor door = new DragonsBloodDoor(); DragonsBloodTrapdoor trapdoor = new DragonsBloodTrapdoor();
        BlockPos base = new BlockPos(-20,64,7);
        for (BlockState state : door.getStateDefinition().getPossibleStates()) {
            boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
            check(state.hasTileEntity() == lower, "Exactly the lower half owns a tile in every open/powered/hinge/facing state");
            check(DragonsBloodSigils.base(state, lower ? base : base.above()).equals(base), "Both door halves resolve one position");
        }
        for (BlockState state : trapdoor.getStateDefinition().getPossibleStates()) {
            check(state.hasTileEntity(), "Trapdoor retains tile when open, powered or waterlogged");
            check(DragonsBloodSigils.base(state, base).equals(base), "Trapdoor never resolves a block below");
        }
        DragonsBloodSigilTile tile = new DragonsBloodSigilTile(TileEntityType.CHEST);
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        IBlockReader world = (IBlockReader) Proxy.newProxyInstance(IBlockReader.class.getClassLoader(), new Class<?>[]{IBlockReader.class}, (proxy, method, args) -> {
            if (method.getName().equals("getBlockState")) return args[0].equals(base) ? lower : args[0].equals(base.above()) ? upper : Blocks.AIR.defaultBlockState();
            if (method.getName().equals("getBlockEntity")) return args[0].equals(base) ? tile : null;
            throw new AssertionError("Unexpected world read: " + method.getName());
        });
        check(DragonsBloodSigils.tile(lower, world, base) == tile && DragonsBloodSigils.tile(upper, world, base.above()) == tile, "Both halves access identical tile/data");
        check(DragonsBloodSigils.tile(upper, world, base.above(3)) == null, "Orphan upper half does not use unrelated tile");
        tile.sigil.install(Kind.BINDING, owner); tile.sigil.trigger(visitor);
        CompoundNBT save = tile.save(new CompoundNBT());
        DragonsBloodSigilTile loaded = new DragonsBloodSigilTile(TileEntityType.CHEST); loaded.load(lower, save);
        check(loaded.sigil.kind() == Kind.BINDING && loaded.sigil.cooldown() == 600 && !loaded.sigil.targets(owner), "Tile round-trip persists kind, exclusion and cooldown");
        CompoundNBT update = tile.getUpdateTag();
        check(update.getBoolean("HasSigil") && !update.contains("DoorSigil") && !update.contains("playerList") && !update.contains("cooldown")
                && !update.toString().contains(owner.toString()), "Chunk update reveals only presence, no identities or cooldown");
        check(tile.getUpdatePacket().getTag().equals(update), "Block update and chunk-load payload match");
        loaded.handleUpdateTag(lower, new CompoundNBT());
        check(loaded.sigil.cooldown() == 600 && loaded.sigil.kind() == Kind.BINDING, "Visual update does not overwrite server data");
        DragonsBloodSigilTile legacy = new DragonsBloodSigilTile(TileEntityType.CHEST); legacy.load(lower, tile.sigil.save());
        check(legacy.sigil.kind() == Kind.BINDING && legacy.sigil.cooldown() == 600, "Legacy root-format data loads");
        CompoundNBT invalid = tile.sigil.save(); invalid.putInt("cooldown", Integer.MAX_VALUE);
        legacy.load(lower, invalid); check(legacy.sigil.cooldown() == 600, "Malformed cooldown capped");
        invalid.putString("sigil", "bewitchment:missing"); legacy.load(lower, invalid);
        check(!legacy.hasSigil() && legacy.sigil.cooldown() == 0, "Unknown sigil clears old state");
    }
}
