package com.bewitchment.entity;

import net.minecraft.util.math.vector.Vector3d;
import com.bewitchment.item.ChestSigilItem;

/** Legacy broom parameters with bounded server-side input and motion. */
public final class BroomRules {
    public enum Kind {
        JUNIPER(.8,.95,.1,0), CYPRESS(1.5,1.05,.125,1), ELDER(2.25,1.3,.15,1), DRAGONS_BLOOD(1.9,1.3,.15,1);
        public final double speed,maxSpeed,thrust; public final int cost;
        Kind(double speed,double max,double thrust,int cost){this.speed=speed;maxSpeed=max;this.thrust=thrust;this.cost=cost;}
        public String id(){return name().toLowerCase(java.util.Locale.ROOT)+"_broom";}
    }
    public static double input(double value){return Double.isFinite(value)?Math.max(-1,Math.min(1,value)):0;}
    /** Interpolate headings along the shortest arc; partialTick is a fraction, never a yaw. */
    public static float renderYaw(float previous,float current,float partialTick){
        return previous+partialTick*net.minecraft.util.math.MathHelper.wrapDegrees(current-previous);
    }
    public static Vector3d motion(Kind kind,Vector3d old,float yaw,float strafe,float forward,boolean jump,boolean powered,boolean ground,boolean wall){
        double x=Double.isFinite(old.x)?old.x:0,y=Double.isFinite(old.y)?old.y:0,z=Double.isFinite(old.z)?old.z:0;
        double side=input(strafe),ahead=input(forward),length=Math.max(1,Math.sqrt(side*side+ahead*ahead));
        double angle=Math.toRadians(Float.isFinite(yaw)?yaw:0),acceleration=.1*kind.speed;
        x+=(side*Math.cos(angle)-ahead*Math.sin(angle))/length*acceleration;
        z+=(ahead*Math.cos(angle)+side*Math.sin(angle))/length*acceleration;
        if(jump&&powered&&y<1)y+=.1+kind.thrust;
        double friction=ground?.4:.98;
        return new Vector3d(wall?0:Math.max(-kind.maxSpeed,Math.min(kind.maxSpeed,x*friction)),
                Math.max(-1,Math.min(1,(y-.1)*.75)),wall?0:Math.max(-kind.maxSpeed,Math.min(kind.maxSpeed,z*friction)));
    }
    public static ChestSigilItem.Kind sigil(String id){return ChestSigilItem.Kind.fromId(id);}
    private BroomRules(){}
}
