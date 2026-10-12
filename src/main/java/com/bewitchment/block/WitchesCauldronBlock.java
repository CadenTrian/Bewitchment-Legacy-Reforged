package com.bewitchment.block;

import com.bewitchment.machine.CauldronTile;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;

public final class WitchesCauldronBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    public static final BooleanProperty LAVA = BooleanProperty.create("lava");
    private static final VoxelShape SHAPE = VoxelShapes.or(Block.box(0,0,0,16,5,16), Block.box(0,0,0,2,11,16),
            Block.box(14,0,0,16,11,16), Block.box(0,0,0,16,11,2), Block.box(0,0,14,16,11,16));
    public WitchesCauldronBlock() {
        super(Properties.copy(Blocks.CAULDRON).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FILLED, false).setValue(LAVA, false));
    }
    @Override protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(FILLED, LAVA); }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new CauldronTile(); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide) return ActionResultType.SUCCESS;
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof CauldronTile)) return ActionResultType.PASS;
        CauldronTile cauldron = (CauldronTile) tile;
        ItemStack stack = player.getItemInHand(hand);
        boolean lavaBucket = stack.getItem() == Items.LAVA_BUCKET;
        if(stack.getItem()==Items.GLASS_BOTTLE && !player.isShiftKeyDown() && player instanceof net.minecraft.entity.player.ServerPlayerEntity){
            ItemStack output=cauldron.bottle((net.minecraft.entity.player.ServerPlayerEntity)player);
            if(!output.isEmpty()){
                stack.shrink(1);if(stack.isEmpty())player.setItemInHand(hand,output);else if(!player.inventory.add(output))player.drop(output,false);
                world.playSound(null,pos,SoundEvents.BOTTLE_FILL,SoundCategory.BLOCKS,1,1);
            }
        } else if ((stack.getItem() == Items.WATER_BUCKET || stack.getItem() == Items.LAVA_BUCKET) && cauldron.fill(stack.getItem() == Items.LAVA_BUCKET)) {
            player.setItemInHand(hand, DrinkHelper.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            world.playSound(null, pos, lavaBucket ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
        } else if (stack.getItem() == Items.BUCKET && cauldron.drainClean()) {
            player.setItemInHand(hand, DrinkHelper.createFilledResult(stack, player, new ItemStack(state.getValue(LAVA) ? Items.LAVA_BUCKET : Items.WATER_BUCKET)));
            world.playSound(null, pos, state.getValue(LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
        } else if (stack.isEmpty()) {
            player.displayClientMessage(new TranslationTextComponent("cauldron.bewitchment.status", cauldron.getHeat(), cauldron.getIngredientCount(),
                    new TranslationTextComponent(cauldron.isLava() ? "cauldron.bewitchment.lava" : cauldron.isFailed() ? "cauldron.bewitchment.failed" : cauldron.isBrewing() ? "cauldron.bewitchment.brewing" : "cauldron.bewitchment.instructions")), true);
        }
        return ActionResultType.CONSUME;
    }
    @Override public void onRemove(BlockState state, World world, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            TileEntity tile = world.getBlockEntity(pos);
            if (!world.isClientSide && tile instanceof CauldronTile) ((CauldronTile) tile).dropContents();
        }
        super.onRemove(state, world, pos, replacement, moving);
    }
}
