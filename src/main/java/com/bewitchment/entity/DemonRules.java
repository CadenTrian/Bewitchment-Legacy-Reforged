package com.bewitchment.entity;
public final class DemonRules {
    public static net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute attributes(net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute builder){
        return builder.add(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH,195).add(net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE,16)
            .add(net.minecraft.entity.ai.attributes.Attributes.ARMOR,6.66).add(net.minecraft.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,3.33)
            .add(net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE,16).add(net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.8).add(net.minecraft.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE,.7);
    }
    public static int skin(int skin){return skin>=0 && skin<4?skin:0;}
    public static boolean fires(int tick,double distanceSquared){return tick>=0 && tick%80<=5 && distanceSquared>4 && Double.isFinite(distanceSquared);}
    public static double spread(double distanceSquared){return Math.sqrt(Math.sqrt(Math.max(0,distanceSquared)))*.5;}
    public static boolean prey(boolean fireImmune,boolean hellish,boolean besmirched,int roll){return !besmirched && (hellish?roll==0:!fireImmune);}
    public static boolean customer(boolean alive,boolean playerAlive,boolean spectator,boolean sameWorld,boolean inWater,boolean grounded,boolean hurt,boolean combat,double distanceSquared){return alive && playerAlive && !spectator && sameWorld && !inWater && grounded && !hurt && !combat && distanceSquared>=0 && distanceSquared<=16;}
    private DemonRules(){}
}