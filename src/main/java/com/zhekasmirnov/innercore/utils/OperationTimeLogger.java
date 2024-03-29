package com.zhekasmirnov.innercore.utils;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.log.ICLog;

public class OperationTimeLogger {
    private long start = 0;

    public OperationTimeLogger(String logTag, boolean showToast) {
    }

    public OperationTimeLogger(boolean showToast) {
        this("Time-Logger", showToast);
    }

    public OperationTimeLogger start() {
        start = System.currentTimeMillis();
        return this;
    }

    public OperationTimeLogger finish(String message) {
        message = String.format(message, (System.currentTimeMillis() - start) / 1000.0);
        if(InnerCoreServer.isDeveloperMode())
            ICLog.d("Time-Logger", message);
        return this;
    }
}
