package com.bewitchment.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.minecraftforge.common.ForgeConfigSpec;

/** A fully corrected template avoids simultaneous first-load autosave/watcher writes. */
public final class DevServerDefaultsCheck {
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Field field=net.minecraftforge.common.ForgeConfig.class.getDeclaredField("serverSpec");
        field.setAccessible(true);ForgeConfigSpec spec=(ForgeConfigSpec)field.get(null);
        int checks=0;
        for(String path:new String[]{"config/dev-defaultconfigs/forge-server.toml","run/defaultconfigs/forge-server.toml"}){
            try(CommentedFileConfig config=CommentedFileConfig.of(path)){
                config.load();String before=new TomlWriter().writeToString(config);
                if(!spec.isCorrect(config))throw new AssertionError("Invalid Forge defaults: "+path);
                spec.correct(config);
                if(!before.equals(new TomlWriter().writeToString(config)))throw new AssertionError("Forge still needs to correct values/comments: "+path);
                checks++;
            }
        }
        System.out.println("PASS: "+checks+" Forge default configs need no startup correction.");
    }
}
