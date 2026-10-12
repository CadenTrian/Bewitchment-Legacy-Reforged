package com.bewitchment.machine;

import com.bewitchment.block.BrazierBlock;
import com.bewitchment.machine.IncenseData.Kind;
import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class IncenseCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static EffectInstance effect(List<EffectInstance> list,Effect type){return list.stream().filter(e->e.getEffect()==type).findFirst().orElseThrow(()->new AssertionError("Missing effect"));}
    private static String signature(List<Kind> kinds){StringBuilder s=new StringBuilder();IncenseData.wakingEffects(kinds).stream().sorted(Comparator.comparing(e->e.getEffect().getDescriptionId())).forEach(e->s.append(e.getEffect().getDescriptionId()).append(':').append(e.getDuration()).append(':').append(e.getAmplifier()).append(';'));return s.toString();}
    public static void main(String[] args){
        Bootstrap.bootStrap();net.minecraftforge.common.crafting.CraftingHelper.register(new ResourceLocation("minecraft:item"),net.minecraftforge.common.crafting.VanillaIngredientSerializer.INSTANCE);
        IncenseData data=new IncenseData();ResourceLocation id=new ResourceLocation("bewitchment:test_incense");IncenseRecipe.Serializer serializer=new IncenseRecipe.Serializer();
        for(Kind kind:Kind.values()){
            data.start(kind);check(data.kind()==kind && data.remaining()==601,"Fresh burn "+kind);
            for(int second=0;second<600;second++)if(!data.second())throw new AssertionError("Early expiry");
            check(data.kind()==kind && data.remaining()==1,"Legacy final second "+kind);
            IncenseData copy=new IncenseData();copy.load(data.save());check(copy.kind()==kind && copy.remaining()==1,"Saved final second "+kind);
            check(!copy.second() && copy.kind()==null && copy.remaining()==0,"Expiry clears identity "+kind);
            JsonObject json=new JsonParser().parse("{\"incense\":\""+kind.id+"\",\"ingredients\":[{\"item\":\"minecraft:coal\"}]}").getAsJsonObject();
            IncenseRecipe recipe=serializer.fromJson(id,json);PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());serializer.toNetwork(buffer,recipe);IncenseRecipe decoded=serializer.fromNetwork(id,buffer);buffer.release();
            Inventory inv=new Inventory(9);inv.setItem(8,new ItemStack(Items.COAL));check(decoded.incense==kind && decoded.matches(inv,null),"JSON and network identity "+kind);
        }
        check(IncenseData.wakingEffects(Collections.emptyList()).isEmpty(),"No sources grants nothing");
        check(IncenseData.wakingEffects(Arrays.asList(Kind.INTENSITY,Kind.CONCENTRATION,Kind.CONCENTRATION)).isEmpty(),"Mixed boosters alone are safe and grant nothing");
        Effect[][] expected={{Effects.DAMAGE_RESISTANCE,Effects.DAMAGE_BOOST},{Effects.JUMP,Effects.MOVEMENT_SPEED},{Effects.DIG_SPEED,Effects.ABSORPTION},{Effects.NIGHT_VISION},{Effects.ABSORPTION,Effects.SATURATION}};
        for(int i=0;i<expected.length;i++){
            List<EffectInstance> effects=IncenseData.wakingEffects(Collections.singletonList(Kind.values()[i]));check(effects.size()==expected[i].length,"Exact effect count "+i);
            for(Effect type:expected[i]){EffectInstance e=effect(effects,type);check(e.getDuration()==(type==Effects.SATURATION?60:12000) && e.getAmplifier()==(type==Effects.SATURATION?1:0),"Legacy base effect "+type);}
        }
        for(int intensity=0;intensity<5;intensity++){
            List<Kind> sources=new ArrayList<>(Arrays.asList(Kind.FULLNESS,Kind.INVIGORATING,Kind.CONCENTRATION,Kind.CONCENTRATION));for(int n=0;n<intensity;n++)sources.add(Kind.INTENSITY);
            List<EffectInstance> effects=IncenseData.wakingEffects(sources);EffectInstance absorption=effect(effects,Effects.ABSORPTION),saturation=effect(effects,Effects.SATURATION);
            check(effects.size()==3 && absorption.getAmplifier()==Math.min(2,intensity) && absorption.getDuration()==(intensity==0?24000:12000),"Booster precedence, cap and duplicate effects "+intensity);
            check(saturation.getAmplifier()==1 && saturation.getDuration()==60,"Saturation unaffected by boosters "+intensity);
            String original=signature(sources);Collections.reverse(sources);check(signature(sources).equals(original),"Position scan order independent "+intensity);
        }
        for(int bad:new int[]{-1,0,602,Integer.MAX_VALUE}){CompoundNBT tag=new CompoundNBT();tag.putString("kind",Kind.VITALITY.id);tag.putInt("remaining",bad);data.start(Kind.DEFTNESS);data.load(tag);check(data.kind()==null && data.remaining()==0,"Reject invalid timer "+bad);}
        CompoundNBT tag=new CompoundNBT();tag.putString("kind","bewitchment:unknown");tag.putInt("remaining",20);data.load(tag);check(data.kind()==null,"Unknown saved identity rejected");
        tag.putString("kind",Kind.VITALITY.id);tag.putString("remaining","20");data.load(tag);check(data.kind()==null,"Wrong timer type rejected");
        data.start(Kind.VITALITY);data.clear();check(data.save().isEmpty() && !data.second(),"Extinguishing clears saved state");
        NonNullList<Ingredient> inputs=NonNullList.create();inputs.add(Ingredient.of(Items.COAL,Items.CHARCOAL));inputs.add(Ingredient.of(Items.COAL));IncenseRecipe recipe=new IncenseRecipe(id,inputs,Kind.INTENSITY);
        Inventory inv=new Inventory(9);inv.setItem(1,new ItemStack(Items.COAL));inv.setItem(8,new ItemStack(Items.CHARCOAL));check(recipe.matches(inv,null),"Shapeless overlapping alternatives");
        inv.setItem(8,new ItemStack(Items.COAL));check(recipe.matches(inv,null),"Repeated ingredient slots");inv.setItem(0,new ItemStack(Items.STICK));check(!recipe.matches(inv,null),"Extra item rejected");inv.setItem(0,ItemStack.EMPTY);inv.setItem(8,new ItemStack(Items.COAL,2));check(!recipe.matches(inv,null),"Oversized stack rejected");inv.setItem(8,ItemStack.EMPTY);check(!recipe.matches(inv,null),"Missing repeat rejected");
        inputs.clear();recipe.getIngredients().clear();check(recipe.getIngredients().size()==2,"Defensive ingredient lists");
        for(int size:new int[]{0,9,-1}){PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());buffer.writeVarInt(size);boolean rejected=false;try{serializer.fromNetwork(id,buffer);}catch(IllegalArgumentException ex){rejected=true;}finally{buffer.release();}check(rejected,"Bad network size "+size);}
        BrazierTile tile=new BrazierTile(TileEntityType.CHEST);tile.incense.start(Kind.CATS_EYE);tile.incense.second();ItemStack paper=new ItemStack(Items.PAPER);paper.getOrCreateTag().putString("keep","yes");tile.contents.setItem(8,paper);
        BrazierTile loaded=new BrazierTile(TileEntityType.CHEST);loaded.load(new BrazierBlock().defaultBlockState().setValue(BrazierBlock.LIT,true),tile.save(new CompoundNBT()));
        check(loaded.incenseKind()==Kind.CATS_EYE && loaded.incense.remaining()==600,"Tile burn survives save without reset");
        check(loaded.contents.getItem(8).getTag().getString("keep").equals("yes"),"Burn persistence preserves inventory");
        check(loaded.getUpdateTag().isEmpty(),"Burn state remains server-owned");
        loaded.load(new BrazierBlock().defaultBlockState(),new CompoundNBT());check(loaded.incenseKind()==null,"Old curse Brazier has no incense");
        System.out.println("PASS: "+checks+" incense checks");
    }
}
