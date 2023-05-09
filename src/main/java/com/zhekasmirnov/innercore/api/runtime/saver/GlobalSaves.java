package com.zhekasmirnov.innercore.api.runtime.saver;

/**
 * Created by zheka on 20.08.2017.
 */

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GlobalSaves {
    public static final String SAVES_FILE_NAME = "moddata.json";
    public static final String SAVES_RESERVE_FILE_NAME = "moddata_backup.json";

    private static ScriptableObject globalScope = null;

    private static boolean isBeautified = true;

    private static void throwError(Throwable e) {
        RuntimeException r = new RuntimeException("error occurred in global saves");
        r.initCause(e);
        throw r;
    }

    private static void readScope(String fileName) {
        String dir = LevelInfo.getAbsoluteDir();
        if (dir == null) {
            return;
//            throwError(new IllegalStateException("reading data while world is not loading"));
        }

        globalScope = null;

        String jsonStr = null;
        try {
            jsonStr = FileTools.readFileText(dir + fileName);
        } catch (IOException e) {
            throwError(e);
        }

        try {
            globalScope = (ScriptableObject) JsonHelper.parseJsonString(jsonStr);
        } catch (JSONException | ClassCastException e) {
            throwError(e);
        }
    }

    private static void writeScope() {
        String dir = LevelInfo.getAbsoluteDir();
        if (dir == null || globalScope == null) {
            return;
            // throwError(new IllegalStateException("writing data while world is not loading"));
        }

        try {
            String text = JsonHelper.scriptableToJsonString(globalScope, isBeautified);
            FileTools.writeFileText(dir + SAVES_FILE_NAME, text);
            FileTools.writeFileText(dir + SAVES_RESERVE_FILE_NAME, text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class LoggedSavesError {
        final String message;
        final Throwable error;
    
        LoggedSavesError(String message, Throwable error) {
            this.message = message;
            this.error = error;
        }
    }
    
    private static ArrayList<LoggedSavesError> savesErrors = new ArrayList<>();
    
    public static void logSavesError(String message, Throwable err) {
        savesErrors.add(new LoggedSavesError(message, err));
    }

    public static void clearSavesErrorLog() {
        savesErrors.clear();
    }

    public static ArrayList<LoggedSavesError> getSavesErrorLog() {
        return savesErrors;
    }

    public static void showSavesErrorsDialogIfRequired(boolean isReading) {
        if (!savesErrors.isEmpty()) {
            StringBuilder log = new StringBuilder();
            for (LoggedSavesError err : savesErrors) {
                log.append(err.message).append("\n").append(err.message).append("\n\n");
            }
            Logger.error("Errors occurred while " + (isReading ? "reading" : "saving") + " data:\n\n" + log, "SOME " + (isReading ? "READING" : "SAVING") + " ERRORS OCCURRED");
        }
    }


    private static HashMap<String, GlobalSavesScope> globalScopeMap = new HashMap<>();

    public static void registerScope(String name, GlobalSavesScope scope) {
        while (globalScopeMap.containsKey(name)) {
            name += name.hashCode() & 0xFF;
        }

        scope.setName(name);
        globalScopeMap.put(name, scope);
    }

    public synchronized static void readSaves() {
        ICLog.d("SAVES", "reading saves...");
        updateAutoSaveTime();


        try {
            clearSavesErrorLog();
            readScope(SAVES_FILE_NAME);
        }
        catch (RuntimeException e) {
            try {
                clearSavesErrorLog();
                readScope(SAVES_RESERVE_FILE_NAME);
            } catch(RuntimeException e2) {
                ICLog.e("SAVES", "failed to read saves", e2);
                globalScope = ScriptableObjectHelper.createEmpty();
            }
        }

        for (String name : globalScopeMap.keySet()) {
            Object saverScope;
            if (globalScope.has(name, globalScope)) {
                saverScope = globalScope.get(name);
            }
            else {
                saverScope = ScriptableObjectHelper.createEmpty();
            }

            try {
                globalScopeMap.get(name).read(saverScope);
            } catch (Exception err) {
                logSavesError("error in reading scope " + name, err);
            }
        }
        showSavesErrorsDialogIfRequired(true);

        Callback.invokeAPICallback("ReadSaves", globalScope);
    }

    public synchronized static void writeSaves() {
        ICLog.d("SAVES", "writing saves...");
        updateAutoSaveTime();

        globalScope = ScriptableObjectHelper.createEmpty();

        clearSavesErrorLog();
        for (String name : globalScopeMap.keySet()) {
            try {
                globalScope.put(name, globalScope, globalScopeMap.get(name).save());
            } catch (Exception err) {
                logSavesError("error in writing saves scope " + name, err);
            }
        }

        Callback.invokeAPICallback("WriteSaves", globalScope);

        try {
            writeScope();
        }
        catch (RuntimeException e) {
            ICLog.e("SAVES", "failed to write saves", e);
        }
        showSavesErrorsDialogIfRequired(false);
    }



    private static Thread currentThread = null;
    private static int currentThreadQueueSize = 0;

    private static boolean isReadComplete = false;

    public static boolean isReadComplete() {
        return isReadComplete;
    }

    public static void setIsReadComplete(boolean isReadComplete) {
        GlobalSaves.isReadComplete = isReadComplete;
    }

    public static void writeSavesInThread(final boolean saveMCPEWorld) {
        currentThreadQueueSize++;

        if (currentThread == null) {
            currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long start, end;
                    while (currentThreadQueueSize > 0) {
                        ICLog.d("SAVES", "started saving world data");

                        MainThreadQueue.serverThread.enqueue(new Runnable() {
                            public void run() {
                                if (saveMCPEWorld) {
                                    ICLog.d("SAVES", "saving minecraft world...");
                                    ICLog.flush();
                                    long start = System.currentTimeMillis();
                                    NativeAPI.forceLevelSave();
                                    long end = System.currentTimeMillis();
                                    ICLog.d("SAVES", "saving minecraft world in thread took " + (end - start) + " ms");
                                }
                            }
                        });

                        start = System.currentTimeMillis();
                        writeSaves();
                        end = System.currentTimeMillis();
                        ICLog.d("SAVES", "saving mod data in thread took " + (end - start) + " ms");

                        ICLog.flush();
                        currentThreadQueueSize--;
                    }
                    currentThread = null;
                }
            });
            currentThread.start();
        }
    }

    public static void sleepUntilThreadEnd() {
        long start = System.currentTimeMillis();
        while (currentThread != null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        long end = System.currentTimeMillis();
        ICLog.d("SAVES", "delaying main thread while saving data took " + (end - start) + " ms");
    }



    private static boolean autoSaveEnabled = false;
    private static int autoSavePeriod = 30000;
    private static long lastAutoSave = 0;

    public static void setAutoSaveParams(boolean enabled, int period) {
        autoSaveEnabled = enabled;
        autoSavePeriod = Math.max(5000, period);
        ICLog.d("SAVES", "auto-save params set enabled=" + enabled + " period=" + period);
    }

    private static void updateAutoSaveTime() {
        lastAutoSave = System.currentTimeMillis();
    }

    // private static void _crashTest() {
    //     for (int i = 0; i < 10000; i++) {
    //         final int id = i;
    //         new Thread(new Runnable() {
    //             @Override
    //             public void run() {
    //                 Process.setThreadPriority(19);
    //                 try {
    //                     Thread.sleep(100);
    //                 } catch (InterruptedException e) {
    //                     e.printStackTrace();
    //                 }
    //                 ICLog.d("SAVES", "crash test started " + id);
    //                 NativeAPI.forceLevelSave();
    //                 ICLog.d("SAVES", "crash test ended " + id);
    //             }
    //         }).start();
    //     }
    // }

    public static void startAutoSaveIfNeeded() {
        if (autoSaveEnabled) {
            long time = System.currentTimeMillis();
            if (time - lastAutoSave > autoSavePeriod) {
                updateAutoSaveTime();
                writeSavesInThread(true);
            }
        }
    }
}
