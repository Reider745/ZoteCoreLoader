package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.Collection;
import java.util.concurrent.*;

public class TickExecutor {
    private static final TickExecutor instance = new TickExecutor();

    public static TickExecutor getInstance() {
        return instance;
    }


    private int threadCount = 0;
    private int additionalThreadPriority = 3;
    private ThreadPoolExecutor executor = null;

    public void setAdditionalThreadPriority(int priority) {
        additionalThreadPriority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority));
        ICLog.d("TickExecutor", "set additional thread priority to " + additionalThreadPriority);
    }

    public void setAdditionalThreadCount(int count) {
        count = Math.max(0, Math.min(7, count));
        if (threadCount != count) {
            threadCount = count;
            if (executor != null) {
                executor.shutdown();
            }
            if (count > 0) {
                ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount, new ThreadFactory(){
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = defaultThreadFactory.newThread(r);
                        thread.setPriority(additionalThreadPriority);
                        return thread;
                    }
                });
            } else {
                executor = null;
            }
        }
        ICLog.d("TickExecutor", "set additional thread count to " + threadCount);
    }

    public boolean isAvailable() {
        return executor != null;
    }

    public void execute(Runnable runnable) {
        if (executor != null) {
            executor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public void execute(Collection<Runnable> runnables) {
        if (executor != null) {
            for (Runnable runnable : runnables) {
                executor.execute(runnable);
            }
        } else {
            for (Runnable runnable : runnables) {
                runnable.run();
            }
        }
    }

    public void blockUntilExecuted() {
        if (executor != null) {
            try {
                // execute all runnables on main thread as well
                BlockingQueue<Runnable> queue = executor.getQueue();
                while (queue.size() > 0) {
                    Runnable runnable = queue.poll(0, TimeUnit.MILLISECONDS);
                    if (runnable != null) {
                        runnable.run();
                    } else {
                        break;
                    }
                }
                // wait until additional threads complete
                while (executor.getActiveCount() > 0) {
                    Thread.sleep(2);   
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}