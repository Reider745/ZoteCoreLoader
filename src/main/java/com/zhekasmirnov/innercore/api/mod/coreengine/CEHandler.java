package com.zhekasmirnov.innercore.api.mod.coreengine;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 24.08.2017.
 */

public class CEHandler {
    public final Executable coreEngine;

    public CEHandler(Executable coreEngine) {
        this.coreEngine = coreEngine;
    }

    private ScriptableObject scope = null;

    private boolean isLoaded = false;
    public void load() {
        if (!isLoaded) {
            isLoaded = true;
            scope = coreEngine.getScope();
            coreEngine.run();
        }
    }

    public static final Object CALL_FAILED = new Object();
    public static final Object GET_FAILED = new Object();

    public Object callMethod(String name, Object ... params) {
        if (!isLoaded || scope == null) {
            return CALL_FAILED;
        }

        Object _func = scope.get(name);
        if (_func instanceof Function) {
            return ((Function) _func).call(Compiler.assureContextForCurrentThread(), scope, scope, params);
        }
        return CALL_FAILED;
    }

    public void injectCoreAPI(ScriptableObject scope) {
        Object result = callMethod("injectCoreAPI", scope);

        if (result == CALL_FAILED) {
            ICLog.e(CoreEngineAPI.LOGGER_TAG, "failed to inject CoreAPI: method call failed", new RuntimeException());
        }
    }

    public Object getValue(String name) {
        if (!isLoaded || scope == null) {
            return CALL_FAILED;
        }

        return scope.get(name);
    }

    public Object requireGlobal(String name) {
        if (!isLoaded || scope == null) {
            return CALL_FAILED;
        }

        return coreEngine.evaluateStringInScope(name);
    }
}
