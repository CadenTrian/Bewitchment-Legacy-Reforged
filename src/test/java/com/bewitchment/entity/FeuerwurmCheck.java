package com.bewitchment.entity;
import com.bewitchment.client.LegacyFeuerwurmModel;
import net.minecraft.util.registry.Bootstrap;
import java.util.UUID;
public final class FeuerwurmCheck {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        Bootstrap.bootStrap();
        for(int skin=0;skin<9;skin++)check(FeuerwurmRules.skin(skin)==skin,"Nine valid variants preserved");
        for(int bad:new int[]{-1,9,255,Integer.MIN_VALUE,Integer.MAX_VALUE})check(FeuerwurmRules.skin(bad)==0,"Invalid variant safe");
        UUID owner=UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
        check(owner.equals(FeuerwurmRules.summoner(owner.toString())),"Summoner survives serialization");
        check(FeuerwurmRules.summoner("")==null && FeuerwurmRules.summoner("broken uuid")==null,"Missing/broken summoner harmless");
        check(FeuerwurmRules.lavaHeals(20,false,true),"Lava regeneration reachable while dry");
        check(!FeuerwurmRules.lavaHeals(20,true,true),"Wet damage takes precedence over lava");
        check(!FeuerwurmRules.lavaHeals(19,false,true) && !FeuerwurmRules.lavaHeals(-20,false,true),"No regeneration outside valid second boundary");
        check(!FeuerwurmRules.lavaHeals(20,false,false),"Dry ground cannot heal");
        check(FeuerwurmRules.prey(true,false,true,false,false,false),"Legacy animal branch takes precedence over fire immunity");
        check(FeuerwurmRules.prey(false,true,true,true,false,false),"Hellhound special prey branch");
        check(!FeuerwurmRules.prey(false,false,false,true,false,false),"Demons excluded from generic prey");
        check(!FeuerwurmRules.prey(false,false,false,false,true,false),"Undead excluded from generic prey");
        check(!FeuerwurmRules.prey(false,false,true,false,false,false),"Fire immune generic prey excluded");
        check(FeuerwurmRules.prey(false,false,false,false,false,false),"Ordinary living prey eligible");
        check(!FeuerwurmRules.prey(true,true,false,false,false,true),"Besmirched set overrides all proactive prey branches");
        LegacyFeuerwurmModel model=new LegacyFeuerwurmModel();
        model.pose(10,30,15,.4F,true);
        check(model.opacity()==.5F,"Spectral opacity restored");
        check(model.neck01.yRot==model.neck02.yRot,"Legacy neck motion coherent");
        model.pose(0,0,0,0,false);
        check(model.opacity()==1,"Solid animal resets spectral alpha");
        check(model.body.yRot==0 && model.neck00.xRot==0 && model.neck01.xRot==0,"Stationary model resets moved pose");
        model.pose(1,0,0,Float.MAX_VALUE,false);
        check(Float.isFinite(model.tail04.yRot) && Math.abs(model.tail04.yRot)<2,"Extreme motion bounded");
        System.out.println("PASS: "+checks+" Feuerwurm variant, summoner, environment, prey and model checks.");
    }
}
