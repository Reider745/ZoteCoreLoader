package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.InventorySource;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;

// WARNING! this class is no longer used and kept here like a stub, in case some mods need it
public class TickManager {
    private static TickManager currentThread;
    private static boolean isCurrentTreadStopped = false;

    private boolean running = false;
    private boolean pause = false;

    public static int globalTickCounter = 0; // reserve time in case thread has stopped
    public int time = 0;

    private static void reportFatalError(Throwable e) {
        ICLog.e("THREADING", "error occurred in ticking thread, it will be stopped for current session", e);
    }

    public static void nativeTick() {
        if (currentThread == null) {
            return;
        }

        if (currentThread.running && !currentThread.pause) {
            try {
                currentThread.callTick(Tick.next());
            } catch (Throwable e) {
                reportFatalError(e);
                stop();
            }
        }
    }

    public void prepare() {
        Tick.resetTickCounter();
    }

    public static void stop() {
        if (currentThread != null) {
            currentThread.running = false;
            currentThread = null;
            isCurrentTreadStopped = true;
        }
    }

    public static void setupAndStart() {
        stop();

        currentThread = new TickManager();
        currentThread.prepare();
        globalTickCounter = 0;

        currentThread.running = true;
        currentThread.pause = false;
        isCurrentTreadStopped = false;

        ICLog.d("THREADING", "ticking thread started");
    }

    public static boolean isStopped() {
        return isCurrentTreadStopped;
    }

    public static Throwable getLastFatalError() {
        return null;
    }

    public static void clearLastFatalError() {
    }

    public static int getTime() {
        return currentThread != null ? currentThread.time : globalTickCounter;
    }

    public static boolean isPaused() {
        return currentThread != null && currentThread.pause;
    }

    public static boolean isRunningNow() {
        return false;
    }

    public static void setPaused(boolean paused) {
        if (currentThread != null) {
            currentThread.pause = paused;
        }
    }

    public static void resume() {
        setPaused(false);
    }

    public static void pause() {
        setPaused(true);
    }

    private static final Object[] EMPTY_ARGS = new Object[] {};

    private void callTick(Tick tick) {
        if (tick == null) {
            time++;
        } else {
            time = tick.time;
        }
        globalTickCounter++;

        InventorySource.tick();
        ArmorRegistry.onTick();
        WorldDataSaverHandler.getInstance().onTick();

        TickExecutor executor = TickExecutor.getInstance();
        if (executor.isAvailable()) {
            executor.execute(Callback.getCallbackAsRunnableList("tick", EMPTY_ARGS));
        } else {
            Callback.invokeAPICallbackUnsafe("tick", EMPTY_ARGS);
        }
        Updatable.getForServer().onTick();
        executor.blockUntilExecuted();
        Updatable.getForServer().onPostTick();
    }

    public static class Tick {
        private static int tickCounter = 0;

        private static void resetTickCounter() {
            tickCounter = 0;
        }

        private final int time;

        private Tick(int time) {
            this.time = time;
        }

        private static Tick next() {
            return new Tick(tickCounter++);
        }
    }
}
