package com.reider745.api;

import cn.nukkit.event.Event;
import cn.nukkit.level.Level;
import com.reider745.api.hooks.HookController;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CallbackHelper {
    public enum Type {
        GENERATION_CHUNK_OVERWORLD,
        GENERATION_CHUNK_NETHER,
        GENERATION_CHUNK_END,
        PRE_GENERATION_CHUNK,
        PRE_GENERATION_CHUNK_NETHER,
        PRE_GENERATION_CHUNK_END,
        GENERATION_CHUNK_CUSTOM,
        PRE_GENERATION_CHUNK_CUSTOM;
    }

    private static final HashMap<Type, ThreadCustomValue<Level>> types = new HashMap<>();

    public interface ICallbackApply {
        void apply();
    }

    public static class ThreadCustomValue<T> extends Thread {
        private T value;
        private final ArrayList<ICallbackApply> applys = new ArrayList<>();

        public final T getValue() {
            return value;
        }

        public void add(ICallbackApply apply) {
            synchronized (applys) {
                this.applys.add(apply);
            }
        }

        @Override
        public void run() {
            while (true) {
                synchronized (applys) {
                    Iterator<ICallbackApply> it = applys.iterator();
                    while (it.hasNext())
                        it.next().apply();
                    applys.clear();
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void registerCallback(Type type) {
        ThreadCustomValue<Level> threadRegion = new ThreadCustomValue<>();
        threadRegion.setName(type.name());
        threadRegion.start();

        types.put(type, threadRegion);
    }

    public static void init() {
        for (Type type : Type.values()) {
            registerCallback(type);
        }
    }

    private static class ThreadCallback extends Thread {
        protected final ICallbackApply apply;

        protected boolean isPrevented;

        public boolean isPrevent() {
            return isPrevented;
        }

        public void prevent() {
            this.isPrevented = true;
        }

        public ThreadCallback(ICallbackApply apply) {
            this.apply = apply;
        }
    }

    private static class ThreadCallbackEvent extends ThreadCallback {
        protected final Event event;
        protected final boolean isPrevented;

        public ThreadCallbackEvent(Event event, ICallbackApply apply, boolean isPrevented) {
            super(apply);
            this.event = event;
            this.isPrevented = isPrevented;
        }

        @Override
        public void run() {
            if (isPrevented || !event.isCancelled()) {
                apply.apply();
                if (isPrevent()) {
                    event.setCancelled();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static class ThreadCallbackController extends ThreadCallback {
        protected final HookController<Event> event;

        public ThreadCallbackController(HookController<Event> event, ICallbackApply apply) {
            super(apply);
            this.event = event;
        }

        @Override
        public void run() {
            apply.apply();
            event.setResult(isPrevent());
        }
    }

    public static void applyRegion(Type type, Level level, ICallbackApply apply) {
        ThreadCustomValue<Level> threadRegion = types.get(type);
        threadRegion.value = level;
        threadRegion.add(apply);
    }

    public static void apply(Event event, ICallbackApply apply, boolean isPrevented) {
        Thread thread = new ThreadCallbackEvent(event, apply, isPrevented);
        thread.setName(event.getEventName());
        thread.start();

        while (thread.isAlive()) {
            java.lang.Thread.yield();
        }
    }

    public static <T> ThreadCustomValue<T> applyCustomValue(String name, ICallbackApply apply, T def) {
        ThreadCustomValue<T> thread = new ThreadCustomValue<>();
        thread.value = def;
        thread.add(apply);
        thread.setName(name);
        thread.start();

        while (thread.isAlive()) {
            java.lang.Thread.yield();
        }
        return thread;
    }

    public static void prevent() {
        Thread thread = Thread.currentThread();
        if (thread instanceof ThreadCallback threadCallback) {
            threadCallback.prevent();
        }
    }

    public static boolean isPrevent() {
        Thread thread = Thread.currentThread();
        if (thread instanceof ThreadCallback threadCallback) {
            return threadCallback.isPrevent();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void setValueForCurrent(Object value) {
        Thread thread = Thread.currentThread();
        if (thread instanceof ThreadCustomValue custom) {
            try {
                custom.value = value;
            } catch (ClassCastException e) {
                Logger.error("Casting to " + custom.getValue() + " cannot be performed, value override failed!");
            }
        }
    }

    public static Level getForCurrentThread() {
        Thread thread = Thread.currentThread();
        if (thread instanceof ThreadCustomValue threadCallback && threadCallback.getValue() instanceof Level) {
            return (Level) threadCallback.getValue();
        }
        return null;
    }
}
