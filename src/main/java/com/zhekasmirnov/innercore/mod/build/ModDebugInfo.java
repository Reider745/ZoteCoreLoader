package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 16.08.2017.
 */

public class ModDebugInfo {
    public static class ExecutableStatus {
        private boolean isCompiled = false;

        private Executable executable;
        private Throwable compileError;

        public ExecutableStatus(Executable executable) {
            this.isCompiled = true;
            this.executable = executable;
        }

        public ExecutableStatus(Throwable compileError) {
            this.isCompiled = false;
            this.compileError = compileError;
        }

        public Throwable getError() {
            if (isCompiled) {
                return executable.getLastRunException();
            }
            else {
                return compileError;
            }
        }

        public String getStatus() {
            if (!isCompiled) {
                return "compile error: " + compileError;
            }
            if (executable.getLastRunException() != null) {
                return "run error: " + executable.getLastRunException();
            }
            return "ok" + (executable.isLoadedFromDex ? " [bytecode]" : "");
        }

        public String getReport() {
            Throwable err = getError();
            return getStatus() + (err != null ? "\n" + err.getMessage() : "");
        }

        public ScriptableObject getFont() {
            return null;
        }
    }

    private HashMap<String, ExecutableStatus> statusMap = new HashMap<>();

    public HashMap<String, ExecutableStatus> getStatusMap() {
        return statusMap;
    }

    public ScriptableObject getFormattedStatusMap() {
        ScriptableObject map = ScriptableObjectHelper.createEmpty();
        for (String name : statusMap.keySet()) {
            ExecutableStatus status = statusMap.get(name);
            ScriptableObject data = ScriptableObjectHelper.createEmpty();
            data.put("font", data, status.getFont());
            data.put("status", data, status.getStatus());
            data.put("report", data, status.getReport());
            map.put(name, map, data);
        }
        return map;
    }

    public void putStatus(String name, ExecutableStatus status) {
        statusMap.put(name, status);
    }

    public void putStatus(String name, Executable status) {
        statusMap.put(name, new ExecutableStatus(status));
    }

    public void putStatus(String name, Throwable status) {
        statusMap.put(name, new ExecutableStatus(status));
    }
}
