package com.bewitchment.mixin;

import com.bewitchment.config.ConfigIoGuard;
import com.electronwill.nightconfig.core.file.FileWatcher;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.io.IOException;
import java.nio.file.Path;

@Mixin(value = ConfigFileTypeHandler.class, remap = false)
public abstract class ConfigFileTypeHandlerMixin {
    @Redirect(method = "lambda$reader$1", at = @At(value = "INVOKE",
            target = "Lcom/electronwill/nightconfig/core/file/FileWatcher;addWatch(Ljava/nio/file/Path;Ljava/lang/Runnable;)V"), require = 1)
    private void bewitchment$serializeWatcher(FileWatcher watcher, Path path, Runnable callback) throws IOException {
        watcher.addWatch(path, () -> ConfigIoGuard.run(callback));
    }
}