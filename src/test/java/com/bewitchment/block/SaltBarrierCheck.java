package com.bewitchment.block;

import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import java.lang.reflect.Proxy;
import java.util.*;

public final class SaltBarrierCheck {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Bootstrap.bootStrap();
        SaltBarrierBlock salt = new SaltBarrierBlock();
        BlockPos origin = new BlockPos(0, 64, 0);
        Map<BlockPos, BlockState> map = new HashMap<>();
        IWorld world = (IWorld) Proxy.newProxyInstance(IWorld.class.getClassLoader(), new Class[]{IWorld.class}, (proxy, method, a) -> {
            switch (method.getName()) {
                case "getBlockState": return map.getOrDefault(a[0], Blocks.AIR.defaultBlockState());
                case "getFluidState": return map.getOrDefault(a[0], Blocks.AIR.defaultBlockState()).getFluidState();
                case "setBlock": map.put(((BlockPos)a[0]).immutable(), (BlockState)a[1]); return true;
                case "isClientSide": return false;
                case "getMaxBuildHeight": return 256;
                case "getHeight": return 256;
                case "getMinBuildHeight": return 0;
                case "getBlockEntity": return null;
                case "hasChunk": return true;
                default: throw new AssertionError("Unexpected world query: " + method.getName());
            }
        });
        for (int mask = 0; mask < 64; mask++) {
            boolean explicit=(mask&1)!=0, living=(mask&2)!=0, silver=(mask&4)!=0, wolf=(mask&8)!=0, cold=(mask&16)!=0, slime=(mask&32)!=0;
            boolean expected=explicit || slime;
            if (living && cold) expected=true;
            if (living && silver && !wolf) expected=true;
            check(SaltBarrierBlock.blocks(explicit,living,silver,wolf,cold,slime)==expected,"Legacy target truth table");
        }
        check(!SaltBarrierBlock.blocks(null),"Missing entity has no wall");
        for (BlockState state : salt.getStateDefinition().getPossibleStates()) {
            check(salt.rotate(salt.rotate(state,Rotation.CLOCKWISE_90),Rotation.COUNTERCLOCKWISE_90)==state,"Rotation inverse preserves all flags");
            for(Mirror mirror:Mirror.values()) check(salt.mirror(salt.mirror(state,mirror),mirror)==state,"Mirror twice is identity");
            for(Direction d:Direction.Plane.HORIZONTAL) {
                for(boolean up:new boolean[]{false,true}) {
                    check(salt.rotate(state,Rotation.CLOCKWISE_90).getValue(SaltBarrierBlock.connection(d.getClockWise(),up))==state.getValue(SaltBarrierBlock.connection(d,up)),"Clockwise maps geometry");
                }
                check(state.getSignal(world,origin,d)==0 && state.getDirectSignal(world,origin,d)==0,"Never supplies power");
            }
            check(!state.isSignalSource(),"No redstone source");
            check(salt.getAiPathNodeType(state,world,origin,null)==PathNodeType.BLOCKED,"Legacy all-mob path obstacle");
            check(state.getPistonPushReaction()==PushReaction.DESTROY,"Piston destroys salt");
            check(salt.getCollisionShape(state,world,origin,ISelectionContext.empty()).isEmpty(),"No entity means no collision");
        }
        for(Direction d:Direction.Plane.HORIZONTAL) {
            map.clear(); map.put(origin.below(),Blocks.STONE.defaultBlockState());
            BlockPos side=origin.relative(d);
            check(salt.canSurvive(salt.defaultBlockState(),world,origin),"Solid floor supports salt");
            map.put(side,salt.defaultBlockState());
            BlockState flat=salt.connections(salt.defaultBlockState(),world,origin);
            check(flat.getValue(SaltBarrierBlock.connection(d,false)) && !flat.getValue(SaltBarrierBlock.connection(d,true)),"Flat salt connection");
            map.put(side,Blocks.STONE.defaultBlockState());map.put(side.above(),salt.defaultBlockState());
            BlockState rise=salt.connections(flat,world,origin);
            check(rise.getValue(SaltBarrierBlock.connection(d,false)) && rise.getValue(SaltBarrierBlock.connection(d,true)),"Climb solid step");
            map.put(origin.above(),Blocks.STONE.defaultBlockState());
            check(!salt.connections(rise,world,origin).getValue(SaltBarrierBlock.connection(d,false)),"Ceiling blocks climbing");
            map.remove(origin.above());map.remove(side.above());map.remove(side);
            map.put(side.below(),salt.defaultBlockState());map.put(side.below().below(),Blocks.STONE.defaultBlockState());
            check(salt.connections(rise,world,origin).getValue(SaltBarrierBlock.connection(d,false)),"Descend open step");
            map.put(side,Blocks.STONE.defaultBlockState());
            check(!salt.connections(rise,world,origin).getValue(SaltBarrierBlock.connection(d,false)),"Solid neighbor blocks descent");
            map.put(side,Blocks.REDSTONE_WIRE.defaultBlockState());map.remove(side.below());
            check(!salt.connections(rise,world,origin).getValue(SaltBarrierBlock.connection(d,false)),"Redstone never connects");
            map.put(side,Blocks.HOPPER.defaultBlockState()); map.put(side.above(),salt.defaultBlockState());
            check(salt.connections(rise,world,origin).getValue(SaltBarrierBlock.connection(d,false)),"Hopper-supported rise");
            map.put(origin,salt.defaultBlockState());map.put(side,Blocks.STONE.defaultBlockState());
            salt.updateIndirectNeighbourShapes(salt.defaultBlockState(),world,origin,2,16);
            check(map.get(side.above()).getValue(SaltBarrierBlock.connection(d.getOpposite(),false)),"Indirect upper neighbor connects down");
            map.remove(origin);
            salt.updateIndirectNeighbourShapes(salt.defaultBlockState(),world,origin,2,16);
            check(!map.get(side.above()).getValue(SaltBarrierBlock.connection(d.getOpposite(),false)),"Removal clears diagonal neighbor");
        }
        map.clear();map.put(origin.below(),Blocks.HOPPER.defaultBlockState());
        check(salt.canSurvive(salt.defaultBlockState(),world,origin),"Hopper supports placement");
        map.put(origin.below(),Blocks.AIR.defaultBlockState());
        check(!salt.canSurvive(salt.defaultBlockState(),world,origin),"Air cannot support salt");
        check(salt.updateShape(salt.defaultBlockState(),Direction.DOWN,Blocks.AIR.defaultBlockState(),world,origin,origin.below()).isAir(),"Support removal removes salt");
        System.out.println("PASS: " + checks + " salt barrier checks");
    }
}