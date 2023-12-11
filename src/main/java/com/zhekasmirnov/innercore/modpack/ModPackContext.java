package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ModPackContext {
    private static final ModPackContext instance = new ModPackContext();

    public static ModPackContext getInstance() {
        return instance;
    }

    public interface ModPackSelectedListener {
        void onSelected(ModPack modPack);
    }

    public interface ModPackDeselectedListener {
        void onDeselected(ModPack modPack);
    }

    private final ModPackStorage storage = new ModPackStorage(
            new File(FileTools.DIR_PACK, "modpacks"),
            new File(FileTools.DIR_PACK, "modpacks-archive"),
            new File(FileTools.DIR_WORK));

    private ModPack currentModPack = null;

    private final List<ModPackSelectedListener> selectedListenerList = new ArrayList<>();
    private final List<ModPackDeselectedListener> deselectedListenerList = new ArrayList<>();

    private ModPackContext() {
    }

    public ModPackStorage getStorage() {
        return storage;
    }

    public ModPack getCurrentModPack() {
        return currentModPack;
    }

    public ModPackJsAdapter assureJsAdapter() {
        assurePackSelected();
        return getCurrentModPack().getJsAdapter();
    }

    public synchronized void setCurrentModPack(ModPack currentModPack) {
        if (this.currentModPack == currentModPack) {
            return;
        }
        if (this.currentModPack != null) {
            for (ModPackDeselectedListener listener : deselectedListenerList) {
                listener.onDeselected(this.currentModPack);
            }
        }
        this.currentModPack = currentModPack;
        currentModPack.reloadAndValidateManifest();
        if (this.currentModPack != null) {
            for (ModPackSelectedListener listener : selectedListenerList) {
                listener.onSelected(this.currentModPack);
            }
        }
    }

    public void assurePackSelected() {
        if (getCurrentModPack() == null) {
            ModPackSelector.restoreSelected();
        }
    }

    public void addSelectedListener(ModPackSelectedListener listener) {
        selectedListenerList.add(listener);
    }

    public void addDeselectedListener(ModPackDeselectedListener listener) {
        deselectedListenerList.add(listener);
    }

    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread taskThread = null;

    public void queueTask(Runnable task) {
        synchronized (taskQueue) {
            if (taskThread == null) {
                taskThread = new Thread(() -> {
                    while (true) {
                        try {
                            Runnable nextTask = taskQueue.poll(5000, TimeUnit.MILLISECONDS);
                            if (nextTask != null) {
                                nextTask.run();
                            } else {
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    synchronized (taskQueue) {
                        taskThread = null;
                    }
                });
                taskThread.start();
            }
            try {
                taskQueue.put(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
