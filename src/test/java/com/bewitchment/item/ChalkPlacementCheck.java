package com.bewitchment.item;

import com.bewitchment.block.*;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import java.lang.reflect.Proxy;
import java.util.*;

public final class ChalkPlacementCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        GlyphBlock glyph=new GlyphBlock();
        check(glyph.getStateDefinition().getPossibleStates().size()==96,"Four types, six letters, four facings");
        for(int type=0;type<4;type++){
            ChalkItem chalk=new ChalkItem(type);ItemStack held=new ItemStack(chalk),other=new ItemStack(Items.STICK,5);
            check(chalk.glyphType()==type,"Every chalk retains its type");
            check(held.getMaxDamage()==128&&held.getMaxStackSize()==1,"Original uses and stack size");
            check(!chalk.isRepairable(held)&&!chalk.isValidRepairItem(held,new ItemStack(Items.PAPER)),"Repair disabled");
            check(!chalk.isEnchantable(held)&&!chalk.isBookEnchantable(held,new ItemStack(Items.ENCHANTED_BOOK))&&!chalk.canApplyAtEnchantingTable(held,Enchantments.UNBREAKING),"All enchantment routes disabled");
            for(boolean client:new boolean[]{false,true})for(boolean accepted:new boolean[]{false,true})for(boolean creative:new boolean[]{false,true}){
                held.setDamageValue(0);List<String> actions=new ArrayList<>();
                ActionResultType result=BlockInteractionCommit.apply(true,client,()->{actions.add("place");return accepted;},()->{actions.add("sound");if(!creative)held.setDamageValue(held.getDamageValue()+1);});
                check(held.getDamageValue()==(!client&&accepted&&!creative?1:0)&&other.getCount()==5,"Placement wears only selected survival chalk after success");
                check(result==(client?ActionResultType.SUCCESS:accepted?ActionResultType.CONSUME:ActionResultType.FAIL),"Failed placement reported to caller");
                check(actions.equals(client?Collections.emptyList():accepted?Arrays.asList("place","sound"):Collections.singletonList("place")),"No client or failure sound");
            }
        }
        for(BlockState state:glyph.getStateDefinition().getPossibleStates()){
            int type=state.getValue(GlyphBlock.TYPE),letter=state.getValue(GlyphBlock.LETTER);Direction facing=state.getValue(GlyphBlock.FACING);
            check(ChalkPlacement.canReplace(state,false)==(type!=0),"Focal storage cannot be overwritten");
            check(!ChalkPlacement.canReplace(state,true),"Fluid blocks every glyph replacement");
            check(glyph.hasTileEntity(state)==(type==0),"Only focal state owns tile storage");
            check(glyph.canBeReplaced(state,(net.minecraft.item.BlockItemUseContext)null)==(type!=0),"Block placement agrees with chalk focal protection");
            for(Rotation rotation:Rotation.values()){
                BlockState turned=glyph.rotate(state,rotation);
                check(turned.getValue(GlyphBlock.FACING)==rotation.rotate(facing),"Rotation maps facing");
                check(turned.getValue(GlyphBlock.TYPE)==type&&turned.getValue(GlyphBlock.LETTER)==letter,"Rotation preserves ritual type and rune");
            }
            for(Mirror mirror:Mirror.values()){
                BlockState reflected=glyph.mirror(state,mirror);
                check(reflected.getValue(GlyphBlock.FACING)==mirror.mirror(facing),"Mirror maps facing");
                check(glyph.mirror(reflected,mirror)==state,"Mirror round trip retains exact native state");
            }
            BlockState turned=state;for(int i=0;i<4;i++)turned=glyph.rotate(turned,Rotation.CLOCKWISE_90);
            check(turned==state,"Four quarter turns preserve exact state");
            check(state.getPistonPushReaction()==PushReaction.DESTROY,"Glyphs remain piston-destroyed");
        }
        for(Block replaceable:new Block[]{Blocks.AIR,Blocks.GRASS,Blocks.TALL_GRASS,Blocks.SNOW})check(ChalkPlacement.canReplace(replaceable.defaultBlockState(),false),"Replaceable dry space accepted");
        for(Block solid:new Block[]{Blocks.STONE,Blocks.CHEST,Blocks.OAK_LOG})check(!ChalkPlacement.canReplace(solid.defaultBlockState(),false),"Solid blocks and storage protected");
        check(!ChalkPlacement.canReplace(Blocks.WATER.defaultBlockState(),true)&&!ChalkPlacement.canReplace(Blocks.LAVA.defaultBlockState(),true),"Fluids cannot be replaced");
        for(int y:new int[]{Integer.MIN_VALUE,-1,0,256,257,Integer.MAX_VALUE})check(!ChalkPlacement.insideHeight(y,256),"Out-of-height or missing floor rejected");
        check(ChalkPlacement.insideHeight(1,256)&&ChalkPlacement.insideHeight(255,256),"Lowest supported and highest legal positions accepted");
        check(GlyphBlock.letter(new BlockPos(0,0,0))==0&&GlyphBlock.letter(new BlockPos(-1,0,0))==1,"Rune coordinate origin and negative position");
        check(GlyphBlock.letter(new BlockPos(Integer.MIN_VALUE,0,Integer.MIN_VALUE))==0&&GlyphBlock.letter(new BlockPos(Integer.MAX_VALUE,0,Integer.MAX_VALUE))==3,"Rune arithmetic does not overflow int coordinates");
        Map<BlockPos,BlockState> map=new HashMap<>();BlockPos pos=new BlockPos(0,64,0);
        IWorld world=(IWorld)Proxy.newProxyInstance(IWorld.class.getClassLoader(),new Class[]{IWorld.class},(proxy,method,a)->{
            if(method.getName().equals("getBlockState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState());
            if(method.getName().equals("getFluidState"))return map.getOrDefault(a[0],Blocks.AIR.defaultBlockState()).getFluidState();
            if(method.getName().equals("getBlockEntity"))return null;
            throw new AssertionError("Unexpected query "+method.getName());
        });
        for(int type=0;type<4;type++){
            BlockState state=glyph.defaultBlockState().setValue(GlyphBlock.TYPE,type);
            map.put(pos.below(),Blocks.STONE.defaultBlockState());check(state.canSurvive(world,pos),"Solid support for each chalk");
            check(state.getCollisionShape(world,pos,ISelectionContext.empty()).isEmpty(),"Drawings do not collide");
            check(Math.abs(state.getShape(world,pos,ISelectionContext.empty()).bounds().maxY-.0025)<.000001,"Thin legacy drawing shape");
            map.clear();check(!state.canSurvive(world,pos),"Unsupported glyph rejected");
            check(glyph.updateShape(state,Direction.DOWN,Blocks.AIR.defaultBlockState(),world,pos,pos.below()).isAir(),"Loss of support removes all glyph types");
        }
        System.out.println("PASS: "+checks+" chalk placement and glyph transform checks");
    }
}