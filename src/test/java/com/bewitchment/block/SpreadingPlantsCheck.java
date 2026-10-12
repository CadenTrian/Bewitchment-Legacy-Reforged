package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Predicate;

public final class SpreadingPlantsCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Random walk(int gate, int x, int up, int down, int z) {
        return new Random(){int call; public int nextInt(int bound){int n=call++;return n==0?gate:new int[]{x,up,down,z}[(n-1)%4];}};
    }
    private static BlockPos offset(BlockPos pos, Random random){return pos.offset(random.nextInt(3)-1,random.nextInt(2)-random.nextInt(2),random.nextInt(3)-1);}
    private static BlockPos original(BlockPos origin,Random random,Predicate<BlockPos> grows){
        if(random.nextInt(25)!=0)return null;
        BlockPos candidate=offset(origin,random);
        for(int i=0;i<4;i++){if(grows.test(candidate))origin=candidate;candidate=offset(origin,random);}
        return grows.test(candidate)?candidate:null;
    }
    public static void main(String[] args){
        Bootstrap.bootStrap();
        net.minecraftforge.common.Tags.Blocks.DIRT.getName();
        net.minecraft.tags.TagRegistryManager.resetAllToEmpty();
        BlockPos origin=new BlockPos(0,64,0);
        for(int gate=1;gate<25;gate++)check(PlantSpread.choose(origin,walk(gate,2,0,0,1),p->{throw new AssertionError("Skipped chance reads world");},p->false,p->true)==null,"Only roll zero spreads");
        for(int count=1;count<=5;count++){
            Set<BlockPos> plants=new HashSet<>();for(int i=0;i<count;i++)plants.add(origin.offset(i,0,0));
            BlockPos result=PlantSpread.choose(origin,walk(0,0,0,0,1),p->true,plants::contains,p->!plants.contains(p));
            check(count==5?result==null:origin.offset(-5,0,0).equals(result),"Five plants suppress, four permit spreading");
        }
        Set<BlockPos> scanned=new HashSet<>();
        PlantSpread.choose(origin,walk(0,1,0,0,1),p->true,p->{scanned.add(p.immutable());return false;},p->false);
        check(scanned.size()==243&&scanned.contains(origin.offset(-4,-1,-4))&&scanned.contains(origin.offset(4,1,4)),"Exact nine by three by nine density volume");
        BlockPos missing=origin.offset(-4,-1,-4);
        check(PlantSpread.choose(origin,walk(0,2,1,0,1),p->!p.equals(missing),p->{throw new AssertionError("Read unavailable density position");},p->true)==null,"Unavailable density region aborts before read");
        Predicate<BlockPos> available=p->p.getX()<3;
        check(PlantSpread.choose(origin,walk(0,2,1,0,1),p->p.getY()<66||available.test(p),p->false,p->{check(p.getY()<66||available.test(p),"No unavailable intermediate growth reads");return true;})==null,"Walking outside loaded footprint aborts");
        check(PlantSpread.choose(origin,walk(0,2,0,0,1),p->p.getX()!=5,p->false,p->true)==null,"Unavailable final candidate aborts");
        check(PlantSpread.choose(origin,walk(0,2,0,0,1),p->!(p.getX()==5&&p.getY()==63),p->false,p->true)==null,"Unavailable final soil aborts");
        check(PlantSpread.choose(origin,walk(0,2,0,0,1),p->true,p->false,p->p.getX()!=5)==null,"Blocked final candidate produces no plant");
        check(PlantSpread.choose(origin,walk(0,2,0,0,1),p->true,p->false,p->false)==null,"No suitable soil produces no plant");
        for(int seed=0;seed<1000;seed++){
            Predicate<BlockPos> grows=p->p.getY()==64&&p.getX()!=0&&p.getZ()!=2;
            check(Objects.equals(original(origin,new Random(seed),grows),PlantSpread.choose(origin,new Random(seed),p->true,p->p.equals(origin),grows)),"Legacy random sequence in available terrain");
        }
        Map<BlockPos,BlockState> map=new HashMap<>();
        IWorld world=(IWorld)Proxy.newProxyInstance(IWorld.class.getClassLoader(),new Class[]{IWorld.class},(proxy,method,a)->{
            if(method.getName().equals("getBlockState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState());
            if(method.getName().equals("getFluidState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState()).getFluidState();
            if(method.getName().equals("getBlockEntity"))return null;
            throw new AssertionError("Unexpected world call "+method.getName());
        });
        BlueInkCapBlock ink=new BlueInkCapBlock();FrostflowerBlock frost=new FrostflowerBlock();
        MagicalPlantBlock ember=new MagicalPlantBlock(false),torch=new MagicalPlantBlock(true);
        for(BlueInkCapBlock plant:new BlueInkCapBlock[]{ink,frost,ember,torch}){
            for(Block soil:new Block[]{Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.COARSE_DIRT,Blocks.PODZOL,Blocks.FARMLAND}){
                map.put(origin.below(),soil.defaultBlockState());
                check(plant.defaultBlockState().canSurvive(world,origin),"Native soil support for all spreading plants");
            }
            map.put(origin.below(),Blocks.AIR.defaultBlockState());
            check(!plant.defaultBlockState().canSurvive(world,origin),"No floating plant");
            check(plant.updateShape(plant.defaultBlockState(),Direction.DOWN,Blocks.AIR.defaultBlockState(),world,origin,origin.below()).isAir(),"Support removal clears plant");
            check(!(plant instanceof IGrowable),"Bonemeal cannot duplicate spreading plants");
            check(plant.defaultBlockState().isRandomlyTicking(),"Plant receives random ticks");
            check(plant.getPlantType(world,origin)==net.minecraftforge.common.PlantType.PLAINS,"Legacy plains plant type");
            check(plant.defaultBlockState().getCollisionShape(world,origin,ISelectionContext.empty()).isEmpty(),"Selection shape does not block movement");
            AxisAlignedBB bounds=plant.getShape(plant.defaultBlockState(),world,origin,ISelectionContext.empty()).bounds();
            check(Math.abs(bounds.maxY-(plant==ember?.9:.6))<.00001&&Math.abs(bounds.minX-(plant==ember?.1:.2))<.00001,"Original plant selection bounds");
        }
        Block customSoil=new Block(AbstractBlock.Properties.copy(Blocks.STONE)) {
            @Override public boolean canSustainPlant(BlockState state,net.minecraft.world.IBlockReader reader,BlockPos pos,Direction face,net.minecraftforge.common.IPlantable plant) { return true; }
        };
        map.put(origin.below(),customSoil.defaultBlockState());
        check(ink.defaultBlockState().canSurvive(world,origin),"Custom Forge soil support respected");
        for(Block soil:new Block[]{Blocks.STONE,Blocks.SAND,Blocks.NETHERRACK}){
            map.put(origin.below(),soil.defaultBlockState());
            check(torch.defaultBlockState().canSurvive(world,origin),"Torchwood rock and sand support");
            check(!ember.defaultBlockState().canSurvive(world,origin),"Embergrass rejects rock and sand");
        }
        for(Block soil:new Block[]{Blocks.SNOW,Blocks.SNOW_BLOCK}){
            map.put(origin.below(),soil.defaultBlockState());
            check(frost.defaultBlockState().canSurvive(world,origin),"Frostflower snow support");
            check(!ink.defaultBlockState().canSurvive(world,origin),"Ink cap rejects snow");
        }
        check(torch.defaultBlockState().getLightEmission()==10&&ember.defaultBlockState().getLightEmission()==0,"Torchwood retains legacy light level");
        System.out.println("PASS: "+checks+" spreading plant checks");
    }
}