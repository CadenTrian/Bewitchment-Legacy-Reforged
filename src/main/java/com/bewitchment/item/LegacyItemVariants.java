package com.bewitchment.item;

public final class LegacyItemVariants {
    public static int sword(String name){
        if(name==null)return 0;
        for(String first:new String[]{"Hudson","Masashi","Emmanuel","Michael","Yoshihiro","Lewis","Katushiro","Ashley"})
            if(name.equalsIgnoreCase(first+" Bat"))return 1;
        return 0;
    }
    public static int eye(String name){
        if(name==null)return 0;
        for(String value:new String[]{"Haru","Haruspex","H4rv5p3x"})if(value.equalsIgnoreCase(name))return 1;
        for(String value:new String[]{"Izuxe","Izu","Izuxe43ui520815"})if(value.equalsIgnoreCase(name))return 2;
        return 0;
    }
    private LegacyItemVariants(){}
}
