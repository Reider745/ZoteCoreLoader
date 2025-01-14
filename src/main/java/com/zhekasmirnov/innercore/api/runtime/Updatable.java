package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.world.ScriptableSaverScope;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheka on 10.08.2017.
 */

public class Updatable {
    private static final Object[] EMPTY_ARGS = new Object[]{};

    private final List<ScriptableObject> updatableObjects = new ArrayList<>();
    private final List<ScriptableObject> disabledDueToError = new ArrayList<>();

    private Context currentContext = null;

    private Updatable() {}

    public List<ScriptableObject> getAllUpdatableObjects() {
        return updatableObjects;
    }

    public void addUpdatable(ScriptableObject obj) {
        Object _update = obj.get("update", obj);
        if (_update instanceof Function) {
            updatableObjects.add(obj);
        } else {
            throw new IllegalArgumentException("cannot add updatable object: <obj>.update must be a function, not " + (_update != null ? _update.getClass() : null) + " " + _update);
        }
    }

    public void onTick() {
        if (currentContext == null) {
            currentContext = Compiler.assureContextForCurrentThread();
        }

        // Keep amount to update a fixed number of objects, excluding new ones; as before, they will be executed on next game tick
        int updatableAmount = updatableObjects.size();
        for (int offset = 0; currentContext != null && offset < updatableAmount; offset++) {
            ScriptableObject updatable = updatableObjects.get(offset);

            if (!ScriptableObjectHelper.getBooleanProperty(updatable, "noupdate", false)) {
                try {
                    ((Function) updatable.get("update")).call(currentContext, updatable.getParentScope(), updatable, EMPTY_ARGS);
                } catch (Throwable e) {
                    updatableObjects.remove(offset--);
                    updatableAmount--;
                    disabledDueToError.add(updatable);

                    Object _handle_error = updatable.get("_handle_error");
                    if (_handle_error instanceof Function) {
                        Function handle_error = (Function) _handle_error;
                        try {
                            handle_error.call(currentContext, handle_error.getParentScope(), updatable, new Object[] { e });
                            continue;
                        } catch (Throwable th) {
                            Logger.error("UPDATABLE ERROR", "Error occurred in error handler for " + updatableToString(currentContext, updatable) + " hash=" + updatable.hashCode(), th);
                        }
                    }

                    reportError(e, updatableToString(currentContext, updatable));
                    continue;
                }
            }

            if (ScriptableObjectHelper.getBooleanProperty(updatable, "remove", false)) {
                updatableObjects.remove(offset--);
                updatableAmount--;
            }
        }
    }

    public void cleanUp() {
        updatableObjects.clear();
        disabledDueToError.clear();
        currentContext = null;
    }

    // for backward compatibility
    @Deprecated
    public void onPostTick() {}

    @Deprecated
    public void onTickSingleThreaded() {
        onTick();
        onPostTick();
    }

    private static final Updatable serverInstance = new Updatable();
    private static final Updatable clientInstance = new Updatable();

    public static Updatable getForServer() {
        return serverInstance;
    }

    public static Updatable getForClient() {
        return clientInstance;
    }

    public static void reportError(Throwable err, String updatableStr) {
        Logger.error("UPDATABLE ERROR", "Updatable " + updatableStr + " was disabled due to error, corresponding object will be disabled. To re-enable it re-enter the world.", err);
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

    static {
        WorldDataScopeRegistry.getInstance().addScope("_updatables", new ScriptableSaverScope() {
            @Override
            public ScriptableObject save() {
                ArrayList<ScriptableObject> updatableObjectsToSave = new ArrayList<>();
                for (ScriptableObject updatable : getForServer().updatableObjects) {
                    if (ObjectSaverRegistry.getSaverFor(updatable) != null) {
                        updatableObjectsToSave.add(updatable);
                    }
                }
                for (ScriptableObject updatable : getForServer().disabledDueToError) {
                    if (ObjectSaverRegistry.getSaverFor(updatable) != null && !ScriptableObjectHelper.getBooleanProperty(updatable, "remove", false)) {
                        updatableObjectsToSave.add(updatable);
                    }
                }
                return new NativeArray(updatableObjectsToSave.toArray());
            }

            @Override
            public void read(Object scope) {
                int successfullyLoaded = 0;
                if (scope instanceof NativeArray) {
                    Object[] updatableObjectsToRead = ((NativeArray) scope).toArray();
                    for (Object possibleUpdatable : updatableObjectsToRead) {
                        if (possibleUpdatable instanceof ScriptableObject) {
                            ScriptableObject scriptableUpdatable = (ScriptableObject) possibleUpdatable;
                            if (scriptableUpdatable.get("update") instanceof Function) {
                                getForServer().addUpdatable(scriptableUpdatable);
                                successfullyLoaded++;
                                continue;
                            }
                        }
                        ICLog.i("UPDATABLE", "loaded updatable data is not a scriptable object or it does not have update function, loading failed. obj=" + possibleUpdatable);
                    }
                } else {
                    ICLog.i("UPDATABLE", "assertion failed: updatable scope is not an array, loading failed");
                }

                ICLog.d("UPDATABLE", "successfully loaded updatables: " + successfullyLoaded);
            }
        });
    }
}
