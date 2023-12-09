package com.zhekasmirnov.innercore.mod.executable;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.NativeJavaScript;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryDependency;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.*;

import java.util.HashMap;

/**
 * Created by zheka on 28.07.2017.
 */

public class Executable implements Runnable {
    public Context parentContext;
    public Script script;
    public ScriptableObject scriptScope;

    public CompilerConfig compilerConfig;
    public API apiInstance;
    public String name;

    private Mod parentMod = null;

    public void setParentMod(Mod parentMod) {
        this.parentMod = parentMod;
    }

    public Mod getParentMod() {
        return parentMod;
    }

    public Executable(Context context, Script script, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        this.parentContext = context;
        this.script = script;
        this.scriptScope = scriptScope;

        this.apiInstance = apiInstance;
        this.compilerConfig = config;
        this.name = config.getName();
    }

    public Executable(Context context, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        this.parentContext = context;
        this.script = null;
        this.scriptScope = scriptScope;

        this.apiInstance = apiInstance;
        this.compilerConfig = config;
        this.name = config.getName();
    }

    public boolean isLoadedFromDex = false;

    protected boolean isRunning = false;
    public boolean isRunning() {
        return isRunning;
    }

    public ScriptableObject getScope() {
        return scriptScope;
    }

    public void addToScope(ScriptableObject obj) {
        if (obj == null) {
            return;
        }

        Object[] keys = obj.getAllIds();
        for (int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            if (key instanceof String) {
                scriptScope.put((String) key, scriptScope, obj.get(key));
            }
        }
    }

    public void injectValueIntoScope(String name, Object obj) {
        scriptScope.put(name, scriptScope, obj);
    }

    public Object evaluateStringInScope(String str) {
        return parentContext.evaluateString(scriptScope, str, name, 0, null);
    }

    public Object callFunction(String name, Object[] args) {
        Object _func = ScriptableObjectHelper.getProperty(scriptScope, name, null);
        if (_func != null && _func instanceof Function) {
            Function func = (Function) _func;
            return func.call(parentContext, scriptScope, scriptScope, args);
        }
        return null;
    }

    public Function getFunction(String name) {
        Object _func = ScriptableObjectHelper.getProperty(scriptScope, name, null);
        if (_func != null && _func instanceof Function) {
            return  (Function) _func;
        }
        return null;
    }

    protected Throwable lastRunException = null;

    public Throwable getLastRunException() {
        return lastRunException;
    }


    private static final HashMap<String, Scriptable> javaWrapCache = new HashMap<String, Scriptable>();

    public void injectStaticAPIs() {
        IDRegistry.injectAPI(scriptScope);

        Function importLib = new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String libName = (String) params[0];
                String valueName = params.length > 1 ? (String) params[1] : "*";

                LibraryDependency dependency = new LibraryDependency(libName);
                dependency.setParentMod(getParentMod());
                LibraryRegistry.importLibrary(scriptScope, dependency, valueName);

                return null;
            }
        };

        scriptScope.put("importLib", scriptScope, importLib);
        scriptScope.put("IMPORT", scriptScope, importLib);

        scriptScope.put("IMPORT_NATIVE", scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                return NativeJavaScript.injectNativeModule((String) params[0], scriptScope);
            }
        });

        scriptScope.put("WRAP_NATIVE", scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                return NativeJavaScript.wrapNativeModule((String) params[0]);
            }
        });

        scriptScope.put("WRAP_JAVA", scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String name = (String) params[0];
                if(name.contains("com.zhekasmirnov.horizon.launcher.ads")){
                    throw new IllegalArgumentException("Unauthorized");
                } 
                Scriptable result = javaWrapCache.get(name);
                if (result != null) {
                    return result;
                }
                try {
                    result = new NativeJavaClass(parent, Class.forName(name), false);
                } catch(ClassNotFoundException e) {
                    result = new NativeJavaPackage(name);
                }
                javaWrapCache.put(name, result);
                return result;
            }
        });

        scriptScope.put("__packdir__", scriptScope, FileTools.DIR_PACK);
        scriptScope.put("__modpack__", scriptScope, Context.javaToJS(ModPackContext.getInstance().assureJsAdapter(), scriptScope));

        if(!InnerCoreServer.canEvalEnable())
            scriptScope.put("eval", scriptScope, new ScriptableFunctionImpl() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    return null;
                }
            });
    }

    private boolean isApiAdded = false;

    public void run() {
        // String str = parentContext.decompileScript(script, 4);
        // System.out.println(str);
        runForResult();

    }

    protected Object runScript() {
        Context context = Compiler.assureContextForCurrentThread();
        return script.exec(context, scriptScope);
    }

    protected void injectAPI() {
        if (!isApiAdded) {
            isApiAdded = true;
            injectStaticAPIs();
            if (apiInstance != null) {
                apiInstance.prepareExecutable(this);
            }
        }
    }

    public Object runForResult() {
        if (isRunning) {
            throw new RuntimeException("Could not run executable '" + name + "', it is already running");
        }
        isRunning = true;

        try {
            injectAPI();
        }
        catch (Throwable e) {
            lastRunException = e;
            ICLog.e("INNERCORE-EXEC", "failed to inject API to executable '" + name + "', some errors occurred:", e);
            return null;
        }

        try {
            return runScript();
        } catch (Throwable e) {
            lastRunException = e;
            ICLog.e("INNERCORE-EXEC", "failed to run executable '" + name + "', some errors occurred:", e);

            return null;
        }
    }

    public void reset() {
        isRunning = false;
    }
}
