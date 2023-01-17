package com.reider745;

public interface Logger {
    void debug(String str);
    void info(String str);
    void error(String str, Exception e);
}
