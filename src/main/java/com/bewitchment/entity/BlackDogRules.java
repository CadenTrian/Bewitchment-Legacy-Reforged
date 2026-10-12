package com.bewitchment.entity;
public final class BlackDogRules {
    public static int skin(int value){return value>=0 && value<5?value:0;}
    public static boolean vanishes(boolean day,boolean raining,boolean named){return day && !raining && !named;}
    public static float jaw(int attackTicks){return attackTicks>0?.45F:0;}
    private BlackDogRules(){}
}
