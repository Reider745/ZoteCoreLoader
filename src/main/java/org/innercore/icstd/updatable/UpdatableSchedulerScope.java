package org.innercore.icstd.updatable;

import com.zhekasmirnov.apparatus.minecraft.enums.EnumsContainer;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.UpdatableSchedulerScopeBindGenBase;
import org.innercore.icstd.updatable.impl.UpdatableSchedulerScopeImpl;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;
import java.util.List;

@ApiBinding
public class UpdatableSchedulerScope extends UpdatableSchedulerScopeBindGenBase {
    public final int scopeId;

    // internals
    String name;
    int curTick = 0;
    UpdatableComponent[] list = new UpdatableComponent[0];
    int[] tickingArray = new int[128];
    Context scriptCtx = null;
    Object[] scriptDtArg = new Object[1];

    UpdatableSchedulerScope(int id) {
        this.scopeId = id;
    }

    @ApiBinding
    public void setCurrentTick(int tickNo) {
        UpdatableScheduler.setCurrentTickImpl(scopeId, tickNo);
        curTick = tickNo;
    }

    @ApiBinding
    public int getCurrentTick() {
        return curTick;
    }

    public long createRawHandle(UpdatableComponent updatable) {
        synchronized (this) {
            long handle = UpdatableScheduler.createHandle(scopeId);
            int idx = (int) (handle & 0xFFFFFFL);
            if (list.length <= idx) {
                list = Arrays.copyOf(list, Math.max(idx + 16, list.length * 3 / 2 + 1));
            }
            list[idx] = updatable;
            return handle;
        }
    }

    public UpdatableHandle create(UpdatableComponent updatable) {
        return new UpdatableHandle(createRawHandle(updatable));
    }

    @ApiBinding("createRawHandle")
    public long createRawHandleForScript(ScriptableObject object, Function function) {
        return createRawHandle(new ScriptUpdatable(this, object, function));
    }

    @ApiBinding("create")
    public UpdatableHandle createForScript(ScriptableObject object, Function function) {
        return create(new ScriptUpdatable(this, object, function));
    }

    @ApiBinding("create")
    public UpdatableHandle createForScript(Scriptable objectOrFunction) {
        if (objectOrFunction instanceof Function) {
            return createForScript(null, (Function) objectOrFunction);
        } else {
            if (!(objectOrFunction instanceof ScriptableObject)) {
                throw new IllegalArgumentException("cannot add script updatable, parameter must be script object or function, got " + objectOrFunction);
            }
            ScriptableObject obj = (ScriptableObject) objectOrFunction;
            Object update = obj.get("update", obj);
            if (!(update instanceof Function)) {
                throw new IllegalArgumentException("cannot add updatable object: <obj>.update must be a function, not " + (update != null ? update.getClass() : null) + " " + update);
            }
            UpdatableHandle handle = createForScript(obj, (Function) update);
            obj.put("_handle", obj, handle);
            return handle;
        }
    }

    @ApiBinding("clearAll")
    public void clearAll() {
        synchronized (this) {
            UpdatableScheduler.clearAllImpl(scopeId);
            list = new UpdatableComponent[0];
            tickingArray = new int[128];
        }
    }

    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    public void executeTickForScope(int tickNo, int timeLimitUs, List<Throwable> outErrorList) {
        synchronized (this) { // synchronize with create
            if (curTick + 1 != tickNo && curTick != tickNo)
                throw new IllegalArgumentException("UpdatableScheduler.executeTickForScope tick mismatch " + curTick + "/" + tickNo);
            setCurrentTick(tickNo);
            tickingArray = nativeExecuteTickForScope(scopeId, tickingArray);
        }
        UpdatableComponent[] list = this.list;
        int[] toTick = tickingArray;
        int toTickEnd = toTick[0];
        int toTickExhaustiveEnd = toTick[1];
        int i;
        // if (toTickEnd != 2 || toTickExhaustiveEnd != toTick.length)
        //     ICLog.d("UpdatableScheduler", "toTick=" + Arrays.toString(toTick));

        // main tick part
        Throwable stoppedOnError = null;
        for (i = 2; i < toTickEnd; i += 2) {
            try {
                for (; i < toTickEnd; i += 2)
                    list[toTick[i]].update(toTick[i + 1]);
            } catch (Throwable t) {
                if (outErrorList != null) {
                    outErrorList.add(t);
                } else {
                    stoppedOnError = t;
                    break;
                }
            }
        }

        // exhaustive tick part
        if (stoppedOnError == null) {
            for (i = toTick.length - 2; i >= toTickExhaustiveEnd; i -= 2) {
                try {
                    for (; i >= toTickExhaustiveEnd; i -= 2)
                        list[toTick[i]].update(toTick[i + 1]);
                } catch (Throwable t) {
                    if (outErrorList != null) {
                        outErrorList.add(t);
                    } else {
                        stoppedOnError = t;
                        break;
                    }
                }
            }

        }

        nativeMarkTicked(scopeId, toTick, i + 2, toTick.length);
        if (stoppedOnError != null) {
            sneakyThrow(stoppedOnError);
        }
    }

    @ApiBinding("executeTickForScope")
    public void executeTickForScope(int tickNo, int timeLimitUs) {
        executeTickForScope(tickNo, timeLimitUs, null);
    }

    private static int[] nativeExecuteTickForScope(int scopeId, int[] toTick) {
        return UpdatableSchedulerScopeImpl.nativeExecuteTickForScope(scopeId, toTick);
    }

    private static void nativeMarkTicked(int scopeId, int[] toTick, int begin, int end) {
        UpdatableSchedulerScopeImpl.nativeMarkTicked(scopeId, toTick, begin, end);
    }
}
