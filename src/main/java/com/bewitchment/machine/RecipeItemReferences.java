package com.bewitchment.machine;

import com.google.gson.*;
import java.util.Map;
import java.util.function.Predicate;

/** Explicit item references in shipped recipe ingredients and outputs must exist. */
public final class RecipeItemReferences {
    public static void validate(JsonElement value, Predicate<String> registered) {
        if(value.isJsonArray())for(JsonElement child:value.getAsJsonArray())validate(child,registered);
        else if(value.isJsonObject())for(Map.Entry<String,JsonElement> field:value.getAsJsonObject().entrySet()) {
            if(field.getKey().equals("item") || (field.getKey().equals("result") && field.getValue().isJsonPrimitive() && field.getValue().getAsJsonPrimitive().isString())) {
                JsonElement id=field.getValue();
                if(!id.isJsonPrimitive()||!id.getAsJsonPrimitive().isString()||!registered.test(id.getAsString()))
                    throw new IllegalArgumentException("Unknown or invalid item reference: "+id);
            } else validate(field.getValue(),registered);
        }
    }
    private RecipeItemReferences(){}
}
