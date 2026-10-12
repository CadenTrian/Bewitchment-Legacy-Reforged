package com.bewitchment.entity;

public final class GhostRules {
    public static int skin(int value){return value>=0 && value<5?value:0;}
    public static boolean ward(int dx,int dy,int dz,boolean salt,boolean litCandelabra){
        return Math.abs((long)dx)<=8 && Math.abs((long)dy)<=8 && Math.abs((long)dz)<=8
            && (litCandelabra || salt && Math.abs((long)dx)<4 && Math.abs((long)dz)<4);
    }
    public static boolean charge(double squaredDistance){return squaredDistance>4 && Double.isFinite(squaredDistance);}
    public static boolean retarget(double squaredDistance){return squaredDistance>=0 && squaredDistance<12;}
    public static double reach(float width,float targetWidth){return width*2F*width*2F+targetWidth;}
    private GhostRules(){}
}
