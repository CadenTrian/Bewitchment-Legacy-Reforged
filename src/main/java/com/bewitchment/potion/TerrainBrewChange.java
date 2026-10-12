package com.bewitchment.potion;
/** Recheck source and permissions after protection listeners before writing. */
public final class TerrainBrewChange {
    public interface Target { boolean allowed(); boolean unchanged(); boolean survives(); boolean canceled(); boolean write(); }
    public static boolean apply(Target target){
        if(!target.allowed()||!target.unchanged()||!target.survives()||target.canceled())return false;
        return target.allowed()&&target.unchanged()&&target.survives()&&target.write();
    }
    private TerrainBrewChange(){}
}