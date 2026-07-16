package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.apparatus.adapter.innercore.UserDialog;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.innercore.icstd.script.ScriptObjectComponent;
import org.innercore.icstd.updatable.UpdatableComponent;
import org.innercore.icstd.serialization.*;
import org.innercore.icstd.updatable.*;
import org.mozilla.javascript.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zheka on 10.08.2017.
 */

public class Updatable {
    private static final Object[] EMPTY_ARGS = new Object[]{};
    private final Object[] DT_SCRIPT_ARGS = new Object[1]; // 1 instance per scope

    private final List<LegacyScriptUpdatable> updatableObjects = new ArrayList<>();

    public final UpdatableSchedulerScope updatableScope;
    private Context currentContext = null;

    private Updatable(String scopeName) {
        updatableScope = UpdatableScheduler.createNewScope(scopeName);
    }

    public List<ScriptableObject> getAllUpdatableObjects() {
        return updatableObjects.stream().map(u -> u.updatable).collect(Collectors.toList());
    }

    private void removeUpdatable(LegacyScriptUpdatable updatable) {
        if (updatableObjects.get(updatable.index) != updatable)
            throw new IllegalStateException();
        LegacyScriptUpdatable last = updatableObjects.get(updatableObjects.size() - 1);
        if (last != updatable) {
            updatableObjects.set(updatable.index, last);
            last.index = updatable.index;
        }
        updatableObjects.remove(updatableObjects.size() - 1);
    }


    public class LegacyScriptUpdatable implements UpdatableComponent, ScriptObjectComponent
    {
        ScriptableObject updatable;
        UpdatableHandle handle;
        int index;
        boolean isDisabledDueToError = false;
        Object[] scriptDtArgs = new Object[] { 1 };

        LegacyScriptUpdatable(ScriptableObject updatable, int index) {
            this.updatable = updatable;
            this.index = index;
            Object _update = updatable.get("update", updatable);
            if (!(_update instanceof Function)) {
                throw new IllegalArgumentException("cannot add updatable object: <obj>.update must be a function, not " + (_update != null ? _update.getClass() : null) + " " + _update);
            }
            this.handle = updatableScope.create(this).setRecurringSchedule(1).schedule(1);
            updatable.put("_handle", updatable, this.handle);
        }

        @Override
        public void update(int dt) {
            if (currentContext == null) {
                currentContext = Compiler.assureContextForCurrentThread();
            }
            if (!ScriptableObjectHelper.getBooleanProperty(updatable, "noupdate", false)) {
                try {
                    DT_SCRIPT_ARGS[0] = dt;
                    ((Function) updatable.get("update")).call(currentContext, updatable.getParentScope(), updatable, DT_SCRIPT_ARGS);
                } catch (Throwable e) {
                    isDisabledDueToError = true;
                    handle.destroy();
                    Object _handle_error = updatable.get("_handle_error");
                    if (_handle_error instanceof Function) {
                        Function handle_error = (Function) _handle_error;
                        try {
                            handle_error.call(currentContext, handle_error.getParentScope(), updatable, new Object[] { e });
                            return;
                        } catch (Throwable th) {
                            UserDialog.insignificantError("Error occurred in error handler for " + updatableToString(currentContext, updatable) + " hash=" + updatable.hashCode(), th, true);
                        }
                    }

                    reportError(e, updatableToString(currentContext, updatable));
                    return;
                }
            }

            if (ScriptableObjectHelper.getBooleanProperty(updatable, "remove", false)) {
                handle.destroy();
                removeUpdatable(this);
            }
        }

        @Override
        public Object getScriptObject() {
            return updatable;
        }

    }

    public void addUpdatable(ScriptableObject obj) {
        updatableObjects.add(new LegacyScriptUpdatable(obj, updatableObjects.size()));
    }

    public void cleanUp() {
        updatableObjects.clear();
        currentContext = null;
        updatableScope.clearAll();
        updatableScope.setCurrentTick(0);
    }

    public void onTick(int tickNo) {
        updatableScope.executeTickForScope(tickNo, -1);
    }

    // for backward compatibility

    @Deprecated
    public void onTick() {
        throw new RuntimeException("Deprecated");
    }

    @Deprecated
    public void onPostTick() {
        throw new RuntimeException("Deprecated");
    }

    @Deprecated
    public void onTickSingleThreaded() {
        throw new RuntimeException("Deprecated");
    }

    private static final Updatable serverInstance = new Updatable("server");
    private static final Updatable clientInstance = new Updatable("local");

    public static Updatable getForServer() {
        return serverInstance;
    }

    public static Updatable getForClient() {
        return clientInstance;
    }

    public static void reportError(Throwable err, String updatableStr) {
        UserDialog.dialog("UPDATABLE ERROR", "Updatable " + updatableStr + " was disabled due to error, corresponding object will be disabled. To re-enable it re-enter the world.", err, true);
    }

    private static String updatableToString(Context ctx, Scriptable obj) {
        Object _to_string = obj.get("_to_string", obj);
        if (_to_string instanceof Function) {
            Function to_string = (Function) _to_string;
            try {
                return "" + to_string.call(ctx, to_string.getParentScope(), obj, EMPTY_ARGS);
            } catch (Throwable ignore) {}
        }
        return "" + obj;
    }

    public static void cleanUpAll() {
        getForServer().cleanUp();
        getForClient().cleanUp();
    }

    // for backward compatibility
    @Deprecated
    public static final int MODE_COUNT_BASED = 0;
    @Deprecated
    public static final int MODE_TIME_BASED = 1;

    @Deprecated
    public static void setPreferences(int mode, int modeValue) {
        if (mode == MODE_COUNT_BASED || mode == MODE_TIME_BASED) {
            return;
        }
        throw new IllegalArgumentException("invalid updatable engine mode: " + mode);
    }

    public static void loadClass() {}

    static {
        WorldDataScopeRegistry.getInstance().addScope("_updatables", new WorldDataScopeRegistry.SaverScope() {
            @Override
            public void write(IScopedWriter io, SerializationContext ctx) {
                IArrayWriter array = io.asArray();
                for (LegacyScriptUpdatable updatable : getForServer().updatableObjects) {
                    if (ObjectSerializer.getSerializerTypeIdFromObject(updatable.updatable) != null) {
                        ObjectSerializer.serializeObject(array.beginScope(), updatable.updatable, ctx, true);
                    }
                }
                io.endScope();
            }

            @Override
            public void read(IScopedReader io, SerializationContext ctx) {
                IArrayReader array = io.asArray();
                if (array == null) {
                    io.endScope();
                    return;
                }
                int successfullyLoaded = 0;
                for (int i = 0; i < array.size(); i++) {
                    Object possibleUpdatable = ObjectSerializer.deserializeObject(array.beginScope(i), Object.class, ctx, true);
                    if (possibleUpdatable instanceof ScriptableObject) {
                        ScriptableObject scriptableUpdatable = (ScriptableObject) possibleUpdatable;
                        if (scriptableUpdatable.get("update") instanceof Function) {
                            getForServer().addUpdatable(scriptableUpdatable);
                            successfullyLoaded++;
                            continue;
                        }
                    }
                    if (possibleUpdatable != null) {
                        ctx.addError(null, possibleUpdatable, "loaded updatable data is not a scriptable object or it does not have update function, loading failed");
                    }
                }
                ICLog.d("UPDATABLE", "successfully loaded " + successfullyLoaded + "/" + array.size() + " updatables");
                io.endScope();
            }
        });
    }
}
