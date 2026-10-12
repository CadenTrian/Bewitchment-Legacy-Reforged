package com.bewitchment.item;

import com.bewitchment.registry.ModItems;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import java.util.*;

/** Restored sigils can be installed in chests or drawn on solid faces. */
public final class ChestSigilItem extends BlockItem {
    public enum Kind {
        MENDING(true,600), BINDING(false,600), CLEANSING(true,1200), LUCK(true,6000), DISORIENTATION(false,600), SHRIEKING(false,0), RUIN(false,600), BATTLE(true,3000), SENTINEL(false,600), FAILURE(false,600), PURITY(true,6000);
        public final boolean positive;public final int cooldown;
        Kind(boolean positive,int cooldown){this.positive=positive;this.cooldown=cooldown;}
        public String id(){return "bewitchment:sigil_"+name().toLowerCase(Locale.ROOT);}
        public static Kind fromId(String id){for(Kind kind:values())if(kind.id().equals(id))return kind;return null;}
        public List<EffectInstance> effects(){
            switch(this){
                case MENDING:return Collections.singletonList(new EffectInstance(Effects.REGENERATION,100,0));
                case BINDING:return Collections.singletonList(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,300,5));
                case LUCK:return Collections.singletonList(new EffectInstance(Effects.LUCK,6000,0));
                case DISORIENTATION:return Collections.singletonList(new EffectInstance(ModEffects.FEAR.get(),300,0));
                case SHRIEKING:return Collections.singletonList(new EffectInstance(Effects.CONFUSION,100,0));
                case RUIN:return Arrays.asList(new EffectInstance(Effects.HUNGER,200,0),new EffectInstance(Effects.BLINDNESS,200,0),new EffectInstance(Effects.CONFUSION,200,0),new EffectInstance(Effects.WITHER,200,0),new EffectInstance(ModEffects.MAGIC_WEAKNESS.get(),200,0));
                case BATTLE:return Arrays.asList(new EffectInstance(Effects.DAMAGE_BOOST,1200,0),new EffectInstance(Effects.DAMAGE_RESISTANCE,1200,0),new EffectInstance(ModEffects.RHINO_HIDE.get(),1200,0));
                default:return Collections.emptyList();
            }
        }
        public void apply(LivingEntity player){
            if(player.level.isClientSide)return; if(this==PURITY)com.bewitchment.combat.PlayerCurses.purify(player); if(this==FAILURE)com.bewitchment.machine.RitualInhibition.apply(player); if(this==SENTINEL)com.bewitchment.machine.SentinelSigil.apply(player);
            if(this==CLEANSING)for(EffectInstance effect:new ArrayList<>(player.getActiveEffects()))
                if(effect.getEffect().getCategory()==EffectType.HARMFUL)player.removeEffect(effect.getEffect());
            for(EffectInstance effect:effects())player.addEffect(effect);
            if(this==SHRIEKING && player instanceof PlayerEntity)((PlayerEntity)player).playNotifySound(SoundEvents.GHAST_HURT,SoundCategory.BLOCKS,1,1);
        }
    }
    public final Kind kind;
    public ChestSigilItem(Kind kind){super(com.bewitchment.registry.ModBlocks.SIGIL.get(),new Properties().tab(ModItems.TAB));this.kind=kind;}
    @Override public String getDescriptionId(){return "item."+kind.id().replace(':','.');}
    @Override public void fillItemCategory(ItemGroup group,NonNullList<ItemStack> items){if(allowdedIn(group))items.add(new ItemStack(this));}
    // Multiple identities share a block; none is its generic item or an implicit drop.
    @Override public void registerBlocks(Map<Block,Item> map,Item item){}
    @Override public void removeFromBlockToItemMap(Map<Block,Item> map,Item item){}
    @Override protected SoundEvent getPlaceSound(BlockState state,World world,BlockPos pos,PlayerEntity player){return com.bewitchment.registry.ModRituals.CHALK_SOUND.get();}
    @Override protected boolean canPlace(BlockItemUseContext context,BlockState state){
        PlayerEntity player=context.getPlayer();World world=context.getLevel();BlockPos pos=context.getClickedPos();
        return player!=null && player.abilities.mayBuild && !player.isSpectator() && world.hasChunkAt(pos)
            && world.getWorldBorder().isWithinBounds(pos) && world.getFluidState(pos).isEmpty()
            && world.mayInteract(player,pos) && player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()) && super.canPlace(context,state);
    }
    public static void verify(){
        com.bewitchment.combat.ParanoiaCurse.verify();
        com.bewitchment.combat.CurseCasting.verify();
        for(Kind kind:Kind.values()){
            Item item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(kind.id()));
            if(!(item instanceof ChestSigilItem) || ((ChestSigilItem)item).kind!=kind)throw new IllegalStateException("Missing chest sigil "+kind);
            if(!item.getDescriptionId().equals("item."+kind.id().replace(':','.')))throw new IllegalStateException("Lost sigil item identity "+kind);
            NonNullList<ItemStack> creative=NonNullList.create();item.fillItemCategory(ModItems.TAB,creative);
            if(creative.size()!=1 || creative.get(0).getItem()!=item)throw new IllegalStateException("Missing creative sigil "+kind);
            for(EffectInstance effect:kind.effects())if(effect.getEffect()==null)throw new IllegalStateException("Missing sigil effect "+kind);
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment {} sigil registrations and effects verified",Kind.values().length);
        if(com.bewitchment.registry.ModBlocks.SIGIL.get().asItem()!=Items.AIR)throw new IllegalStateException("Placed sigil must not have a generic item");
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment placed sigil item identities and creative entries verified");
        Block door = com.bewitchment.registry.ModTreeBlocks.DRAGONS_BLOOD.door.get();
        Block trapdoor = com.bewitchment.registry.ModTreeBlocks.DRAGONS_BLOOD.trapdoor.get();
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.HALF, net.minecraft.state.properties.DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, net.minecraft.state.properties.DoubleBlockHalf.UPPER);
        if (!(door instanceof com.bewitchment.block.DragonsBloodDoor) || !(trapdoor instanceof com.bewitchment.block.DragonsBloodTrapdoor)
                || !lower.hasTileEntity() || upper.hasTileEntity() || !trapdoor.defaultBlockState().hasTileEntity()
                || door.createTileEntity(lower, null).getType() != com.bewitchment.machine.ModMachines.DRAGONS_BLOOD_SIGIL.get()
                || trapdoor.createTileEntity(trapdoor.defaultBlockState(), null).getType() != com.bewitchment.machine.ModMachines.DRAGONS_BLOOD_SIGIL.get())
            throw new IllegalStateException("Missing Dragon's Blood door/trapdoor sigil storage");
        org.apache.logging.log4j.LogManager.getLogger().info("Verified Dragon's Blood door/trapdoor sigil storage and single lower-half ownership");
    }
}
