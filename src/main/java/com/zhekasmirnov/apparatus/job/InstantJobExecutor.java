package com.zhekasmirnov.apparatus.job;

import com.zhekasmirnov.innercore.api.log.ICLog;

public class InstantJobExecutor implements JobExecutor {
    private final String name;

    public InstantJobExecutor(String name) {
        this.name = name;
    }

    public InstantJobExecutor() {
        this("Unknown Instant Executor");
    }

    @Override
    public void add(Job job) {
        try {
            job.run();
        } catch (Throwable e) {
            ICLog.e("NON-FATAL ERROR", "Main thread job executor \"" + name + "\" failed to execute job with pending exception.", e);
        }
    }
}
