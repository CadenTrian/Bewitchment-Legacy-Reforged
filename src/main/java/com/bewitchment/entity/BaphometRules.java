package com.bewitchment.entity;

import com.bewitchment.machine.CurseData;
import net.minecraft.entity.ai.attributes.*;
import java.util.*;

/** Legacy thresholds and sequential shared summon cooldown. */
public final class BaphometRules {
    public static final List<CurseData.Kind> CONTRACTS=Collections.unmodifiableList(Arrays.asList(
        CurseData.Kind.BERSERKER,CurseData.Kind.SALAMANDRINE_TOLERANCE,CurseData.Kind.DEVOURING_RAGE,
        CurseData.Kind.DESICCATION,CurseData.Kind.IGNITION));
    public static AttributeModifierMap.MutableAttribute attributes(AttributeModifierMap.MutableAttribute b){return b
        .add(Attributes.MAX_HEALTH,666).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,.7)
        .add(Attributes.ARMOR,13.616).add(Attributes.ARMOR_TOUGHNESS,6.66).add(Attributes.KNOCKBACK_RESISTANCE,.9);}
    public static int timer(int value,int maximum){return Math.max(0,Math.min(maximum,value));}
    public static boolean pulse(int tick,int period){return tick>=0 && period>0 && tick%period<=5;}
    public static boolean pull(double distanceSquared,int cooldown){return Double.isFinite(distanceSquared) && distanceSquared>100 && cooldown<=0;}
    public static boolean fireball(int tick,double distanceSquared){return pulse(tick,80) && Double.isFinite(distanceSquared) && distanceSquared>4;}
    public static final class Summons {
        public final int timer,kind;
        private Summons(int timer,int kind){this.timer=timer;this.kind=kind;}
    }
    public static Summons summons(float healthFraction,int cooldown){
        int timer=timer(cooldown,180),kind=0;
        if(healthFraction<.5F && timer==0){kind=1;timer=180;}else if(timer>0)timer--;
        // The original tests the second threshold AFTER decrementing the shared timer.
        if(healthFraction<.3F && timer==0){kind=2;timer=180;}
        return new Summons(timer,kind);
    }
    public static String contractFailure(boolean pledged,int levels,CurseData data){
        if(!pledged)return "notpledged";
        if(levels<6)return "lowlevel";
        for(CurseData.Kind kind:CONTRACTS)if(data.remaining(kind)>0)return "alreadyhascontract";
        return null;
    }
    public static CurseData.Kind grant(CurseData data,Random random){
        for(CurseData.Kind kind:CONTRACTS)if(data.remaining(kind)>0)return null;
        CurseData.Kind kind=CONTRACTS.get(random.nextInt(CONTRACTS.size()));
        return data.add(kind,7)?kind:null;
    }
    private BaphometRules(){}
}
