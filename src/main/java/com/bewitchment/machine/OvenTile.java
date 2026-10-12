package com.bewitchment.machine;

import com.bewitchment.block.WitchesOvenBlock;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.RangedWrapper;
import javax.annotation.Nullable;

public final class OvenTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private int burnTime, fuelTime, progress;
    private ItemStack processingInput = ItemStack.EMPTY;
    public final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0
                    : slot == 1 ? stack.getItem() == ModItems.EMPTY_JAR.get() : slot == 2;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
        @Override protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> inputs = LazyOptional.of(() -> new RangedWrapper(inventory, 0, 3));
    private final LazyOptional<IItemHandler> outputs = LazyOptional.of(() -> new RangedWrapper(inventory, 3, 5));
    public final IIntArray data = new IIntArray() {
        public int get(int index) { return index == 0 ? burnTime : index == 1 ? fuelTime : progress; }
        public void set(int index, int value) { if (index == 0) burnTime = value; else if (index == 1) fuelTime = value; else progress = value; }
        public int getCount() { return 3; }
    };

    public OvenTile() { super(ModMachines.OVEN.get()); }

    private boolean fits(int slot, ItemStack stack) {
        ItemStack existing = inventory.getStackInSlot(slot);
        return existing.isEmpty() || (ItemStack.isSame(existing, stack) && ItemStack.tagMatches(existing, stack)
                && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), inventory.getSlotLimit(slot)));
    }

    private void addOutput(int slot, ItemStack output) {
        ItemStack result = inventory.getStackInSlot(slot).copy();
        if (result.isEmpty()) result = output.copy(); else result.grow(output.getCount());
        inventory.setStackInSlot(slot, result);
    }

    @Override public void tick() {
        if (level == null || level.isClientSide) return;
        boolean wasLit = burnTime > 0;
        if (burnTime > 0) burnTime--;
        ItemStack input = inventory.getStackInSlot(2);
        if (!ItemStack.isSame(input, processingInput) || !ItemStack.tagMatches(input, processingInput)) progress = 0;
        processingInput = input.copy();
        Inventory recipeInput = new Inventory(input);
        OvenRecipe recipe = level.getRecipeManager().getRecipeFor(OvenRecipe.TYPE, recipeInput, level).orElse(null);
        ItemStack byproduct = recipe == null ? ItemStack.EMPTY : recipe.getByproduct();
        ItemStack output = input.isEmpty() ? ItemStack.EMPTY : recipe != null ? recipe.assemble(recipeInput)
                : level.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, recipeInput, level)
                    .map(smelting -> smelting.assemble(recipeInput)).orElse(ItemStack.EMPTY);
        boolean canProcess = !output.isEmpty() && fits(3, output)
                && (byproduct.isEmpty() || fits(4, byproduct));
        if (canProcess && burnTime == 0) {
            ItemStack fuel = inventory.getStackInSlot(0);
            int time = ForgeHooks.getBurnTime(fuel, IRecipeType.SMELTING);
            if (time > 0) {
                ItemStack remainder = fuel.getContainerItem();
                ItemStack rest = fuel.copy(); rest.shrink(1);
                inventory.setStackInSlot(0, rest.isEmpty() ? remainder : rest);
                burnTime = fuelTime = time;
            }
        }
        if (canProcess && burnTime > 0) {
            if (++progress >= 200) {
                addOutput(3, output);
                if (recipe != null && !byproduct.isEmpty() && (!recipe.requiresJar || !inventory.getStackInSlot(1).isEmpty())
                        && level.random.nextFloat() < recipe.chance) {
                    if (recipe.requiresJar) inventory.extractItem(1, 1, false);
                    addOutput(4, byproduct);
                }
                inventory.extractItem(2, 1, false);
                progress = 0;
            }
        } else if (!canProcess) progress = 0;
        else progress = Math.max(0, progress - 2);
        if (wasLit != (burnTime > 0)) level.setBlock(worldPosition, getBlockState().setValue(WitchesOvenBlock.LIT, burnTime > 0), 3);
        if (wasLit || burnTime > 0 || progress > 0) setChanged();
    }

    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("BurnTime", burnTime); tag.putInt("FuelTime", fuelTime); tag.putInt("Progress", progress);
        return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); inventory.deserializeNBT(tag.getCompound("Inventory"));
        burnTime = tag.getInt("BurnTime"); fuelTime = tag.getInt("FuelTime"); progress = tag.getInt("Progress");
        processingInput = inventory.getStackInSlot(2).copy();
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (side == Direction.DOWN ? outputs : inputs).cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); inputs.invalidate(); outputs.invalidate(); }
    @Override public ITextComponent getDisplayName() { return new TranslationTextComponent("block.bewitchment.witches_oven"); }
    @Override public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) { return new OvenMenu(id, playerInventory, this); }
}
