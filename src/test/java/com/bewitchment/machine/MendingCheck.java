package com.bewitchment.machine;
import com.bewitchment.potion.*;
import net.minecraft.block.*;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.util.*;

public final class MendingCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final class Visitor implements SampledTerrainWork.Visitor {
        final Set<BlockPos> positions=new HashSet<>();UUID owner;boolean permit=true;
        public boolean allowed(UUID id){owner=id;return permit;}
        public void visit(BlockPos pos){if(!positions.add(pos))throw new AssertionError("Repeated sampled position");}
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.bootStrap();
        for(CreatureAttribute type:new CreatureAttribute[]{CreatureAttribute.UNDEFINED,CreatureAttribute.UNDEAD,CreatureAttribute.ARTHROPOD,CreatureAttribute.ILLAGER,CreatureAttribute.WATER})for(int flags=0;flags<4;flags++)check(MendingEffect.heals(type,(flags&1)!=0,(flags&2)!=0)==(type!=CreatureAttribute.UNDEAD&&flags==0),"Healing excludes undead, demons and spirits independently");
        for(int amp:new int[]{Integer.MIN_VALUE,-1,0,1,2,255,Integer.MAX_VALUE})check(MendingEffect.healing(amp)==6F*(Math.max(0,Math.min(255,amp))+1),"Healing scales without integer overflow");
        for(Block block:new Block[]{Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL,Blocks.MYCELIUM}){check(MendingEffect.canRestore(block.defaultBlockState(),false),"Original soil variants eligible");check(!MendingEffect.canRestore(block.defaultBlockState(),true),"Opaque cover blocks restoration");}
        for(Block block:new Block[]{Blocks.GRASS_BLOCK,Blocks.FARMLAND,Blocks.GRASS_PATH,Blocks.SAND,Blocks.GRAVEL,Blocks.STONE,Blocks.CHEST,Blocks.AIR,Blocks.CRIMSON_NYLIUM,Blocks.WARPED_NYLIUM})for(boolean opaque:new boolean[]{true,false})check(!MendingEffect.canRestore(block.defaultBlockState(),opaque),"Other blocks unchanged");
        CompoundNBT saved=new CompoundNBT();check(MendingEffect.existingCurer(saved)==null,"New cure has no player attribution");UUID player=UUID.randomUUID();saved.putUUID("ConversionPlayer",player);CompoundNBT snapshot=saved.copy();check(player.equals(MendingEffect.existingCurer(saved))&&saved.equals(snapshot),"Keep existing cure attribution without mutating saved data");saved.putIntArray("ConversionPlayer",new int[]{1,2});check(MendingEffect.existingCurer(saved)==null,"Malformed cure UUID safely ignored");
        check(ZombieVillagerEntity.class.getDeclaredMethod("startConverting",UUID.class,int.class).getReturnType()==void.class,"Mapped vanilla conversion method signature");
        for(int amplifier:new int[]{0,1,2,Integer.MAX_VALUE}){
            MendingWork work=new MendingWork();Visitor visitor=new Visitor();BlockPos center=new BlockPos(12,64,-12);long seed=87654;work.enqueue(center,amplifier,seed,player);int r=SampledTerrainWork.radius(amplifier),side=2*r+1,total=side*side*side,examined=0;
            while(work.pending()>0){int count=work.tick(visitor);check(count>0&&count<=128,"Single-job scan bound");examined+=count;CompoundNBT checkpoint=work.save(new CompoundNBT());work=new MendingWork();work.load(checkpoint);}
            Set<BlockPos> expected=new HashSet<>();for(int i=0;i<total;i++)if(SampledTerrainWork.selected(seed,i))expected.add(center.offset(i%side-r,i/side%side-r,i/(side*side)-r));
            check(examined==total&&expected.equals(visitor.positions),"Mending scan and seed survive repeated save/reload");check(player.equals(visitor.owner),"Player ownership preserved");
        }
        RuinWork old=new RuinWork();old.enqueue(new BlockPos(0,64,0),0,55,null);CompoundNBT legacy=old.save(new CompoundNBT());RuinWork reloaded=new RuinWork();reloaded.load(legacy);check(reloaded.save(new CompoundNBT()).equals(legacy),"Ruin saved schema unchanged by shared implementation");
        MendingWork separate=new MendingWork();separate.enqueue(new BlockPos(0,64,0),0,55,null);Visitor denied=new Visitor();denied.permit=false;separate.tick(denied);check(separate.pending()==0&&old.pending()==1,"Mending and Ruin queues remain independent");
        check(!MendingWork.ID.equals(RuinWork.ID),"Distinct world storage keys");
        MendingEffect effect=new MendingEffect();check(effect.isInstantenous()&&effect.isBeneficial()&&effect.getColor()==0xffff00,"Legacy metadata");
        check(BrewIngredients.ids().contains("minecraft:golden_apple")&&BrewIngredients.ids().contains("minecraft:enchanted_golden_apple"),"Both original golden apples accepted");
        System.out.println("PASS: "+checks+" Mending healing, cure metadata, soil, saved sampling and Ruin compatibility checks");
    }
}
