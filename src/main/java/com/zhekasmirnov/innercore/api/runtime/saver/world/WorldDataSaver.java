package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.apparatus.adapter.innercore.UserDialog;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.saver.CriticalDataFile;
import org.innercore.icstd.serialization.ObjectSerializerDebug;
import org.innercore.icstd.serialization.SerializationContext;
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

    private static final String MAIN_FILE_NAME = "moddata.json";
    private static final String LEGACY_MAIN_SAVES_BACKUP = "moddata_backup.json";

    private final File worldDirectory;
    private final CriticalDataFile savesFile;
    private final Map<String, Object> missingScopeData = new HashMap<>();

    private SaverState state = SaverState.IDLE;
    private boolean savingErrorsReportedAsDialog = false;

    public WorldDataSaver(File worldDirectory) {
        this.worldDirectory = worldDirectory;
        savesFile = new CriticalDataFile(new File(worldDirectory, MAIN_FILE_NAME));
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
                return readJsonImpl(new File(directory, name));
            }
        }
        return null;
    }

    private JSONObject readJsonImpl(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            ICLog.i("SAVER", "reading saves from " + file.getAbsolutePath());
            return FileUtils.readJSON(file);
        } catch (IOException | JSONException exception) {
            ICLog.e("SAVER", "Error reading saves from " + file.getAbsolutePath(), exception);
            UserDialog.dialog("FAILED TO READ SAVES", "Failed to read this world main saves file " + file.getName(), exception, false);
            return null;
        }
    }

    // read all data on current thread
    public synchronized void readAllData(boolean showLogOnError) {
        if (worldDirectory == null) {
            return;
        }
        savingErrorsReportedAsDialog = false;

        JSONObject json = readJsonImpl(savesFile.beginRead());
        if (json == null) {
            json = readJsonImpl(savesFile.readBackupAfterMainFailed());
        }
        savesFile.endRead();
        if (json == null) {
            json = readJsonWithLockCheck(worldDirectory, LEGACY_MAIN_SAVES_BACKUP, false);
        }
        if (json == null) {
            return;
        }

        state = SaverState.READ;
        missingScopeData.clear();
        SerializationContext ctx = new SerializationContext();
        WorldDataScopeRegistry.getInstance().readAllScopes(json, ctx, missingScopeData);
        state = SaverState.IDLE;

        if (showLogOnError) {
            showErrorLog(SaverState.READ, ctx);
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
        SerializationContext ctx = new SerializationContext();
        WorldDataScopeRegistry.getInstance().saveAllScopes(json, ctx);
        state = SaverState.IDLE;

        File fileToWrite = savesFile.beginWrite();
        try {
            ICLog.i("SAVER", "writing saves to " + fileToWrite.getAbsolutePath());
            FileUtils.writeJSON(fileToWrite, json);
            if (!savesFile.endSuccessfulWrite(true))
                throw new RuntimeException("Failed finalize saving to " + savesFile);
        } catch (Throwable exception) {
            ICLog.e("SAVER", "failed to write saves to " + savesFile, exception);
            UserDialog.dialog("FAILED TO WRITE SAVES", "Failed to write this world saves", exception, false);
            savesFile.abortWrite();
        }

        if (showLogOnError) {
            showErrorLog(SaverState.SAVE, ctx);
        }
    }


    private static class StackTraceTreeNode {
        private final List<SerializationContext.ErrorRecord> records = new ArrayList<>();
        private final HashMap<StackTraceElement, StackTraceTreeNode> children = new HashMap<>();
        private final StackTraceTreeNode parent;

        public StackTraceTreeNode(StackTraceTreeNode parent) {
            this.parent = parent;
        }

        private void addRecordToRoot(SerializationContext.ErrorRecord record) {
            // add record to the tree
            Throwable err = record.getException();
            if (err == null) {
                records.add(record);
                return;
            }
            // get original cause
            while (err.getCause() != null) {
                err = err.getCause();
            }
            StackTraceTreeNode node = this;
            StackTraceElement[] trace = err.getStackTrace();
            for (int i = trace.length - 1; i >= 0; i--) {
                final StackTraceTreeNode parent = node;
                node = Java8BackComp.computeIfAbsent(node.children, trace[i], e -> new StackTraceTreeNode(parent));
            }
            node.records.add(record);
        }

        void appendToLog(StringBuilder sb, String prefix) {
            if (!records.isEmpty() || children.size() > 1) {
                prefix += ".  ";
            }
            if (!records.isEmpty()) {
                sb.append(prefix).append("\n");
                for (SerializationContext.ErrorRecord r : records) {
                    sb.append(prefix);
                    sb.append(r.formatMsg(false, 0, 0));
                    sb.append("\n");
                }
                sb.append(prefix).append("\n");
            }
            for (Map.Entry<StackTraceElement, StackTraceTreeNode> node : children.entrySet()) {
                sb.append(prefix).append(node.getKey()).append('\n');
                node.getValue().appendToLog(sb, prefix);
            }
        }
    }

    public void showErrorLog(SaverState state, SerializationContext ctx) {
        if (!ctx.hasErrors()) return;
        StackTraceTreeNode tree = new StackTraceTreeNode(null);
        for (SerializationContext.ErrorRecord err : ctx.errorsList) {
            tree.addRecordToRoot(err);
        }
        StringBuilder log = new StringBuilder();
        tree.appendToLog(log, "");

        Logger.error("SERIALIZATION ERRORS: \n\n" + log.toString());
        if (state == SaverState.SAVE) {
            if (savingErrorsReportedAsDialog) {
                UserDialog.toast("errors occurred during saving data, see log for details");
                return;
            }
            savingErrorsReportedAsDialog = true;
        }
        DialogHelper.openFormattedDialog("Errors occurred while " + (state == SaverState.READ ? "reading" : "saving") + " data:\n\n" + log, "SOME " + (state == SaverState.READ ? "READING" : "SAVING") + " ERRORS OCCURRED");
    }
}
