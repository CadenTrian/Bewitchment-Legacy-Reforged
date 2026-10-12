package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;
public final class ArachnophobiaCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        int[] amps={Integer.MIN_VALUE,-1,0,1,2,3,4,255,Integer.MAX_VALUE};
        int[] counts={2,2,2,4,6,8,8,8,8};
        for(int i=0;i<amps.length;i++)check(ArachnophobiaRules.count(amps[i])==counts[i],"Legacy count with bounded command and Leonard levels");
        for(Block b:new Block[]{Blocks.AIR,Blocks.CAVE_AIR,Blocks.VOID_AIR,Blocks.GRASS,Blocks.FERN,Blocks.SNOW})check(ArachnophobiaRules.webCandidate(b.defaultBlockState()),"Dry replaceable block may become web");
        for(Block b:new Block[]{Blocks.WATER,Blocks.LAVA,Blocks.COBWEB,Blocks.STONE,Blocks.CHEST,Blocks.SEAGRASS,Blocks.OAK_SLAB})for(BlockState state:b.getStateDefinition().getPossibleStates())check(!ArachnophobiaRules.webCandidate(state),"Fluids, containers and solid blocks survive webbing");
        for(BlockState snow:Blocks.SNOW.getStateDefinition().getPossibleStates())check(ArachnophobiaRules.webCandidate(snow)==(snow.getValue(SnowBlock.LAYERS)==1),"Only single-layer snow is replaceable by webs");
        ArachnophobiaRules.Budget budget=new ArachnophobiaRules.Budget();
        for(int tick:new int[]{0,1,-1,Integer.MAX_VALUE}){
            for(int i=0;i<16;i++)check(budget.mob(tick),"Admit sixteen mob attempts per tick");
            for(int i=0;i<4;i++)check(!budget.mob(tick),"Failed creation still consumes slot");
            for(int i=0;i<128;i++)check(budget.position(tick),"Independent position budget");
            for(int i=0;i<4;i++)check(!budget.position(tick),"Bound all failed search attempts");
        }
        ArachnophobiaRules.Budget other=new ArachnophobiaRules.Budget();check(other.mob(Integer.MAX_VALUE)&&other.position(Integer.MAX_VALUE),"World budgets independent");
        UUID target=UUID.randomUUID(),second=UUID.randomUUID();int[] calls={0};
        check(ArachnophobiaRules.guarded(target,()->{
            calls[0]++;check(!ArachnophobiaRules.guarded(target,()->calls[0]++),"Same target callback cannot recursively summon");
            check(ArachnophobiaRules.guarded(second,()->calls[0]++),"Different target remains possible within global budget");
        }),"Initial summon admitted");
        check(calls[0]==2,"Only admitted actions run");
        try{ArachnophobiaRules.guarded(target,()->{throw new IllegalStateException("callback");});throw new AssertionError("Exception swallowed");}catch(IllegalStateException expected){checks++;}
        check(ArachnophobiaRules.guarded(target,()->calls[0]++),"Exception releases target guard");
        for(int amplifier:amps){
            Set<BlockPos> visited=new HashSet<>();int r=BrewTerrain.radius(amplifier),side=2*r+1;
            int changed=BrewTerrain.clear(new BlockPos(0,64,0),amplifier,new BrewTerrain.Terrain(){
                public boolean loaded(BlockPos p){return true;}public boolean insideBorder(BlockPos p){return true;}
                public boolean eligible(BlockPos p){return true;}public boolean mayEdit(BlockPos p){return true;}
                public boolean clear(BlockPos p){return visited.add(p);}
            });
            check(changed==side*side*side&&visited.size()==changed&&changed<=343,"Web volume bounded and unique");
        }
        for(int failure=0;failure<=5;failure++)for(boolean handled:new boolean[]{false,true}){
            final int stage=failure;int[] callsAt={0,0,0,0};boolean[] valid={stage!=1};
            boolean result=ArachnophobiaRules.spawn(new ArachnophobiaRules.SpawnSteps(){
                public boolean valid(){return valid[0];}
                public boolean permitted(){callsAt[0]++;if(stage==3)valid[0]=false;return stage!=2;}
                public boolean special(){callsAt[1]++;if(stage==4)valid[0]=false;return handled;}
                public void initialize(){callsAt[2]++;if(stage==5)valid[0]=false;}
                public boolean insert(){callsAt[3]++;return true;}
            });
            boolean success=stage==0||stage==5&&handled;
            check(result==success&&callsAt[3]==(success?1:0),"Callback mutation or denial prevents insertion");
            check(callsAt[2]==(!handled&&(stage==0||stage==5)?1:0),"SpecialSpawn or invalid state prevents default initialization");
            check(callsAt[1]==(stage==0||stage>=4?1:0),"Denied or moved spawn never reaches SpecialSpawn");
        }
        check(!ArachnophobiaRules.spawn(new ArachnophobiaRules.SpawnSteps(){
            public boolean valid(){return true;}public boolean permitted(){return true;}public boolean special(){return false;}
            public void initialize(){}public boolean insert(){return false;}
        }),"Rejected insertion reports failure without retry");
        ArachnophobiaEffect effect=new ArachnophobiaEffect();check(effect.isInstantenous()&&!effect.isBeneficial()&&effect.getColor()==0xc0c0c0,"Original instant harmful metadata");
        check(BrewIngredients.ids().contains("minecraft:cobweb"),"Original cobweb ingredient registered");
        check(ArachnophobiaRules.BUFF_TICKS==2400&&ArachnophobiaRules.ATTEMPTS==16,"Original buff duration and placement attempts");
        System.out.println("PASS: "+checks+" Arachnophobia web states, spawn limits, guard lifecycle and delivery bounds checks");
    }
}