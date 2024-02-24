package com.reider745.api.pointers;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadCheckToClear<T> extends Thread {
    private static final long TIME = 100 * 1000;
    private final PointersStorage<?> storage;
    private final ConcurrentHashMap<Long, ClassPointer<T>> pointers;

    public ThreadCheckToClear(PointersStorage<T> storage) {
        this.storage = storage;
        this.pointers = storage.getPointers();
    }

    public long getDynamicTimeClear(int size, long maxThreshold) {
        return Math.max(1, maxThreshold / (1L << (size - 1)));
    }

    @Override
    public void run() {
        while (true) {
            try {
                Iterator<Long> it = pointers.keySet().iterator();
                while (it.hasNext()) {
                    Long key = it.next();
                    if (pointers.get(key).hasClear())
                        storage.removePointer(key);
                }
                sleep(getDynamicTimeClear(1, TIME));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
