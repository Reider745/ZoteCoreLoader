package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by zheka on 24.02.2018.
 */

public class Library extends Executable {
    private String libName = null;
    private int versionCode = 0;
    private boolean isShared = false;

    private ArrayList<LibraryDependency> dependencies = new ArrayList<>();

    public Library(Context context, Script script, ScriptableObject scriptScope, CompilerConfig config,
            API apiInstance) {
        super(context, script, scriptScope, config, apiInstance);
    }

    public Library(Context context, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        super(context, scriptScope, config, apiInstance);
    }

    public String getLibName() {
        return libName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public ArrayList<LibraryDependency> getDependencies() {
        return dependencies;
    }

    public boolean isShared() {
        return isShared;
    }

    private LibraryState state = LibraryState.NONE;
    // private boolean isOldFormatted = false;

    private void setState(LibraryState state) {
        this.state = state;
    }

    public boolean isInvalid() {
        return state == LibraryState.INVALID;
    }

    public boolean isInitialized() {
        return state != LibraryState.NONE;
    }

    public boolean isPrepared() {
        return isInitialized() && !isInvalid() && state != LibraryState.INITIALIZED;
    }

    public boolean isLoaded() {
        return state == LibraryState.LOADED;
    }

    private void onFatalException(Throwable exception) {
        setState(LibraryState.INVALID);

        // report
        lastRunException = exception;
        ICLog.e("INNERCORE-EXEC", "failed to run executable '" + name + "', some errors occurred:", exception);
    }

    private static class RunInterruptionException extends RuntimeException {

    }

    private static class InvalidHeaderCall extends RuntimeException {
        public InvalidHeaderCall(String msg) {
            super(msg);
        }
    }

    private void headerCall(String name, int version, String apiName, boolean shared,
            ArrayList<LibraryDependency> dependencies) {
        if (name == null) {
            throw new InvalidHeaderCall("Error in library initialization - name is not given");
        }

        API api = API.getInstanceByName(apiName);
        if (api == null) {
            throw new InvalidHeaderCall("Error in library initialization - invalid API name: " + apiName);
        }

        if (!isPrepared()) {
            libName = name;
            versionCode = version;
            isShared = shared;
            apiInstance = api;

            this.dependencies = dependencies;
            prepare();

            throw new RunInterruptionException();
        }
    }

    public void initialize() {
        setState(LibraryState.INITIALIZED);

        scriptScope.put("LIBRARY", scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                ScriptableObjectWrapper params = new ScriptableObjectWrapper((Scriptable) args[0]);

                ArrayList<LibraryDependency> dependencies = new ArrayList<>();
                ScriptableObjectWrapper dependenciesArr = params.getScriptableWrapper("dependencies");
                if (dependenciesArr != null) {
                    Object[] strings = dependenciesArr.asArray();
                    for (Object str : strings) {
                        if (str instanceof String) {
                            LibraryDependency dependency = new LibraryDependency((String) str);
                            dependency.setParentMod(getParentMod());
                            dependencies.add(dependency);
                        }
                    }
                }

                headerCall(params.getString("name"), params.getInt("version"), params.getString("api"),
                        params.getBoolean("shared"), dependencies);
                return null;
            }
        });

