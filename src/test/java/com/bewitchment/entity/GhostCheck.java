package com.bewitchment.entity;

public final class GhostCheck {
    private static int checks;
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    public static void main(String[] args){
        for(int i=0;i<5;i++)check(GhostRules.skin(i)==i,"Five saved appearances");
        check(GhostRules.skin(-1)==0 && GhostRules.skin(5)==0 && GhostRules.skin(Integer.MAX_VALUE)==0,"Invalid skins recover");
        int salt=0,candles=0;
        for(int x=-9;x<=9;x++)for(int y=-9;y<=9;y++)for(int z=-9;z<=9;z++){
            if(GhostRules.ward(x,y,z,true,false))salt++;
            if(GhostRules.ward(x,y,z,false,true))candles++;
        }
        check(salt==7*17*7,"Salt uses strict horizontal radius and full vertical range");
        check(candles==17*17*17,"Candelabra includes cube corners");
        check(GhostRules.ward(-3,-8,3,true,false),"Salt inclusive inner boundary");
        check(!GhostRules.ward(4,0,0,true,false) && !GhostRules.ward(0,9,0,true,false),"Salt outer boundary");
        check(GhostRules.ward(8,-8,8,false,true),"Candelabra corner");
        check(!GhostRules.ward(9,0,0,false,true),"Candelabra outer boundary");
        check(!GhostRules.ward(0,0,0,false,false),"Unlit or ordinary candle is not a ward");
        check(!GhostRules.ward(Integer.MIN_VALUE,0,0,true,true),"No absolute-value overflow");
        check(!GhostRules.charge(4) && GhostRules.charge(4.001),"Charge starts beyond two blocks");
        check(!GhostRules.charge(Double.NaN) && !GhostRules.charge(Double.POSITIVE_INFINITY),"Invalid distance cannot start charge");
        check(GhostRules.retarget(11.999) && !GhostRules.retarget(12),"Close charge retarget boundary");
        check(!GhostRules.retarget(-1) && !GhostRules.retarget(Double.NaN),"Invalid retarget distance rejected");
        check(Math.abs(GhostRules.reach(.6F,.6F)-2.04)<.0001,"Legacy squared hit reach");
        check(BlackDogRules.vanishes(true,false,false),"Clear daytime removes unnamed ghost");
        check(!BlackDogRules.vanishes(true,true,false) && !BlackDogRules.vanishes(true,false,true) && !BlackDogRules.vanishes(false,false,false),"Rain, names and night preserve ghost");
        check(FeuerwurmRules.summoner("bad-owner")==null,"Malformed saved owner recovers");
        System.out.println("PASS: "+checks+" Ghost checks");
    }
}
