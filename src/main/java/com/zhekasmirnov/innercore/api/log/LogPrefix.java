package com.zhekasmirnov.innercore.api.log;

/**
 * Created by zheka on 09.08.2017.
 */

public enum LogPrefix {
    DEBUG,
    INFO,
    LOADER,
    MOD,
    ERROR,
    WARNING;

    public String toFontColor() {
        switch (this) {
            case INFO:
                return "#0000FF";
            case WARNING:
                return "#FFFF00";
            case ERROR:
                return "#FF0000";
            default:
                return "#FFFFFF";
        }
    }

    public static LogPrefix fromString(String str) {
        switch (str) {
            case "DEBUG":
                return DEBUG;
            case "ERROR":
                return ERROR;
            case "INFO":
                return INFO;
            case "WARNING":
                return WARNING;
            default:
                return str.contains("MOD") ? MOD : LOADER;
        }
    }
}
