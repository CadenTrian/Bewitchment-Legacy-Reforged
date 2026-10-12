package com.bewitchment.machine;

import com.bewitchment.entity.BroomRules;
import com.bewitchment.enchantment.MagicProtectionEnchantment;
import com.bewitchment.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;

public final class BroomCheck {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static boolean near(double a,double b){return Math.abs(a-b)<.00001;}
    public static void main(String[] args){
        net.minecraft.util.registry.Bootstrap.bootStrap();
        for(float heading:new float[]{-720,-179,-90,0,45,90,179,360,720})
            for(float partial:new float[]{0,.25F,.5F,.75F,1})
                check(near(BroomRules.renderYaw(heading,heading,partial),heading),"Stationary heading cannot spin between frames");
        check(near(BroomRules.renderYaw(30,90,0),30),"Turn begins at previous heading");
        check(near(BroomRules.renderYaw(30,90,.5F),60),"Turn interpolates by frame fraction");
        check(near(BroomRules.renderYaw(30,90,1),90),"Turn ends at current heading");
        check(near(BroomRules.renderYaw(179,-179,.5F),180),"Positive wrap takes two-degree arc");
        check(near(BroomRules.renderYaw(-179,179,.5F),-180),"Negative wrap takes two-degree arc");
        check(near(BroomRules.renderYaw(90,450,.5F),90),"Equivalent headings cannot cause full rotation");
        check(BroomRules.Kind.values().length==4,"Four legacy wood types");
        for(BroomRules.Kind kind:BroomRules.Kind.values()) {
            check(kind.cost==(kind==BroomRules.Kind.JUNIPER?0:1),"Legacy flight cost");
            Vector3d north=BroomRules.motion(kind,Vector3d.ZERO,0,0,1,false,true,false,false);
            Vector3d west=BroomRules.motion(kind,Vector3d.ZERO,90,0,1,false,true,false,false);
            check(near(north.x,0)&&north.z>0&&west.x<0&&near(west.z,0),"Yaw-relative controls");
            Vector3d diagonal=BroomRules.motion(kind,Vector3d.ZERO,0,1,1,false,true,false,false);
            check(near(diagonal.x*diagonal.x+diagonal.z*diagonal.z,north.x*north.x+north.z*north.z),"Diagonal speed normalized");
            Vector3d up=BroomRules.motion(kind,Vector3d.ZERO,0,0,0,true,true,false,false);
            Vector3d down=BroomRules.motion(kind,Vector3d.ZERO,0,0,0,true,false,false,false);
            check(up.y>0&&down.y<0,"Unpaid ascent falls; paid ascent rises");
            Vector3d wall=BroomRules.motion(kind,new Vector3d(1,0,1),0,1,1,false,true,false,true);
            check(wall.x==0&&wall.z==0,"Wall stops horizontal flight");
            for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY}) {
                Vector3d safe=BroomRules.motion(kind,new Vector3d(bad,bad,bad),Float.NaN,Float.POSITIVE_INFINITY,Float.NaN,true,true,false,false);
                check(Double.isFinite(safe.x)&&Double.isFinite(safe.y)&&Double.isFinite(safe.z),"Malformed movement stays finite");
            }
            Vector3d motion=Vector3d.ZERO;
            for(int i=0;i<400;i++) {
                motion=BroomRules.motion(kind,motion,45,1,1,true,true,false,false);
                check(Math.abs(motion.x)<=kind.maxSpeed&&Math.abs(motion.z)<=kind.maxSpeed&&Math.abs(motion.y)<=1,"Sustained flight bounded");
            }
        }
        for(ChestSigilItem.Kind kind:ChestSigilItem.Kind.values())check(BroomRules.sigil(kind.id())==kind,"All eleven installed sigil identities");
        for(String invalid:new String[]{null,"","mending","other:sigil_mending","bewitchment:sigil_missing"})check(BroomRules.sigil(invalid)==null,"Invalid sigil inert");
        for(String name:new String[]{"Hudson","Masashi","Emmanuel","Michael","Yoshihiro","Lewis","Katushiro","Ashley"})check(LegacyItemVariants.sword(name+" BAT")==1,"Legacy sword aliases");
        for(String name:new String[]{"Haru","Haruspex","H4rv5p3x"})check(LegacyItemVariants.eye(name.toUpperCase(java.util.Locale.ROOT))==1,"First eye aliases");
        for(String name:new String[]{"Izuxe","Izu","Izuxe43ui520815"})check(LegacyItemVariants.eye(name)==2,"Second eye aliases");
        for(String name:new String[]{null,"","Hudson","Hudson Bat ","Haru!"})check(LegacyItemVariants.sword(name)==0&&LegacyItemVariants.eye(name)==0,"Exact names only");
        MagicProtectionEnchantment magic=new MagicProtectionEnchantment();
        check(magic.getMaxLevel()==4,"Magic protection max IV");
        for(Enchantment protection:new Enchantment[]{Enchantments.ALL_DAMAGE_PROTECTION,Enchantments.FIRE_PROTECTION,Enchantments.BLAST_PROTECTION,Enchantments.PROJECTILE_PROTECTION})
            check(!magic.isCompatibleWith(protection)&&!protection.isCompatibleWith(magic),"Protection excludes in both directions");
        check(magic.isCompatibleWith(Enchantments.FALL_PROTECTION)&&Enchantments.FALL_PROTECTION.isCompatibleWith(magic),"Feather Falling compatible");
        for(int level=0;level<=4;level++) {
            check(magic.getDamageProtection(level,DamageSource.MAGIC)==level,"Vanilla magic EPF");
            check(magic.getDamageProtection(level,DamageSource.GENERIC)==0&&magic.getDamageProtection(level,DamageSource.OUT_OF_WORLD)==0,"No physical/void EPF");
        }
        check(near(MagicProtectionEnchantment.reduce(75,16,true,false),59),"Additional legacy armor reduction");
        check(near(MagicProtectionEnchantment.reduce(75,100,true,false),55),"Reduction cap");
        check(MagicProtectionEnchantment.reduce(75,-1,true,false)==75,"Negative level ignored");
        check(MagicProtectionEnchantment.reduce(75,16,false,false)==75&&MagicProtectionEnchantment.reduce(75,16,true,true)==75,"Physical/void bypass extra reduction");
        System.out.println("Broom, named model and magic protection checks passed: "+checks);
    }
}
