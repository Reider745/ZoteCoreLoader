package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.apparatus.ecs.core.EntitySystem;
import com.zhekasmirnov.apparatus.ecs.types.ServerTicking;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.InventorySource;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.GlobalSaves;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;

import java.util.ArrayList;

// WARNING! this class is no longer used and kept here like a stub, in case some mods need it
public class TickManager {
    private static String FATAL_ERROR_MESSAGE = "Fatal error occurred in mods thread, all mods are stopped. To resume you must re-enter the world.";

    private static TickManager currentThread;
    private static boolean isCurrentTreadStopped = false;
    private static Throwable lastFatalError = null;

    private boolean running = false;
    private boolean pause = false;
    private boolean isRunningNow = false;

    public static int globalTickCounter = 0; // reserve time in case thread has stopped
    public int time = 0;

    private ArrayList<Tick> tickQueue = new ArrayList<>();

    private static void reportFatalError(Throwable e) {
        ICLog.e("THREADING", "error occurred in ticking thread, it will be stopped for current session", e);
    }

    private static long debugTickTime = 0;
    public static void nativeTick() {
        if (currentThread == null) {
            return;
        }

        if (currentThread.running && !currentThread.pause) {
            try {
                long timeStart = System.currentTimeMillis();
                currentThread.callTick(Tick.next());
                long timeEnd = System.currentTimeMillis();
                debugTickTime += timeEnd - timeStart;

                /*
                if (getTime() % 20 == 0) {
                    ICLog.d("TickExecutor", "server tick took " + debugTickTime / 20.0 + " ms");
                    debugTickTime = 0;
                }*/
            }
            catch (Throwable e) {
                reportFatalError(e);
                stop();
            }
        }
    }



    public void prepare() {
        tickQueue.clear();
        Tick.resetTickCounter();
    }

    public static void stop() {
        if (currentThread != null) {
            currentThread.running = false;
            currentThread.tickQueue.clear();
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
        lastFatalError = null;
        isCurrentTreadStopped = false;

        ICLog.d("THREADING", "ticking thread started");
    }

    public static boolean isStopped() {
        return isCurrentTreadStopped;
    }

    public static Throwable getLastFatalError() {
        return lastFatalError;
    }

    public static void clearLastFatalError() {
        lastFatalError = null;
    }

    public static int getTime() {
        return currentThread != null ? currentThread.time : globalTickCounter;
    }

    public static boolean isPaused() {
        return currentThread != null && currentThread.pause;
    }

    public static boolean isRunningNow() {
        return currentThread != null && currentThread.isRunningNow;
    }

    public static void setPaused(boolean paused) {
        if (currentThread != null) {
            currentThread.pause = paused;
        }
    }

	public static void resume() {
        // NativeAPI.setTicksPerSecond(20);
        setPaused(false);
	}

    public static void pause() {
        setPaused(true);
        // NativeAPI.setTicksPerSecond(0);
    }



    private static final Object[] EMPTY_ARGS = new Object[]{};

    private void callTick(Tick tick) {
        if (tick == null) {
            time++;
        }
        else {
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
