package com.bewitchment.machine;

import com.bewitchment.item.ChestSigilItem.Kind;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import java.util.UUID;

/** Loaded server ticks; preserve the original initial delay and pulse boundary. */
public final class PlacedSigilData {
    public static final int LIFETIME=24000*7;
    public enum Step { WAIT, PULSE, EXPIRE }
    public final ChestSigilData targets=new ChestSigilData();
    private int remaining,delay;
    public int remaining(){return remaining;}
    public int delay(){return delay;}
    public boolean install(Kind kind,UUID owner){if(!targets.install(kind,owner))return false;remaining=LIFETIME;delay=kind.cooldown;return true;}
    public Step advance(){
        if(remaining<=0 || targets.kind()==null)return Step.EXPIRE;
        remaining--;
        if(delay>0){delay--;return Step.WAIT;}
        delay=targets.kind().cooldown;return Step.PULSE;
    }
    public static AxisAlignedBB area(BlockPos pos){return new AxisAlignedBB(pos.getX()-6,pos.getY()-3,pos.getZ()-6,pos.getX()+6,pos.getY()+3,pos.getZ()+6);}
    public CompoundNBT save(){CompoundNBT tag=new CompoundNBT();tag.put("targeting",targets.save());tag.putInt("timer",remaining);tag.putInt("delay",delay);return tag;}
    public void load(CompoundNBT tag){targets.load(tag.getCompound("targeting"));remaining=targets.kind()==null?0:Math.max(0,Math.min(LIFETIME,tag.getInt("timer")));delay=targets.kind()==null?0:Math.max(0,Math.min(targets.kind().cooldown,tag.getInt("delay")));}
}
