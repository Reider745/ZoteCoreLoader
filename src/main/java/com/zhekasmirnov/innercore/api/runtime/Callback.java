package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static int count(String name) {
        return callbacks.getOrDefault(name, new ArrayList<>()).size();
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
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        Scriptable parent;
        if (funcs != null) {
            for (CallbackFunction callback : funcs) {
                parent = callback.function.getParentScope();
                callback.function.call(ctx, parent, parent, params);
            }
        }
    }

    public static List<Runnable> getCallbackAsRunnableList(String name, final Object[] params) {
        List<Runnable> result = new ArrayList<Runnable>();
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        if (funcs != null) {
            for (CallbackFunction func0 : funcs) {
                final CallbackFunction func = func0;
                result.add(new Runnable() {
                    public void run() {
                        Scriptable parent = func0.function.getParentScope();
                        func0.function.call(Compiler.assureContextForCurrentThread(), parent, parent, params);
                    }
                });
            }
        }
        return result;
    }

    public static void invokeCallback(String name, Object ... params) {
        invokeCallbackV(name, params);
    }

    public static void invokeAPICallbackUnsafe(String name, Object[] params) {
        invokeCallbackV(name, params);
    }

    public static Throwable invokeAPICallback(String name, Object ... params) {
        Throwable result = null;
        try {
            invokeAPICallbackUnsafe(name, params);
        } catch (Exception e) {
            ICLog.e("INNERCORE-CALLBACK", "error occurred while calling callback " + name, e);
            result = e;
            Logger.error("Non-Fatal error occurred in callback " + name, e);
        }
        return result;
    }
}
