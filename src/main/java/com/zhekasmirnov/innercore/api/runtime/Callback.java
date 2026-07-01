package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.innercore.icstd.event.EventController;
import org.innercore.icstd.event.Events;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Created by zheka on 09.08.2017.
 */

public class Callback {
    public static void addCallback(String name, Function func, int priority) {
        Events.onScriptEvent(name, (ctrl, args) -> {
            Scriptable scope = func.getParentScope();
            if (ctrl.scriptCtx == null)
                ctrl.scriptCtx = Compiler.assureContextForCurrentThread();
            func.call((Context) ctrl.scriptCtx, scope, scope, args);
        }).setPriority(priority);
    }

    public static void invokeCallbackV(String name, Object[] params) {
        Events.invokeScriptEvent(null, name, params);
    }

    public static void invokeCallback(String name, Object ... params) {
        invokeCallbackV(name, params);
    }

    public static void invokeAPICallbackUnsafe(String name, Object[] params) {
        invokeCallbackV(name, params);
    }

    public static Throwable invokeAPICallback(String name, Object ... params) {
        EventController ctrl = new EventController();
        ctrl.catchExceptions = true;
        Events.invokeScriptEvent(ctrl, name, params);
        if (ctrl.errors != null && !ctrl.errors.isEmpty()) {
            for (Throwable e : ctrl.errors) {
                ICLog.e("INNERCORE-CALLBACK", "error occurred while calling callback " + name, e);
            }
            DialogHelper.reportNonFatalErrorList("Non-Fatal error occurred in callback " + name, ctrl.errors);
        }
        return null; // compatibility for potential legacy java mods, result is unused
    }
}
