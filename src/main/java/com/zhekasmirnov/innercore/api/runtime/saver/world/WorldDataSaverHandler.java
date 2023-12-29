package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.OperationTimeLogger;

import java.io.File;

public class WorldDataSaverHandler {
    private static final WorldDataSaverHandler instance = new WorldDataSaverHandler();

    public static WorldDataSaverHandler getInstance() {
        return instance;
    }

    private WorldDataSaver worldDataSaver = null;

    private boolean autoSaveEnabled = true;
    private boolean autoSaveMinecraftWorld = true;
    private int autoSaveInterval = 30000;

    private boolean saveWasQueued = false;
    private long previousAutoSave = 0;

    public void fetchParamsFromConfig() {
        final int auto_save_period = InnerCoreServer.getAutoSavePeriod();

        setParams(
                auto_save_period >= 20,
                InnerCoreServer.canAutoSaveWorld(),
                auto_save_period * 1000
        );
    }

    public void setParams(boolean autoSaveEnabled, boolean autoSaveMinecraftWorld, int autoSaveInterval) {
        this.autoSaveEnabled = autoSaveEnabled;
        this.autoSaveMinecraftWorld = autoSaveMinecraftWorld;
        this.autoSaveInterval = autoSaveInterval;
    }

    public void queueSave() {
        saveWasQueued = true;
    }

    public void onLevelSelected(File worldDirectory) {
        ICLog.d("WorldDataSaverHandler", "level selected: " + worldDirectory.getAbsolutePath());
        initSaverOnNewWorldLoad(worldDirectory);
    }

    public void onConnectedToRemoteWorld() {
        initSaverOnNewWorldLoad(null);
    }

    public void onLevelLoading() {
        ICLog.d("WorldDataSaverHandler", "reading data: "
                + (worldDataSaver != null ? "dir=" + worldDataSaver.getWorldDirectory() : "save is null"));
        readDataOnLoad();
        previousAutoSave = System.currentTimeMillis();
    }

    public void onLevelLeft() {
        ICLog.d("WorldDataSaverHandler", "level left: "
                + (worldDataSaver != null ? "dir=" + worldDataSaver.getWorldDirectory() : "save is null"));
        saveAndReleaseSaver();
    }

    public void onPauseScreenOpened() {
        queueSave();
    }

    public void onTick() {
        if (saveWasQueued) {
            saveWasQueued = false;
            runWorldAndDataSave();
        }
        if (autoSaveEnabled) {
            if (autoSaveInterval + previousAutoSave < System.currentTimeMillis()) {
                previousAutoSave = System.currentTimeMillis();
                runWorldAndDataSave();
            }
        }
    }

    public synchronized WorldDataSaver getWorldDataSaver() {
        return worldDataSaver;
    }

    // if directory was changed save and release previous instance
    private synchronized WorldDataSaver initSaverOnNewWorldLoad(File worldDirectory) {
        if (worldDataSaver != null
                && (worldDirectory == null || !worldDirectory.equals(worldDataSaver.getWorldDirectory()))) {
            saveAndReleaseSaver();
        }
        worldDataSaver = new WorldDataSaver(worldDirectory);
        return worldDataSaver;
    }

    private synchronized void runWorldAndDataSave() {
        if (worldDataSaver != null) {
            //OperationTimeLogger logger = new OperationTimeLogger(false).start();
            worldDataSaver.saveAllData(true);
            //logger.finish("saving all mod data done in %f seconds");
            if (autoSaveMinecraftWorld) {
                NativeAPI.forceLevelSave();
               // logger.finish("minecraft world done in %f seconds");
            }
        } else {
            reportUnexpectedStateError("World data saver was not initialized during runWorldAndDataSave() call");
        }
    }

    private synchronized void readDataOnLoad() {
        if (worldDataSaver != null) {
            OperationTimeLogger logger = new OperationTimeLogger(EngineConfig.isDeveloperMode()).start();
            worldDataSaver.readAllData(true);
            logger.finish("reading all mod data done in %f seconds");
        } else {
            reportUnexpectedStateError("World data saver was not initialized during readDataOnLoad() call");
        }
    }

    private synchronized void saveAndReleaseSaver() {
        if (worldDataSaver != null) {
            OperationTimeLogger logger = new OperationTimeLogger(EngineConfig.isDeveloperMode()).start();
            worldDataSaver.saveAllData(true);
            logger.finish("saving all mod data done in %f seconds").start();
            worldDataSaver = null;
        } else {
            reportUnexpectedStateError("World data saver was not initialized during saveAndReleaseSaver() call");
        }
    }

    private void reportUnexpectedStateError(String message) {
        Logger.error("UNEXPECTED WORLD SAVER STATE", message);
    }
}
