package com.bewitchment.entity;

public final class GreaterDemonCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        for(int bits=0;bits<8;bits++)check(GreaterDemonEntity.showBar((bits&1)!=0,(bits&2)!=0,(bits&4)!=0)==(bits==3),"Only tracked living unpledged viewers see boss bar");
        check(GreaterDemonEntity.healthFraction(50,100)==.5F,"Half health");
        check(GreaterDemonEntity.healthFraction(100,100)==1,"Full health");
        check(GreaterDemonEntity.healthFraction(0,100)==0,"Dead boss");
        check(GreaterDemonEntity.healthFraction(-1,100)==0,"Negative health bounded");
        check(GreaterDemonEntity.healthFraction(101,100)==1,"Excess health bounded");
        for(float value:new float[]{Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
            check(GreaterDemonEntity.healthFraction(value,100)==0,"Invalid health rejected");
            check(GreaterDemonEntity.healthFraction(50,value)==0,"Invalid max health rejected");
        }
        check(GreaterDemonEntity.healthFraction(50,0)==0 && GreaterDemonEntity.healthFraction(50,-1)==0,"Nonpositive maximum rejected");
        check(Pledgeable.class.isAssignableFrom(GreaterDemonEntity.class),"Greater demons use pledge hook");
        System.out.println("PASS: "+checks+" greater-demon boss-bar visibility and health checks");
    }
}