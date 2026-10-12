package com.bewitchment.machine;
import com.bewitchment.potion.*;
import com.google.gson.*;
import net.minecraft.advancements.*;
import net.minecraft.potion.*;
import net.minecraft.util.registry.Bootstrap;

public final class TargetedBrewsCheck {
    private static int checks;
    private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int flags=0;flags<32;flags++)check(TargetedBrewEffect.canine((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0,(flags&16)!=0,"example.Ordinary")== (flags!=0),"Independent canine compatibility paths");
        for(String name:new String[]{"example.Werewolf","example.WerewolfBoss","example.EntityCoyote","example.EntityFeralWolf","example.EntityFox"}){
            check(TargetedBrewEffect.legacyCanine(name),"Original compatibility class matching");
            check(TargetedBrewEffect.canine(false,false,false,false,false,name),"Class compatibility remains a fallback");
        }
        for(String name:new String[]{"example.WolfArmor","example.werewolf","example.EntityFoxExtra","example.EntityCoyoteHat","example.FoxEntity","example.Player"})check(!TargetedBrewEffect.legacyCanine(name),"No broader guessed suffix matching");
        for(int flags=0;flags<16;flags++)check(TargetedBrewEffect.deviant((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0)==(flags!=0),"Illager, witch, advanced player and tag alternatives");
        for(int amp:new int[]{0,1,2,255})check(TargetedBrewEffect.damage(amp)==8F*(amp+1),"Original damage per level");
        check(TargetedBrewEffect.damage(-1)==8&&TargetedBrewEffect.damage(Integer.MAX_VALUE)==2048,"Invalid amplifiers bounded");
        TargetedBrewEffect wolf=new TargetedBrewEffect(true),deviant=new TargetedBrewEffect(false);
        check(wolf.isInstantenous()&&wolf.isBeneficial()&&wolf.getColor()==0x3f00ff,"Wolfsbane metadata");
        check(deviant.isInstantenous()&&!deviant.isBeneficial()&&deviant.getColor()==0xdc7f10,"Deviant metadata");
        check(wolf.isDurationEffectTick(1,0)&&!wolf.isDurationEffectTick(0,0),"Command tick duration boundary");
        JsonObject root=AltarMilestone.resource("root"),altarJson=AltarMilestone.resource("crafted_altar");
        // Standalone bootstrap has vanilla items only; runtime validation parses the real IDs.
        root.getAsJsonObject("display").getAsJsonObject("icon").addProperty("item","minecraft:stone");
        altarJson.getAsJsonObject("display").getAsJsonObject("icon").addProperty("item","minecraft:stone");
        for(java.util.Map.Entry<String,JsonElement> entry:altarJson.getAsJsonObject("criteria").entrySet())
            entry.getValue().getAsJsonObject().getAsJsonObject("conditions").getAsJsonArray("items").get(0).getAsJsonObject().addProperty("item","minecraft:stone");
        Advancement altar=AltarMilestone.parse(root,altarJson);
        check(altar.getId().equals(TargetedBrewEffect.ALTAR)&&altar.getParent().getId().toString().equals("bewitchment:root"),"Original advancement identities");
        check(altar.getCriteria().size()==6&&altar.getRequirements().length==1&&altar.getRequirements()[0].length==6,"One OR group for six inventory variants");
        Gson gson=new GsonBuilder().registerTypeAdapter(AdvancementProgress.class,new AdvancementProgress.Serializer()).create();
        for(String criterion:altar.getCriteria().keySet()){
            AdvancementProgress progress=new AdvancementProgress();progress.update(altar.getCriteria(),altar.getRequirements());
            check(!progress.isDone(),"New player not a witch");
            check(progress.grantProgress(criterion)&&progress.isDone(),"Any one altar criterion completes milestone");
            check(!progress.grantProgress(criterion),"Repeated acquisition is idempotent");
            AdvancementProgress saved=gson.fromJson(gson.toJson(progress,AdvancementProgress.class),AdvancementProgress.class);saved.update(altar.getCriteria(),altar.getRequirements());
            check(saved.isDone(),"Vanilla advancement progress survives serialization");
            check(saved.revokeProgress(criterion)&&!saved.isDone(),"Revocation removes milestone completion");
            check(!saved.grantProgress("missing")&&!saved.isDone(),"Unknown criterion cannot grant witch status");
        }
        System.out.println("PASS: "+checks+" targeted brew eligibility, compatibility, damage and altar progression checks");
    }
}
