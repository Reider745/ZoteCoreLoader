package com.zhekasmirnov.innercore.api.log;

/**
 * Created by zheka on 09.08.2017.
 */

public enum LogType {
    DEBUG(0),
    LOG(1),
    IMPORTANT(2),
    ERROR(3);

    public int level;

    LogType(int level) {
        this.level = level;
    }
}
