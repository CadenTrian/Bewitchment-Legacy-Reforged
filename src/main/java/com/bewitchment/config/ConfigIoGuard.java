package com.bewitchment.config;

/** Forge 36 installs watchers before its initial correction/save has completed. */
public final class ConfigIoGuard {
    private static final Object LOCK = new Object();

    public static void run(Runnable action) {
        synchronized (LOCK) {
            action.run();
        }
    }

    private ConfigIoGuard() {}
}