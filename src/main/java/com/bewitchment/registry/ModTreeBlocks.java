package com.bewitchment.registry;

import com.bewitchment.world.LegacyTrees;
import net.minecraft.block.*;
import net.minecraftforge.fml.RegistryObject;

public final class ModTreeBlocks {
    public static final WoodFamily JUNIPER = new WoodFamily("juniper", () -> new LegacyTrees.Grower(false));
    public static final WoodFamily CYPRESS = new WoodFamily("cypress", () -> new LegacyTrees.Grower(true));
    public static final WoodFamily DRAGONS_BLOOD = new WoodFamily("dragons_blood", com.bewitchment.world.DragonTree.Grower::new, com.bewitchment.block.DragonsBloodLog::new);
    public static final RegistryObject<Block> POTTED_DRAGONS_BLOOD = pot("dragons_blood", DRAGONS_BLOOD.sapling);
    public static final RegistryObject<Block> POTTED_ELDER = pot("elder", ModBlocks.ELDER_SAPLING);
    public static final RegistryObject<Block> POTTED_JUNIPER = pot("juniper", JUNIPER.sapling);
    public static final RegistryObject<Block> POTTED_CYPRESS = pot("cypress", CYPRESS.sapling);
    private static RegistryObject<Block> pot(String name, RegistryObject<Block> sapling) {
        return ModBlocks.BLOCKS.register("potted_" + name + "_sapling", () ->
                new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, sapling, AbstractBlock.Properties.copy(Blocks.POTTED_OAK_SAPLING)));
    }
    public static void init() {}
    private ModTreeBlocks() {}
}
