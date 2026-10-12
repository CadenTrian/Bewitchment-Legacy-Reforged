package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.DyeColor;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import java.lang.reflect.Proxy;
import java.util.*;

public final class DecorativeParityCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static boolean near(double a,double b){return Math.abs(a-b)<.000001;}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(boolean lit:new boolean[]{false,true}) for(boolean flint:new boolean[]{false,true})
        for(boolean allowed:new boolean[]{false,true}) for(boolean client:new boolean[]{false,true}) for(boolean accepted:new boolean[]{false,true}){
            List<String> actions=new ArrayList<>();
            ActionResultType result=CandleInteraction.use(lit,flint,allowed,client,()->{actions.add("change");return accepted;},()->actions.add("wear"),()->actions.add("sound"));
            if(!allowed||!lit&&!flint)check(result==ActionResultType.PASS&&actions.isEmpty(),"Ineligible interaction has no side effects");
            else if(client)check(result==ActionResultType.SUCCESS&&actions.isEmpty(),"Client prediction never edits or pays");
            else if(!accepted)check(result==ActionResultType.FAIL&&actions.equals(Collections.singletonList("change")),"Rejected change never pays or sounds");
            else check(result==ActionResultType.CONSUME&&actions.equals(lit?Arrays.asList("change","sound"):Arrays.asList("change","wear","sound")),"One committed change before selected-hand wear and sound");
        }
        for(int seed=0;seed<100;seed++){
            Random expected=new Random(seed);float ignite=.8F+.4F*expected.nextFloat();
            check(near(CandleInteraction.pitch(false,new Random(seed)),ignite),"Legacy ignition pitch sequence");
            expected=new Random(seed);float extinguish=2.6F+(expected.nextFloat()-expected.nextFloat())*.8F;
            check(near(CandleInteraction.pitch(true,new Random(seed)),extinguish),"Legacy extinguish pitch sequence");
        }
        check(CandleInteraction.volume(false)==1&&CandleInteraction.volume(true)==.5F,"Original sound volumes");
        Map<BlockPos,BlockState> map=new HashMap<>();BlockPos pos=new BlockPos(0,64,0);
        IWorld world=(IWorld)Proxy.newProxyInstance(IWorld.class.getClassLoader(),new Class[]{IWorld.class},(proxy,method,a)->{
            if(method.getName().equals("getBlockState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState());
            if(method.getName().equals("getFluidState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState()).getFluidState();
            if(method.getName().equals("getBlockEntity"))return null;
            throw new AssertionError("Unexpected query "+method.getName());
        });
        Map<String,Block> blocks=new LinkedHashMap<>();
        for(DyeColor color:DyeColor.values())blocks.put(color.getName()+"_candle",new CandleBlock(false));
        for(String metal:new String[]{"iron","gold","silver"})blocks.put("candelabra_"+metal,new CandleBlock(true));
        blocks.put("goblet",new GobletBlock(false));blocks.put("filled_goblet",new GobletBlock(true));
        blocks.put("blue_ink_cap",new BlueInkCapBlock());blocks.put("frostflower",new FrostflowerBlock());
        blocks.put("embergrass",new MagicalPlantBlock(false));blocks.put("torchwood",new MagicalPlantBlock(true));
        check(blocks.size()==25,"Exactly twenty-five variants covered");
        for(Map.Entry<String,Block> entry:blocks.entrySet()){
            String name=entry.getKey();Block block=entry.getValue();
            for(BlockState state:block.getStateDefinition().getPossibleStates()){
                if(block instanceof BlueInkCapBlock){
                    map.put(pos.below(),Blocks.DIRT.defaultBlockState());
                    check(state.canSurvive(world,pos),name+" native soil support");
                    check(state.getCollisionShape(world,pos,ISelectionContext.empty()).isEmpty(),name+" noncolliding plant");
                    check(state.getLightEmission()==(name.equals("torchwood")?10:0),name+" light");
                    continue;
                }
                map.put(pos.below(),Blocks.STONE.defaultBlockState());check(state.canSurvive(world,pos),name+" solid support");
                map.put(pos.below(),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));check(state.canSurvive(world,pos),name+" top slab support");
                map.put(pos.below(),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));check(!state.canSurvive(world,pos),name+" bottom slab cannot support");
                map.put(pos.below(),Blocks.AIR.defaultBlockState());
                check(block.updateShape(state,Direction.DOWN,Blocks.AIR.defaultBlockState(),world,pos,pos.below()).isAir(),name+" support removal");
                AxisAlignedBB bounds=state.getShape(world,pos,ISelectionContext.empty()).bounds();
                if(block instanceof CandleBlock){
                    boolean big=((CandleBlock)block).isCandelabra();boolean lit=state.getValue(CandleBlock.LIT);
                    check(near(bounds.minX,big?1D/16:.38)&&near(bounds.maxX,big?15D/16:.62)&&near(bounds.maxY,big?18D/16:.5),name+" selection bounds");
                    check(state.getLightEmission()==(lit?9:0),name+" both light states");
                    check(block.getEnchantPowerBonus(state,world,pos)==(big?1.5F:.5F),name+" enchantment power");
                    check(state.getPistonPushReaction()==PushReaction.DESTROY,name+" piston behavior");
                    check(!state.isSignalSource(),name+" not a redstone source");
                    for(Direction face:Direction.values())check(block.getFlammability(state,world,pos,face)==0&&block.getFireSpreadSpeed(state,world,pos,face)==0,name+" fireproof wax and metal");
                }else{
                    boolean filled=((GobletBlock)block).isFilled();
                    check(near(bounds.minX,.375)&&near(bounds.maxX,.625)&&near(bounds.maxY,.375),name+" original cup bounds");
                    check(state.isSignalSource()==filled,name+" power source status");
                    for(Direction face:Direction.values())check(state.getSignal(world,pos,face)==(filled?8:0)&&state.getDirectSignal(world,pos,face)==0,name+" weak-only power on every face");
                }
            }
        }
        System.out.println("PASS: "+checks+" decorative checks across 25 block variants");
    }
}