package com.bewitchment.trade;

import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.ChestSigilData;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import java.util.*;

/** Server-owned state; the future merchant adapter must call completeTrade exactly once per purchase. */
public final class DemonMerchantState {
    @FunctionalInterface public interface Factory {MerchantOffer create(DemonTrades.Entry entry,Random random,boolean discounted);}
    public static final class TradeResult {
        public final boolean accepted,scheduledReset;public final int xp;
        private TradeResult(boolean accepted,boolean scheduledReset,int xp){this.accepted=accepted;this.scheduledReset=scheduledReset;this.xp=xp;}
    }
    private static final List<String> CAREERS=Collections.unmodifiableList(new ArrayList<>(DemonTrades.careers()));
    private final String career;
    private final MerchantOffers offers=new MerchantOffers();
    private int level,resetTicks;
    private UUID lastBuyer;
    public DemonMerchantState(String career){if(!CAREERS.contains(career))throw new IllegalArgumentException("Unknown demon career");this.career=career;}
    public static DemonMerchantState random(Random random){return new DemonMerchantState(CAREERS.get(random.nextInt(CAREERS.size())));}
    public String career(){return career;}
    public int level(){return level;}
    public int resetTicks(){return resetTicks;}
    public UUID lastBuyer(){return lastBuyer;}
    public List<MerchantOffer> offers(){return Collections.unmodifiableList(offers);}
    public void open(Random random,boolean discounted){open(random,discounted,DemonTrades::generate);}
    public void open(Random random,boolean discounted,Factory factory){
        if(level!=0)return;
        List<MerchantOffer> initial=generate(1,random,discounted,factory);
        offers.addAll(initial);level=1;
    }
    private List<MerchantOffer> generate(int next,Random random,boolean discounted,Factory factory){
        List<MerchantOffer> generated=new ArrayList<>();
        for(DemonTrades.Entry entry:DemonTrades.select(career,next,random)){
            MerchantOffer offer=Objects.requireNonNull(factory.create(entry,random,discounted),"Missing generated offer");
            validate(offer);generated.add(offer);
        }
        return generated;
    }
    public TradeResult completeTrade(MerchantOffer offer,UUID buyer,Random random){
        if(offer==null || !offers.contains(offer) || offer.isOutOfStock())return new TradeResult(false,false,0);
        offer.increaseUses();int xp=3+random.nextInt(4);
        boolean schedule=offer.getUses()==1 || random.nextInt(5)==0;
        if(schedule){resetTicks=40;lastBuyer=buyer;xp+=5;}
        return new TradeResult(true,schedule,offer.shouldRewardExp()?xp:0);
    }
    public boolean tick(boolean trading,boolean discounted,Random random){return tick(trading,discounted,random,DemonTrades::generate);}
    /** Returns true once when the caller should apply 200 ticks of Regeneration I. */
    public boolean tick(boolean trading,boolean discounted,Random random,Factory factory){
        if(trading || resetTicks==0)return false;
        if(resetTicks>1){resetTicks--;return false;}
        // Stage everything before committing, so generation failure cannot partly restock a merchant.
        List<MerchantOffer> replacements=new ArrayList<>(offers);
        for(int i=0;i<offers.size();i++){
            MerchantOffer offer=offers.get(i);
            if(offer.isOutOfStock() && !(offer.getResult().getItem() instanceof KillContractItem)){
                CompoundNBT tag=offer.createTag();int increase=random.nextInt(6)+random.nextInt(6)+2;
                tag.putInt("maxUses",(int)Math.min(Integer.MAX_VALUE,(long)offer.getMaxUses()+increase));
                replacements.set(i,new MerchantOffer(tag));
            }
        }
        int next=Math.min(5,level+1);
        if(level<5)replacements.addAll(generate(next,random,discounted,factory));
        offers.clear();offers.addAll(replacements);level=next;resetTicks=0;return true;
    }
    public CompoundNBT save(){
        CompoundNBT tag=new CompoundNBT();tag.putInt("version",1);tag.putInt("careerId",CAREERS.indexOf(career)+1);
        tag.putInt("careerLevel",level);tag.put("recipeList",offers.createTag());tag.putInt("resetTicks",resetTicks);
        if(lastBuyer!=null)tag.putString("lastBuyer",lastBuyer.toString());return tag;
    }
    public static DemonMerchantState load(CompoundNBT tag){
        if(tag==null || !tag.contains("version",3) || tag.getInt("version")!=1 || !tag.contains("careerId",3)
            || !tag.contains("careerLevel",3) || !tag.contains("resetTicks",3) || !tag.contains("recipeList",10))throw new IllegalArgumentException("Invalid merchant state");
        int id=tag.getInt("careerId"),level=tag.getInt("careerLevel"),delay=tag.getInt("resetTicks");
        if(id<1 || id>CAREERS.size() || level<0 || level>5 || delay<0 || delay>40 || level==0 && delay!=0)throw new IllegalArgumentException("Invalid merchant progress");
        CompoundNBT recipes=tag.getCompound("recipeList");if(!recipes.contains("Recipes",9))throw new IllegalArgumentException("Missing offer list");
        ListNBT raw=(ListNBT)recipes.get("Recipes");if(!raw.isEmpty() && raw.getElementType()!=10)throw new IllegalArgumentException("Wrong offer list type");
        ListNBT list=recipes.getList("Recipes",10);int expected=level==0?0:Math.min(level,4)*2+(level==5?1:0);
        if(list.size()!=expected)throw new IllegalArgumentException("Unexpected offer count");
        DemonMerchantState state=new DemonMerchantState(CAREERS.get(id-1));state.level=level;state.resetTicks=delay;
        for(int i=0;i<list.size();i++){
            CompoundNBT saved=list.getCompound(i);
            if(!saved.contains("uses",3) || !saved.contains("maxUses",3))throw new IllegalArgumentException("Missing stock fields");
            MerchantOffer offer=new MerchantOffer(saved);validate(offer);state.offers.add(offer);
        }
        if(tag.contains("lastBuyer")){if(!tag.contains("lastBuyer",8) || (state.lastBuyer=ChestSigilData.parse(tag.getString("lastBuyer")))==null)throw new IllegalArgumentException("Invalid last buyer");}
        return state;
    }
    private static void validate(MerchantOffer offer){
        ItemStack output=offer.getResult(),cost=offer.getBaseCostA();
        if(output.isEmpty() || cost.isEmpty() || output.getCount()>output.getMaxStackSize() || cost.getCount()>cost.getMaxStackSize()
            || !offer.getCostB().isEmpty() || offer.getMaxUses()<1 || offer.getUses()<0 || offer.getUses()>offer.getMaxUses()
            || offer.getPriceMultiplier()!=0 || offer.getSpecialPriceDiff()!=0 || offer.getDemand()!=0)throw new IllegalArgumentException("Invalid saved offer");
        if(output.getItem() instanceof KillContractItem && (offer.getMaxUses()!=1 || KillContractItem.data(output)==null && KillContractItem.donationData(output)==null || KillContractItem.signed(output)))throw new IllegalArgumentException("Invalid contract stock");
    }
}