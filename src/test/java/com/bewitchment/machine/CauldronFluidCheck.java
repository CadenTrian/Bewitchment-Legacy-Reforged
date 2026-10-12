package com.bewitchment.machine;

import com.bewitchment.block.WitchesCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.util.registry.Bootstrap;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.Reader;

/** Native model parsing catches syntactically valid JSON with the wrong Minecraft schema. */
public final class CauldronFluidCheck {
    public static void main(String[] args)throws Exception {
        Bootstrap.bootStrap();
        WitchesCauldronBlock block=new WitchesCauldronBlock();
        if(block.defaultBlockState().getValue(WitchesCauldronBlock.FILLED)||block.defaultBlockState().getValue(WitchesCauldronBlock.LAVA))throw new AssertionError("New cauldron must be empty");
        if(block.getStateDefinition().getPossibleStates().size()!=4)throw new AssertionError("Both fluid properties must be registered");
        BlockState water=block.defaultBlockState().setValue(WitchesCauldronBlock.FILLED,true);
        if(water.getValue(WitchesCauldronBlock.LAVA))throw new AssertionError("Existing filled-only states remain water");
        for(boolean lava:new boolean[]{false,true})for(int heat:new int[]{0,1,4,5,6}){
            java.util.List<String> actions=new java.util.ArrayList<>();
            CauldronContact.apply(lava,heat,new CauldronContact.Target(){
                public void extinguish(){actions.add("extinguish");}
                public void hurt(net.minecraft.util.DamageSource source,float amount){
                    if(source!=(lava?net.minecraft.util.DamageSource.LAVA:net.minecraft.util.DamageSource.HOT_FLOOR))throw new AssertionError("Wrong contact damage source");
                    actions.add("damage:"+amount);
                }
                public void ignite(int seconds){actions.add("ignite:"+seconds);}
            });
            java.util.List<String> expected=lava?java.util.Arrays.asList("damage:4.0","ignite:15"):
                    heat>=5?java.util.Arrays.asList("extinguish","damage:1.0"):java.util.Arrays.asList("extinguish");
            if(!actions.equals(expected))throw new AssertionError("Fluid contact order/heat boundary: "+actions);
        }
        int count=0;
        try(java.util.stream.Stream<Path> paths=Files.walk(Paths.get("src/main/resources/assets/bewitchment/models"))){
            for(Path path:(Iterable<Path>)paths.filter(p->p.toString().endsWith(".json"))::iterator){
                try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){BlockModel.fromStream(reader);count++;}
                catch(Exception error){throw new IllegalStateException("Invalid native model: "+path,error);}
            }
        }
        boolean rejected=false;
        try{BlockModel.fromString("{\"parent\":\"minecraft:item/handheld\",\"overrides\":{\"predicate\":{\"bewitchment:legacy_variant\":1},\"model\":\"bewitchment:item/cold_iron_sword_variant\"}}");}
        catch(com.google.gson.JsonParseException expected){rejected=true;}
        if(!rejected)throw new AssertionError("Native model check must reject the previous broken sword shape");
        System.out.println("PASS: 10 water/lava contact cases, cauldron default/backward-compatible fluid states and "+count+" native models; malformed sword regression rejected");
    }
}
