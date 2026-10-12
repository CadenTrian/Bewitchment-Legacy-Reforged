package com.bewitchment.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class ConfigIoGuardCheck {
    public static void main(String[] args) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch writing = new CountDownLatch(1), finish = new CountDownLatch(1), watcherStarted = new CountDownLatch(1);
            AtomicInteger callbacks = new AtomicInteger();
            Future<?> initial = workers.submit(() -> ConfigIoGuard.run(() -> {
                writing.countDown();
                try { if (!finish.await(5, TimeUnit.SECONDS)) throw new AssertionError("Writer timeout"); }
                catch (InterruptedException e) { throw new AssertionError(e); }
            }));
            if (!writing.await(5, TimeUnit.SECONDS)) throw new AssertionError("Writer never entered");
            Future<?> reload = workers.submit(() -> { watcherStarted.countDown(); ConfigIoGuard.run(callbacks::incrementAndGet); });
            if (!watcherStarted.await(5, TimeUnit.SECONDS)) throw new AssertionError("Watcher never started");
            try { reload.get(100, TimeUnit.MILLISECONDS); throw new AssertionError("Watcher overlapped initial save"); }
            catch (TimeoutException expected) { }
            finally { finish.countDown(); }
            initial.get(5, TimeUnit.SECONDS); reload.get(5, TimeUnit.SECONDS);
            if (callbacks.get() != 1) throw new AssertionError("Reload lost or duplicated");
            RuntimeException failure = new RuntimeException("expected");
            try { ConfigIoGuard.run(() -> { throw failure; }); throw new AssertionError("Exception swallowed"); }
            catch (RuntimeException e) { if (e != failure) throw e; }
            workers.submit(() -> ConfigIoGuard.run(callbacks::incrementAndGet)).get(5, TimeUnit.SECONDS);
            ConfigIoGuard.run(() -> ConfigIoGuard.run(callbacks::incrementAndGet));
            if (callbacks.get() != 3) throw new AssertionError("Lock not released or reentrant callback failed");
            AtomicInteger active = new AtomicInteger();
            Runnable stress = () -> { for (int i=0; i<1000; i++) ConfigIoGuard.run(() -> {
                if (active.incrementAndGet()!=1) throw new AssertionError("Concurrent config access");
                Thread.yield(); active.decrementAndGet();
            }); };
            Future<?> a=workers.submit(stress), b=workers.submit(stress);
            a.get(10,TimeUnit.SECONDS); b.get(10,TimeUnit.SECONDS);
            System.out.println("PASS: watcher exclusion, delayed reload, exception propagation/release, reentrancy and 2000 serialized operations");
        } finally { workers.shutdownNow(); }
    }
}