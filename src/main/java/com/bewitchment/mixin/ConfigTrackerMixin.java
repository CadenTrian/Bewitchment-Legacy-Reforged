package com.bewitchment.mixin;

import com.bewitchment.config.ConfigIoGuard;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(value = ConfigTracker.class, remap = false)
public abstract class ConfigTrackerMixin {
    @Redirect(method = {"loadConfigs", "unloadConfigs"}, at = @At(value = "INVOKE",
            target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V"), require = 1)
    private void bewitchment$serializeConfigIo(Set<ModConfig> configs, Consumer<ModConfig> action) {
        ConfigIoGuard.run(() -> configs.forEach(action));
    }
}