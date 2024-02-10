package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zheka on 09.08.2017.
 */

public class Callback {
    private static class CallbackFunction {
        public final Function function;
        public final int priority;

        public CallbackFunction(Function function, int priority) {
            this.function = function;
            this.priority = priority;
        }
    }

    private static HashMap<String, ArrayList<CallbackFunction>> callbacks = new HashMap<>();
    public static boolean profilingEnabled = false, profilingShowParameters = false;

    public static int count(String name) {
        List<CallbackFunction> encounted = callbacks.getOrDefault(name, null);
        return encounted != null ? encounted.size() : 0;
    }

    public static void addCallback(String name, Function func, int priority) {
        if (!callbacks.containsKey(name)) {
            callbacks.put(name, new ArrayList<CallbackFunction>());
        }

        CallbackFunction callback = new CallbackFunction(func, priority);
        ArrayList<CallbackFunction> callbacksByName = callbacks.get(name);
        for (int i = 0; i < callbacksByName.size(); i++) {
            if (callbacksByName.get(i).priority < priority) {
                callbacksByName.add(i, callback);
                return;
            }
        }
        callbacksByName.add(callback);
    }

    public static void invokeCallbackV(String name, Object[] params) {
        Context ctx = Compiler.assureContextForCurrentThread();
        boolean isTick = name.contains("tick") || name.contains("Tick");
        if (profilingShowParameters && !isTick) {
            try {
                Logger.debug("Profiling/" + name,
                        ScriptableObjectHelper.stringify(ScriptableObjectHelper.createArray(params), null));
            } catch (RuntimeException e) {
            }
        }
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        Scriptable parent;
        if (funcs != null) {
            long profilingNanos = !profilingEnabled || isTick ? 0 : System.nanoTime();
            for (CallbackFunction callback : funcs) {
                parent = callback.function.getParentScope();
                callback.function.call(ctx, parent, parent, params);
            }
            if (profilingNanos != 0) {
                profilingNanos = System.nanoTime() - profilingNanos;
                Logger.debug("Profiling/" + name,
                        funcs.size() + " functions in " + (profilingNanos / 1000000f) + "ms.");
            }
        }
    }

    public static List<Runnable> getCallbackAsRunnableList(String name, final Object[] params) {
        List<Runnable> result = new ArrayList<Runnable>();
        boolean isTick = name.contains("tick") || name.contains("Tick");
        if (profilingShowParameters && !isTick) {
            result.add(() -> {
                try {
                    Logger.debug("Profiling/" + name,
                            ScriptableObjectHelper.stringify(ScriptableObjectHelper.createArray(params), null));
                } catch (RuntimeException e) {
                }
            });
        }
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        if (funcs != null) {
            AtomicLong profilingNanos = new AtomicLong();
            if (profilingEnabled && !isTick) {
                result.add(() -> profilingNanos.set(System.nanoTime()));
            }
            for (CallbackFunction func0 : funcs) {
                result.add(new Runnable() {
                    public void run() {
                        Scriptable parent = func0.function.getParentScope();
                        func0.function.call(Compiler.assureContextForCurrentThread(), parent, parent, params);
                    }
                });
            }
            if (profilingNanos.get() != 0) {
                result.add(() -> Logger.debug("Profiling/" + name, funcs.size() + " functions in "
                        + ((System.nanoTime() - profilingNanos.get()) / 1000000f) + "ms."));
            }
        }
        return result;
    }

    public static void invokeCallback(String name, Object... params) {
        invokeCallbackV(name, params);
    }

    public static void invokeAPICallbackUnsafe(String name, Object[] params) {
        invokeCallbackV(name, params);
    }

    public static Throwable invokeAPICallback(String name, Object... params) {
        try {
            invokeAPICallbackUnsafe(name, params);
        } catch (Throwable e) {
            ICLog.e("INNERCORE-CALLBACK", "error occurred while calling callback " + name, e);
            return e;
        }
        return null;
    }
}
