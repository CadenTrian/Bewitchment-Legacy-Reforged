package com.bewitchment.machine;

import com.bewitchment.entity.LeonardEntity;
import com.bewitchment.registry.ModClothing;
import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

/** Original extraction-time bonuses, ready for the future brewing-mode integration. */
public final class BrewBonuses {
    public final boolean alchemist;
    public final int leonards;
    private BrewBonuses(boolean alchemist,int leonards){this.alchemist=alchemist;this.leonards=leonards;}
    public static BrewBonuses forPlayer(ServerPlayerEntity player){
        boolean alchemist=ModClothing.hasSet(player,"alchemist");int count=0;
        if(DemonPledges.get(player.getLevel()).has(player.getUUID(),DemonPledges.Patron.LEONARD)){
            AxisAlignedBB area=new AxisAlignedBB(player.getX()-32,player.getY()-32,player.getZ()-32,player.getX()+32,player.getY()+32,player.getZ()+32);
            count=player.getLevel().getEntitiesOfClass(LeonardEntity.class,area,entity->entity.isAlive()).size();
        }
        return new BrewBonuses(alchemist,count);
    }
}