        try {
            runScript();
        } catch (RunInterruptionException ignore) {
            // isOldFormatted = false;
        } catch (InvalidHeaderCall err) {
            onFatalException(err);
        } catch (Throwable ignore) {
            // isOldFormatted = true;
            isShared = true;
            libName = compilerConfig.getName();
            versionCode = 0;

            if (libName.endsWith(".js")) {
                libName = libName.substring(0, libName.length() - 3);
            }

            prepare();
        }
    }

    public void prepare() {
        if (isInvalid() || !isInitialized()) {
            return;
        }

        // inject default API
        try {
            injectStaticAPIs();
            if (apiInstance != null) {
                apiInstance.injectIntoScope(scriptScope);
                apiInstance.prepareExecutable(this);
            }
        } catch (Throwable err) {
            onFatalException(err);
            return;
        }

        // inject annotations
        new LibraryAnnotation("$EXPORT", new Class[] { CharSequence.class }).injectMethod(scriptScope);
        new LibraryAnnotation("$BACKCOMP", new Class[] { Number.class }).injectMethod(scriptScope);

        // inject export method
        ScriptableFunctionImpl EXPORT = new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                String name = (String) objects[0];
                Object value = objects[1];
                int targetVersion = -1;

                if (name.contains(":")) {
                    String[] parts = name.split(":");
                    name = parts[0];
                    try {
                        targetVersion = Integer.valueOf(parts[1]);
                    } catch (NumberFormatException ignore) {
                        ICLog.i("ERROR",
                                "invalid formatted library export name " + name + " target version will be ignored");
                    }
                }

                LibraryExport export = new LibraryExport(name, value);
                export.setTargetVersion(targetVersion);
                addExport(export);
                return null;
            }
        };

        scriptScope.put("registerAPIUnit", scriptScope, EXPORT);
        scriptScope.put("EXPORT", scriptScope, EXPORT);

        setState(LibraryState.PREPARED);
    }

    private boolean isLoadingInProgress = false;

    public boolean isLoadingInProgress() {
        return isLoadingInProgress;
    }

    public void load() {
        if (!isPrepared()) {
            onFatalException(new IllegalStateException("Trying to load library without calling prepare()"));
            return;
        }

        isLoadingInProgress = true;

        // force all required libs to load
        for (LibraryDependency dependency : dependencies) {
            if (LibraryRegistry.resolveDependencyAndLoadLib(dependency) == null) {
                ICLog.i("ERROR", "failed to resolve dependency " + dependency + " for library " + libName
                        + ", it may load incorrectly.");
            }
        }

        try {
            runScript();
        } catch (Throwable err) {
            onFatalException(err);
            isLoadingInProgress = false;
            return;
        }

        ArrayList<LibraryAnnotation.AnnotationSet> allAnnotations = LibraryAnnotation.getAllAnnotations(scriptScope);
        for (LibraryAnnotation.AnnotationSet annotationSet : allAnnotations) {
            resolveAnnotations(annotationSet);
        }

        ICLog.d("LIBRARY", "library loaded " + libName + ":" + versionCode);

        setState(LibraryState.LOADED);
        isLoadingInProgress = false;
    }

    private void resolveAnnotations(LibraryAnnotation.AnnotationSet set) {
        LibraryAnnotation.AnnotationInstance exportAnnotation = set.find("$EXPORT");
        LibraryAnnotation.AnnotationInstance backCompAnnotation = set.find("$BACKCOMP");

        if (exportAnnotation != null) {
            LibraryExport export = new LibraryExport(exportAnnotation.getParameter(0, CharSequence.class).toString(),
                    set.getTarget());
            if (backCompAnnotation != null) {
                export.setTargetVersion(backCompAnnotation.getParameter(0, Number.class).intValue());
            }
            addExport(export);
        }
    }

    private ArrayList<LibraryExport> exports = new ArrayList<>();
    private HashSet<String> exportNames = new HashSet<>();

    public HashSet<String> getExportNames() {
        return exportNames;
    }

    private void addExport(LibraryExport export) {
        if (export.name == null || export.name.equals("*")) {
            throw new IllegalArgumentException("invalid library export name: " + export.name);
        }

        exports.add(export);
        exportNames.add(export.name);
    }

    public LibraryExport getExportForDependency(LibraryDependency dependency, String exportName) {
        LibraryExport result = null;

        for (LibraryExport export : exports) {
            if (exportName.equals(export.name)) {
                if (result == null) {
                    result = export;
                }
                // back comp resolve
                if (dependency.hasTargetVersion() && export.hasTargetVersion()) {
                    if (export.getTargetVersion() >= dependency.minVersion) {
                        if (!result.hasTargetVersion() || result.getTargetVersion() > export.getTargetVersion()) {
                            result = export;
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Object runForResult() {
        throw new UnsupportedOperationException("runForResult is not supported for library executables");
    }
}
