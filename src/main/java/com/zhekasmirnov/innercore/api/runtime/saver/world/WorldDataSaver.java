package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldDataSaver {
    public enum SaverState {
        IDLE,
        READ,
        SAVE
    }

    private static class LoggedSavesError {
        final String message;
        final SaverState state;
        final Throwable error;

        LoggedSavesError(String message, SaverState state, Throwable error) {
            this.message = message;
            this.state = state;
            this.error = error;
        }
    }

    private static final String[] worldSavesFileNames = new String[] { "moddata.json", "moddata_backup.json" };

    private final File worldDirectory;
    private final List<LoggedSavesError> errorLog = new ArrayList<>();
    private final Map<String, Object> missingScopeData = new HashMap<>();

    private SaverState state = SaverState.IDLE;


    public WorldDataSaver(File worldDirectory) {
        this.worldDirectory = worldDirectory;
    }

    public File getWorldDirectory() {
        return worldDirectory;
    }

    public SaverState getState() {
        return state;
    }

    private JSONObject readJsonWithLockCheck(File directory, String name, boolean lockCheck) {
        File file = new File(directory, name);
        if (file.exists()) {
            if (!lockCheck || !FileUtils.getFileFlag(directory, name + "-opened")) {
                try {
                    return FileUtils.readJSON(file);
                } catch (Exception exception) {
                    Logger.error("FAILED TO READ SAVES", exception);
                }
            }
        }
        return null;
    }

    // read all data on current thread
    public synchronized void readAllData(boolean showLogOnError) {
        if (worldDirectory == null) {
            return;
        }

        JSONObject json = null;
        for (String name : worldSavesFileNames) {
            json = readJsonWithLockCheck(worldDirectory, name, true);
            if (json != null) {
                break;
            }
        }

        if (json == null) {
            json = readJsonWithLockCheck(worldDirectory, "moddata.json", false);
            if (json == null) {
                return;
            }
        }

        state = SaverState.READ;
        missingScopeData.clear();
        WorldDataScopeRegistry.getInstance().readAllScopes(
                json,
                (scope, error) -> logError("While reading scope " + scope, SaverState.READ, error),
                missingScopeData::put
        );
        state = SaverState.IDLE;

        if (showLogOnError) {
            showAndClearErrorLog(SaverState.SAVE);
        }
    }

    // save all data on current thread
    public synchronized void saveAllData(boolean showLogOnError) {
        // if in remote world
        if (worldDirectory == null) {
            return;
        }

        // assure directory
        if (!worldDirectory.isDirectory()) {
            worldDirectory.mkdirs();
        }

        state = SaverState.SAVE;
        JSONObject json = new JSONObject(missingScopeData);
        WorldDataScopeRegistry.getInstance().saveAllScopes(
                json,
                (scope, error) -> logError("While saving scope " + scope, SaverState.SAVE, error)
        );
        state = SaverState.IDLE;

        for (String name : worldSavesFileNames) {
            try {
                FileUtils.setFileFlag(worldDirectory, name + "-opened", true);
                FileUtils.writeJSON(new File(worldDirectory, name), json);
                FileUtils.setFileFlag(worldDirectory, name + "-opened", false);
            } catch (Exception exception) {
                Logger.error("FAILED TO WRITE SAVES",  exception);
            }
        }

        if (showLogOnError) {
            showAndClearErrorLog(SaverState.SAVE);
        }
    }



    public void logError(String message, SaverState state, Throwable err) {
        ICLog.e("", "", err);
        errorLog.add(new LoggedSavesError(message, state, err));
    }

    public void logError(String message, Throwable err) {
        errorLog.add(new LoggedSavesError(message, state, err));
    }

    public void clearErrorLog() {
        errorLog.clear();
    }

    public void showAndClearErrorLog(SaverState state) {
        if (!errorLog.isEmpty()) {
            StringBuilder log = new StringBuilder();
            for (LoggedSavesError err : errorLog) {
                log.append(err.message).append("\n").append(err.error.getMessage()).append("\n\n");
            }
            Logger.error("Errors occurred while " + (state == SaverState.READ ? "reading" : "saving") + " data:\n\n" + log, "SOME " + (state == SaverState.READ ? "READING" : "SAVING") + " ERRORS OCCURRED");
        }
        clearErrorLog();
    }

    public List<LoggedSavesError> getErrorLog() {
        return errorLog;
    }

    public static void logErrorStatic(String message, Throwable err) {
        WorldDataSaver saver = WorldDataSaverHandler.getInstance().getWorldDataSaver();
        if (saver != null) {
            saver.logError(message, err);
        }
    }
}
