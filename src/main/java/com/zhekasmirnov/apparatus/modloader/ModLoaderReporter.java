package com.zhekasmirnov.apparatus.modloader;

public interface ModLoaderReporter {
    void reportError(String message, Throwable error);
}