package com.zhekasmirnov.apparatus.multiplayer;

import java.util.Objects;
import java.util.function.Supplier;

public class ThreadTypeMarker {
    private static final ThreadTypeMarker singleton = new ThreadTypeMarker();

    public static ThreadTypeMarker getSingleton() {
        return singleton;
    }


    public enum Mark {
        CLIENT,
        SERVER,
        UNKNOWN
    }

    private ThreadTypeMarker() {
    }

    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    private final ThreadLocal<Mark> threadMark = new SuppliedThreadLocal<>(() -> Mark.UNKNOWN);

    public Mark getCurrentThreadMark() {
        return threadMark.get();
    }

    public static boolean isClientThread() {
        return singleton.threadMark.get().equals(Mark.CLIENT);
    }

    public static boolean isServerThread() {
        return singleton.threadMark.get().equals(Mark.SERVER);
    }

    public static void assertServerThread() {
        Mark mark = singleton.threadMark.get();
        if (mark != Mark.SERVER && mark != Mark.UNKNOWN) {
            throw new IllegalStateException("working with server stuff on client thread!");
        }
    }

    public static void assertClientThread() {
        Mark mark = singleton.threadMark.get();
        if (mark != Mark.CLIENT && mark != Mark.UNKNOWN) {
            throw new IllegalStateException("working with client stuff on server thread!");
        }
    }

    public static void markThreadAs(Mark mark) {
        singleton.threadMark.set(mark);
    }
}
