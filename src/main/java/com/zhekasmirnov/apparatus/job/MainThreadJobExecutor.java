package com.zhekasmirnov.apparatus.job;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;

public class MainThreadJobExecutor implements JobExecutor {
    private final MainThreadQueue threadQueue;
    private final String name;

    public MainThreadJobExecutor(MainThreadQueue threadQueue, String name) {
        this.threadQueue = threadQueue;
        this.name = name;
    }

    public MainThreadJobExecutor(MainThreadQueue threadQueue) {
        this(threadQueue, "UnnamedJobExecutor");
    }

    @Override
    public void add(Job job) {
        threadQueue.enqueue(() -> {
            try {
                job.run();
            } catch (Throwable e) {
                ICLog.e("NON-FATAL NETWORK ERROR","Main thread job executor \"" + name + "\" failed to execute job with pending exception.", e);
            }
        });
    }
}
