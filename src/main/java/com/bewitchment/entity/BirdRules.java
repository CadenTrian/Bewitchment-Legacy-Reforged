package com.bewitchment.entity;

import java.util.Random;

public final class BirdRules {
    public static int shearTimer(int saved){return Math.max(0,Math.min(12000,saved));}
    public static int shedTimer(int saved){return Math.max(1,Math.min(11999,saved));}
    public static int shedDelay(Random random){return 6000+random.nextInt(6000);}
    public static int featherCount(Random random){return 1+random.nextInt(3);}
    public static boolean seeksShade(boolean day,boolean exposed,boolean sitting,boolean hasTarget){return day && exposed && !sitting && !hasTarget;}
    public static float peck(int time,boolean sitting){
        return !sitting && time>0 && time<=60?(float)(Math.toRadians(time%(60F/time))*6+Math.toRadians(30)):0;
    }
    public static float flightPitch(double x,double y,double z){
        if(!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))return 0;
        if(!(Math.abs(y)>0 && (Math.abs(x)>.05 || Math.abs(z)>.05)) && Math.abs(y)<=.25)return 0;
        float pitch=(float)(-Math.atan2(y,Math.hypot(x,z))/1.5);
        return pitch<0?pitch/3:pitch;
    }
    private BirdRules(){}
}
