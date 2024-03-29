package com.zhekasmirnov.innercore.api.mod.ui.window;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.UIEventHandler;
import com.zhekasmirnov.innercore.api.mod.util.InventorySource;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import java.util.ArrayList;

/**
 * Created by zheka on 01.08.2017.
 */

@Deprecated(since = "Zote")
public class WindowProvider {
    public static final WindowProvider instance;
    static {
        instance = new WindowProvider();
    }

    private static long frame = 0;

    public static long getFrame() {
        return frame;
    }

    private Thread currentThread = null;
    private int currentThreadId = 0;
    private boolean isThreadRunning = false;

    private final ArrayList<IWindow> openedWindows = new ArrayList<>();

    private void refreshCurrentState() {
        if (openedWindows.size() == 0) {
            if (isThreadRunning)
                stopThread();
        } else {
            if (!isThreadRunning)
                startThread();
        }

        InventorySource.isUpdating = false;
        for (IWindow window : openedWindows) {
            if (window.isInventoryNeeded()) {
                InventorySource.isUpdating = true;
                break;
            }
        }
    }

    public void onWindowOpened(IWindow window) {
        if (!openedWindows.contains(window)) {
            synchronized (openedWindows) {
                openedWindows.add(0, window);
                refreshCurrentState();
            }
        }
    }

    public void onWindowClosed(IWindow window) {
        if (openedWindows.contains(window)) {
            synchronized (openedWindows) {
                openedWindows.remove(window);
                refreshCurrentState();
            }
        }
    }

    private void startThread() {
        isThreadRunning = true;
        currentThreadId++;
        currentThread = new Thread(new ThreadRunnable(this));
        currentThread.start();
    }

    private void stopThread() {
        isThreadRunning = false;
        currentThreadId++;
    }

    private class ThreadRunnable implements Runnable {
        private WindowProvider windowProvider;
        private int runId;

        private ThreadRunnable(WindowProvider windowProvider) {
            this.windowProvider = windowProvider;
            this.runId = windowProvider.currentThreadId;
        }

        private void setupThreadAsUI() {
            // assure js context
            Compiler.assureContextForCurrentThread();
            // setup log event handler
            ICLog.setupEventHandlerForCurrentThread(new UIEventHandler());
        }

        @Override
        public void run() {
            setupThreadAsUI();

            // run
            while (runId == windowProvider.currentThreadId) {
                long time = System.currentTimeMillis();
                synchronized (openedWindows) {
                    for (IWindow window : openedWindows) {
                        window.frame(time);
                    }
                }
                frame++;
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static final int backPressEventTimeout = 250;
    private long lastBackPressEvent = 0;

    public boolean onBackPressed() {
        if (System.currentTimeMillis() < lastBackPressEvent + backPressEventTimeout) {
            return false;
        }
        lastBackPressEvent = System.currentTimeMillis();
        for (IWindow window : new ArrayList<IWindow>(openedWindows)) {
            if (window.onBackPressed()) {
                NativeAPI.preventPendingKeyEvent(0, backPressEventTimeout);
                return true;
            }
        }
        return false;
    }

    public void onActivityStopped() {
        for (IWindow window : new ArrayList<IWindow>(openedWindows)) {
            window.onBackPressed();
        }
    }
}
