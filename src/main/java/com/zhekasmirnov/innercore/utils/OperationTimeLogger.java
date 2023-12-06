package com.zhekasmirnov.innercore.utils;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;

public class OperationTimeLogger {
    private final boolean showToast;
    private long start = 0;

    public OperationTimeLogger(String logTag, boolean showToast) {
        this.showToast = showToast;
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
        if (showToast) {
            PrintStacking.print(message);
        }
        ICLog.d("Time-Logger", message);
        return this;
    }
}
