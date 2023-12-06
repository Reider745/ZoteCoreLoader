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
    private final List<ScriptableObject> updatableObjects = new ArrayList<>();
    private final List<ScriptableObject> disabledDueToError = new ArrayList<>();
    private final boolean isMultithreadingAllowed;

    private Updatable(boolean isMultithreadingAllowed) {
        this.isMultithreadingAllowed = isMultithreadingAllowed;
    }

    public void cleanUp() {
        updatableObjects.clear();
        disabledDueToError.clear();
        postedRemovedUpdatables.clear();
        currentArrayPosition = 0;
        currentContext = null;
    }

    public List<ScriptableObject> getAllUpdatableObjects() {
        return updatableObjects;
    }

    public void addUpdatable(ScriptableObject obj) {
        Object _update = obj.get("update", obj);
        if (_update instanceof Function) {
            updatableObjects.add(obj);
        } else {
            throw new IllegalArgumentException("cannot add updatable object: <obj>.update must be a function, not "
                    + (_update != null ? _update.getClass() : null) + " " + _update);
        }
    }

    public static void cleanUpAll() {
        getForServer().cleanUp();
        getForClient().cleanUp();
    }

    public static final int MODE_COUNT_BASED = 0;
    public static final int MODE_TIME_BASED = 1;

    private static int currentMode = 0;
    private static int maxUpdateCallsPerTick = 128;
    private static int maxUpdateTimePerTick = 50;

    public static void setPreferences(int mode, int modeValue) {
        switch (mode) {
            case MODE_COUNT_BASED:
                maxUpdateCallsPerTick = modeValue;
                ICLog.d("THREADING", "updatable engine uses count-based mode, maxCount=" + modeValue);
                break;
            case MODE_TIME_BASED:
                maxUpdateTimePerTick = modeValue;
                ICLog.d("THREADING", "updatable engine uses time-based mode, maxTime=" + modeValue);
                break;
            default:
                throw new IllegalArgumentException("invalid updatable engine mode: " + mode);
        }

        currentMode = mode;
    }

    private static boolean shouldBeRemoved(Scriptable obj) {
        Object _remove = obj.get("remove", obj);
        return (_remove instanceof Boolean && (boolean) _remove);
    }

    private static String updatableToString(Context ctx, Scriptable obj) {
        Object _to_string = obj.get("_to_string", obj);
        if (_to_string instanceof Function) {
            try {
                Function to_string = (Function) _to_string;
                return "" + to_string.call(ctx, to_string.getParentScope(), obj, EMPTY_ARGS);
            } catch (Throwable ignore) {
            }
        }
        return "" + obj;
    }

    public static void reportError(Throwable err, String updatableStr) {
        Logger.error("UPDATABLE ERROR", new RuntimeException("Updatable " + updatableStr
                + " was disabled due to error, corresponding object will be disabled. To re-enable it re-enter the world.",
                err));
    }

    private static final Object[] EMPTY_ARGS = new Object[] {};

    private Context currentContext = null;

    private void executeUpdateWithContext(Context ctx, ScriptableObject obj) {
        Object _update = obj.get("update", obj);
        try {
            Function update = (Function) _update;
            update.call(ctx, update.getParentScope(), obj, EMPTY_ARGS);
        } catch (Throwable err) {
            disabledDueToError.add(obj);
            postedRemovedUpdatables.add(obj);
            Object _handle_error = obj.get("_handle_error", obj);
            if (_handle_error instanceof Function) {
                try {
                    Function handle_error = (Function) _handle_error;
                    handle_error.call(ctx, handle_error.getParentScope(), obj, new Object[] { err });
                } catch (Throwable err2) {
                    Logger.error("Error occurred in error handler for " + updatableToString(ctx, obj) + " hash="
                            + obj.hashCode(), err2);
                }
            } else {
                reportError(err, updatableToString(ctx, obj));
            }
            return;
        }

        if (shouldBeRemoved(obj)) {
            postedRemovedUpdatables.add(obj);
        }
    }

    private boolean executeUpdate(ScriptableObject obj) {
        if (ScriptableObjectHelper.getBooleanProperty(obj, "noupdate", false)) {
            return false;
        }

        TickExecutor executor = TickExecutor.getInstance();
        if (isMultithreadingAllowed && executor.isAvailable()) {
            TickExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    executeUpdateWithContext(Compiler.assureContextForCurrentThread(), obj);
                }
            });
        } else {
            executeUpdateWithContext(currentContext, obj);
        }

        return true;
    }

    private int currentArrayPosition = 0;
    private final ArrayList<ScriptableObject> postedRemovedUpdatables = new ArrayList<>();

    private void removePosted() {
        for (ScriptableObject updatable : postedRemovedUpdatables) {
            updatableObjects.remove(updatable);
        }
        postedRemovedUpdatables.clear();
    }

    private boolean executeCurrentToNext() {
        if (updatableObjects.size() == 0) {
            return true;
        }
        currentArrayPosition = currentArrayPosition % updatableObjects.size();
        boolean executed = executeUpdate(updatableObjects.get(currentArrayPosition));
        currentArrayPosition++;
        return executed;
    }

    private void onCountBasedTick() {
        int callCount = maxUpdateCallsPerTick;

        int calls = 0;
        for (int i = 0; i < callCount && i < updatableObjects.size();) {
            if (calls++ >= updatableObjects.size()) {
                break;
            }
            if (executeCurrentToNext()) {
                i++;
            }
        }
    }

    private void onTimeBasedTick() {
        int callCount = updatableObjects.size();
        long timeStart = System.currentTimeMillis();

        for (int i = 0; i < callCount; i++) {
            executeCurrentToNext();
            long timeCur = System.currentTimeMillis();
            if (timeCur - timeStart > maxUpdateTimePerTick) {
                break;
            }
        }
    }

    public void onTick() {
        if (currentContext == null) {
            currentContext = Compiler.assureContextForCurrentThread();
        }
        if (currentMode == MODE_COUNT_BASED || isMultithreadingAllowed && TickExecutor.getInstance().isAvailable()) {
            onCountBasedTick();
        } else if (currentMode == MODE_TIME_BASED) {
            onTimeBasedTick();
        }
    }

    public void onPostTick() {
        removePosted();
    }

    public void onTickSingleThreaded() {
        onTick();
        onPostTick();
    }

    private static final Updatable serverInstance = new Updatable(true);
    private static final Updatable clientInstance = new Updatable(false);

    public static Updatable getForServer() {
        return serverInstance;
    }

    public static Updatable getForClient() {
        return clientInstance;
    }

    public static void init() {
        // TODO: UNATTENDED
    }

    static {
        setPreferences(MODE_COUNT_BASED, 256);

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
                    if (ObjectSaverRegistry.getSaverFor(updatable) != null && !shouldBeRemoved(updatable)) {
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
                        ICLog.i("UPDATABLE",
                                "loaded updatable data is not a scriptable object or it does not have update function, loading failed. obj="
                                        + possibleUpdatable);
                    }
                } else {
                    ICLog.i("UPDATABLE", "assertion failed: updatable scope is not an array, loading failed");
                }

                ICLog.d("UPDATABLE", "successfully loaded updatables: " + successfullyLoaded);
            }
        });
    }
}
