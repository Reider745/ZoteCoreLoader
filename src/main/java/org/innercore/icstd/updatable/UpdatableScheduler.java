package org.innercore.icstd.updatable;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.UpdatableSchedulerBindGenBase;
import com.zhekasmirnov.innercore.api.runtime.Updatable;
import org.innercore.icstd.annotations.ArgOverload;
import org.innercore.icstd.script.ScriptObjectComponent;
import org.innercore.icstd.updatable.impl.UpdatableSchedulerScopeImpl;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;

@ApiBinding("UpdatableScheduler")
public class UpdatableScheduler extends UpdatableSchedulerBindGenBase {
    private static UpdatableSchedulerScope[] scopes = new UpdatableSchedulerScope[4];

    @ApiBinding
    public static void clearAllImpl(int scopeId) {
        UpdatableSchedulerScopeImpl.clearAllImpl(scopeId);
    }

    @ApiBinding
    public static int createNewScopeImpl(String scopeName) {
        return UpdatableSchedulerScopeImpl.createNewScopeImpl(scopeName);
    }

    @ApiBinding
    public static long createHandle(int scopeId) {
        return UpdatableSchedulerScopeImpl.create(scopeId);
    }

    @ApiBinding
    public static boolean destroyHandle(long handleId) {
        return UpdatableSchedulerScopeImpl.destroy(handleId);
    }

    @ApiBinding
    public static boolean isRawHandleAlive(long handleId) {
        return UpdatableSchedulerScopeImpl.isRawHandleAlive(handleId);
    }

    @ApiBinding
    public static void scheduleByRawHandle(long handleId, int tickAt, @ArgOverload("-1") int tickUntil) {
        UpdatableSchedulerScopeImpl.scheduleByRawHandle(handleId, tickAt, tickUntil);
    }

    @ApiBinding
    public static void setRecurringScheduleByRawHandle(long handleId, int tickAt, @ArgOverload("-1") int tickUntil) {
        UpdatableSchedulerScopeImpl.setRecurringScheduleByRawHandle(handleId, tickAt, tickUntil);
    }

    @ApiBinding
    public static void clearScheduleByRawHandle(long handleId) {
        UpdatableSchedulerScopeImpl.clearScheduleByRawHandle(handleId);
    }

    @ApiBinding
    public static void setCurrentTickImpl(int scopeId, int tickNo) {
        UpdatableSchedulerScopeImpl.setCurrentTickImpl(scopeId, tickNo);
    }


    @ApiBinding("createNewScope")
    public static UpdatableSchedulerScope createNewScope(String name) {
        int scopeId = createNewScopeImpl(name);
        if (scopes.length <= scopeId) {
            scopes = Arrays.copyOf(scopes, Math.max(scopeId + 2, scopes.length * 3 / 2 + 1));
        }
        UpdatableSchedulerScope scope = scopes[scopeId] = new UpdatableSchedulerScope(scopeId);
        scope.name = name;
        scope.setCurrentTick(0);
        return scopes[scopeId];
    }

    @ApiBinding("getMainServerScope")
    public static UpdatableSchedulerScope getMainServerScope() { return Updatable.getForServer().updatableScope; }
    @ApiBinding("getMainClientScope")
    public static UpdatableSchedulerScope getMainClientScope() { return Updatable.getForClient().updatableScope; }

    public static UpdatableHandle createOnServer(UpdatableComponent updatable) { return getMainServerScope().create(updatable); }
    public static UpdatableHandle createOnClient(UpdatableComponent updatable) { return getMainClientScope().create(updatable); }

    @ApiBinding("createOnServer")
    public static UpdatableHandle createOnServerForScript(ScriptableObject object, Function function) { return getMainServerScope().createForScript(object, function); }
    @ApiBinding("createOnClient")
    public static UpdatableHandle createOnClientForScript(ScriptableObject object, Function function) { return getMainClientScope().createForScript(object, function); }
    @ApiBinding("createOnServer")
    public static UpdatableHandle createOnServerForScript(Scriptable objectOrFunction) { return getMainServerScope().createForScript(objectOrFunction); }
    @ApiBinding("createOnClient")
    public static UpdatableHandle createOnClientForScript(Scriptable objectOrFunction) { return getMainClientScope().createForScript(objectOrFunction); }


    @ApiBinding
    public static boolean destroyRawHandle(long handle) {
        return destroyHandle(handle);
        // do not remove anything from updatable list here, as it might interfere with tick execution (removing updatable during tick)
    }

    public static UpdatableComponent getByRawHandle(long handle) {
        if (isRawHandleAlive(handle)) {
            int idx = (int) (handle & 0xFFFFFFL);
            int scopeId = (int) (handle >> 32L);
            return scopes[scopeId].list[idx];
        }
        return null;
    }

    @ApiBinding("getByRawHandle")
    public static Object getByRawHandleForScript(long handle) {
        UpdatableComponent updatable = getByRawHandle(handle);
        return updatable instanceof ScriptObjectComponent ? ((ScriptObjectComponent) updatable).getScriptObject() : null;
    }

    @ApiBinding
    public static UpdatableSchedulerScope getScopeByRawHandle(long handle) {
        if (isRawHandleAlive(handle)) {
            int scopeId = (int) (handle >> 32L);
            return scopes[scopeId];
        }
        return null;
    }
}
