package com.bewitchment.block;

import com.bewitchment.combat.MaterialEffects;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import java.util.Random;

public final class FrostfireBlock extends Block {
    private static final ITag.INamedTag<Item> IRON_ORES = ItemTags.createOptional(new ResourceLocation("forge", "ores/iron"));

    public FrostfireBlock() {
        super(Properties.of(Material.FIRE).noCollission().noOcclusion().strength(0).sound(SoundType.SNOW)
                .lightLevel(state -> 7).noDrops());
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction face, BlockState neighbor, IWorld world, BlockPos pos, BlockPos other) {
        return canSurvive(state, world, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState old, boolean moving) {
        if (!world.isClientSide && !old.is(this)) world.getBlockTicks().scheduleTick(pos, this, 20);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!canSurvive(state, world, pos)) { world.removeBlock(pos, false); return; }
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(pos))) {
            ItemStack input = entity.getItem();
            if (entity.isAlive() && IRON_ORES.contains(input.getItem())) {
                boolean converted = FrostfireConversion.convert(input, new ItemStack(ModItems.COLD_IRON_INGOT.get()), stack -> {
                    ItemEntity output = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, stack);
                    output.setDefaultPickUpDelay();
                    return world.addFreshEntity(output);
                });
                if (converted) {
                    if (input.isEmpty()) entity.remove(); else entity.setItem(input.copy());
                    world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.25F, 0.5F);
                }
            }
        }
        world.getBlockTicks().scheduleTick(pos, this, 20);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity && MaterialEffects.COLD_IRON_WEAK.contains(entity.getType()))
            entity.hurt(DamageSource.MAGIC, 2);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return BottledFireCollection.collect(world, pos, player, hand, hit, ModItems.BOTTLED_FROSTFIRE);
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextBoolean()) world.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextDouble(),
                pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
    }

    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.DESTROY; }
}
