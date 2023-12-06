package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by zheka on 28.09.2017.
 */

public class MainThreadQueue {
    public static final MainThreadQueue localThread = new MainThreadQueue();
    public static final MainThreadQueue serverThread = new MainThreadQueue();

    private boolean queueLocked = false;
    private final ArrayList<Runnable> queue = new ArrayList<>();
    private final ArrayList<Runnable> nextTickQueue = new ArrayList<>();
    private final Map<Runnable, Integer> delayedQueue = new HashMap<>();

    public void clearQueue() {
        synchronized (queue) {
            queue.clear();
        }
    }

    public void executeQueue() {
        long start = System.currentTimeMillis();
        synchronized (queue) {
            queueLocked = true;
            try {
                Iterator<Runnable> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    iterator.next().run();
                }
            } catch (Throwable err) {
                ICLog.e("ERROR", "Error occurred in main thread queue, all posted actions was cleared", err);
            }
            queueLocked = false;
            queue.clear();
            queue.addAll(nextTickQueue);
            nextTickQueue.clear();
        }

        synchronized (delayedQueue) {
            try {
                Iterator<Entry<Runnable, Integer>> iterator = delayedQueue.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<Runnable, Integer> entry = iterator.next();
                    int value = entry.getValue();
                    if (value-- <= 0) {
                        entry.getKey().run();
                        iterator.remove();
                    }
                    entry.setValue(value);
                }
            } catch (Throwable err) {
                ICLog.e("ERROR", "Error occurred in main thread queue, all posted delayed actions was cleared", err);
            }
        }

        long end = System.currentTimeMillis();
        if (start - end > 8) {
            ICLog.i("WARNING", "main thread tick taking too long: " + (start - end) + " ms");
        }
    }

    public void enqueue(Runnable action) {
        synchronized (queue) {
            if (queueLocked) {
                nextTickQueue.add(action);
            } else {
                queue.add(action);
            }
        }
    }

    public void enqueueDelayed(int ticks, Runnable action) {
        synchronized (delayedQueue) {
            delayedQueue.put(action, ticks);
        }
    }
}
