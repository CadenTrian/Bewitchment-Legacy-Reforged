package com.bewitchment.potion;

import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import java.util.*;
import javax.annotation.Nullable;

public final class UtilityBrewEffect extends InstantEffect {
    public enum Kind {
        ABSENCE(false,0xffffff),PURIFICATION(false,0xff7fff),CORRUPTION(true,0x5c007f),
        SLEEPING(true,0xff007f),CURSED_LEAPS(true,0x10c440),BUTTERFINGERS(true,0xF8DE7E),DISROBING(true,0x407f20);
        final boolean harmful;final int color;
        Kind(boolean harmful,int color){this.harmful=harmful;this.color=color;}
        public boolean removes(EffectType category){return this==ABSENCE||this==PURIFICATION&&category==EffectType.HARMFUL||this==CORRUPTION&&category!=EffectType.HARMFUL;}
    }
    private final Kind kind;
    public UtilityBrewEffect(Kind kind){super(kind.harmful?EffectType.HARMFUL:EffectType.BENEFICIAL,kind.color);this.kind=kind;}
    public static int level(int amplifier){return Math.max(0,Math.min(255,amplifier))+1;}
    public static int sleepDuration(int amplifier){return 200*level(amplifier);}
    public static boolean lethalSleep(float health,int amplifier){return Float.isFinite(health)&&health>0&&health<=2F*level(amplifier);}
    public static double leap(int amplifier){return 1+level(amplifier)/4D;}
    @Override public void applyEffectTick(LivingEntity target,int amplifier){applyInstantenousEffect(null,null,target,amplifier,1);}
    @Override public void applyInstantenousEffect(@Nullable Entity source,@Nullable Entity owner,LivingEntity target,int amplifier,double intensity){
        if(target.level.isClientSide)return;
        switch(kind){
            case ABSENCE:case PURIFICATION:case CORRUPTION:
                // Remove only selected effects, preserving retained modifiers and hidden effect chains.
                for(EffectInstance effect:new ArrayList<>(target.getActiveEffects()))
                    if(kind.removes(effect.getEffect().getCategory()))target.removeEffect(effect.getEffect());
                break;
            case SLEEPING:
                target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,sleepDuration(amplifier),3));
                target.addEffect(new EffectInstance(Effects.BLINDNESS,sleepDuration(amplifier),3));
                if(lethalSleep(target.getHealth(),amplifier))target.hurt(DamageSource.MAGIC,Integer.MAX_VALUE);
                break;
            case CURSED_LEAPS:
                target.setDeltaMovement(target.getDeltaMovement().add(0,leap(amplifier),0));target.hasImpulse=true;
                if(target instanceof ServerPlayerEntity)((ServerPlayerEntity)target).connection.send(new SEntityVelocityPacket(target));
                break;
            case BUTTERFINGERS:
                for(EquipmentSlotType hand:new EquipmentSlotType[]{EquipmentSlotType.MAINHAND,EquipmentSlotType.OFFHAND}){
                    ItemStack held=target.getItemBySlot(hand);
                    if(!BrewEquipment.consumingPotion(target.isUsingItem(),target.getUseItem(),held))drop(target,equipment(target,hand),Integer.MAX_VALUE,1);
                }
                break;
            case DISROBING:
                List<BrewEquipment.Slot> slots=new ArrayList<>();
                for(EquipmentSlotType slot:new EquipmentSlotType[]{EquipmentSlotType.FEET,EquipmentSlotType.LEGS,EquipmentSlotType.CHEST,EquipmentSlotType.HEAD})slots.add(equipment(target,slot));
                if(target instanceof PlayerEntity)CuriosApi.getCuriosHelper().getCuriosHandler(target).ifPresent(handler->handler.getCurios().values().forEach(group->{
                    IDynamicStackHandler stacks=group.getStacks();
                    for(int i=0;i<stacks.getSlots();i++){final int index=i;slots.add(new BrewEquipment.Slot(){
                        public ItemStack get(){return stacks.getStackInSlot(index);}
                        public void set(ItemStack stack){stacks.setStackInSlot(index,stack);}
                    });}
                }));
                // Empty slots remain in the draw, as in the original armor/Baubles list.
                for(int i=0;i<level(amplifier);i++)if(target.getRandom().nextBoolean())drop(target,slots.get(target.getRandom().nextInt(slots.size())),1,0);
                break;
            default:break;
        }
    }
    private static BrewEquipment.Slot equipment(LivingEntity target,EquipmentSlotType type){return new BrewEquipment.Slot(){
        public ItemStack get(){return target.getItemBySlot(type);}
        public void set(ItemStack stack){target.setItemSlot(type,stack);}
    };}
    private static void drop(LivingEntity target,BrewEquipment.Slot slot,int count,double height){
        BrewEquipment.drop(slot,count,stack->{
            ItemEntity item=new ItemEntity(target.level,target.getX(),target.getY()+height,target.getZ(),stack);
            item.setDefaultPickUpDelay();return target.level.addFreshEntity(item);
        });
    }
}
