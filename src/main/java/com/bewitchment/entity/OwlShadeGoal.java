package com.bewitchment.entity;

import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/** Vanilla FleeSunGoal requires burning; an Owl should seek shade without it. */
final class OwlShadeGoal extends FleeSunGoal {
    private final LegacyBirdEntity bird;
    OwlShadeGoal(LegacyBirdEntity bird){super(bird,1);this.bird=bird;}
    @Override public boolean canUse(){
        return BirdRules.seeksShade(bird.level.isDay(),bird.level.canSeeSky(bird.blockPosition()),bird.isOrderedToSit(),bird.getTarget()!=null)
                && setWantedPos();
    }
    @Override public boolean canContinueToUse(){
        return bird.level.isDay() && !bird.isOrderedToSit() && bird.getTarget()==null && super.canContinueToUse();
    }
    @Override protected Vector3d getHidePos(){
        BlockPos origin=bird.blockPosition();
        for(int i=0;i<10;i++){
            BlockPos pos=origin.offset(bird.getRandom().nextInt(20)-10,bird.getRandom().nextInt(6)-3,bird.getRandom().nextInt(20)-10);
            if(pos.getY()<0 || pos.getY()>=255 || !bird.level.hasChunkAt(pos) || bird.level.canSeeSky(pos))continue;
            Vector3d target=Vector3d.atBottomCenterOf(pos);
            if(bird.level.noCollision(bird,bird.getBoundingBox().move(target.subtract(bird.position()))))return target;
        }
        return null;
    }
}
