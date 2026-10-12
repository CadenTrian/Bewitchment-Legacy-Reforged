package com.bewitchment.registry;

import net.minecraft.block.*;
import net.minecraft.block.trees.Tree;
import net.minecraft.item.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.fml.RegistryObject;
import java.util.function.Supplier;

/** Shared vanilla behavior for the additional legacy wood sets. */
public final class WoodFamily {
    public final RegistryObject<Block> log, planks, leaves, sapling, stairs, slab, fence, gate, door, trapdoor, button, plate;
    public WoodFamily(String name, Supplier<Tree> tree) {
        this(name, tree, Log::new);
    }
    public WoodFamily(String name, Supplier<Tree> tree, Supplier<Block> logFactory) {
        log = register(name + "_wood", logFactory);
        planks = register(name + "_planks", Planks::new);
        leaves = register(name + "_leaves", Leaves::new);
        sapling = register(name + "_sapling", () -> new SaplingBlock(tree.get(), AbstractBlock.Properties.copy(Blocks.OAK_SAPLING)));
        stairs = register(name + "_wood_stairs", () -> new Stairs(() -> planks.get().defaultBlockState()));
        slab = register(name + "_wood_slab", Slab::new);
        fence = register(name + "_fence", Fence::new);
        gate = register(name + "_fence_gate", name.equals("juniper") ? com.bewitchment.block.JuniperGate::new : Gate::new);
        door = register(name + "_door", () -> name.equals("juniper") ? new com.bewitchment.block.JuniperDoor() : name.equals("dragons_blood") ? new com.bewitchment.block.DragonsBloodDoor() : new DoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_DOOR)) {});
        trapdoor = register(name + "_trapdoor", () -> name.equals("juniper") ? new com.bewitchment.block.JuniperTrapdoor() : name.equals("dragons_blood") ? new com.bewitchment.block.DragonsBloodTrapdoor() : new TrapDoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_TRAPDOOR)) {});
        button = register(name + "_button", () -> new WoodButtonBlock(AbstractBlock.Properties.copy(Blocks.OAK_BUTTON)) {});
        plate = register(name + "_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, AbstractBlock.Properties.copy(Blocks.OAK_PRESSURE_PLATE)));
    }
    private static RegistryObject<Block> register(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(ModItems.TAB)));
        return block;
    }
    private interface Flammable extends IForgeBlock {
        default boolean wet(BlockState state) { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED); }
        @Override default int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
            return wet(state) ? 0 : this instanceof LeavesBlock ? 60 : this instanceof RotatedPillarBlock ? 5 : 20;
        }
        @Override default int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) { return wet(state) ? 0 : this instanceof LeavesBlock ? 30 : 5; }
    }
    private static final class Log extends RotatedPillarBlock implements Flammable { Log() { super(Properties.copy(Blocks.OAK_LOG)); } }
    private static final class Planks extends Block implements Flammable { Planks() { super(Properties.copy(Blocks.OAK_PLANKS)); } }
    private static final class Leaves extends LeavesBlock implements Flammable { Leaves() { super(Properties.copy(Blocks.OAK_LEAVES)); } }
    private static final class Stairs extends StairsBlock implements Flammable { Stairs(Supplier<BlockState> state) { super(state, Properties.copy(Blocks.OAK_STAIRS)); } }
    private static final class Slab extends SlabBlock implements Flammable { Slab() { super(Properties.copy(Blocks.OAK_SLAB)); } }
    private static final class Fence extends FenceBlock implements Flammable { Fence() { super(Properties.copy(Blocks.OAK_FENCE)); } }
    private static final class Gate extends FenceGateBlock implements Flammable { Gate() { super(Properties.copy(Blocks.OAK_FENCE_GATE)); } }
}
