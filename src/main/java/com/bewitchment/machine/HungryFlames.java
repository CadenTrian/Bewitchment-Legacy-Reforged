package com.bewitchment.machine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.server.ServerWorld;

/** Legacy two-second, one-item-per-stack smelting pulse. */
public final class HungryFlames {
    public static boolean due(long gameTime) { return Math.floorMod(gameTime, 40L) == 0; }
    public static AxisAlignedBB bounds(BlockPos center) { return new AxisAlignedBB(center).inflate(3); }
    static final class Outcome {
        final ItemStack remaining, product;
        final int experience;
        Outcome(ItemStack remaining, ItemStack product, int experience) {
            this.remaining = remaining; this.product = product; this.experience = experience;
        }
    }
    static Outcome plan(ItemStack input, ItemStack cooked, double roll) {
        if (input.isEmpty() || input.getItem().hasContainerItem(input) || cooked.isEmpty()
                || cooked.getCount() > cooked.getMaxStackSize()) return null;
        if (!Double.isFinite(roll) || roll < 0 || roll >= 1) throw new IllegalArgumentException("Invalid smelting roll");
        ItemStack remaining = input.copy(); remaining.shrink(1);
        return new Outcome(remaining, roll < .7 ? cooked.copy() : ItemStack.EMPTY, roll < .7 ? 0 : 2);
    }
    public static void tick(ServerWorld world, BlockPos center) {
        world.sendParticles(ParticleTypes.FLAME, center.getX() + .5, center.getY() + .25, center.getZ() + .5, 1, 2, .25, 2, .05);
        if (!due(world.getGameTime())) return;
        // This snapshot excludes outputs spawned during the current pulse.
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, bounds(center))) {
            if (!entity.isAlive()) continue;
            ItemStack input = entity.getItem().copy();
            if (input.isEmpty() || input.getItem().hasContainerItem(input)) continue;
            ItemStack unit = input.copy(); unit.setCount(1);
            Inventory inventory = new Inventory(unit);
            ItemStack cooked = world.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, inventory, world)
                    .map(recipe -> recipe.assemble(inventory)).orElse(ItemStack.EMPTY);
            if (cooked.isEmpty()) continue;
            Outcome result = plan(input, cooked, world.random.nextDouble());
            if (result == null) continue;
            Entity output;
            if (result.product.isEmpty()) output = new ExperienceOrbEntity(world, entity.getX(), entity.getY(), entity.getZ(), result.experience);
            else {
                ItemEntity item = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), result.product);
                item.setDefaultPickUpDelay(); output = item;
            }
            // Reserve the input before spawning. Failed spawn restores it only if a callback has not altered it.
            entity.setItem(result.remaining);
            if (!world.addFreshEntity(output)) {
                if (entity.isAlive() && ItemStack.matches(entity.getItem(), result.remaining)) entity.setItem(input);
            } else if (entity.getItem().isEmpty()) entity.remove();
        }
    }
    private HungryFlames() {}
}
