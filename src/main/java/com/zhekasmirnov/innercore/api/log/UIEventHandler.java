package com.zhekasmirnov.innercore.api.log;

/**
 * Created by zheka on 09.08.2017.
 */

public class UIEventHandler implements IEventHandler {
    @Override
    public void onDebugEvent(String prefix, String message) {
    }

    @Override
    public void onLogEvent(String prefix, String message) {
    }

    @Override
    public void onImportantEvent(String prefix, String message) {
    }

    @Override
    public void onErrorEvent(String prefix, String message, Throwable error) {
    }
}
