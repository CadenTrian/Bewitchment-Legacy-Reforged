package com.bewitchment.block;

import com.bewitchment.combat.PlayerFortunes;
import com.bewitchment.item.GrimoireMagiaItem;
import com.bewitchment.machine.*;
import com.bewitchment.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;

/** Stateless fortune reader; pending fortunes persist on the player. */
public final class CrystalBallBlock extends Block {
    private static final VoxelShape SHAPE=Block.box(3,0,3,13,12,13);
    public CrystalBallBlock(){super(Properties.of(Material.GLASS).strength(1,2).sound(SoundType.GLASS).noOcclusion());}
    @Override public VoxelShape getShape(BlockState state,IBlockReader world,BlockPos pos,ISelectionContext context){return SHAPE;}
    @Override public ActionResultType use(BlockState state,World world,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
        if(hand!=Hand.MAIN_HAND)return ActionResultType.PASS;
        if(world.isClientSide)return ActionResultType.SUCCESS;
        if(player.getMainHandItem().getItem()==ModItems.TAGLOCK.get()){
            com.bewitchment.combat.PlayerTarot.read(player,world.mayInteract(player,pos),amount->pay(world,pos,player,amount));
            return ActionResultType.CONSUME;
        }
        CrystalBallReading.Result result=CrystalBallReading.read(player.getUUID(),new CrystalBallReading.Access(){
            public boolean eligible(){return PlayerFortunes.eligible(player)&&world.mayInteract(player,pos);}
            public boolean taglock(){return player.getMainHandItem().getItem()==ModItems.TAGLOCK.get();}
            public boolean pending(){return PlayerFortunes.read(player).kind()!=null;}
            public boolean pay(int amount){return CrystalBallBlock.pay(world,pos,player,amount);}
            public void assign(){PlayerFortunes.assign(player,CrystalBallReading.choose(player.getRandom().nextInt(FortuneData.Kind.values().length)));}
        });
        FortuneData.Kind fortune=PlayerFortunes.read(player).kind();
        player.displayClientMessage(new TranslationTextComponent("crystal_ball.bewitchment."+result.name().toLowerCase(java.util.Locale.ROOT),
                fortune==null?"":new TranslationTextComponent("fortune.bewitchment."+fortune.id)),true);
        return ActionResultType.CONSUME;
    }
    private static boolean pay(World world,BlockPos pos,PlayerEntity player,int amount){
        AltarTile altar=AltarTile.nearest((ServerWorld)world,pos);
        return (altar!=null&&altar.tryDrainMagic(amount))||GrimoireMagiaItem.drain(player,amount);
    }
}
