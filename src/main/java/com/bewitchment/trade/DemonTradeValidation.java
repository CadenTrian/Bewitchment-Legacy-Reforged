package com.bewitchment.trade;

import com.bewitchment.Bewitchment;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import java.util.Random;

@Mod.EventBusSubscriber(modid=Bewitchment.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class DemonTradeValidation {
    @SubscribeEvent public static void setup(FMLCommonSetupEvent event){event.enqueueWork(()->{
        int count=0;Random random=new Random(85);
        for(String career:DemonTrades.careers())for(int level=1;level<=5;level++)for(DemonTrades.Entry entry:DemonTrades.entries(career,level)){
            MerchantOffer offer=DemonTrades.generate(entry,random,false);
            if(offer.getResult().isEmpty() || offer.getCostA().isEmpty() || offer.getResult().getCount()>offer.getResult().getMaxStackSize())throw new IllegalStateException("Invalid registered demon trade");
            count++;
        }
        for(String career:DemonTrades.careers()){
            DemonMerchantState state=new DemonMerchantState(career);state.open(random,false);
            for(int next=2;next<=5;next++){
                state.completeTrade(state.offers().get(state.offers().size()-1),null,random);
                for(int tick=0;tick<40;tick++)state.tick(false,true,random);
                if(state.level()!=next)throw new IllegalStateException("Demon career failed to advance");
                state=DemonMerchantState.load(state.save());
            }
            if(state.offers().size()!=9)throw new IllegalStateException("Incorrect final demon offer count");
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Verified all three demon merchant lifecycles through level five and NBT reload");
        org.apache.logging.log4j.LogManager.getLogger().info("Verified {} demon trade definitions against live registries",count);
    });}
    private DemonTradeValidation(){}
}