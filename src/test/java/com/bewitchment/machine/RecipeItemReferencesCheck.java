package com.bewitchment.machine;
import com.google.gson.*;
import java.util.*;

public final class RecipeItemReferencesCheck {
    private static int checks;
    private static void verify(String json,boolean accepted) {
        Set<String> known=new HashSet<>(Arrays.asList("minecraft:glass_bottle","bewitchment:flying_ointment"));
        try {
            RecipeItemReferences.validate(new JsonParser().parse(json),known::contains);
            if(!accepted)throw new AssertionError("Unknown or malformed reference accepted: "+json);
        } catch(IllegalArgumentException error) {
            if(accepted)throw new AssertionError("Valid recipe rejected",error);
        }
        checks++;
    }
    public static void main(String[] args) {
        verify("{\"ingredients\":[{\"item\":\"minecraft:glass_bottle\"}],\"result\":{\"item\":\"bewitchment:flying_ointment\"}}",true);
        verify("{\"ingredients\":[{\"item\":\"bewitchment:missing\"}]}",false);
        verify("{\"result\":{\"item\":\"bewitchment:missing\"}}",false);
        verify("{\"ingredients\":[[{\"item\":\"minecraft:glass_bottle\"},{\"item\":\"bewitchment:missing\"}]]}",false);
        verify("{\"ingredients\":[[{\"item\":\"minecraft:glass_bottle\"},{\"item\":\"bewitchment:flying_ointment\"}]]}",true);
        for(String value:new String[]{"null","1","true","[]","{}","\"\""})verify("{\"item\":"+value+"}",false);
        verify("{\"ingredients\":[{\"tag\":\"forge:wax\"}],\"count\":1,\"group\":\"item\"}",true);
        verify("{\"outputs\":[{\"chance\":0.5,\"stack\":{\"item\":\"bewitchment:missing\"}}]}",false);
        verify("{\"item\":\"other:flying_ointment\"}",false);
        verify("{\"result\":\"bewitchment:missing\"}",false);
        verify("{\"result\":\"bewitchment:flying_ointment\"}",true);
        System.out.println("PASS: "+checks+" recipe reference checks for nested ingredients, outputs, alternatives, malformed IDs and tag deferral");
    }
}
