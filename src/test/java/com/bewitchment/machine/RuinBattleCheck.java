package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import com.bewitchment.potion.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;

public final class RuinBattleCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();MagicWeaknessEffect weakness=new MagicWeaknessEffect();RhinoHideEffect hide=new RhinoHideEffect();
        check(weakness.getCategory()==EffectType.HARMFUL && weakness.getColor()==0x7f00c0,"Legacy harmful purple Magic Weakness");
        check(hide.getCategory()==EffectType.BENEFICIAL && hide.getColor()==0xBEBEBE,"Legacy beneficial gray Rhino Hide");
        check(MagicWeaknessEffect.damage(4,0,DamageSource.MAGIC.isMagic())==7,"Magic Weakness I adds three");
        check(MagicWeaknessEffect.damage(4,1,true)==10,"Magic Weakness II adds six");
        check(MagicWeaknessEffect.damage(4,3,true)==16,"Amplifier scales vulnerability");
        check(MagicWeaknessEffect.damage(4,3,DamageSource.FALL.isMagic())==4,"Nonmagic damage unchanged");
        check(MagicWeaknessEffect.damage(.5F,0,true)==3.5F,"Post-mitigation fractional damage");
        check(MagicWeaknessEffect.damage(0,0,true)==3,"Legacy zero-amount damage-event behavior");
        check(MagicWeaknessEffect.damage(4,-1,true)==7,"Malformed negative amplifier treated as level one");
        check(Float.isFinite(MagicWeaknessEffect.damage(4,Integer.MAX_VALUE,true)),"Extreme amplifier does not overflow into negative damage");
        AttributeModifier modifier=hide.getAttributeModifiers().get(Attributes.ARMOR_TOUGHNESS);
        check(hide.getAttributeModifiers().size()==1 && modifier!=null,"Rhino Hide affects only toughness");
        check(modifier.getId().toString().equals(RhinoHideEffect.MODIFIER) && modifier.getOperation()==AttributeModifier.Operation.ADDITION,"Original modifier UUID and operation");
        check(hide.getAttributeModifierValue(0,modifier)==1 && hide.getAttributeModifierValue(2,modifier)==3,"One toughness per effect level");
        AttributeModifierManager attributes=new AttributeModifierManager(AttributeModifierMap.builder().add(Attributes.ARMOR_TOUGHNESS,2).build());
        hide.addAttributeModifiers(null,attributes,0);check(attributes.getValue(Attributes.ARMOR_TOUGHNESS)==3,"Modifier applied to existing toughness");
        hide.addAttributeModifiers(null,attributes,0);check(attributes.getValue(Attributes.ARMOR_TOUGHNESS)==3,"Reapplication does not stack duplicate modifier");
        hide.addAttributeModifiers(null,attributes,2);check(attributes.getValue(Attributes.ARMOR_TOUGHNESS)==5,"Stronger effect replaces weaker modifier");
        attributes.getInstance(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(UUID.randomUUID(),"Other armor",2,AttributeModifier.Operation.ADDITION));
        hide.removeAttributeModifiers(null,attributes,2);check(attributes.getValue(Attributes.ARMOR_TOUGHNESS)==4,"Removal restores base and preserves unrelated modifiers");
        check(!Kind.RUIN.positive && Kind.RUIN.cooldown==600,"Ruin targeting and cooldown");
        check(Kind.BATTLE.positive && Kind.BATTLE.cooldown==3000,"Battle targeting and cooldown");
        check(Kind.fromId("bewitchment:sigil_ruin")==Kind.RUIN && Kind.fromId("bewitchment:sigil_battle")==Kind.BATTLE,"Persisted kind IDs");
        System.out.println("PASS: "+checks+" Ruin, Battle and potion checks");
    }
}
