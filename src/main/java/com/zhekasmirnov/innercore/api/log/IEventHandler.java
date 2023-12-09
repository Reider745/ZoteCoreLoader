package com.zhekasmirnov.innercore.api.log;

/**
 * Created by zheka on 09.08.2017.
 */

public interface IEventHandler {
    void onDebugEvent(String prefix, String message);

    void onLogEvent(String prefix, String message);

    void onImportantEvent(String prefix, String message);

    void onErrorEvent(String prefix, String message, Throwable error);
}
