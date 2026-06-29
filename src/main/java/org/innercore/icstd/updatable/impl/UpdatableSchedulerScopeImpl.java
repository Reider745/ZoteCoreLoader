package org.innercore.icstd.updatable.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Static owner of all scopes (replaces global g_updatable_scopes).
 * Provides the high‑level API that matches the original CGB‑bindable functions.
 */
public class UpdatableSchedulerScopeImpl {
    private static final List<UpdatableScheduler> scopes = new ArrayList<>();

    /**
     * Creates a new scope and returns its ID.
     */
    public static int createNewScopeImpl(String scopeName) {
        synchronized (scopes) {
            int scope = scopes.size();
            scopes.add(new UpdatableScheduler((short) scope));
            return scope;
        }
    }

    /**
     * Clears all updatables in a scope.
     */
    public static void clearAllImpl(int scope) {
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return;
            sched = scopes.get(scope);
        }
        sched.extLock.writeLock().lock();
        try {
            sched.clearAll();
        } finally {
            sched.extLock.writeLock().unlock();
        }
    }

    /**
     * Creates a new updatable in the given scope, returning the packed handle.
     */
    public static long create(int scope) {
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return -1;
            sched = scopes.get(scope);
        }
        return sched.create().getHandle();
    }

    /**
     * Destroys an updatable identified by its packed handle.
     */
    public static boolean destroy(long handle) {
        UpdatableRef ref = new UpdatableRef(handle);
        short scope = ref.getScope();
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return false;
            return scopes.get(scope).destroy(ref);
        }
    }

    /**
     * Checks whether a handle is still alive.
     */
    public static boolean isRawHandleAlive(long handle) {
        UpdatableRef ref = new UpdatableRef(handle);
        short scope = ref.getScope();
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return false;
            return scopes.get(scope).exists(ref);
        }
    }

    /**
     * Schedules an updatable for a one‑shot tick window.
     */
    public static void scheduleByRawHandle(long handle, int tickAt, int tickUntil) {
        if (tickUntil < tickAt) tickUntil = tickAt;
        UpdatableRef ref = new UpdatableRef(handle);
        short scope = ref.getScope();
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return;
            sched = scopes.get(scope);
        }
        sched.schedule(ref, tickAt, tickUntil);
    }

    /**
     * Sets a recurring schedule for an updatable.
     */
    public static void setRecurringScheduleByRawHandle(long handle, int tickAt, int tickUntil) {
        if (tickUntil < tickAt) tickUntil = tickAt;
        UpdatableRef ref = new UpdatableRef(handle);
        short scope = ref.getScope();
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return;
            sched = scopes.get(scope);
        }
        sched.setRecurringSchedule(ref, tickAt, tickUntil);
    }

    /**
     * Clears the immediate schedule of an updatable (keeps recurring settings).
     */
    public static void clearScheduleByRawHandle(long handle) {
        UpdatableRef ref = new UpdatableRef(handle);
        short scope = ref.getScope();
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scope < 0 || scope >= scopes.size()) return;
            sched = scopes.get(scope);
        }
        sched.clearSchedule(ref);
    }

    /**
     * Sets the current tick counter for a scope.
     */
    public static void setCurrentTickImpl(int scopeId, int tickNo) {
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scopeId < 0 || scopeId >= scopes.size()) return;
            sched = scopes.get(scopeId);
        }
        sched.extLock.writeLock().lock();
        try {
            sched.curTick = tickNo;
        } finally {
            sched.extLock.writeLock().unlock();
        }
    }

    /**
     * Replaces the native JNI method – executes a tick for a scope and returns
     * an int array with indices of updatables that must be processed now and later.
     */
    public static int[] nativeExecuteTickForScope(int scopeId, int[] retainArr) {
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scopeId < 0 || scopeId >= scopes.size()) return new int[0];
            sched = scopes.get(scopeId);
        }

        sched.extLock.writeLock().lock();
        try {
            int alive = sched.getAliveCnt();
            int needed = alive * 2 + 4;
            if (retainArr == null || retainArr.length < needed) {
                retainArr = new int[needed + 64];  // generous extra
            }
            sched.scheduleAtTickForJava(retainArr);
            return retainArr;
        } finally {
            sched.extLock.writeLock().unlock();
        }
    }

    /**
     * Replaces the native JNI method – marks a range of indices as ticked.
     */
    public static void nativeMarkTicked(int scopeId, int[] arr, int begin, int end) {
        UpdatableScheduler sched;
        synchronized (scopes) {
            if (scopeId < 0 || scopeId >= scopes.size()) return;
            sched = scopes.get(scopeId);
        }
        sched.markBatchTickedForJava(arr, begin, end);
    }
}
