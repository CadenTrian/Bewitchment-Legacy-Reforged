package com.bewitchment.item;

import com.bewitchment.entity.*;
import com.bewitchment.registry.ModBrooms;
import com.bewitchment.registry.ModItems;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tags.*;
import net.minecraft.block.Block;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import java.util.*;

public final class BroomItem extends Item {
    public static final ITag.INamedTag<Block> SWEEPABLE=BlockTags.createOptional(new ResourceLocation("bewitchment:broom_sweepables"));
    private static final Set<UUID> ACTIVE=new HashSet<>(); // Server main thread only.
    public final BroomRules.Kind kind;
    public BroomItem(BroomRules.Kind kind){super(new Properties().stacksTo(1).tab(ModItems.TAB));this.kind=kind;}
    @Override public ActionResultType useOn(ItemUseContext context){
        PlayerEntity player=context.getPlayer();BlockPos pos=context.getClickedPos();
        if(player==null||player.isSpectator()||!player.abilities.mayBuild||!context.getLevel().hasChunkAt(pos)
                ||!context.getLevel().mayInteract(player,pos)||!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()))return ActionResultType.FAIL;
        if(context.getLevel().isClientSide)return ActionResultType.SUCCESS;
        if(!ACTIVE.add(player.getUUID()))return ActionResultType.FAIL;
        try{
            ServerWorld world=(ServerWorld)context.getLevel();
            if(kind==null){
                if(!world.getBlockState(pos).is(SWEEPABLE))return ActionResultType.PASS;
                if(!(player instanceof ServerPlayerEntity)||net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world,((ServerPlayerEntity)player).gameMode.getGameModeForPlayer(),(ServerPlayerEntity)player,pos)<0)return ActionResultType.FAIL;
                if(!world.getBlockState(pos).is(SWEEPABLE)||!world.destroyBlock(pos,true,player))return ActionResultType.FAIL;
                world.playSound(null,pos,ModBrooms.SWEEP.get(),SoundCategory.BLOCKS,.8F,.8F+world.random.nextFloat()*.4F);
                world.sendParticles(net.minecraft.particles.ParticleTypes.SWEEP_ATTACK,pos.getX()+.5,pos.getY()+.1,pos.getZ()+.5,1,0,0,0,0);
                return ActionResultType.CONSUME;
            }
            BroomEntity broom=ModBrooms.ENTITIES.get(kind).get().create(world);
            if(broom==null)return ActionResultType.FAIL;
            broom.moveTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,player.yRot,0);broom.setItem(context.getItemInHand());
            if(!BroomEntity.loadedSpace(world,broom.getBoundingBox())||!world.noCollision(broom)||world.containsAnyLiquid(broom.getBoundingBox())||!world.addFreshEntity(broom))return ActionResultType.FAIL;
            context.getItemInHand().shrink(1);return ActionResultType.CONSUME;
        }finally{ACTIVE.remove(player.getUUID());}
    }
    @Override public boolean isFoil(ItemStack stack){return stack.hasTag();}
    @Override public void appendHoverText(ItemStack stack,net.minecraft.world.World world,List<net.minecraft.util.text.ITextComponent> lines,net.minecraft.client.util.ITooltipFlag flag){
        ChestSigilItem.Kind sigil=stack.hasTag()?BroomRules.sigil(stack.getTag().getString("sigil")):null;
        if(sigil!=null)lines.add(new net.minecraft.util.text.TranslationTextComponent("item."+sigil.id().replace(':','.')));
    }
}
