package com.bewitchment.item;

import com.bewitchment.machine.*;
import com.bewitchment.entity.FeuerwurmEntity;
import com.bewitchment.registry.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.stream.Collectors;

public final class SealedEvilBoxItem extends Item {
    public SealedEvilBoxItem(){super(new Properties().tab(ModItems.TAB));}
    public static boolean usable(boolean alive,boolean spectator,boolean sneaking){return alive && !spectator && !sneaking;}
    @Override public ActionResult<ItemStack> use(World level,PlayerEntity player,Hand hand){
        ItemStack held=player.getItemInHand(hand);
        if(!usable(player.isAlive(),player.isSpectator(),player.isShiftKeyDown()))return ActionResult.pass(held);
        if(level.isClientSide)return ActionResult.success(held);
        SealedBoxPlan plan=SealedBoxPlan.random(player.getRandom());
        held.shrink(1); // Consume the used hand, including creative, as a single server operation.
        player.inventory.setChanged();
        apply((ServerWorld)level,player,plan);
        level.playSound(null,player.blockPosition(),SoundEvents.EVOKER_CAST_SPELL,SoundCategory.PLAYERS,5,1);
        return ActionResult.consume(held);
    }
    private static void give(PlayerEntity player,ItemStack stack){if(!stack.isEmpty() && !player.inventory.add(stack))player.drop(stack,false);}
    private static ItemStack gift(String id,int count){Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));return item==null?ItemStack.EMPTY:new ItemStack(item,count);}
    private static void potion(PlayerEntity player,Effect effect,int ticks,int amplifier){player.addEffect(new EffectInstance(effect,ticks,amplifier));}
    private static void apply(ServerWorld world,PlayerEntity player,SealedBoxPlan plan){
        for(SealedBoxPlan.Gift gift:plan.gifts)give(player,gift(gift.id,gift.count));
        for(SealedBoxPlan.Potion effect:plan.potions){Effect type=ForgeRegistries.POTIONS.getValue(new ResourceLocation(effect.id));if(type!=null)potion(player,type,effect.ticks,effect.amplifier);}
        switch(plan.outcome){
            case 0:
                LootContext context=new LootContext.Builder(world).withRandom(player.getRandom()).withLuck(player.getLuck())
                    .withParameter(LootParameters.ORIGIN,player.position()).withOptionalParameter(LootParameters.THIS_ENTITY,player).create(LootParameterSets.CHEST);
                for(ItemStack stack:world.getServer().getLootTables().get(new ResourceLocation("minecraft:chests/desert_pyramid")).getRandomItems(context))give(player,stack);
                break;
            case 1:
                List<Item> accessories=ForgeRegistries.ITEMS.getValues().stream().filter(i->i instanceof LegacyAccessoryItem)
                    .sorted(Comparator.comparing(i->i.getRegistryName().toString())).collect(Collectors.toList());
                if(!accessories.isEmpty())give(player,new ItemStack(accessories.get(player.getRandom().nextInt(accessories.size()))));break;


            case 4:
                for(Block block:ForgeRegistries.BLOCKS.getValues())if(Tags.Blocks.ORES.contains(block) && block.asItem()!=Items.AIR)player.drop(new ItemStack(block),false);break;
            case 7:give(player,ContractTasks.random(ModItems.CONTRACT.get(),player.getRandom()));break;
            case 14:world.setWeatherParameters(0,plan.weatherTicks,true,true);break;
            case 15:world.setDayTime(SealedBoxPlan.midnight(world.getDayTime()));break;



            case 22:
                LightningBoltEntity bolt=EntityType.LIGHTNING_BOLT.create(world);
                if(bolt!=null){bolt.moveTo(player.getX(),player.getY(),player.getZ());bolt.setVisualOnly(true);world.addFreshEntity(bolt);}break;
            case 25:
                for(BlockPos pos:BlockPos.betweenClosed(player.blockPosition().offset(-1,0,-1),player.blockPosition().offset(1,2,1)))
                    if(pos.getY()>=0 && pos.getY()<world.getMaxBuildHeight() && world.hasChunkAt(pos) && world.getBlockState(pos).getMaterial().isReplaceable()
                        && player.abilities.mayBuild && world.mayInteract(player,pos) && player.mayUseItemAt(pos,Direction.DOWN,ItemStack.EMPTY))world.setBlockAndUpdate(pos,Blocks.COBWEB.defaultBlockState());
                break;
            default:break;
        }
        for(SealedBoxPlan.Gift spawn:plan.spawns)spawn(world,player,spawn,plan.outcome==12);
    }
    private static void spawn(ServerWorld world,PlayerEntity player,SealedBoxPlan.Gift request,boolean spectral){
        EntityType<?> type=ForgeRegistries.ENTITIES.getValue(new ResourceLocation(request.id));
        if(type==null || world.getDifficulty()==Difficulty.PEACEFUL && type.getCategory()==EntityClassification.MONSTER)return;
        for(int i=0;i<request.count;i++){
            Entity entity=type.create(world);if(entity==null)continue;
            SealedBoxPlan.place(()->{
                double spread=request.id.equals("minecraft:bat") || request.id.equals("bewitchment:raven")?1:2;
                entity.moveTo(player.getX()+player.getRandom().nextGaussian()*spread,player.getY()+(spread==1?1:.5),player.getZ()+player.getRandom().nextGaussian()*spread,0,0);
                BlockPos pos=entity.blockPosition();
                if(pos.getY()<0 || entity.getBoundingBox().maxY>world.getMaxBuildHeight() || !world.hasChunksAt(new BlockPos(entity.getBoundingBox().minX,pos.getY(),entity.getBoundingBox().minZ),new BlockPos(entity.getBoundingBox().maxX,pos.getY(),entity.getBoundingBox().maxZ))
                    || !world.getWorldBorder().isWithinBounds(entity.getBoundingBox()) || !world.noCollision(entity))return false;
                if(entity instanceof MobEntity){MobEntity mob=(MobEntity)entity;mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),SpawnReason.MOB_SUMMONED,null,null);
                    if(spectral && mob instanceof FeuerwurmEntity)((FeuerwurmEntity)mob).summon(null,1200,((FeuerwurmEntity)mob).skin());
                    if(type.getCategory()==EntityClassification.MONSTER && !request.id.equals("minecraft:cave_spider") && !request.id.equals("minecraft:silverfish"))mob.setTarget(player);
                }
                world.addFreshEntity(entity);return true;
            });
        }
    }
}