package com.bewitchment.machine;

import java.time.*;
import java.util.Random;

public final class WednesdayRules {
    public static String failure(boolean enabled,LocalDate date){return !enabled?"wednesday_disabled":date.getDayOfWeek()!=DayOfWeek.WEDNESDAY?"wednesday_day":null;}
    static boolean extra(long time,Random random){return time%10==0 && random.nextInt(6)==0;}
    static int lifetime(Random random){return 60+random.nextInt(100);}
    static int height(Random random){return 2+random.nextInt(6);}
    static double velocity(Random random){return ((random.nextFloat()*2-random.nextFloat())-random.nextFloat())/2;}
    public static boolean expires(int duration){return duration==1;}
    private WednesdayRules(){}
}
